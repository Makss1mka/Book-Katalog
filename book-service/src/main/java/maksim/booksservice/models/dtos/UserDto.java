package maksim.booksservice.models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import maksim.booksservice.models.entities.User;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private Integer id = null;
    private String name = null;
    private String profilePicPath = null;
    private String email = null;

    public UserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.profilePicPath = user.getProfilePicPath();
        this.email = user.getEmail();
    }

    public UserDto() {}
}
