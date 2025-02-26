package maksim.reviewsservice.models;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LikeDtoForCreating {
    @NotNull(message = "Review id is required")
    private Integer reviewId;

    @NotNull(message = "User id is required")
    private Integer userId;
}
