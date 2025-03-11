package maksim.userservice.models.dtos;

import lombok.Getter;
import lombok.Setter;
import maksim.userservice.utils.enums.BookStatus;

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
