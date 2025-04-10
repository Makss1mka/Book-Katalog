package maksim.userservice.services;

import jakarta.transaction.Transactional;
import maksim.kafkaclient.dtos.CreateStatusKafkaDto;
import maksim.kafkaclient.dtos.CreateLikeKafkaDto;
import maksim.kafkaclient.dtos.DeleteLikeKafkaDto;
import maksim.kafkaclient.dtos.DeleteStatusKafkaDto;
import maksim.kafkaclient.dtos.UpdateStatusKafkaDto;
import maksim.userservice.exceptions.BadRequestException;
import maksim.userservice.exceptions.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import maksim.userservice.config.AppConfig;
import maksim.userservice.exceptions.ConflictException;
import maksim.userservice.exceptions.NoContentException;
import maksim.userservice.models.dtos.crud.CreateBookStatusDto;
import maksim.userservice.models.dtos.crud.CreateUserDto;
import maksim.userservice.models.dtos.crud.UpdateBookStatusDto;
import maksim.userservice.models.dtos.crud.UpdateUserDto;
import maksim.userservice.models.dtos.result.BookDto;
import maksim.userservice.models.dtos.result.UserDto;
import maksim.userservice.models.entities.Book;
import maksim.userservice.models.entities.User;
import maksim.userservice.models.entities.UserBookStatus;
import maksim.userservice.repositories.UserBookStatusRepository;
import maksim.userservice.repositories.UserRepository;
import maksim.userservice.services.kafka.producers.LikeEventsProducer;
import maksim.userservice.services.kafka.producers.StatusEventsProducer;
import maksim.userservice.utils.enums.BookStatus;
import maksim.userservice.utils.enums.JoinMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final AppConfig appConfig;
    private final StatusEventsProducer statusEventsProducer;
    private final LikeEventsProducer likeEventsProducer;
    private final UserBookStatusRepository userBookStatusRepository;

    @Autowired
    UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        RestTemplate restTemplate,
        AppConfig appConfig,
        StatusEventsProducer statusEventsProducer,
        LikeEventsProducer likeEventsProducer,
        UserBookStatusRepository userBookStatusRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.appConfig = appConfig;
        this.statusEventsProducer = statusEventsProducer;
        this.likeEventsProducer = likeEventsProducer;
        this.userBookStatusRepository = userBookStatusRepository;
    }

    private User getUserEntityById(int userId, JoinMode mode) {
        logger.trace("UserService method entrance: getUserEntityById | Params: user id {} ; join mode {}", userId, mode);

        Optional<User> user = switch (mode) {
            case WITH_STATUSES_AND_BOOKS, WITH_STATUSES -> userRepository.findByIdWithJoinStatusesAndBooks(userId);
            case WITHOUT -> userRepository.findById(userId);
        };

        if (user.isEmpty()) {
            throw new NotFoundException("Cannot find user such id");
        }

        logger.trace("UserService method end: getUserEntityById | User was found");

        return user.get();
    }

    public UserDto getUserById(int userId, JoinMode mode) {
        logger.trace("UserService method entrance: getUserById | Params: user id {} ; join mode {}", userId, mode);

        User user = getUserEntityById(userId, mode);

        logger.trace("UserService method end: getUserById | User was found");

        return new UserDto(user, mode);
    }

    public List<BookDto> getAllBooksByUserStatus(int userId, BookStatus status, Pageable pageable) {
        logger.trace("UserService method entrance: getAllReadingBooks | Params: user id {}", userId);

        List<UserBookStatus> statuses = userRepository.findAllBooksByUserStatus(userId, status.toString(), pageable);

        List<BookDto> books = new ArrayList<>(statuses.size());

        for (UserBookStatus st : statuses) {
            books.add(new BookDto(
                st.getBook(), BookStatus.fromValue(st.getStatus())
            ));
        }

        logger.trace("UserService method end: getAllReadingBooks | Is found {} books", books.size());

        return books;
    }



    @Transactional
    public UserDto createUser(CreateUserDto userDto) {
        logger.trace("UserService method entrance: createUser");

        Optional<User> comparableUser = userRepository.findByEmail(userDto.getEmail());
        if (comparableUser.isPresent()) {
            throw new ConflictException("Cannot add user. Email is taken.");
        }

        comparableUser = userRepository.findByName(userDto.getName());
        if (comparableUser.isPresent()) {
            throw new ConflictException("Cannot add user. Name is taken.");
        }

        User newUser = new User();
        newUser.setName(userDto.getName());
        newUser.setEmail(userDto.getEmail());
        newUser.setPassword(
            passwordEncoder.encode(userDto.getPassword())
        );

        userRepository.save(newUser);

        logger.trace("UserService method end: createUser | User was created");

        return new UserDto(newUser, null);
    }

    @Transactional
    public UserDto createStatus(int userId, CreateBookStatusDto statusDto) {
        logger.trace("UserService method entrance: addStatus | Params: user id {} ; book id {} ; status {}",
                userId, statusDto.getBookId(), statusDto.getStatus());

        User user = getUserEntityById(userId, JoinMode.WITH_STATUSES_AND_BOOKS);

        // Status existence check
        for (UserBookStatus st : user.getBookStatuses()) {
            if (st.getBook().getId() == statusDto.getBookId()) {
                throw new ConflictException("Status for such user and book already exist");
            }
        }

        // Book existence check
        ResponseEntity<Book> bookRequest = restTemplate.getForEntity(
                appConfig.getBookServiceUrl() + "/api/v1/books/" + statusDto.getBookId(),
                Book.class);
        if (bookRequest.getStatusCode() != HttpStatus.OK) {
            throw new NotFoundException("Cannot get book with such id");
        }

        // Create status
        UserBookStatus newStatus = new UserBookStatus();
        newStatus.setBook(bookRequest.getBody());
        newStatus.setStatus(statusDto.getStatus().toString());
        newStatus.setUser(user);

        userBookStatusRepository.save(newStatus);

        user.getBookStatuses().add(newStatus);

        userRepository.save(user);

        statusEventsProducer.publishStatusCreate(
            new CreateStatusKafkaDto(userId, statusDto.getBookId(), statusDto.getStatus().toString())
        );

        logger.trace("UserService method end: addStatus | Status was added successfully");

        return new UserDto(user, JoinMode.WITH_STATUSES_AND_BOOKS);
    }

    @Transactional
    public void addLike(int userId, int bookId) {
        logger.trace("UserService method entrance: addLike | Params: {} , {}", userId, bookId);

        User user = getUserEntityById(userId, JoinMode.WITH_STATUSES_AND_BOOKS);

        for (Book book : user.getLikedBooks()) {
            if (book.getId() == bookId) {
                throw new ConflictException("Like for such book is already exist");
            }
        }

        ResponseEntity<Book> bookRequest = restTemplate.getForEntity(
                appConfig.getBookServiceUrl() + "/api/v1/books/" + bookId,
                Book.class);
        if (bookRequest.getStatusCode() != HttpStatus.OK) {
            throw new NotFoundException("Cannot get book with such id");
        }

        user.getLikedBooks().add(bookRequest.getBody());

        userRepository.save(user);

        likeEventsProducer.publishLikeCreate(new CreateLikeKafkaDto(userId, bookId));

        logger.trace("UserService method end: addLike | Like was added successfully");
    }



    @Transactional
    public UserDto updateStatus(int userId, UpdateBookStatusDto statusDto) {
        logger.trace("UserService method entrance: changeStatus | Params: user id {} ; book id {} ; new status {}",
                userId, statusDto.getBookId(), statusDto.getStatus());

        User user = getUserEntityById(userId, JoinMode.WITH_STATUSES_AND_BOOKS);

        UserBookStatus statusEntity = null;

        // Status existence check
        for (UserBookStatus st : user.getBookStatuses()) {
            if (st.getBook().getId() == statusDto.getBookId()) {
                statusEntity = st;
            }
        }

        if (statusEntity == null) {
            throw new NotFoundException("Cannot find status with such user and book ids");
        }

        boolean isSmthChanged = false;

        // Status changing
        switch (statusDto.getStatus()) {
            case ANY -> throw new BadRequestException("Any here is not supported");
            default -> {
                if (!statusEntity.getStatus().equals(statusDto.getStatus().toString())) {
                    statusEntity.setStatus(statusDto.getStatus().toString());

                    isSmthChanged = true;
                }
            }
        }

        if (isSmthChanged) {
            userRepository.save(user);

            statusEventsProducer.publishStatusUpdate(new UpdateStatusKafkaDto(
                userId, statusDto.getBookId(), statusDto.getStatus().toString()
            ));
        } else {
            throw new NoContentException("Nothing has changed");
        }

        logger.trace("UserService method end: changeStatus | Status was changed successfully");

        return new UserDto(user, JoinMode.WITH_STATUSES_AND_BOOKS);
    }

    @Transactional
    public UserDto updateUser(int userId, UpdateUserDto userDto) {
        logger.trace("UserService method entrance: changeUser");

        User user = getUserEntityById(userId, JoinMode.WITH_STATUSES_AND_BOOKS);

        // Change values
        boolean isSmthChanged = false;

        if (userDto.getNewName() != null && !userDto.getNewName().equals(user.getName())) {
            user.setName(userDto.getNewName());
            isSmthChanged = true;
        }

        if (userDto.getNewEmail() != null && !userDto.getNewEmail().equals(user.getEmail())) {
            user.setEmail(userDto.getNewEmail());
            isSmthChanged = true;
        }

        if (userDto.getNewPassword() != null && userDto.getOldPassword() != null) {
            if (passwordEncoder.matches(
                userDto.getOldPassword(),
                user.getPassword()
            )) {
                if (Objects.equals(userDto.getOldPassword(), userDto.getNewPassword())) {
                    throw new BadRequestException("Old password and new password are equal");
                }

                userDto.setNewPassword(
                    passwordEncoder.encode(
                        userDto.getNewPassword()
                    )
                );

                isSmthChanged = true;
            } else {
                throw new BadRequestException("Password is incorrect");
            }
        }

        // Saving data
        if (isSmthChanged) {
            userRepository.save(user);
        } else {
            throw new NoContentException("Nothing changed in your account");
        }

        logger.trace("UserService method end: changeUser | Status was changed successfully");

        return new UserDto(user, JoinMode.WITH_STATUSES_AND_BOOKS);
    }



    @Transactional
    public UserDto deleteStatus(int userId, int bookId) {
        logger.trace("UserService method entrance: deleteStatus");

        User user = getUserEntityById(userId, JoinMode.WITH_STATUSES_AND_BOOKS);

        List<UserBookStatus> statuses = user.getBookStatuses();
        boolean isFound = false;
        String bookStatus = null;

        for (int i = 0; i < user.getBookStatuses().size(); i++) {
            if (user.getBookStatuses().get(i).getBook().getId() == bookId) {
                bookStatus = user.getBookStatuses().get(i).getStatus();

                userBookStatusRepository.delete(user.getBookStatuses().get(i));
                user.getBookStatuses().remove(i);

                isFound = true;
                break;
            }
        }

        if (!isFound) {
            throw new NotFoundException("Cannot find status for that book in user's statuses");
        }

        userRepository.save(user);

        statusEventsProducer.publishStatusDelete(new DeleteStatusKafkaDto(userId, bookId, bookStatus));

        logger.trace("UserService method end: deleteStatus | Status was changed successfully");

        return new UserDto(user, JoinMode.WITH_STATUSES_AND_BOOKS);
    }

    @Transactional
    public void deleteUser(int userId) {
        logger.trace("UserService method entrance: deleteUser | Params: user id {}", userId);

        User user = getUserEntityById(userId, JoinMode.WITH_STATUSES_AND_BOOKS);

        userRepository.delete(user);

        logger.trace("UserService method end: deleteUser | User was deleted successfully");
    }

    @Transactional
    public void deleteLike(int userId, int bookId) {
        logger.trace("UserService method entrance: deleteLike");

        User user = getUserEntityById(userId, JoinMode.WITH_STATUSES_AND_BOOKS);

        boolean isDeleted = false;

        for (Book book : user.getLikedBooks()) {
            if (book.getId() == bookId) {
                user.getLikedBooks().remove(book);
                isDeleted = true;
                break;
            }
        }

        if (!isDeleted) {
            throw new ConflictException("Like for such book doesn't exist");
        }

        userRepository.save(user);

        likeEventsProducer.publishLikeDelete(new DeleteLikeKafkaDto(userId, bookId));

        logger.trace("UserService method end: deleteLike | Like was deleted successfully");
    }

}
