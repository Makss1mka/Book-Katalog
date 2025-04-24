package maksim.userservice.models.dtos.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import maksim.userservice.models.entities.Book;
import maksim.userservice.models.entities.User;
import maksim.userservice.utils.enums.JoinMode;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class UserDto {
    private Integer id = null;

    private String name = null;

    private String profilePicPath = null;

    private String email = null;

    private List<UserBookStatusDto> bookStatuses = null;

    private List<BookDto> likedBooks = null;

    public UserDto(User user, JoinMode mode) {
        this.id = user.getId();
        this.name = user.getName();
        this.profilePicPath = user.getProfilePicPath();
        this.email = user.getEmail();

        if (mode == JoinMode.WITH_STATUSES || mode == JoinMode.WITH_STATUSES_AND_BOOKS) {
            this.bookStatuses = new ArrayList<>(user.getBookStatuses().size());
            this.likedBooks = new ArrayList<>(user.getLikedBooks().size());

            user.getBookStatuses().forEach(status ->
                this.bookStatuses.add(new UserBookStatusDto(status, mode))
            );

            user.getLikedBooks().forEach(book ->
                this.likedBooks.add(new BookDto(book, null))
            );
        }
    }

}
