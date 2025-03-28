package maksim.reviewsservice.models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import maksim.reviewsservice.models.entities.User;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Returning user object")
public class UserDto {
    @Schema(description = "User id", example = "16")
    private Integer id = null;

    @Schema(description = "Username", example = "Username")
    private String name = null;

    @Schema(description = "Profile picture path", example = "Some path")
    private String profilePicPath = null;

    @Schema(description = "Email", example = "email@email.email")
    private String email = null;

    public UserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.profilePicPath = user.getProfilePicPath();
        this.email = user.getEmail();
    }

    public UserDto() {}
}
