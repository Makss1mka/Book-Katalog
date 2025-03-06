package maksim.user_service.models.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDtoForCreating {

    private String name;

    private String password;

    private String email;

}
