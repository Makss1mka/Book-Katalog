package maksim.user_service.controllers;

import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import maksim.user_service.models.Book;
import maksim.user_service.models.User;
import maksim.user_service.models.dtos.CreateBookStatusDto;
import maksim.user_service.models.dtos.CreateUserDto;
import maksim.user_service.models.dtos.UpdateBookStatusDto;
import maksim.user_service.models.dtos.UpdateUserDto;
import maksim.user_service.services.UserService;
import maksim.user_service.utils.enums.BookStatus;
import maksim.user_service.utils.enums.JoinMode;
import maksim.user_service.utils.validators.StringValidators;
import maksim.user_service.utils.validators.CreateUserDtoValidators;
import maksim.user_service.utils.validators.UpdateUserDtoValidators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
@Valid
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final CreateUserDtoValidators createUserDtoValidators;
    private final UpdateUserDtoValidators updateUserDtoValidators;

    @Autowired
    UserController(
        UserService userService,
        CreateUserDtoValidators createUserDtoValidators,
        UpdateUserDtoValidators updateUserDtoValidators
    ) {
        this.userService = userService;
        this.createUserDtoValidators = createUserDtoValidators;
        this.updateUserDtoValidators = updateUserDtoValidators;
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
    public ResponseEntity<User> createUser(@RequestBody CreateUserDto createUserDto) {
        logger.trace("BookController method entrance: createUser");

        if (!createUserDtoValidators.isValid(createUserDto)) {
            throw new BadRequestException("Body contains invalid symbols");
        }

        User user = userService.createUser(createUserDto);

        logger.trace("BookController method return: createUser | User was successfully created");

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/{userId}/book-status")
    public ResponseEntity<User> createStatus(
        @PathVariable(name = "userId") int userId,
        @RequestBody CreateBookStatusDto statusDto
    ) {
        logger.trace("BookController method entrance: createStatus");

        User user = userService.createStatus(userId, statusDto);

        logger.trace("BookController method return: createStatus | User was successfully created");

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }



    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(
        @PathVariable(name = "userId") int userId,
        @RequestBody UpdateUserDto updateUserDto
    ) {
        logger.trace("BookController method entrance: updateUser");

        if (!updateUserDtoValidators.isValid(updateUserDto)) {
            throw new BadRequestException("Body contains invalid symbols");
        }

        User user = userService.updateUser(userId, updateUserDto);

        logger.trace("BookController method return: updateUser | User was successfully updated");

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PutMapping("/{userId}/book-status")
    public ResponseEntity<User> updateStatus(
        @PathVariable(name = "userId") int userId,
        @RequestBody UpdateBookStatusDto statusDto
    ) {
        logger.trace("BookController method entrance: updateStatus");

        User user = userService.updateStatus(userId, statusDto);

        logger.trace("BookController method return: updateStatus | Status was successfully updated");

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable(name = "userId") int userId) {
        logger.trace("BookController method entrance: deleteUser");

        userService.deleteUser(userId);

        logger.trace("BookController method return: deleteUser | User was successfully deleted");

        return new ResponseEntity<>("User was successfully deleted", HttpStatus.CREATED);
    }

    @DeleteMapping("/{userId}/book-status/{bookId}")
    public ResponseEntity<User> deleteStatus(
        @PathVariable(name = "userId") int userId,
        @PathVariable(name = "bookId") int bookId
    ) {
        logger.trace("BookController method entrance: deleteUser");

        User user = userService.deleteStatusEntity(userId, bookId);

        logger.trace("BookController method return: deleteUser | Status was successfully deleted");

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

}
