package maksim.userservice.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import maksim.userservice.utils.enums.BookStatus;

@Getter
@Setter
@Schema(description = "Data updating book status")
public class UpdateBookStatusDto {
    @NotBlank(message = "Username shouldn't be as empty string")
    @Size(min = 3, max = 50, message = "Username length should be from 3 to 50 chars")
    @Schema(description = "Book id", example = "16")
    private Integer bookId;

    @NotNull(message = "Status shouldn't be null")
    @Schema(description = "Status", example = "READ")
    private BookStatus status;

    @NotNull(message = "Status value shouldn't be null")
    @Schema(description = "Status value", example = "true")
    private Boolean statusValue = null;

    UpdateBookStatusDto(Integer bookId, String status, Boolean statusValue) {
        this.bookId = bookId;
        this.status = BookStatus.fromValue(status);
        this.statusValue = statusValue;
    }
}
