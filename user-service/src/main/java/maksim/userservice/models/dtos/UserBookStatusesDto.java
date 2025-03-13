package maksim.userservice.models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import maksim.userservice.models.entities.Book;
import maksim.userservice.models.entities.UserBookStatuses;
import maksim.userservice.utils.enums.BookStatus;
import maksim.userservice.utils.enums.JoinMode;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserBookStatusesDto {
    private Integer id = null;
    private Boolean like = null;
    private BookStatus status = null;
    private Book book = null;

    public UserBookStatusesDto(UserBookStatuses statuses, JoinMode mode) {
        this.id = statuses.getId();
        this.like = statuses.getLike();
        this.status = BookStatus.fromValue(statuses.getStatus());

        if (mode == JoinMode.WITH_STATUSES_AND_BOOKS) {
            this.book = statuses.getBook();
        }
    }

    public UserBookStatusesDto() {}
}
