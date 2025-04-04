package maksim.userservice.models.dtos.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;
import maksim.userservice.models.entities.User;
import maksim.userservice.utils.enums.JoinMode;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Schema(description = "Returning user object")
public class UserDto {
    @Schema(description = "User id", example = "16")
    private Integer id = null;

    @Schema(description = "Username", example = "Maks")
    private String name = null;

    @Schema(description = "Profile picture path", example = "/some/path")
    private String profilePicPath = null;

    @Schema(description = "Email", example = "email@email.email")
    private String email = null;

    @ArraySchema(
        schema = @Schema(
            description = "User book statuses",
            implementation = UserBookStatusDto.class
        )
    )
    private List<UserBookStatusDto> bookStatuses = null;

    public UserDto(User user, JoinMode mode) {
        this.id = user.getId();
        this.name = user.getName();
        this.profilePicPath = user.getProfilePicPath();
        this.email = user.getEmail();

        if (mode == JoinMode.WITH_STATUSES || mode == JoinMode.WITH_STATUSES_AND_BOOKS) {
            this.bookStatuses = new ArrayList<>(user.getBookStatuses().size());

            user.getBookStatuses().forEach(statuses ->
                this.bookStatuses.add(new UserBookStatusDto(statuses, mode))
            );
        }
    }

}
