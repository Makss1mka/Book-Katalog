package maksim.reviewsservice.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Data for updating review")
public class UpdateReviewDto {
    @Min(value = 0, message = "rating should be greater than 0 and less than 5")
    @Max(value = 5, message = "rating should be greater than 0 and less than 5")
    @Schema(description = "New rating", example = "5.0")
    private Integer rating;

    @NotBlank(message = "Message shouldn't be empty string")
    @Size(max = 200, message = "Review text should be less than 200 chars.")
    @Schema(description = "Review text", example = "Some review text!")
    private String text;
}
