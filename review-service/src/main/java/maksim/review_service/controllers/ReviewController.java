package maksim.review_service.controllers;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import maksim.review_service.models.Review;
import maksim.review_service.services.ReviewService;
import maksim.review_service.utils.Pagination;
import maksim.review_service.utils.enums.ReviewLikeTableLinkingMode;
import maksim.review_service.utils.enums.SelectionCriteria;
import maksim.review_service.utils.enums.SortDirection;
import maksim.review_service.utils.enums.SortField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReviewController {
    private final static Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewService reviewService;
    private final Pagination pagination;

    @Autowired
    ReviewController(
            ReviewService reviewService,
            Pagination pagination
    ) {
        this.reviewService = reviewService;
        this.pagination = pagination;
    }

    @GetMapping("/reviews/get/by/id/{reviewId}")
    public ResponseEntity<Review> getReviewById(
            @NotNull @Min(0) @PathVariable int reviewId,
            @RequestParam(required = false, defaultValue = "without") String strLinkingMode
    ) {
        ReviewLikeTableLinkingMode linkingMode = ReviewLikeTableLinkingMode.fromValue(strLinkingMode);

        logger.trace("Controller method enter: getReviewById | Params: reviewId {} ; linking mode {}",
                reviewId, linkingMode);

        Review review = reviewService.getById(reviewId, linkingMode);

        logger.trace("Controller method return: getReviewById | Result: review was found successfully");

        return new ResponseEntity<>(review, HttpStatus.OK);
    }

    @GetMapping("/reviews/get/by/{strSelectionCriteria}/{id}")
    public ResponseEntity<List<Review>> getReviewsByUserOrBookId(
            @NotNull @Min(0) @PathVariable int id,
            @NotBlank @Size(min = 4, max = 4) @PathVariable String strSelectionCriteria,
            @RequestParam(required = false, defaultValue = "without") String strLinkingMode,
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "20") int itemsAmount,
            @RequestParam(required = false, defaultValue = "rating") String sortStrField,
            @RequestParam(required = false, defaultValue = "desc") String sortStrDirection
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



}
