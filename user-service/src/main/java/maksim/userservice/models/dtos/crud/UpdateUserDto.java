package maksim.userservice.models.dtos.crud;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Data updating book status")
public class UpdateUserDto {
    @Size(min = 3, max = 50, message = "Username length should be from 3 to 50 chars")
    @Schema(description = "New username", example = "New username")
    private String newName = null;

    @Size(min = 3, max = 50, message = "Email length should be from 3 to 50 chars")
    @Email(message = "Invalid format for email")
    @Schema(description = "New Email", example = "new_email@email.email")
    private String newEmail = null;

    @Size(min = 3, max = 50, message = "New password length should be from 3 to 50 chars")
    @Schema(description = "New password", example = "New password")
    private String newPassword = null;

    @Size(min = 3, max = 50, message = "Old password length should be from 3 to 50 chars")
    @Schema(description = "Old password", example = "Old password")
    private String oldPassword = null;
}
