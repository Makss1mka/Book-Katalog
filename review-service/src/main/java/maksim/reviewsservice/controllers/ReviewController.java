package maksim.reviewsservice.controllers;

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
import maksim.reviewsservice.exceptions.BadRequestException;
import java.util.List;

import maksim.reviewsservice.models.dtos.ReviewDto;
import maksim.reviewsservice.models.dtos.CreateLikeDto;
import maksim.reviewsservice.models.dtos.CreateReviewDto;
import maksim.reviewsservice.models.dtos.UpdateReviewDto;
import maksim.reviewsservice.services.ReviewService;
import maksim.reviewsservice.utils.Pagination;
import maksim.reviewsservice.utils.enums.JoinMode;
import maksim.reviewsservice.utils.enums.SelectionCriteria;
import maksim.reviewsservice.utils.enums.SortDirection;
import maksim.reviewsservice.utils.enums.SortField;
import maksim.reviewsservice.utils.validators.StringValidators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/reviews")
@Validated
public class ReviewController {
    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;
    private final Pagination pagination;
    private final StringValidators stringValidators;

    @Autowired
    ReviewController(
        ReviewService reviewService,
        Pagination pagination,
        StringValidators stringValidators
    ) {
        this.reviewService = reviewService;
        this.pagination = pagination;
        this.stringValidators = stringValidators;
    }



    @GetMapping("/{reviewId}")
    @Operation(
        summary = "Update review's data",
        description = "Update some review's fields"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Review update",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReviewDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request (validation failed)",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Invalid id value")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Review not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Review not found")
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
    public ResponseEntity<ReviewDto> getReviewById(
            @Parameter(description = "Review id", example = "16", required = false)
            @NotNull(message = "Review id shouldn't be null") @Min(value = 0, message = "Review id should be greater than 0")
            @PathVariable
            int reviewId,

            @Parameter(description = "Linking mode for review", example = "without", required = false)
            @Valid @RequestParam(name = "linkMode", required = false, defaultValue = "without")
            String strLinkingMode
    ) {
        JoinMode linkingMode = JoinMode.fromValue(strLinkingMode);

        logger.trace("Controller method enter: getReviewById | Params: reviewId {} ; linking mode {}",
                reviewId, linkingMode);

        ReviewDto review = reviewService.getById(reviewId, linkingMode);

        logger.trace("Controller method return: getReviewById | Result: review was found successfully");

        return new ResponseEntity<>(review, HttpStatus.OK);
    }



    @GetMapping
    @Operation(
        summary = "Get all review to one book/from one user",
        description = "Get all review to one book/from one user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Reviews returned",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(implementation = ReviewDto.class)
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
            responseCode = "500",
            description = "Server error",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Something goes wrong, Sorry my bad :(")
            )
        )
    })
    public ResponseEntity<List<ReviewDto>> getReviewsByUserOrBookId(
            @Parameter(description = "User/book id", example = "16", required = true)
            @NotNull(message = "ID shouldn't be null") @Min(value = 0, message = "Id should be greater than 0")
            @RequestParam(name = "id")
            int id,

            @Parameter(description = "Selection criteria (userId/bookId)", example = "userId", required = true)
            @NotBlank(message = "Criteria  shouldn't be empty string")
            @Size(min = 2, max = 10, message = "Too much chars for criteria")
            @RequestParam(name = "criteria")
            String strSelectionCriteria,

            @Parameter(description = "Link mode (values: with/without)", example = "without", required = false)
            @RequestParam(name = "joinMode", required = false, defaultValue = "without")
            @NotBlank(message = "Link mode shouldn't be empty string")
            @Size(min = 2, max = 10, message = "Too much chars for joinMode")
            String strJoinMode,

            @Parameter(description = "Page number", example = "0", required = false)
            @RequestParam(name = "pageNum", required = false, defaultValue = "0")
            @Min(value = 0, message = "Page num should be greater than 0")
            int pageNum,

            @Parameter(description = "Page size", example = "20", required = false)
            @RequestParam(name = "pageSize", required = false, defaultValue = "20")
            @Min(value = 0, message = "Page size should be greater than 0")
            int pageSize,

            @Parameter(description = "Sort field (values: likes/rating)", example = "rating", required = false)
            @RequestParam(name = "sortField", required = false, defaultValue = "rating")
            @NotBlank(message = "Sort field shouldn't be empty string")
            @Size(min = 2, max = 10, message = "Too much chars for sort field")
            String sortStrField,

            @Parameter(description = "Sort direction (values: asc/desc)", example = "asc", required = false)
            @RequestParam(name = "sortDir", required = false, defaultValue = "desc")
            @NotBlank(message = "Sort direction shouldn't be empty string")
            @Size(min = 2, max = 10, message = "Too much chars for sort direction")
            String sortStrDirection
    ) {
        SortField sortField = SortField.fromValue(sortStrField);
        SortDirection sortDirection = SortDirection.fromValue(sortStrDirection);
        SelectionCriteria selectionCriteria = SelectionCriteria.fromValue(strSelectionCriteria);
        JoinMode joinMode = JoinMode.fromValue(strJoinMode);

        Pageable pageable = pagination.getPageable(pageNum, pageSize, sortField, sortDirection);

        logger.trace("""
                Controller method enter: getReviewById |\s
                Params: join mode {} ; selection criteria {} ; id {} ; pageable {},
               \s""",
                joinMode, selectionCriteria, id, pageable);

        List<ReviewDto> reviews = reviewService.getAllByBookOrUserId(id, selectionCriteria, joinMode, pageable);

        logger.trace("Controller method return: getReviewById | Result: found {} items", reviews.size());

        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }



    @PostMapping
    @Operation(
            summary = "Create review",
            description = "Create review"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Review was created",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReviewDto.class)
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
            description = "Book/User not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Book/User not found")
            )
        ),
        @ApiResponse(
            responseCode = "407",
            description = "Some new values conflicted with db uniques",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Conflicted data")
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
    public ResponseEntity<ReviewDto> addReview(
        @Parameter(description = "Data for creating review")
        @Valid @RequestBody
        CreateReviewDto reviewData
    ) {
        logger.trace("Controller method entry: addReview");

        reviewData.setText(
            stringValidators.textScreening(reviewData.getText())
        );

        if (!stringValidators.isSafeFromSqlInjection(reviewData.getText())) {
            logger.trace("Controller method: addReview | Invalid review text");

            throw new BadRequestException("Cannot add review. Review contains invalid review text.");
        }

        ReviewDto createdReview = reviewService.addReview(reviewData);

        logger.trace("Controller method return: addReview | Review was added successfully (review id {})", createdReview.getId());

        return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
    }



    @PostMapping("/like")
    @Operation(
        summary = "Add like to review",
        description = "Add like to review"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Like was added to review",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(example = "Like added")
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
            description = "Review not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Review not found")
            )
        ),
        @ApiResponse(
            responseCode = "407",
            description = "Some values conflicted with db uniques",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Conflicted data")
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
    public ResponseEntity<String> addLike(
        @Parameter(description = "Data for creating like")
        @Valid @RequestBody
        CreateLikeDto likeData
    ) {
        logger.trace("Controller method entry: addLike");

        reviewService.addLike(likeData);

        logger.trace("Controller method return: addReview | Review like was added successfully");

        return new ResponseEntity<>("Like was added successfully", HttpStatus.CREATED);
    }



    @DeleteMapping("/{reviewId}")
    @Operation(
        summary = "Delete review",
        description = "Delete review data"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Review deletion",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(example = "Review was deleted")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request (validation failed)",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Invalid id value")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Review not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Review not found")
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
    public ResponseEntity<String> deleteReview(
        @Parameter(description = "Review id", example = "16", required = false)
        @NotNull(message = "Review id shouldn't be null") @Min(value = 0, message = "Review id should be greater than 0")
        @PathVariable
        int reviewId
    ) {
        logger.trace("Controller method entry: deleteReview");

        reviewService.deleteReview(reviewId);

        logger.trace("Controller method return: deleteReview | Review was successfully deleted");

        return ResponseEntity.ok("Review was successfully deleted");
    }



    @DeleteMapping("/like")
    @Operation(
        summary = "Delete like from review",
        description = "Delete like from review"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Like deletion",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(example = "Like was deleted")
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
            description = "Like not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Like not found")
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
    public ResponseEntity<String> deleteLike(
        @Parameter(description = "User id", example = "16", required = false)
        @NotNull(message = "User id shouldn't be null") @Min(value = 0, message = "User id should be greater than 0")
        @RequestParam(name = "fromUser")
        int userId,

        @Parameter(description = "Review id", example = "16", required = false)
        @NotNull(message = "Review id shouldn't be null") @Min(value = 0, message = "Review id should be greater than 0")
        @RequestParam(name = "toReview")
        int reviewId
    ) {
        logger.trace("Controller method entry: deleteLike");

        reviewService.deleteLike(reviewId, userId);

        logger.trace("Controller method return: deleteReview | Like to review was successfully deleted");

        return ResponseEntity.ok("Like to review was successfully deleted");
    }



    @PatchMapping("/{reviewId}")
    @Operation(
        summary = "Update review's data",
        description = "Update some review's fields"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Review update",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReviewDto.class)
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
            description = "Review not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Review not found")
            )
        ),
        @ApiResponse(
            responseCode = "407",
            description = "Some new values conflicted with db uniques",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Conflicted data")
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
    public ResponseEntity<ReviewDto> updateReview(
        @Parameter(description = "Review id", example = "16", required = false)
        @NotNull(message = "Review id shouldn't be null") @Min(value = 0, message = "Review id should be greater than 0")
        @PathVariable
        int reviewId,

        @Parameter(description = "Data for updating review")
        @Valid @RequestBody
        UpdateReviewDto reviewData
    ) {
        logger.trace("Controller method entry: updateReview");

        reviewData.setText(
            stringValidators.textScreening(reviewData.getText())
        );

        if (!stringValidators.isSafeFromSqlInjection(reviewData.getText())) {
            logger.trace("Controller method: updateReview | Invalid review text");

            throw new BadRequestException("Cannot update review. New review's text contains invalid chars.");
        }

        ReviewDto review = reviewService.updateReview(reviewId, reviewData);

        logger.trace("Controller method return: updateReview | Review was successfully updated");

        return new ResponseEntity<>(review, HttpStatus.OK);
    }

}
