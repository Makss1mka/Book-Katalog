package maksim.review_service.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class LikeDtoForCreating {
    @NotNull(message = "Book id is required")
    private Integer bookId;

    @NotNull(message = "User id is required")
    private Integer userId;

    @NotBlank
    @Size(max = 200, message = "Review text should be less than 200 chars.")
    private String text;
}
