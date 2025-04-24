package maksim.reviewsservice.models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import maksim.reviewsservice.models.entities.Review;
import maksim.reviewsservice.models.entities.User;
import maksim.reviewsservice.utils.enums.JoinMode;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Returning review object")
public class ReviewDto {
    @ArraySchema(
        schema = @Schema(
            description = "Liked users",
            implementation = UserDto.class
        )
    )
    private Set<UserDto> likedUsers = null;

    @Schema(description = "Review id", example = "16")
    private Integer id = null;

    @Schema(description = "Review text", example = "Some review text")
    private String text = null;

    @Schema(description = "Rating", example = "4.0")
    private Integer rating = null;

    @Schema(description = "Likes", example = "1000")
    private Integer likes = null;

    @Schema(description = "Book id", example = "16")
    private Integer bookId = null;

    private UserDto author = null;

    public ReviewDto(Review review, JoinMode joinMode) {
        this.id = review.getId();
        this.text = review.getText();
        this.rating = review.getRating();
        this.likes = review.getLikes();
        this.bookId = review.getBookId();

        if (joinMode == JoinMode.WITH) {
            likedUsers = new HashSet<>(review.getLikedUsers().size());

            for (User user : review.getLikedUsers()) {
                likedUsers.add(
                    new UserDto(user)
                );
            }

            author = new UserDto(review.getAuthor());
        }
    }

    public ReviewDto() {}
}
