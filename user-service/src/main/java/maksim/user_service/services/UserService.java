package maksim.user_service.services;

import jakarta.ws.rs.NotFoundException;
import maksim.user_service.models.Book;
import maksim.user_service.models.User;
import maksim.user_service.models.UserBookStatuses;
import maksim.user_service.repositories.UserRepository;
import maksim.user_service.utils.enums.BookStatus;
import maksim.user_service.utils.enums.JoinMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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



}
