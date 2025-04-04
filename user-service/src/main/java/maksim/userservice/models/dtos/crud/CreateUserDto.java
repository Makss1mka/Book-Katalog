package maksim.userservice.models.dtos.crud;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Data for creating user")
public class CreateUserDto {
    @NotBlank(message = "Username shouldn't be as empty string")
    @Size(min = 3, max = 50, message = "Username length should be from 3 to 50 chars")
    @Schema(description = "Username", example = "Some username")
    private String name;

    @NotBlank(message = "Password shouldn't be as empty string")
    @Size(min = 3, max = 50, message = "Password length should be from 3 to 50 chars")
    @Schema(description = "Password", example = "Some password")
    private String password;

    @NotBlank(message = "Email shouldn't be as empty string")
    @Size(min = 3, max = 50, message = "Email length should be from 3 to 50 chars")
    @Email(message = "Invalid email format")
    @Schema(description = "Email", example = "email@email.email")
    private String email;
}
