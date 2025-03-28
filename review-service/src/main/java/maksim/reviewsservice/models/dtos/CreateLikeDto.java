package maksim.reviewsservice.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "Data for creating like")
public class CreateLikeDto {
    @NotNull(message = "Review id is required")
    @Min(value = 0, message = "Review id should be greater than 0")
    @Schema(description = "Review id", example = "16")
    private Integer reviewId;

    @NotNull(message = "User id is required")
    @Min(value = 0, message = "User id should be greater than 0")
    @Schema(description = "User id", example = "16")
    private Integer userId;
}
