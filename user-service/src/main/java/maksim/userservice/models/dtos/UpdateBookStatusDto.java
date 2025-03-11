package maksim.userservice.models.dtos;

import lombok.Getter;
import lombok.Setter;
import maksim.userservice.utils.enums.BookStatus;

@Getter
@Setter
public class UpdateBookStatusDto {

    private Integer bookId = null;

    private BookStatus status = null;

    private Boolean statusValue = null;

    UpdateBookStatusDto(Integer bookId, String status, Boolean statusValue) {
        this.bookId = bookId;
        this.status = BookStatus.fromValue(status);
        this.statusValue = statusValue;
    }

}
