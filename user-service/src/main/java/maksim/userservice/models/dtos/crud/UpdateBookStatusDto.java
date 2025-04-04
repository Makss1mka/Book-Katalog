package maksim.userservice.models.dtos.crud;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import maksim.userservice.utils.enums.BookStatus;

@Getter
@Setter
@Schema(description = "Data updating book status")
public class UpdateBookStatusDto {
    @NotNull(message = "Username shouldn't be as empty string")
    @Min(value = 0, message = "Id should be greater than 0")
    @Schema(description = "Book id", example = "16")
    private Integer bookId;

    @NotNull(message = "Status shouldn't be null")
    @Schema(description = "Status", example = "READ")
    private BookStatus status;

    @NotNull(message = "Status value shouldn't be null")
    @Schema(description = "Status value", example = "true")
    private Boolean statusValue = null;

    public UpdateBookStatusDto(Integer bookId, String status, Boolean statusValue) {
        this.bookId = bookId;
        this.status = BookStatus.fromValue(status);
        this.statusValue = statusValue;
    }
}
