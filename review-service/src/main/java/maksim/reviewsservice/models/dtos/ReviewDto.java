package maksim.reviewsservice.models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import maksim.reviewsservice.models.entities.Review;
import maksim.reviewsservice.models.entities.User;
import maksim.reviewsservice.utils.enums.JoinMode;

import java.util.Set;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewDto {
    private Set<User> likedUsers = null;
    private Integer id = null;
    private String text = null;
    private Integer rating = null;
    private Integer likes = null;
    private Integer bookId = null;
    private Integer userId = null;

    public ReviewDto(Review review, JoinMode joinMode) {
        this.id = review.getId();
        this.text = review.getText();
        this.rating = review.getRating();
        this.likes = review.getLikes();
        this.bookId = review.getBookId();
        this.userId = review.getUserId();

        if (joinMode == JoinMode.WITH) {
            this.likedUsers = review.getLikedUsers();
        }
    }

    public ReviewDto() {}
}
