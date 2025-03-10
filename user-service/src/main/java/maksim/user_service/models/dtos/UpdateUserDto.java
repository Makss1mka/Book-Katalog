package maksim.user_service.models.dtos;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDto {

    private String newName = null;

    @Email
    private String newEmail = null;

    private String newPassword = null;

    private String oldPassword = null;

}
