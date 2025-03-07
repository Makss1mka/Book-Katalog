package maksim.user_service.models.dtos;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserDto {

    private String name;

    private String password;

    @Email
    private String email;

}
