package maksim.reviewsservice.services;

import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;
import maksim.reviewsservice.models.Review;
import maksim.reviewsservice.models.User;
import maksim.reviewsservice.models.dtos.LikeDtoForCreating;
import maksim.reviewsservice.models.dtos.ReviewDtoForCreating;
import maksim.reviewsservice.models.dtos.ReviewDtoForUpdating;
import maksim.reviewsservice.repositories.ReviewRepository;
import maksim.reviewsservice.repositories.UserRepository;
import maksim.reviewsservice.utils.enums.ReviewLikeTableLinkingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;

    @Autowired
    ReviewService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            KafkaProducerService kafkaProducerService
    ) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.kafkaProducerService = kafkaProducerService;
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



    public Review addReview(ReviewDtoForCreating reviewData) {
        logger.trace("Method enter: addReview | Params: bookId {} ; userId {} ; rating {}",
                reviewData.getBookId(), reviewData.getUserId(), reviewData.getRating());

        /*
        * Тут надо проверки на то существуют ли book и user по переданным id
        * */

        Review newReview = new Review();
        newReview.setBookId(reviewData.getBookId());
        newReview.setUserId(reviewData.getUserId());
        newReview.setText(reviewData.getText());
        newReview.setRating(reviewData.getRating());

        reviewRepository.save(newReview);

        kafkaProducerService.publishReviewChanges(newReview, 1);

        logger.trace("Method return: addReview | Result: review add successfully");

        return newReview;
    }

    public void addLike(LikeDtoForCreating likeData) {
        logger.trace("Method enter: addLike | Params: reviewId {} ; userId {}",
                likeData.getReviewId(), likeData.getUserId());

        Optional<Review> review = reviewRepository.findById(likeData.getReviewId());
        Optional<User> user = userRepository.findById(likeData.getUserId());

        if (review.isEmpty() || user.isEmpty()) {
            throw new NotFoundException("Cannot add like (cannot find review or user)");
        }

        int prevSize = review.get().getLikedUsers().size();

        review.get()
                .getLikedUsers()
                .add(user.get());

        if (prevSize != review.get().getLikedUsers().size()) {
            review.get()
                .setLikes(
                    review.get().getLikes() + 1
                );

            reviewRepository.save(review.get());
        }

        logger.trace("Method return: addLike | Result: like to review add successfully");
    }



    public void deleteReview(int reviewId) {
        logger.trace("Method enter: deleteReview | Params: reviewId {}", reviewId);

        Optional<Review> review = reviewRepository.findById(reviewId);

        if (review.isEmpty()) {
            throw new NotFoundException("Cannot delete review (this review doesn't exist)");
        }

        kafkaProducerService.publishReviewChanges(review.get(), -1);

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

        review.get()
                .getLikedUsers()
                .remove(user.get());

        reviewRepository.save(review.get());

        logger.trace("Method return: deleteLike | Result: like to review add successfully");
    }



    public Review updateReview(int reviewId, ReviewDtoForUpdating reviewData) {
        if (reviewData.getText() != null) {
            logger.trace("Method enter: deleteLike | Params: reviewId {} ; rating {} ; new text length {}",
                    reviewId, reviewData.getRating(), reviewData.getText().length());
        } else {
            logger.trace("Method enter: deleteLike | Params: reviewId {} ; rating {} ; new text is null",
                    reviewId, reviewData.getRating());
        }

        Optional<Review> review = reviewRepository.findById(reviewId);

        if (review.isEmpty()) {
            throw new NotFoundException("Cannot update review (such review doesn't exist)");
        }

        int touchedFields = 0;

        if (reviewData.getRating() != null) {
            review.get().setRating(reviewData.getRating());
            touchedFields++;

            kafkaProducerService.publishReviewChanges(review.get(), 0);
        }

        if (reviewData.getText() != null && !reviewData.getText().isEmpty()) {
            review.get().setText(reviewData.getText());
            touchedFields++;
        }

        if (reviewData.getRating() != null || (reviewData.getText() != null && !reviewData.getText().isEmpty())) {
            reviewRepository.save(review.get());
        }

        logger.trace("Method return: updateReview | Result: method was successfully updated ; touched fields {}",
                touchedFields);

        return review.get();
    }

}
