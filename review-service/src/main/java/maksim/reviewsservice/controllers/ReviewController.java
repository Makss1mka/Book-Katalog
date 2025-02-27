package maksim.reviewsservice.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.BadRequestException;
import maksim.reviewsservice.models.dtos.LikeDtoForCreating;
import maksim.reviewsservice.models.Review;
import maksim.reviewsservice.models.dtos.ReviewDtoForCreating;
import maksim.reviewsservice.models.dtos.ReviewDtoForUpdating;
import maksim.reviewsservice.services.ReviewService;
import maksim.reviewsservice.utils.Pagination;
import maksim.reviewsservice.utils.enums.ReviewLikeTableLinkingMode;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/reviews")
public class ReviewController {
    private final static Logger logger = LoggerFactory.getLogger(ReviewService.class);

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

    @GetMapping("/get/by/id/{reviewId}")
    public ResponseEntity<Review> getReviewById(
            @NotNull @Min(0) @PathVariable int reviewId,
            @RequestParam(name = "linkMode", required = false, defaultValue = "without") String strLinkingMode
    ) {
        ReviewLikeTableLinkingMode linkingMode = ReviewLikeTableLinkingMode.fromValue(strLinkingMode);

        logger.trace("Controller method enter: getReviewById | Params: reviewId {} ; linking mode {}",
                reviewId, linkingMode);

        Review review = reviewService.getById(reviewId, linkingMode);

        logger.trace("Controller method return: getReviewById | Result: review was found successfully");

        return new ResponseEntity<>(review, HttpStatus.OK);
    }

    @GetMapping("/get/by/{strSelectionCriteria}/{id}")
    public ResponseEntity<List<Review>> getReviewsByUserOrBookId(
            @NotNull @Min(0) @PathVariable int id,
            @NotBlank @Size(min = 3, max = 7) @PathVariable String strSelectionCriteria,
            @RequestParam(name = "linkMode", required = false, defaultValue = "without") String strLinkingMode,
            @RequestParam(name = "pageNum", required = false, defaultValue = "0") int pageNum,
            @RequestParam(name = "itemsAmount", required = false, defaultValue = "20") int itemsAmount,
            @RequestParam(name = "sortField", required = false, defaultValue = "rating") String sortStrField,
            @RequestParam(name = "sortDir", required = false, defaultValue = "desc") String sortStrDirection
    ) {
        SortField sortField = SortField.fromValue(sortStrField);
        SortDirection sortDirection = SortDirection.fromValue(sortStrDirection);
        SelectionCriteria selectionCriteria = SelectionCriteria.fromValue(strSelectionCriteria);
        ReviewLikeTableLinkingMode linkingMode = ReviewLikeTableLinkingMode.fromValue(strLinkingMode);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sortField, sortDirection);

        logger.trace("""
                Controller method enter: getReviewById |\s
                Params: linking mode {} ; selection criteria {} ; id {} ; pageable {},
               \s""",
                linkingMode, selectionCriteria, id, pageable);

        List<Review> reviews = switch (selectionCriteria) {
            case BOOK -> reviewService.getByBookId(id, linkingMode, pageable);
            case USER -> reviewService.getByUserId(id, linkingMode, pageable);
        };

        logger.trace("Controller method return: getReviewById | Result: found {} items", reviews.size());

        return new ResponseEntity<>(reviews, HttpStatus.OK);

    }


    @PostMapping("/add/review")
    public ResponseEntity<Review> addReview(@Valid @RequestBody ReviewDtoForCreating reviewData) {
        logger.trace("Controller method entry: addReview");

        reviewData.setText(
                stringValidators.textScreening(reviewData.getText())
        );

        if (!stringValidators.isSafeFromSqlInjection(reviewData.getText())) {
            logger.trace("Controller method: addReview | Invalid review text");

            throw new BadRequestException("Cannot add review. Review contains invalid review text.");
        }

        Review createdReview = reviewService.addReview(reviewData);

        logger.trace("Controller method return: addReview | Review was added successfully (review id {})", createdReview.getId());

        return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
    }

    @PostMapping("/add/like")
    public ResponseEntity<String> addLike(@Valid @RequestBody LikeDtoForCreating likeData) {
        logger.trace("Controller method entry: addLike");

        reviewService.addLike(likeData);

        logger.trace("Controller method return: addReview | Review like was added successfully");

        return new ResponseEntity<>("Like was added successfully", HttpStatus.CREATED);
    }


    @DeleteMapping("/delete/review/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable @Min(0) int reviewId) {
        logger.trace("Controller method entry: deleteReview");

        reviewService.deleteReview(reviewId);

        logger.trace("Controller method return: deleteReview | Review was successfully deleted");

        return ResponseEntity.ok("Review was successfully deleted");
    }

    @DeleteMapping("/delete/like/fromUser/{userId}/toReview/{reviewId}")
    public ResponseEntity<String> deleteLike(
            @PathVariable @Min(0) int userId,
            @PathVariable @Min(0) int reviewId
    ) {
        logger.trace("Controller method entry: deleteLike");

        reviewService.deleteLike(reviewId, userId);

        logger.trace("Controller method return: deleteReview | Like to review was successfully deleted");

        return ResponseEntity.ok("Like to review was successfully deleted");
    }


    @PutMapping("/put/review/{reviewId}")
    public ResponseEntity<Review> updateReview(
            @PathVariable @Min(0) int reviewId,
            @Valid @RequestBody ReviewDtoForUpdating reviewData
    ) {
        logger.trace("Controller method entry: updateReview");

        reviewData.setText(
                stringValidators.textScreening(reviewData.getText())
        );

        if (!stringValidators.isSafeFromSqlInjection(reviewData.getText())) {
            logger.trace("Controller method: updateReview | Invalid review text");

            throw new BadRequestException("Cannot update review. New review's text contains invalid chars.");
        }

        Review review = reviewService.updateReview(reviewId, reviewData);

        logger.trace("Controller method return: updateReview | Review was successfully updated");

        return new ResponseEntity<>(review, HttpStatus.OK);
    }

}
