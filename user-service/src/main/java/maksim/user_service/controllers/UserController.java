package maksim.user_service.controllers;

import jakarta.ws.rs.NotFoundException;
import maksim.user_service.models.Book;
import maksim.user_service.models.User;
import maksim.user_service.models.dtos.UserDtoForCreating;
import maksim.user_service.services.UserService;
import maksim.user_service.utils.enums.BookStatus;
import maksim.user_service.utils.enums.JoinMode;
import maksim.user_service.utils.validators.StringValidators;
import maksim.user_service.utils.validators.UserDtoForCreatingValidators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserService userService;
    private final StringValidators stringValidators;
    private final UserDtoForCreatingValidators userDtoForCreatingValidators;

    @Autowired
    UserController(
        UserService userService,
        StringValidators stringValidators,
        UserDtoForCreatingValidators userDtoForCreatingValidators
    ) {
        this.userService = userService;
        this.stringValidators = stringValidators;
        this.userDtoForCreatingValidators = userDtoForCreatingValidators;
    }

    @GetMapping("/{userId}/books")
    public ResponseEntity<List<Book>> getAllBooksByUserStatus(
        @PathVariable(name = "userId") int userId,
        @RequestParam(name = "status") String strStatus,
        @RequestParam(name = "pageNum", required = false, defaultValue = "0") int pageNum,
        @RequestParam(name = "pageSize", required = false, defaultValue = "20") int pageSize
    ) {
        BookStatus bookStatus = BookStatus.fromValue(strStatus);

        logger.trace("BookController method entrance: getAllBooksByUserStatus | Params: userId {} ; status {} ; page num/size {}/{}",
            userId, bookStatus, pageNum, pageSize);

        Pageable pageable = PageRequest.of(pageNum, pageSize);

        List<Book> books = userService.getAllBooksByUserStatus(userId, bookStatus, pageable);

        logger.trace("BookController method return: getAllBooksByUserStatus | Found {} items", books.size());

        return ResponseEntity.ok(books);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(
        @PathVariable(name = "userId") int userId,
        @RequestParam(name = "joinMode", required = false, defaultValue = "without") String strJoinMode
    ) {
        JoinMode joinMode = JoinMode.fromValue(strJoinMode);

        logger.trace("BookController method entrance: getAllBooksByUserStatus | Params: userId {} ; join mode {}",
                userId, joinMode);

        User user = userService.getUserById(userId, joinMode);

        logger.trace("BookController method return: getAllBooksByUserStatus | User was found successfully");

        return ResponseEntity.ok(user);
    }



    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDtoForCreating userDtoForCreating) {
        logger.trace("BookController method entrance: createUser");

        User user;

        logger.trace("BookController method return: createUser | User was successfully created");

        return ResponseEntity.ok(user);
    }




}
