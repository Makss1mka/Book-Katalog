package maksim.reviewsservice.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReviewDto {
    @NotNull(message = "Review id is required")
    private Integer bookId;

    @NotNull(message = "User id is required")
    private Integer userId;

    @NotNull(message = "Rating is required")
    private Integer rating;

    @NotBlank
    @Size(max = 200, message = "Review text should be less than 200 chars.")
    private String text;
}
