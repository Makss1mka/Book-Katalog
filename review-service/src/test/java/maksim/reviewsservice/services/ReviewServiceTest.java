package maksim.reviewsservice.services;

import maksim.reviewsservice.exceptions.NotFoundException;
import maksim.reviewsservice.models.dtos.CreateLikeDto;
import maksim.reviewsservice.models.dtos.CreateReviewDto;
import maksim.reviewsservice.models.dtos.ReviewDto;
import maksim.reviewsservice.models.dtos.UpdateReviewDto;
import maksim.reviewsservice.models.entities.Review;
import maksim.reviewsservice.models.entities.User;
import maksim.reviewsservice.repositories.ReviewRepository;
import maksim.reviewsservice.repositories.UserRepository;
import maksim.reviewsservice.utils.enums.JoinMode;
import maksim.reviewsservice.utils.enums.SelectionCriteria;
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

    @InjectMocks
    private ReviewService reviewService;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pageable = PageRequest.of(0, 10);
    }

//    @Test
//    void getById() {
//        Review review = new Review();
//
//        when(reviewRepository.findByIdWithJoin(anyInt())).thenReturn(Optional.of(review));
//        when(reviewRepository.findByIdWithoutJoin(anyInt())).thenReturn(Optional.of(review));
//
//        ReviewDto result;
//
//        result = reviewService.getById(1, JoinMode.WITH);
//        assertNotNull(result);
//
//        result = reviewService.getById(1, JoinMode.WITHOUT);
//        assertNotNull(result);
//
//        verify(reviewRepository, times(1)).findByIdWithJoin(1);
//        verify(reviewRepository, times(1)).findByIdWithoutJoin(1);
//    }

    @Test
    void getById_NotFound() {
        when(reviewRepository.findByIdWithJoin(anyInt())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            reviewService.getById(1, JoinMode.WITH);
        });
    }

//    @Test
//    void getAllByBookOrUserId() {
//        List<Review> reviews = List.of(new Review());
//
//        when(reviewRepository.findByBookIdWithJoin(1, pageable)).thenReturn(reviews);
//        when(reviewRepository.findByBookIdWithoutJoin(1, pageable)).thenReturn(reviews);
//        when(reviewRepository.findByUserIdWithJoin(1, pageable)).thenReturn(reviews);
//        when(reviewRepository.findByUserIdWithoutJoin(1, pageable)).thenReturn(reviews);
//
//        List<ReviewDto> result;
//
//        result = reviewService.getAllByBookOrUserId(1, SelectionCriteria.BOOK, JoinMode.WITH, pageable);
//        assertEquals(1, result.size());
//
//        result = reviewService.getAllByBookOrUserId(1, SelectionCriteria.BOOK, JoinMode.WITHOUT, pageable);
//        assertEquals(1, result.size());
//
//        result = reviewService.getAllByBookOrUserId(1, SelectionCriteria.USER, JoinMode.WITH, pageable);
//        assertEquals(1, result.size());
//
//        result = reviewService.getAllByBookOrUserId(1, SelectionCriteria.USER, JoinMode.WITHOUT, pageable);
//        assertEquals(1, result.size());
//
//        verify(reviewRepository, times(1)).findByBookIdWithJoin(1, pageable);
//        verify(reviewRepository, times(1)).findByBookIdWithoutJoin(1, pageable);
//        verify(reviewRepository, times(1)).findByUserIdWithJoin(1, pageable);
//        verify(reviewRepository, times(1)).findByUserIdWithoutJoin(1, pageable);
//    }
//
//    @Test
//    void addReview() {
//        Review review = new Review();
//
//        CreateReviewDto createReviewDto = new CreateReviewDto();
//
//        when(reviewRepository.save(any(Review.class))).thenReturn(review);
//
//        ReviewDto result = reviewService.addReview(createReviewDto);
//
//        assertNotNull(result);
//
//        verify(reviewRepository, times(1)).save(any(Review.class));
//    }

    @Test
    void addLike() {
        User user = new User();
        Review review = new Review();
        CreateLikeDto createLikeDto = new CreateLikeDto();

        when(reviewRepository.findById(any())).thenReturn(Optional.of(review));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        reviewService.addLike(createLikeDto);

        assertEquals(1, review.getLikes());
        assertEquals(1, review.getLikedUsers().size());

        verify(reviewRepository, times(1)).findById(any());
        verify(userRepository, times(1)).findById(any());
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    void deleteReview() {
        Review review = new Review();

        when(reviewRepository.findByIdWithJoin(anyInt())).thenReturn(Optional.of(review));

        reviewService.deleteReview(1);

        verify(reviewRepository, times(1)).findByIdWithJoin(1);
        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    void deleteLike() {
        User user = new User();

        Review review = new Review();
        review.setLikes(1);
        review.getLikedUsers().add(user);

        when(reviewRepository.findByIdWithJoin(anyInt())).thenReturn(Optional.of(review));
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));

        reviewService.deleteLike(1, 1);

        assertEquals(0, review.getLikes());
        verify(reviewRepository, times(1)).findByIdWithJoin(1);
        verify(userRepository, times(1)).findById(1);
        verify(reviewRepository, times(1)).save(review);
    }

//    @Test
//    void updateReview() {
//        Review review = new Review();
//        UpdateReviewDto updateReviewDto = new UpdateReviewDto();
//        updateReviewDto.setText("Updated review text");
//        updateReviewDto.setRating(4);
//
//        when(reviewRepository.findByIdWithJoin(anyInt())).thenReturn(Optional.of(review));
//
//        ReviewDto result;
//
//        result = reviewService.updateReview(1, updateReviewDto);
//        assertEquals("Updated review text", result.getText());
//        assertEquals(4, result.getRating());
//
//        updateReviewDto.setText(null);
//        updateReviewDto.setRating(null);
//        result = reviewService.updateReview(1, updateReviewDto);
//        assertEquals("Updated review text", result.getText());
//        assertEquals(4, result.getRating());
//
//        updateReviewDto.setText("");
//        result = reviewService.updateReview(1, updateReviewDto);
//        assertEquals("Updated review text", result.getText());
//        assertEquals(4, result.getRating());
//
//        verify(reviewRepository, times(3)).findByIdWithJoin(1);
//        verify(reviewRepository, times(1)).save(review);
//    }
}