package maksim.reviewsservice.controllers;

import maksim.reviewsservice.config.GlobalExceptionHandler;
import maksim.reviewsservice.models.Review;
import maksim.reviewsservice.models.dtos.LikeDtoForCreating;
import maksim.reviewsservice.models.dtos.ReviewDtoForCreating;
import maksim.reviewsservice.models.dtos.ReviewDtoForUpdating;
import maksim.reviewsservice.services.ReviewService;
import maksim.reviewsservice.utils.Pagination;
import maksim.reviewsservice.utils.enums.ReviewLikeTableLinkingMode;
import maksim.reviewsservice.utils.enums.SortDirection;
import maksim.reviewsservice.utils.enums.SortField;
import maksim.reviewsservice.utils.validators.StringValidators;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReviewControllerTest {
    @Mock
    private ReviewService reviewService;

    @Mock
    private Pagination pagination;

    @Mock
    private StringValidators stringValidators;

    @InjectMocks
    private ReviewController reviewController;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getReviewById() {
        Review review = new Review();

        when(reviewService.getById(anyInt(), any(ReviewLikeTableLinkingMode.class))).thenReturn(review);

        ResponseEntity<Review> response = reviewController.getReviewById(1, "without");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(review, response.getBody());

        verify(reviewService, times(1)).getById(1, ReviewLikeTableLinkingMode.WITHOUT_LINKING);
    }

    @Test
    void getReviewsByUserOrBookId() {
        List<Review> reviews = List.of(new Review());

        when(reviewService.getByBookId(anyInt(), any(ReviewLikeTableLinkingMode.class), any(Pageable.class)))
                .thenReturn(reviews);
        when(pagination.getPageable(anyInt(), anyInt(), any(SortField.class), any(SortDirection.class)))
                .thenReturn(pageable);

        ResponseEntity<List<Review>> response = reviewController.getReviewsByUserOrBookId(
                1, "bookId", "without", 0, 20, "rating", "desc");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());

        verify(reviewService, times(1)).getByBookId(1, ReviewLikeTableLinkingMode.WITHOUT_LINKING, pageable);
    }

    @Test
    void addReview() {
        Review review = new Review();
        ReviewDtoForCreating reviewDtoForCreating = new ReviewDtoForCreating();

        when(stringValidators.textScreening(anyString())).thenReturn("Great book!");
        when(stringValidators.isSafeFromSqlInjection(any())).thenReturn(true);
        when(reviewService.addReview(any(ReviewDtoForCreating.class))).thenReturn(review);

        ResponseEntity<Review> response = reviewController.addReview(reviewDtoForCreating);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(review, response.getBody());

        verify(reviewService, times(1)).addReview(any(ReviewDtoForCreating.class));
    }

    @Test
    void addLike() {
        doNothing().when(reviewService).addLike(any());

        ResponseEntity<String> response = reviewController.addLike(any(LikeDtoForCreating.class));

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Like was added successfully", response.getBody());

        verify(reviewService, times(1)).addLike(any());
    }

    @Test
    void deleteReview_ShouldReturnSuccessMessage() {
        doNothing().when(reviewService).deleteReview(anyInt());

        ResponseEntity<String> response = reviewController.deleteReview(1);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Review was successfully deleted", response.getBody());

        verify(reviewService, times(1)).deleteReview(1);
    }

    @Test
    void deleteLike_ShouldReturnSuccessMessage() {
        doNothing().when(reviewService).deleteLike(anyInt(), anyInt());

        ResponseEntity<String> response = reviewController.deleteLike(1, 1);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Like to review was successfully deleted", response.getBody());

        verify(reviewService, times(1)).deleteLike(1, 1);
    }

    @Test
    void updateReview_ShouldReturnUpdatedReview() {
        Review review = new Review();
        ReviewDtoForUpdating reviewDtoForUpdating = new ReviewDtoForUpdating();

        when(stringValidators.textScreening(anyString())).thenReturn("Updated review text");
        when(stringValidators.isSafeFromSqlInjection(any())).thenReturn(true);
        when(reviewService.updateReview(anyInt(), any(ReviewDtoForUpdating.class))).thenReturn(review);

        ResponseEntity<Review> response = reviewController.updateReview(1, reviewDtoForUpdating);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(review, response.getBody());
        verify(reviewService, times(1)).updateReview(1, reviewDtoForUpdating);
    }
}