package maksim.userservice.controllers;

import jakarta.validation.Valid;
import maksim.userservice.exceptions.BadRequestException;
import java.util.List;
import maksim.userservice.models.dtos.*;
import maksim.userservice.services.UserService;
import maksim.userservice.utils.enums.BookStatus;
import maksim.userservice.utils.enums.JoinMode;
import maksim.userservice.utils.validators.CreateUserDtoValidators;
import maksim.userservice.utils.validators.UpdateUserDtoValidators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
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
    public ResponseEntity<List<BookDto>> getAllBooksByUserStatus(
        @PathVariable(name = "userId") int userId,
        @RequestParam(name = "status", required = false, defaultValue = "any") String strStatus,
        @RequestParam(name = "pageNum", required = false, defaultValue = "0") int pageNum,
        @RequestParam(name = "pageSize", required = false, defaultValue = "20") int pageSize
    ) {
        BookStatus bookStatus = BookStatus.fromValue(strStatus);

        logger.trace("BookController method entrance: getAllBooksByUserStatus | Params: userId {} ; status {} ; page num/size {}/{}",
            userId, bookStatus, pageNum, pageSize);

        Pageable pageable = PageRequest.of(pageNum, pageSize);

        List<BookDto> books = userService.getAllBooksByUserStatus(userId, bookStatus, pageable);

        logger.trace("BookController method return: getAllBooksByUserStatus | Found {} items", books.size());

        return ResponseEntity.ok(books);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(
        @PathVariable(name = "userId") int userId,
        @RequestParam(name = "joinMode", required = false, defaultValue = "without") String strJoinMode
    ) {
        JoinMode joinMode = JoinMode.fromValue(strJoinMode);

        logger.trace("BookController method entrance: getAllBooksByUserStatus | Params: userId {} ; join mode {}",
                userId, joinMode);

        UserDto user = userService.getUserById(userId, joinMode);

        logger.trace("BookController method return: getAllBooksByUserStatus | User was found successfully");

        return ResponseEntity.ok(user);
    }



    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserDto createUserDto) {
        logger.trace("BookController method entrance: createUser");

        if (!createUserDtoValidators.isValid(createUserDto)) {
            throw new BadRequestException("Body contains invalid symbols");
        }

        UserDto user = userService.createUser(createUserDto);

        logger.trace("BookController method return: createUser | User was successfully created");

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/{userId}/book-status")
    public ResponseEntity<UserDto> createStatus(
        @PathVariable(name = "userId") int userId,
        @RequestBody CreateBookStatusDto statusDto
    ) {
        logger.trace("BookController method entrance: createStatus");

        UserDto user = userService.createStatus(userId, statusDto);

        logger.trace("BookController method return: createStatus | User was successfully created");

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }



    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(
        @PathVariable(name = "userId") int userId,
        @RequestBody UpdateUserDto updateUserDto
    ) {
        logger.trace("BookController method entrance: updateUser");

        if (!updateUserDtoValidators.isValid(updateUserDto)) {
            throw new BadRequestException("Body contains invalid symbols");
        }

        UserDto user = userService.updateUser(userId, updateUserDto);

        logger.trace("BookController method return: updateUser | User was successfully updated");

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PutMapping("/{userId}/book-status")
    public ResponseEntity<UserDto> updateStatus(
        @PathVariable(name = "userId") int userId,
        @RequestBody UpdateBookStatusDto statusDto
    ) {
        logger.trace("BookController method entrance: updateStatus");

        UserDto user = userService.updateStatus(userId, statusDto);

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
    public ResponseEntity<UserDto> deleteStatus(
        @PathVariable(name = "userId") int userId,
        @PathVariable(name = "bookId") int bookId
    ) {
        logger.trace("BookController method entrance: deleteStatus");

        UserDto user = userService.deleteStatusEntity(userId, bookId);

        logger.trace("BookController method return: deleteUser | Status was successfully deleted");

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

}
