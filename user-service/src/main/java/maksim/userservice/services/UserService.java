package maksim.userservice.services;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import maksim.userservice.config.AppConfig;
import maksim.userservice.exceptions.ConflictException;
import maksim.userservice.exceptions.NoContentException;
import maksim.userservice.models.Book;
import maksim.userservice.models.User;
import maksim.userservice.models.UserBookStatuses;
import maksim.userservice.models.dtos.CreateBookStatusDto;
import maksim.userservice.models.dtos.CreateUserDto;
import maksim.userservice.models.dtos.UpdateBookStatusDto;
import maksim.userservice.models.dtos.UpdateUserDto;
import maksim.userservice.repositories.UserRepository;
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

    @Autowired
    UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        RestTemplate restTemplate,
        AppConfig appConfig
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.appConfig = appConfig;
    }

    public User getUserById(int userId, JoinMode mode) {
        logger.trace("UserService method entrance: getUserById | Params: user id {} ; join mode {}", userId, mode);

        Optional<User> optionalUser = switch (mode) {
            case WITH_STATUSES_AND_BOOKS -> userRepository.findByIdWithJoinStatusesAndBooks(userId);
            case WITH_STATUSES -> userRepository.findByIdWithJoinStatuses(userId);
            case WITHOUT -> userRepository.findById(userId);
        };

        if (optionalUser.isEmpty()) {
            throw new NotFoundException("Cannot find user such id");
        }

        User user = optionalUser.get();

        if (mode == JoinMode.WITH_STATUSES || mode == JoinMode.WITH_STATUSES_AND_BOOKS) {
            user.setStatuses(
                user.getUserBookStatuses()
            );
        }

        if (mode == JoinMode.WITH_STATUSES_AND_BOOKS) {
            for (UserBookStatuses status : user.getStatuses()) {
                status.setBook(
                    status.getStatusBook()
                );
            }
        }

        logger.trace("UserService method end: getUserById | User was found");

        return user;
    }

    public List<Book> getAllBooksByUserStatus(int userId, BookStatus status, Pageable pageable) {
        logger.trace("UserService method entrance: getAllReadingBooks | Params: user id {}", userId);

        List<Book> books = userRepository.findAllBooksByUserStatus(userId, status.getValue(), pageable);

        logger.trace("UserService method end: getAllReadingBooks | Is found {} books", books.size());

        return books;
    }



    @Transactional
    public User createUser(CreateUserDto userDto) {
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

        logger.trace("UserService method end: createUser | User was found");

        return newUser;
    }

    @Transactional
    public User createStatus(int userId, CreateBookStatusDto statusDto) {
        logger.trace("UserService method entrance: addStatus | Params: user id {} ; book id {} ; status {}",
                userId, statusDto.getBookId(), statusDto.getStatus());

        User user = getUserById(userId, JoinMode.WITH_STATUSES_AND_BOOKS);

        // Status existence check
        for (UserBookStatuses st : user.getUserBookStatuses()) {
            if (st.getBook().getId() == statusDto.getBookId()) {
                throw new ConflictException("Status for such user and book already exist");
            }
        }

        // Book existence check
        ResponseEntity<Book> bookRequest = restTemplate.getForEntity(
                appConfig.getBookServiceUrl() + "/books/" + statusDto.getBookId(),
                Book.class);
        if (bookRequest.getStatusCode() != HttpStatus.OK) {
            throw new BadRequestException("Cannot get book with such id");
        }

        // Create status
        UserBookStatuses newStatus = new UserBookStatuses();
        newStatus.setBook(bookRequest.getBody());
        switch (statusDto.getStatus()) {
            case READ -> newStatus.setStatusRead(true);
            case READING -> newStatus.setStatusReading(true);
            case DROP -> newStatus.setStatusDrop(true);
            case LIKED -> newStatus.setLike(true);
            default -> throw new BadRequestException("Any here is not supported");
        }

        // Create and save status
        user.getUserBookStatuses().add(newStatus);
        userRepository.save(user);

        logger.trace("UserService method end: addStatus | Status was added successfully");

        return user;
    }



    @Transactional
    public User updateStatus(int userId, UpdateBookStatusDto userDto) {
        logger.trace("UserService method entrance: changeStatus | Params: user id {} ; book id {} ; new status {}",
                userId, userDto.getBookId(), userDto.getStatus());

        User user = getUserById(userId, JoinMode.WITH_STATUSES_AND_BOOKS);

        UserBookStatuses statusEntity = null;

        // Status existence check
        for (UserBookStatuses st : user.getUserBookStatuses()) {
            if (st.getBook().getId() == userDto.getBookId()) {
                statusEntity = st;
            }
        }

        if (statusEntity == null) {
            throw new NotFoundException("Cannot find status with such user and book ids");
        }

        boolean isSmthChanged = false;

        // Status changing
        switch (userDto.getStatus()) {
            case READ -> {
                if (statusEntity.getStatusRead() == userDto.getStatusValue()) {
                    statusEntity.setStatusReading(false);
                    statusEntity.setStatusDrop(false);
                    statusEntity.setStatusRead(userDto.getStatusValue());

                    isSmthChanged = true;
                }
            }
            case READING -> {
                if (statusEntity.getStatusReading() != userDto.getStatusValue()) {
                    statusEntity.setStatusReading(userDto.getStatusValue());
                    statusEntity.setStatusDrop(false);
                    statusEntity.setStatusRead(false);

                    isSmthChanged = true;
                }
            }
            case DROP -> {
                if (statusEntity.getStatusDrop() != userDto.getStatusValue()) {
                    statusEntity.setStatusReading(false);
                    statusEntity.setStatusRead(false);
                    statusEntity.setStatusDrop(userDto.getStatusValue());

                    isSmthChanged = true;
                }
            }
            case LIKED -> {
                if (statusEntity.getLike() != userDto.getStatusValue()) {
                    statusEntity.setLike(userDto.getStatusValue());

                    isSmthChanged = true;
                }
            }
            default -> throw new BadRequestException("Any here is not supported");
        }

        if (!statusEntity.getStatusReading() && !statusEntity.getStatusRead()
            && !statusEntity.getStatusDrop() && !statusEntity.getLike()) {
            deleteStatusEntity(userId, userDto.getBookId());
        } else {
            if (isSmthChanged) {
                userRepository.save(user);
            } else {
                throw new NoContentException("Nothing has changed");
            }
        }

        logger.trace("UserService method end: changeStatus | Status was changed successfully");

        return user;
    }

    @Transactional
    public User updateUser(int userId, UpdateUserDto userDto) {
        logger.trace("UserService method entrance: changeUser");

        User user = getUserById(userId, JoinMode.WITH_STATUSES_AND_BOOKS);

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
                    logger.info("UserService method: changeUser | Old password and new password are equal");

                    throw new BadRequestException("Old password and new password are equal");
                }

                userDto.setNewPassword(
                    passwordEncoder.encode(
                        userDto.getNewPassword()
                    )
                );

                isSmthChanged = true;
            } else {
                logger.info("UserService method: changeUser | Old password is not equals to entity password");

                throw new BadRequestException("Password is incorrect");
            }
        }

        // Saving data
        if (isSmthChanged) {
            userRepository.save(user);
        }

        logger.trace("UserService method end: changeUser | Status was changed successfully");

        return user;
    }



    @Transactional
    public User deleteStatusEntity(int userId, int bookId) {
        logger.trace("UserService method entrance: deleteStatus | Params: user id {} ; book id {}", userId, bookId);

        User user = getUserById(userId, JoinMode.WITH_STATUSES_AND_BOOKS);

        List<UserBookStatuses> statuses = user.getUserBookStatuses();
        boolean isFound = false;

        // Status existence check
        for (int i = 0; i < statuses.size(); i++) {
            if (statuses.get(i).getBook().getId() == bookId) {
                statuses.remove(i);

                isFound = true;
                break;
            }
        }

        if (!isFound) {
            throw new NotFoundException("Cannot find status for that book in user's statuses");
        }

        userRepository.save(user);

        logger.trace("UserService method end: deleteStatus | Status was changed successfully");

        return user;
    }

    @Transactional
    public void deleteUser(int userId) {
        logger.trace("UserService method entrance: deleteUser | Params: user id {}", userId);

        User user = getUserById(userId, JoinMode.WITH_STATUSES_AND_BOOKS);

        userRepository.delete(user);

        logger.trace("UserService method end: deleteUser | User was deleted successfully");
    }

}
