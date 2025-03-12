package maksim.reviewsservice.controllers;

import maksim.reviewsservice.models.dtos.ReviewDto;
import maksim.reviewsservice.models.entities.Review;
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
        ReviewDto review = new ReviewDto();

        when(reviewService.getById(anyInt(), any(JoinMode.class))).thenReturn(review);

        ResponseEntity<ReviewDto> response = reviewController.getReviewById(1, "without");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(review, response.getBody());

        verify(reviewService, times(1)).getById(1, JoinMode.WITHOUT);
    }

    @Test
    void getReviewsByUserOrBookId() {
        List<ReviewDto> reviews = List.of(new ReviewDto());

        when(reviewService.getAllByBookOrUserId(anyInt(), any(SelectionCriteria.class), any(JoinMode.class), any(Pageable.class)))
                .thenReturn(reviews);
        when(pagination.getPageable(anyInt(), anyInt(), any(SortField.class), any(SortDirection.class)))
                .thenReturn(pageable);

        ResponseEntity<List<ReviewDto>> response = reviewController.getReviewsByUserOrBookId(
                1, "bookId", "without", 0, 20, "rating", "desc");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());

        verify(reviewService, times(1)).getAllByBookOrUserId(anyInt(), any(SelectionCriteria.class), any(JoinMode.class), any(Pageable.class));
    }

    @Test
    void addReview() {
        ReviewDto review = new ReviewDto();
        CreateReviewDto createReviewDto = new CreateReviewDto();

        when(stringValidators.textScreening(anyString())).thenReturn("Great book!");
        when(stringValidators.isSafeFromSqlInjection(any())).thenReturn(true);
        when(reviewService.addReview(any(CreateReviewDto.class))).thenReturn(review);

        ResponseEntity<ReviewDto> response = reviewController.addReview(createReviewDto);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(review, response.getBody());

        verify(reviewService, times(1)).addReview(any(CreateReviewDto.class));
    }

    @Test
    void addLike() {
        doNothing().when(reviewService).addLike(any());

        ResponseEntity<String> response = reviewController.addLike(any(CreateLikeDto.class));

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
        ReviewDto review = new ReviewDto();
        UpdateReviewDto updateReviewDto = new UpdateReviewDto();

        when(stringValidators.textScreening(anyString())).thenReturn("Updated review text");
        when(stringValidators.isSafeFromSqlInjection(any())).thenReturn(true);
        when(reviewService.updateReview(anyInt(), any(UpdateReviewDto.class))).thenReturn(review);

        ResponseEntity<ReviewDto> response = reviewController.updateReview(1, updateReviewDto);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(review, response.getBody());
        verify(reviewService, times(1)).updateReview(1, updateReviewDto);
    }
}