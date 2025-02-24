package maksim.review_service.services;

import jakarta.ws.rs.NotFoundException;
import maksim.review_service.models.Review;
import maksim.review_service.models.User;
import maksim.review_service.repositories.ReviewRepository;
import maksim.review_service.repositories.UserRepository;
import maksim.review_service.utils.enums.ReviewLikeTableLinkingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    private final static Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Autowired
    ReviewService(
            ReviewRepository reviewRepository,
            UserRepository userRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    public Review getById(int reviewId, ReviewLikeTableLinkingMode mode) {
        logger.trace("Method enter: findByUd | Params: review id {} ; mode {}", reviewId, mode);

        Optional<Review> review = switch (mode) {
            case ReviewLikeTableLinkingMode.WITH_LINKING -> reviewRepository.findById(reviewId);
            case ReviewLikeTableLinkingMode.WITHOUT_LINKING -> reviewRepository.findByIdWithoutLinkingTables(reviewId);
        };

        if (review.isEmpty()) {
            throw new NotFoundException("Cannot find review");
        }

        logger.trace("Method return: findById | Result: found successfully");

        return review.get();
    }

    public List<Review> getByBookId(int bookId, ReviewLikeTableLinkingMode mode, Pageable pageable) {
        logger.trace("Method enter: getByBookId | Params: book id {} ; mode {}", bookId, mode);

        List<Review> reviews = switch (mode) {
            case WITH_LINKING -> reviewRepository.findByBookId(bookId, pageable);
            case WITHOUT_LINKING -> reviewRepository.findByBookIdWithoutLinkingTables(bookId, pageable);
        };

        logger.trace("Method return: findByBookId | found {} items", reviews.size());

        return reviews;
    }

    public List<Review> getByUserId(int userId, ReviewLikeTableLinkingMode mode, Pageable pageable) {
        logger.trace("Method enter: getByUserId | Params: user id {} ; mode {}", userId, mode);

        List<Review> reviews = switch (mode) {
            case WITH_LINKING -> reviewRepository.findByUserId(userId, pageable);
            case WITHOUT_LINKING -> reviewRepository.findByUserIdWithoutLinkingTables(userId, pageable);
        };

        logger.trace("Method return: findByUserId | found {} items", reviews.size());

        return reviews;
    }



    public Review addReview(int bookId, int userId, String text, int rating) {
        logger.trace("Method enter: addReview | Params: bookId {} ; userId {} ; text size {} ; rating {}",
                bookId, userId, text.length(), rating);

        /*
        * Тут надо проверки на то существуют ли book и user по переданным id
        * */

        Review newReview = new Review();
        newReview.setBookId(bookId);
        newReview.setUserId(userId);
        newReview.setText(text);
        newReview.setRating(rating);

        reviewRepository.save(newReview);

        logger.trace("Method return: addReview | Result: review add successfully");

        return newReview;
    }

    public void addLike(int reviewId, int userId) {
        logger.trace("Method enter: addLike | Params: reviewId {} ; userId {}", reviewId, userId);

        Optional<Review> review = reviewRepository.findById(reviewId);
        Optional<User> user = userRepository.findById(userId);

        if (review.isEmpty() || user.isEmpty()) {
            throw new NotFoundException("Cannot add like (cannot find review or user)");
        }

        review.get()
                .getLikedUsers()
                .add(user.get());

        review.get()
                .setLikes(
                        review.get().getLikes() + 1
                );

        reviewRepository.save(review.get());

        logger.trace("Method return: addLike | Result: like to review add successfully");
    }



    public void deleteReview(int reviewId) {
        logger.trace("Method enter: deleteReview | Params: reviewId {}", reviewId);

        Optional<Review> review = reviewRepository.findById(reviewId);

        if (review.isEmpty()) {
            throw new NotFoundException("Cannot delete review (this review doesn't exist)");
        }

        reviewRepository.delete(review.get());

        logger.trace("Method return: deleteReview | Result: like to review add successfully");
    }

    public void deleteLike(int reviewId, int userId) {
        logger.trace("Method enter: deleteLike | Params: reviewId {} ; userId {}", reviewId, userId);

        Optional<Review> review = reviewRepository.findById(reviewId);
        Optional<User> user = userRepository.findById(userId);

        if (review.isEmpty() || user.isEmpty()) {
            throw new NotFoundException("Cannot delete like (review or user doesn't exist)");
        }

        review.get()
                .setLikes(
                        review.get().getLikes() - 1
                );

        reviewRepository.delete(review.get());

        logger.trace("Method return: deleteLike | Result: like to review add successfully");
    }



    public Review updateReview(int reviewId, Integer rating, String text) {
        if (text != null) {
            logger.trace("Method enter: deleteLike | Params: reviewId {} ; rating {} ; new text length {}",
                    reviewId, rating, text.length());
        } else {
            logger.trace("Method enter: deleteLike | Params: reviewId {} ; rating {} ; new text is null",
                    reviewId, rating);
        }

        Optional<Review> review = reviewRepository.findById(reviewId);

        if (review.isEmpty()) {
            throw new NotFoundException("Cannot update review (such review doesn't exist)");
        }

        int touchedFields = 0;

        if (rating != null) {
            review.get().setRating(rating);
            touchedFields++;
       }

        if (text != null && !text.isEmpty()) {
            review.get().setText(text);
            touchedFields++;
        }

        if (rating != null || text != null) {
            reviewRepository.save(review.get());
        }

        logger.trace("Method return: updateReview | Result: method was successfully updated ; touched fields {}",
                touchedFields);

        return review.get();
    }

}
