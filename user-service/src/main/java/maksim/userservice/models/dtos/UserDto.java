package maksim.userservice.models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import maksim.userservice.models.entities.User;
import maksim.userservice.utils.enums.JoinMode;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private Integer id = null;
    private String name = null;
    private String profilePicPath = null;
    private String email = null;
    private List<UserBookStatusesDto> bookStatuses = null;

    public UserDto(User user, JoinMode mode) {
        this.id = user.getId();
        this.name = user.getName();
        this.profilePicPath = user.getProfilePicPath();
        this.email = user.getEmail();

        if (mode == JoinMode.WITH_STATUSES) {
            this.bookStatuses = new ArrayList<>(user.getBookStatuses().size());

            user.getBookStatuses().forEach(bookStatuses -> {
                this.bookStatuses.add(new UserBookStatusesDto(bookStatuses, mode));
            });
        }
    }

    public UserDto() {}
}
