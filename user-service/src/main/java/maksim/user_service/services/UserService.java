package maksim.user_service.services;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import maksim.user_service.config.AppConfig;
import maksim.user_service.models.Book;
import maksim.user_service.models.User;
import maksim.user_service.models.UserBookStatuses;
import maksim.user_service.models.dtos.CreateUserDto;
import maksim.user_service.models.dtos.UpdateUserDto;
import maksim.user_service.repositories.UserRepository;
import maksim.user_service.utils.enums.BookStatus;
import maksim.user_service.utils.enums.JoinMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
            logger.trace("UserService method end: getUserById | User with such id not found");

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
            logger.trace("UserService method: createUser | Cannot add user: email is taken");

            throw new BadRequestException("Cannot add user. Email is taken.");
        }

        comparableUser = userRepository.findByName(userDto.getName());
        if (comparableUser.isPresent()) {
            logger.trace("UserService method: createUser | Cannot add user: name is taken");

            throw new BadRequestException("Cannot add user. Name is taken.");
        }

        boolean isPasswordContainsDigit = false;
        boolean isPasswordContainsSpecs = false;
        boolean isPasswordContainsLetters = false;
        byte[] chars = userDto.getPassword().getBytes();
        for (int i = 0; i < userDto.getPassword().length(); i++) {
            if (!isPasswordContainsDigit && (chars[i] >= 48 && chars[i] <= 57)) {
                isPasswordContainsDigit = true;
                continue;
            }

            if (!isPasswordContainsLetters && ((chars[i] >= 65 && chars[i] <= 90) || (chars[i] >= 97 && chars[i] <= 122))) {
                isPasswordContainsLetters = true;
                continue;
            }

            if (!isPasswordContainsSpecs && (chars[i] >= 33 && chars[i] <= 42)) {
                isPasswordContainsSpecs = true;
            }
        }

        if (!isPasswordContainsDigit || !isPasswordContainsLetters || !isPasswordContainsSpecs) {
            logger.trace("UserService method: createUser | Cannot add user: name is taken");

            throw new BadRequestException("Password should contains at least: 1 spec symbol, 1 letter, 1 digit.");
        }

        User newUser = new User();
        newUser.setName(userDto.getName());
        newUser.setEmail(userDto.getEmail());
        newUser.setPassword(
            passwordEncoder.encode(userDto.getPassword())
        );

        try {
            userRepository.save(newUser);
        } catch(DataIntegrityViolationException ex) {
            logger.warn("UserService method exception: createUser | Cannot add user: {}", ex.getMessage());

            throw new InternalServerErrorException("Cannot add user");
        }

        logger.trace("UserService method end: createUser | User was found");

        return newUser;
    }

    @Transactional
    public void addStatus(int userId, int bookId, BookStatus status) {
        logger.trace("UserService method entrance: addStatus | Params: user id {} ; book id {} ; status {}",
                userId, bookId, status);

        // User existence check
        Optional<User> user = userRepository.findByIdWithJoinStatusesAndBooks(userId);
        if (user.isEmpty()) {
            logger.trace("UserService method: addStatus | Cannot find user with such id");

            throw new NotFoundException("User with such id does not exist");
        }

        // Status existence check
        for (UserBookStatuses st : user.get().getUserBookStatuses()) {
            if (st.getBook().getId() == bookId) {
                logger.trace("UserService method: addStatus | Status for such user and book already exist");

                throw new NotFoundException("Status for such user and book already exist");
            }
        }

        // Book existence check
        ResponseEntity<Book> bookRequest = restTemplate.getForEntity(
                appConfig.getBookServiceUrl() + "/books/" + bookId,
                Book.class);
        if (bookRequest.getStatusCode() != HttpStatus.OK) {
            logger.trace("UserService method: addStatus | Status for such user and book already exist");

            throw new BadRequestException("Status for such user and book already exist");
        }

        // Create status
        UserBookStatuses newStatus = new UserBookStatuses();
        newStatus.setBook(bookRequest.getBody());
        switch (status) {
            case READ -> newStatus.setStatusRead(true);
            case READING -> newStatus.setStatusReading(true);
            case DROP -> newStatus.setStatusDrop(true);
            case LIKED -> newStatus.setLike(true);
        }

        // Save status
        user.get().getUserBookStatuses().add(newStatus);
        userRepository.save(user.get());

        logger.trace("UserService method end: addStatus | Status was added successfully");
    }


    @Transactional
    public void changeStatus(int userId, int bookId, BookStatus status) {
        logger.trace("UserService method entrance: changeStatus | Params: user id {} ; book id {} ; new status {}",
                userId, bookId, status);

        // User existence check
        Optional<User> user = userRepository.findByIdWithJoinStatusesAndBooks(userId);
        if (user.isEmpty()) {
            logger.trace("UserService method: changeStatus | Cannot find user with such id");

            throw new NotFoundException("User with such id does not exist");
        }

        UserBookStatuses statusEntity = null;

        // Status existence check
        for (UserBookStatuses st : user.get().getUserBookStatuses()) {
            if (st.getBook().getId() == bookId) {
                statusEntity = st;
            }
        }

        if (statusEntity == null) {
            logger.trace("UserService method: changeStatus | Cannot find status with such user and book ids");

            throw new NotFoundException("Cannot find status with such user and book ids");
        }

        // Status changing
        switch (status) {
            case READ -> {
                if (statusEntity.getStatusRead()) return;

                statusEntity.setStatusReading(false);
                statusEntity.setStatusDrop(false);
                statusEntity.setStatusRead(true);
            }
            case READING -> {
                if (statusEntity.getStatusReading()) return;

                statusEntity.setStatusReading(true);
                statusEntity.setStatusDrop(false);
                statusEntity.setStatusRead(false);
            }
            case DROP -> {
                if (statusEntity.getStatusDrop()) return;

                statusEntity.setStatusReading(false);
                statusEntity.setStatusRead(false);
                statusEntity.setStatusDrop(true);
            }
            default -> {
                logger.trace("UserService method: changeStatus | Invalid status value");

                throw new BadRequestException("Invalid status value. Status can be: read, reading, drop");
            }
        }

        userRepository.save(user.get());

        logger.trace("UserService method end: changeStatus | Status was changed successfully");
    }

    @Transactional
    public void changeUser(int userId, UpdateUserDto userDto) {
        logger.trace("UserService method entrance: changeUser");

        // User existence check
        Optional<User> user = userRepository.findByIdWithJoinStatusesAndBooks(userId);
        if (user.isEmpty()) {
            logger.info("UserService method: changeUser | Cannot find user with such id");

            throw new NotFoundException("User with such id does not exist");
        }

        // Change values
        boolean isSmthChanged = false;

        if (userDto.getNewName() != null && !userDto.getNewName().equals(user.get().getName())) {
            user.get().setName(userDto.getNewName());
            isSmthChanged = true;
        }

        if (userDto.getNewEmail() != null && !userDto.getNewEmail().equals(user.get().getEmail())) {
            user.get().setEmail(userDto.getNewEmail());
            isSmthChanged = true;
        }

        if (userDto.getNewPassword() != null && userDto.getOldPassword() != null) {
            if (passwordEncoder.matches(
                userDto.getOldPassword(),
                user.get().getPassword()
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

        // Saving date
        if (isSmthChanged) userRepository.save(user.get());

        logger.trace("UserService method end: changeUser | Status was changed successfully");
    }


    @Transactional
    public void deleteStatus(int userId, int bookId) {
        logger.trace("UserService method entrance: deleteStatus | Params: user id {} ; book id {}", userId, bookId);

        // User existence check
        Optional<User> user = userRepository.findByIdWithJoinStatusesAndBooks(userId);
        if (user.isEmpty()) {
            logger.trace("UserService method: deleteStatus | Cannot find user with such id");

            throw new NotFoundException("User with such id does not exist");
        }

        List<UserBookStatuses> statuses = user.get().getUserBookStatuses();
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
            logger.trace("UserService method: deleteStatus | Cannot find status for that book in user's statuses");

            throw new NotFoundException("Cannot find status for that book in user's statuses");
        }

        userRepository.save(user.get());

        logger.trace("UserService method end: deleteStatus | Status was changed successfully");
    }

    @Transactional
    public void deleteUser(int userId) {
        logger.trace("UserService method entrance: deleteUser | Params: user id {}", userId);

        // User existence check
        Optional<User> user = userRepository.findByIdWithJoinStatusesAndBooks(userId);
        if (user.isEmpty()) {
            logger.trace("UserService method: deleteUser | Cannot find user with such id");

            throw new NotFoundException("User with such id does not exist");
        }

        userRepository.delete(user.get());

        logger.trace("UserService method end: deleteUser | User was deleted successfully");
    }



}
