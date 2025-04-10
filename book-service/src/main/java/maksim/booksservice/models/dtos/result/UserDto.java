package maksim.booksservice.models.dtos.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import maksim.booksservice.models.entities.User;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User object (in books is author)")
public class UserDto {
    @Schema(description = "Author id", example = "16")
    private Integer id = null;

    @Schema(description = "Author name", example = "Author name")
    private String name = null;

    @Schema(description = "File path to author profile picture", example = "/some/path")
    private String profilePicPath = null;

    public UserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.profilePicPath = user.getProfilePicPath();
    }

    public UserDto() {}
}
