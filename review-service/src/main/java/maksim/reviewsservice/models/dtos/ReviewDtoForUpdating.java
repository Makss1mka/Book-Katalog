package maksim.reviewsservice.models.dtos;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDtoForUpdating {
    private Integer rating;

    @Size(max = 200, message = "Review text should be less than 200 chars.")
    private String text;
}
