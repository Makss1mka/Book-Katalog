package maksim.userservice.models.dtos.crud;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateLikeDto {
    @NotNull(message = "Book id is required")
    @Min(value = 0, message = "Book id should be greater than 0")
    private Integer bookId;
}

