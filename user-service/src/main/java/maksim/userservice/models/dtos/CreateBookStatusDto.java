package maksim.userservice.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import maksim.userservice.utils.enums.BookStatus;

@Getter
@Setter
@Schema(description = "Data for creating book status")
public class CreateBookStatusDto {
    @NotNull(message = "bookId shouldn't be null") @Min(value = 0, message = "id should be greater than 0")
    @Schema(description = "Book id", example = "16")
    private Integer bookId;

    @NotNull(message = "Status shouldn't be as empty string")
    @Schema(description = "Status", example = "READ")
    private BookStatus status = null;

    public CreateBookStatusDto(Integer bookId, String status) {
        this.bookId = bookId;
        this.status = BookStatus.fromValue(status);
    }

}
