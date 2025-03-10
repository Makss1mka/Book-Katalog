package maksim.user_service.models.dtos;

import lombok.Getter;
import lombok.Setter;
import maksim.user_service.utils.enums.BookStatus;

@Getter
@Setter
public class CreateBookStatusDto {

    private Integer bookId = null;

    private BookStatus status = null;

    CreateBookStatusDto(Integer bookId, String status) {
        this.bookId = bookId;
        this.status = BookStatus.fromValue(status);
    }

}
