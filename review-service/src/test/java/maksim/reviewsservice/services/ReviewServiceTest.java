package maksim.reviewsservice.services;

import jakarta.ws.rs.NotFoundException;
import maksim.reviewsservice.models.*;
import maksim.reviewsservice.models.dtos.LikeDtoForCreating;
import maksim.reviewsservice.models.dtos.ReviewDtoForCreating;
import maksim.reviewsservice.models.dtos.ReviewDtoForUpdating;
import maksim.reviewsservice.repositories.ReviewRepository;
import maksim.reviewsservice.repositories.UserRepository;
import maksim.reviewsservice.utils.enums.ReviewLikeTableLinkingMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private ReviewService reviewService;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getById() {
        Review review = new Review();

        when(reviewRepository.findById(anyInt())).thenReturn(Optional.of(review));
        when(reviewRepository.findByIdWithoutLinkingTables(anyInt())).thenReturn(Optional.of(review));

        Review result;

        result = reviewService.getById(1, ReviewLikeTableLinkingMode.WITH_LINKING);
        assertNotNull(result);

        result = reviewService.getById(1, ReviewLikeTableLinkingMode.WITHOUT_LINKING);
        assertNotNull(result);

        verify(reviewRepository, times(1)).findById(1);
        verify(reviewRepository, times(1)).findByIdWithoutLinkingTables(1);
    }

    @Test
    void getById_NotFound() {
        when(reviewRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            reviewService.getById(1, ReviewLikeTableLinkingMode.WITH_LINKING);
        });
    }

    @Test
    void getByBookId() {
        List<Review> reviews = List.of(new Review());

        when(reviewRepository.findByBookId(1, pageable)).thenReturn(reviews);
        when(reviewRepository.findByBookIdWithoutLinkingTables(1, pageable)).thenReturn(reviews);

        List<Review> result;

        result = reviewService.getByBookId(1, ReviewLikeTableLinkingMode.WITH_LINKING, pageable);
        assertEquals(1, result.size());

        result = reviewService.getByBookId(1, ReviewLikeTableLinkingMode.WITHOUT_LINKING, pageable);
        assertEquals(1, result.size());

        verify(reviewRepository, times(1)).findByBookId(1, pageable);
        verify(reviewRepository, times(1)).findByBookIdWithoutLinkingTables(1, pageable);
    }

    @Test
    void getByUserId() {
        List<Review> reviews = List.of(new Review());

        when(reviewRepository.findByUserId(1, pageable)).thenReturn(reviews);
        when(reviewRepository.findByUserIdWithoutLinkingTables(1, pageable)).thenReturn(reviews);

        List<Review> result;

        result = reviewService.getByUserId(1, ReviewLikeTableLinkingMode.WITH_LINKING, pageable);
        assertEquals(1, result.size());

        result = reviewService.getByUserId(1, ReviewLikeTableLinkingMode.WITHOUT_LINKING, pageable);
        assertEquals(1, result.size());

        verify(reviewRepository, times(1)).findByUserId(1, pageable);
        verify(reviewRepository, times(1)).findByUserIdWithoutLinkingTables(1, pageable);
    }

    @Test
    void addReview() {
        Review review = new Review();

        ReviewDtoForCreating reviewDtoForCreating = new ReviewDtoForCreating();

        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        doNothing().when(kafkaProducerService).publishReviewChanges(any(Review.class), anyInt());

        Review result = reviewService.addReview(reviewDtoForCreating);

        assertNotNull(result);

        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void addLike() {
        User user = new User();
        Review review = new Review();
        LikeDtoForCreating likeDtoForCreating = new LikeDtoForCreating();

        when(reviewRepository.findById(any())).thenReturn(Optional.of(review));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        reviewService.addLike(likeDtoForCreating);

        assertEquals(1, review.getLikes());
        assertEquals(1, review.getLikedUsers().size());

        reviewService.addLike(likeDtoForCreating);
        assertEquals(1, review.getLikes());
        assertEquals(1, review.getLikedUsers().size());

        verify(reviewRepository, times(2)).findById(any());
        verify(userRepository, times(2)).findById(any());
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    void deleteReview() {
        Review review = new Review();

        when(reviewRepository.findById(anyInt())).thenReturn(Optional.of(review));
        doNothing().when(kafkaProducerService).publishReviewChanges(any(Review.class), anyInt());

        reviewService.deleteReview(1);

        verify(reviewRepository, times(1)).findById(1);
        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    void deleteLike() {
        User user = new User();

        Review review = new Review();
        review.setLikes(1);
        review.getLikedUsers().add(user);

        when(reviewRepository.findById(anyInt())).thenReturn(Optional.of(review));
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));

        reviewService.deleteLike(1, 1);

        assertEquals(0, review.getLikes());
        verify(reviewRepository, times(1)).findById(1);
        verify(userRepository, times(1)).findById(1);
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    void updateReview() {
        Review review = new Review();
        ReviewDtoForUpdating reviewDtoForUpdating = new ReviewDtoForUpdating();
        reviewDtoForUpdating.setText("Updated review text");
        reviewDtoForUpdating.setRating(4);

        when(reviewRepository.findById(anyInt())).thenReturn(Optional.of(review));
        doNothing().when(kafkaProducerService).publishReviewChanges(any(Review.class), anyInt());

        Review result;

        result = reviewService.updateReview(1, reviewDtoForUpdating);
        assertEquals("Updated review text", result.getText());
        assertEquals(4, result.getRating());

        reviewDtoForUpdating.setText(null);
        reviewDtoForUpdating.setRating(null);
        result = reviewService.updateReview(1, reviewDtoForUpdating);
        assertEquals("Updated review text", result.getText());
        assertEquals(4, result.getRating());

        reviewDtoForUpdating.setText("");
        result = reviewService.updateReview(1, reviewDtoForUpdating);
        assertEquals("Updated review text", result.getText());
        assertEquals(4, result.getRating());

        verify(reviewRepository, times(3)).findById(1);
        verify(reviewRepository, times(1)).save(review);
    }
}