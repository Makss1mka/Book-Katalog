package maksim.userservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import maksim.userservice.exceptions.BadRequestException;
import java.util.List;
import maksim.userservice.models.dtos.crud.*;
import maksim.userservice.models.dtos.result.BookDto;
import maksim.userservice.models.dtos.result.UserDto;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final CreateUserDtoValidators createUserDtoValidators;
    private final UpdateUserDtoValidators updateUserDtoValidators;

    @Autowired
    public UserController(
        UserService userService,
        CreateUserDtoValidators createUserDtoValidators,
        UpdateUserDtoValidators updateUserDtoValidators
    ) {
        this.userService = userService;
        this.createUserDtoValidators = createUserDtoValidators;
        this.updateUserDtoValidators = updateUserDtoValidators;
    }

    @GetMapping("/{userId}/books")
    @Operation(
        summary = "Get all users books by status",
        description = "Get all users books by status"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Get books",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(implementation = BookDto.class)
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request (validation failed)",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Some fields contains invalid chars")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "User not found")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Server error",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Something goes wrong, Sorry my bad :(")
            )
        )
    })
    public ResponseEntity<List<BookDto>> getAllBooksByUserStatus(
        @Parameter(description = "user id", required = true)
        @NotNull(message = "User id shouldn't be null") @Min(value = 0, message = "User id should be greater than 0")
        @PathVariable(name = "userId")
        int userId,

        @Parameter(description = "status", required = false)
        @NotBlank(message = "Status shouldn't be empty string") @Size(min = 2, max = 30, message = "Status length should be from 2 to 10 chars")
        @RequestParam(name = "status", required = false, defaultValue = "any")
        String strStatus,

        @Parameter(description = "page num", required = true)
        @NotNull(message = "Page num shouldn't be null") @Min(value = 0, message = "Page num should be greater than 0")
        @RequestParam(name = "pageNum", required = false, defaultValue = "0")
        int pageNum,

        @Parameter(description = "page size", required = true)
        @NotNull(message = "Page size shouldn't be null") @Min(value = 0, message = "Page size should be greater than 0")
        @RequestParam(name = "pageSize", required = false, defaultValue = "20")
        int pageSize
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
    @Operation(
        summary = "Get user",
        description = "Get user data"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Get user",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request (validation failed)",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Invalid value for id")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "User not found")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Server error",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Something goes wrong, Sorry my bad :(")
            )
        )
    })
    public ResponseEntity<UserDto> getUserById(
        @Parameter(description = "user id", required = true)
        @NotNull(message = "User id shouldn't be null") @Min(value = 0, message = "User id should be greater than 0")
        @PathVariable(name = "userId")
        int userId,

        @Parameter(
            description = "join mode (type: String , value: without/with_statuses/with_statuses_and_books)",
            required = true
        )
        @NotBlank(message = "Status shouldn't be empty string") @Size(min = 2, max = 30, message = "Status length should be from 2 to 10 chars")
        @RequestParam(name = "joinMode", required = false, defaultValue = "without")
        String strJoinMode
    ) {
        JoinMode joinMode = JoinMode.fromValue(strJoinMode);

        logger.trace("BookController method entrance: getUserById | Params: userId {} ; join mode {}",
                userId, joinMode);

        UserDto user = userService.getUserById(userId, joinMode);

        logger.trace("BookController method return: getUserById | User was found successfully");

        return ResponseEntity.ok(user);
    }



    @PostMapping
    @Operation(
        summary = "Create user",
        description = "Create new user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User created",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request (validation failed)",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Some fields contains invalid chars")
            )
        ),
        @ApiResponse(
            responseCode = "407",
            description = "User data contains conflicted data",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "User data contains conflicted data")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Server error",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Something goes wrong, Sorry my bad :(")
            )
        )
    })
    public ResponseEntity<UserDto> createUser(
        @Parameter(description = "user data", required = true)
        @Valid @RequestBody
        CreateUserDto createUserDto
    ) {
        logger.trace("BookController method entrance: createUser");

        if (!createUserDtoValidators.isValid(createUserDto)) {
            throw new BadRequestException("Body contains invalid symbols");
        }

        UserDto user = userService.createUser(createUserDto);

        HttpHeaders headers = new HttpHeaders();
        headers.add(
            "Authorization",
            String.format("Bearer %s", userService.getToken(user.getName(), createUserDto.getPassword()))
        );

        logger.trace("BookController method return: createUser | User was successfully created");

        return new ResponseEntity<>(user, headers, HttpStatus.CREATED);
    }



    @PostMapping("/{userId}/book-status")
    @Operation(
        summary = "Add book status for user",
        description = "Add book status for user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Get user",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request (validation failed)",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Some fields contains invalid chars")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "User not found")
            )
        ),
        @ApiResponse(
            responseCode = "407",
            description = "Status data contains conflicted data",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Status data contains conflicted data")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Server error",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Something goes wrong, Sorry my bad :(")
            )
        )
    })
    public ResponseEntity<UserDto> createStatus(
        @Parameter(description = "user id", required = true)
        @NotNull(message = "User id shouldn't be null") @Min(value = 0, message = "User id should be greater than 0")
        @PathVariable(name = "userId")
        int userId,

        @Parameter(description = "Status data", required = true)
        @Valid @RequestBody
        CreateBookStatusDto statusDto
    ) {
        logger.trace("BookController method entrance: createStatus");

        UserDto user = userService.createStatus(userId, statusDto);

        logger.trace("BookController method return: createStatus | User was successfully created");

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/{userId}/like")
    public ResponseEntity<String> createLike(
            @NotNull(message = "User id shouldn't be null") @Min(value = 0, message = "User id should be greater than 0")
            @PathVariable(name = "userId")
            int userId,

            @Valid @RequestBody
            CreateLikeDto likeDto
    ) {
        logger.trace("BookController method entrance: createLike | Params {} {}", userId, likeDto.getBookId());

        userService.addLike(userId, likeDto.getBookId());

        logger.trace("BookController method return: createLike | Like was successfully added");

        return ResponseEntity.ok("Like was successfully added");
    }




    @PatchMapping("/{userId}")
    @Operation(
        summary = "Update user",
        description = "Update user data"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User was updated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request (validation failed)",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Some fields contains invalid chars")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "User not found")
            )
        ),
        @ApiResponse(
            responseCode = "407",
            description = "New user data contains conflicted data",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "New user data contains conflicted data")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Server error",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Something goes wrong, Sorry my bad :(")
            )
        )
    })
    public ResponseEntity<UserDto> updateUser(
        @Parameter(description = "user id", required = true)
        @NotNull(message = "User id shouldn't be null") @Min(value = 0, message = "User id should be greater than 0")
        @PathVariable(name = "userId")
        int userId,

        @Parameter(description = "New user data", required = true)
        @Valid @RequestBody
        UpdateUserDto updateUserDto
    ) {
        logger.trace("BookController method entrance: updateUser");

        if (!updateUserDtoValidators.isValid(updateUserDto)) {
            throw new BadRequestException("Body contains invalid symbols");
        }

        UserDto user = userService.updateUser(userId, updateUserDto);

        logger.trace("BookController method return: updateUser | User was successfully updated");

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }



    @PatchMapping("/{userId}/book-status")
    @Operation(
        summary = "Update user book status",
        description = "Update user book status data"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Status was updated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request (validation failed)",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Some fields contains invalid chars")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "User not found")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Server error",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Something goes wrong, Sorry my bad :(")
            )
        )
    })
    public ResponseEntity<UserDto> updateStatus(
        @Parameter(description = "user id", required = true)
        @NotNull(message = "User id shouldn't be null") @Min(value = 0, message = "User id should be greater than 0")
        @PathVariable(name = "userId")
        int userId,

        @Parameter(description = "New status data", required = true)
        @Valid @RequestBody
        UpdateBookStatusDto statusDto
    ) {
        logger.trace("BookController method entrance: updateStatus");

        UserDto user = userService.updateStatus(userId, statusDto);

        logger.trace("BookController method return: updateStatus | Status was successfully updated");

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    

    @DeleteMapping("/{userId}")
    @Operation(
        summary = "Delete user",
        description = "Delete user data"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User was deleted",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(example = "Text was deleted")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request (validation failed)",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Invalid value for id")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "User not found")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Server error",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Something goes wrong, Sorry my bad :(")
            )
        )
    })
    public ResponseEntity<String> deleteUser(
        @Parameter(description = "user id", required = true)
        @NotNull(message = "User id shouldn't be null") @Min(value = 0, message = "User id should be greater than 0")
        @PathVariable(name = "userId")
        int userId
    ) {
        logger.trace("BookController method entrance: deleteUser");

        userService.deleteUser(userId);

        logger.trace("BookController method return: deleteUser | User was successfully deleted");

        return new ResponseEntity<>("User was successfully deleted", HttpStatus.CREATED);
    }



    @DeleteMapping("/{userId}/book-status/{bookId}")
    @Operation(
        summary = "Delete user book status",
        description = "Delete user book status data"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Status was deleted",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request (validation failed)",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Some fields contains invalid chars")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "User not found")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Server error",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Something goes wrong, Sorry my bad :(")
            )
        )
    })
    public ResponseEntity<UserDto> deleteStatus(
        @Parameter(description = "user id", required = true)
        @NotNull(message = "User id shouldn't be null") @Min(value = 0, message = "User id should be greater than 0")
        @PathVariable(name = "userId")
        int userId,

        @Parameter(description = "book id", required = true)
        @NotNull(message = "Book id shouldn't be null") @Min(value = 0, message = "Book id should be greater than 0")
        @PathVariable(name = "bookId")
        int bookId
    ) {
        logger.trace("BookController method entrance: deleteStatus");

        UserDto user = userService.deleteStatus(userId, bookId);

        logger.trace("BookController method return: deleteUser | Status was successfully deleted");

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @DeleteMapping("/{userId}/like")
    public ResponseEntity<String> deleteLike(
            @NotNull(message = "User id shouldn't be null") @Min(value = 0, message = "User id should be greater than 0")
            @PathVariable(name = "userId")
            int userId,

            @NotNull(message = "Review id shouldn't be null") @Min(value = 0, message = "Review id should be greater than 0")
            @RequestParam(name = "toBook")
            int bookId
    ) {
        logger.trace("BookController method entrance: deleteLike");

        userService.deleteLike(userId, bookId);

        logger.trace("BookController method return: createLike | Like was successfully deleted");

        return ResponseEntity.ok("Like was successfully deleted");
    }

}
