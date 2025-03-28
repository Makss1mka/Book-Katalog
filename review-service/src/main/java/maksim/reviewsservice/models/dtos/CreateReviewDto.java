package maksim.reviewsservice.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Data for creating review")
public class CreateReviewDto {
    @NotNull(message = "Book id shouldn't be null")
    @Min(value = 0, message = "Book id should be greater than 0")
    private Integer bookId;

    @NotNull(message = "User id shouldn't be null")
    @Min(value = 0, message = "User id should be greater than 0")
    private Integer userId;

    @NotNull(message = "Rating shouldn't be null")
    @Min(value = 0, message = "User id should be greater than 0")
    private Integer rating;

    @NotBlank(message = "Message shouldn't be empty string")
    @Size(max = 200, message = "Review text should be less than 200 chars.")
    private String text;
}
