package maksim.reviewsservice.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import java.util.List;
import maksim.reviewsservice.models.Review;
import maksim.reviewsservice.models.dtos.LikeDtoForCreating;
import maksim.reviewsservice.models.dtos.ReviewDtoForCreating;
import maksim.reviewsservice.models.dtos.ReviewDtoForUpdating;
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
@RequestMapping(value = "/reviews")
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
    public ResponseEntity<Review> getReviewById(
            @NotNull @Min(0) @PathVariable int reviewId,
            @RequestParam(name = "linkMode", required = false, defaultValue = "without") String strLinkingMode
    ) {
        JoinMode linkingMode = JoinMode.fromValue(strLinkingMode);

        logger.trace("Controller method enter: getReviewById | Params: reviewId {} ; linking mode {}",
                reviewId, linkingMode);

        Review review = reviewService.getById(reviewId, linkingMode);

        logger.trace("Controller method return: getReviewById | Result: review was found successfully");

        return new ResponseEntity<>(review, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Review>> getReviewsByUserOrBookId(
            @RequestParam(name = "id") int id,
            @RequestParam(name = "criteria") String strSelectionCriteria,
            @RequestParam(name = "joinMode", required = false, defaultValue = "without") String strJoinMode,
            @RequestParam(name = "pageNum", required = false, defaultValue = "0") int pageNum,
            @RequestParam(name = "itemsAmount", required = false, defaultValue = "20") int itemsAmount,
            @RequestParam(name = "sortField", required = false, defaultValue = "rating") String sortStrField,
            @RequestParam(name = "sortDir", required = false, defaultValue = "desc") String sortStrDirection
    ) {
        SortField sortField = SortField.fromValue(sortStrField);
        SortDirection sortDirection = SortDirection.fromValue(sortStrDirection);
        SelectionCriteria selectionCriteria = SelectionCriteria.fromValue(strSelectionCriteria);
        JoinMode joinMode = JoinMode.fromValue(strJoinMode);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sortField, sortDirection);

        logger.trace("""
                Controller method enter: getReviewById |\s
                Params: join mode {} ; selection criteria {} ; id {} ; pageable {},
               \s""",
                joinMode, selectionCriteria, id, pageable);

        List<Review> reviews = reviewService.getAllByBookOrUserId(id, selectionCriteria, joinMode, pageable);

        logger.trace("Controller method return: getReviewById | Result: found {} items", reviews.size());

        return new ResponseEntity<>(reviews, HttpStatus.OK);

    }


    @PostMapping("")
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

    @PostMapping("/like")
    public ResponseEntity<String> addLike(@Valid @RequestBody LikeDtoForCreating likeData) {
        logger.trace("Controller method entry: addLike");

        reviewService.addLike(likeData);

        logger.trace("Controller method return: addReview | Review like was added successfully");

        return new ResponseEntity<>("Like was added successfully", HttpStatus.CREATED);
    }


    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable @Min(0) int reviewId) {
        logger.trace("Controller method entry: deleteReview");

        reviewService.deleteReview(reviewId);

        logger.trace("Controller method return: deleteReview | Review was successfully deleted");

        return ResponseEntity.ok("Review was successfully deleted");
    }

    @DeleteMapping("/like")
    public ResponseEntity<String> deleteLike(
            @RequestParam(name = "fromUser") int userId,
            @RequestParam(name = "toReview") int reviewId
    ) {
        logger.trace("Controller method entry: deleteLike");

        reviewService.deleteLike(reviewId, userId);

        logger.trace("Controller method return: deleteReview | Like to review was successfully deleted");

        return ResponseEntity.ok("Like to review was successfully deleted");
    }


    @PutMapping("/{reviewId}")
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
