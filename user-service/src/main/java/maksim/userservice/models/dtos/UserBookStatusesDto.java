package maksim.userservice.models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import maksim.userservice.models.entities.UserBookStatuses;
import maksim.userservice.utils.enums.BookStatus;
import maksim.userservice.utils.enums.JoinMode;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Returning user-book status object")
public class UserBookStatusesDto {
    @Schema(description = "User-book status id", example = "16")
    private Integer id = null;

    @Schema(description = "Like status", example = "true")
    private Boolean like = null;

    @Schema(description = "Status", example = "READ")
    private BookStatus status = null;

    @Schema(description = "Book id", example = "16")
    private Integer bookId = null;

    @Schema(description = "Book object", implementation = BookDto.class)
    private BookDto book = null;

    public UserBookStatusesDto(UserBookStatuses st, JoinMode mode) {
        this.id = st.getId();
        this.like = st.getLike();

        if (st.getStatus() != null) {
            this.status = BookStatus.fromValue(st.getStatus());
        }

        if (mode == JoinMode.WITH_STATUSES) {
            this.bookId = st.getBook().getId();
        } else if (mode == JoinMode.WITH_STATUSES_AND_BOOKS) {
            this.book = new BookDto(st.getBook(), null);
        }
    }

    public UserBookStatusesDto() {}
}
