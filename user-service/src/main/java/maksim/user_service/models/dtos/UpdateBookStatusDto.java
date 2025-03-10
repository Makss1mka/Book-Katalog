package maksim.user_service.models.dtos;

import lombok.Getter;
import lombok.Setter;
import maksim.user_service.utils.enums.BookStatus;

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
