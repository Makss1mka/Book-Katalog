package maksim.reviewsservice.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import maksim.reviewsservice.exceptions.ConflictException;
import maksim.reviewsservice.exceptions.NotFoundException;
import maksim.reviewsservice.models.dtos.ReviewDto;
import maksim.reviewsservice.models.entities.Review;
import maksim.reviewsservice.models.entities.User;
import maksim.reviewsservice.models.dtos.CreateLikeDto;
import maksim.reviewsservice.models.dtos.CreateReviewDto;
import maksim.reviewsservice.models.dtos.UpdateReviewDto;
import maksim.reviewsservice.repositories.ReviewRepository;
import maksim.reviewsservice.repositories.UserRepository;
import maksim.reviewsservice.utils.enums.JoinMode;
import maksim.reviewsservice.utils.enums.SelectionCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

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

    private void saveOrThrow(Review review) {
        try {
            reviewRepository.save(review);
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Your review contains conflicted data");
        }
    }


    public ReviewDto getById(int reviewId, JoinMode mode) {
        logger.trace("Method enter: findByUd | Params: review id {} ; mode {}", reviewId, mode);

        Optional<Review> review = switch (mode) {
            case JoinMode.WITH -> reviewRepository.findByIdWithJoin(reviewId);
            case JoinMode.WITHOUT -> reviewRepository.findByIdWithoutJoin(reviewId);
        };

        if (review.isEmpty()) {
            throw new NotFoundException("Cannot find review");
        }

        logger.trace("Method return: findById | Result: found successfully");

        return new ReviewDto(review.get(), mode);
    }

    public List<ReviewDto> getAllByBookOrUserId(int id, SelectionCriteria criteria, JoinMode mode, Pageable pageable) {
        logger.trace("Method enter: getAllByBookOrUserId | Params: id {} ; mode {} ; criteria {}", id, mode, criteria);

        List<Review> reviewsEntities = switch (criteria) {
            case BOOK -> switch (mode) {
                case WITH -> reviewRepository.findByBookIdWithJoin(id, pageable);
                case WITHOUT -> reviewRepository.findByBookIdWithoutJoin(id, pageable);
            };
            case USER -> switch (mode) {
                case WITH -> reviewRepository.findByUserIdWithJoin(id, pageable);
                case WITHOUT -> reviewRepository.findByUserIdWithoutJoin(id, pageable);
            };
        };

        List<ReviewDto> reviews = new ArrayList<>(reviewsEntities.size());

        reviewsEntities.forEach(review -> reviews.add(new ReviewDto(review, mode)));

        logger.trace("Method return: getAllByBookOrUserId | found {} items", reviews.size());

        return reviews;
    }


    public ReviewDto addReview(CreateReviewDto reviewData) {
        logger.trace("Method enter: addReview | Params: bookId {} ; userId {} ; rating {}",
                reviewData.getBookId(), reviewData.getUserId(), reviewData.getRating());

        /*
        * Тут надо проверки на то существуют ли book и user по переданным id
        * */
        Optional<User> user = userRepository.findById(reviewData.getUserId());

        if (user.isEmpty()) {
            throw new NotFoundException("Cannot find such user");
        }

        Review newReview = new Review();
        newReview.setBookId(reviewData.getBookId());
        newReview.setAuthor(user.get());
        newReview.setText(reviewData.getText());
        newReview.setRating(reviewData.getRating());

        saveOrThrow(newReview);

        logger.trace("Method return: addReview | Result: review add successfully");

        return new ReviewDto(newReview, JoinMode.WITHOUT);
    }

    public void addLike(CreateLikeDto likeData) {
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

            saveOrThrow(review.get());
        } else {
            throw new ConflictException("Cannot add like");
        }

        logger.trace("Method return: addLike | Result: like to review add successfully");
    }



    public void deleteReview(int reviewId) {
        logger.trace("Method enter: deleteReview | Params: reviewId {}", reviewId);

        Optional<Review> review = reviewRepository.findByIdWithJoin(reviewId);

        if (review.isEmpty()) {
            throw new NotFoundException("Cannot delete review (this review doesn't exist)");
        }

        reviewRepository.delete(review.get());

        logger.trace("Method return: deleteReview | Result: like to review add successfully");
    }

    public void deleteLike(int reviewId, int userId) {
        logger.trace("Method enter: deleteLike | Params: reviewId {} ; userId {}", reviewId, userId);

        Optional<Review> review = reviewRepository.findByIdWithJoin(reviewId);
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



    public ReviewDto updateReview(int reviewId, UpdateReviewDto reviewData) {
        if (reviewData.getText() != null) {
            logger.trace("Method enter: deleteLike | Params: reviewId {} ; rating {} ; new text length {}",
                    reviewId, reviewData.getRating(), reviewData.getText().length());
        } else {
            logger.trace("Method enter: deleteLike | Params: reviewId {} ; rating {} ; new text is null",
                    reviewId, reviewData.getRating());
        }

        Optional<Review> review = reviewRepository.findByIdWithJoin(reviewId);

        if (review.isEmpty()) {
            throw new NotFoundException("Cannot update review (such review doesn't exist)");
        }

        int touchedFields = 0;

        if (reviewData.getRating() != null) {
            review.get().setRating(reviewData.getRating());
            touchedFields++;
        }

        if (reviewData.getText() != null && !reviewData.getText().isEmpty()) {
            review.get().setText(reviewData.getText());
            touchedFields++;
        }

        if (reviewData.getRating() != null || (reviewData.getText() != null && !reviewData.getText().isEmpty())) {
            saveOrThrow(review.get());
        }

        logger.trace("Method return: updateReview | Result: method was successfully updated ; touched fields {}",
                touchedFields);

        return new ReviewDto(review.get(), JoinMode.WITH);
    }

}
