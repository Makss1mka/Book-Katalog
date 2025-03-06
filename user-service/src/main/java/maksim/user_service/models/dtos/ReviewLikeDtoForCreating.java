package maksim.user_service.models.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReviewLikeDtoForCreating {
    @NotNull(message = "Review id is required")
    private Integer reviewId;

    @NotNull(message = "User id is required")
    private Integer userId;
}
