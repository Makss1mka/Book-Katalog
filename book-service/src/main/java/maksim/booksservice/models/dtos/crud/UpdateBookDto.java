package maksim.booksservice.models.dtos.crud;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Schema for updating book data")
public class UpdateBookDto {
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50, message = "Name should be 3-50 chars length")
    @Schema(description = "New book name", example = "New Name")
    private String name;

    @Schema(description = "New list of genres", example = "[ \"Adventure\", \"Horror\", \"Action\" ]")
    private List<String> genres = new ArrayList<>();
}
