package maksim.userservice.services;

import jakarta.transaction.Transactional;
import maksim.userservice.exceptions.BadRequestException;
import maksim.userservice.exceptions.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import maksim.userservice.config.AppConfig;
import maksim.userservice.exceptions.ConflictException;
import maksim.userservice.exceptions.NoContentException;
import maksim.userservice.models.dtos.*;
import maksim.userservice.models.entities.Book;
import maksim.userservice.models.entities.User;
import maksim.userservice.models.entities.UserBookStatuses;
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

        List<UserBookStatuses> statuses = userRepository.findAllBooksByUserStatus(userId, status.toString(), pageable);

        List<BookDto> books = new ArrayList<>(statuses.size());

        for (UserBookStatuses st : statuses) {
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

        logger.trace("UserService method end: createUser | User was found");

        return new UserDto(newUser, null);
    }

    @Transactional
    public UserDto createStatus(int userId, CreateBookStatusDto statusDto) {
        logger.trace("UserService method entrance: addStatus | Params: user id {} ; book id {} ; status {}",
                userId, statusDto.getBookId(), statusDto.getStatus());

        User user = getUserEntityById(userId, JoinMode.WITH_STATUSES_AND_BOOKS);

        // Status existence check
        for (UserBookStatuses st : user.getBookStatuses()) {
            if (st.getBook().getId() == statusDto.getBookId()) {
                throw new ConflictException("Status for such user and book already exist");
            }
        }

        // Book existence check
        ResponseEntity<Book> bookRequest = restTemplate.getForEntity(
                appConfig.getBookServiceUrl() + "/api/v1/books/" + statusDto.getBookId(),
                Book.class);
        if (bookRequest.getStatusCode() != HttpStatus.OK) {
            throw new BadRequestException("Cannot get book with such id");
        }

        // Create status
        UserBookStatuses newStatus = new UserBookStatuses();
        newStatus.setBook(bookRequest.getBody());
        newStatus.setStatus(statusDto.getStatus().toString());
        newStatus.setUser(user);

        // Add and save status
        user.getBookStatuses().add(newStatus);
        userRepository.save(user);

        logger.trace("UserService method end: addStatus | Status was added successfully");

        return new UserDto(user, JoinMode.WITH_STATUSES_AND_BOOKS);
    }



    @Transactional
    public UserDto updateStatus(int userId, UpdateBookStatusDto statusDto) {
        logger.trace("UserService method entrance: changeStatus | Params: user id {} ; book id {} ; new status {}",
                userId, statusDto.getBookId(), statusDto.getStatus());

        User user = getUserEntityById(userId, JoinMode.WITH_STATUSES_AND_BOOKS);

        UserBookStatuses statusEntity = null;

        // Status existence check
        for (UserBookStatuses st : user.getBookStatuses()) {
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
            case ANY -> {
                throw new BadRequestException("Any here is not supported");
            }
            case LIKED -> {
                statusEntity.setLike(statusDto.getStatusValue());
            }
            default -> {
                if (Objects.equals(statusDto.getStatus().toString(), statusEntity.getStatus()) && !statusDto.getStatusValue()) {
                    statusEntity.setStatus(null);

                    isSmthChanged = true;
                }

                if (statusDto.getStatusValue()) {
                    statusEntity.setStatus(statusDto.getStatus().toString());

                    isSmthChanged = true;
                }
            }
        }

        if (statusEntity.getStatus() == null && !statusEntity.getLike()) {
            deleteStatusEntity(userId, statusDto.getBookId());
        } else {
            if (isSmthChanged) {
                userRepository.save(user);
            } else {
                throw new NoContentException("Nothing has changed");
            }
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

        return new UserDto(user, JoinMode.WITH_STATUSES_AND_BOOKS);
    }



    @Transactional
    public UserDto deleteStatusEntity(int userId, int bookId) {
        logger.trace("UserService method entrance: deleteStatus | Params: user id {} ; book id {}", userId, bookId);

        User user = getUserEntityById(userId, JoinMode.WITH_STATUSES_AND_BOOKS);

        List<UserBookStatuses> statuses = user.getBookStatuses();
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

        return new UserDto(user, JoinMode.WITH_STATUSES_AND_BOOKS);
    }

    @Transactional
    public void deleteUser(int userId) {
        logger.trace("UserService method entrance: deleteUser | Params: user id {}", userId);

        User user = getUserEntityById(userId, JoinMode.WITH_STATUSES_AND_BOOKS);

        userRepository.delete(user);

        logger.trace("UserService method end: deleteUser | User was deleted successfully");
    }

}
