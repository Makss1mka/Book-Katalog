package maksim.booksservice.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Schema(description = "Schema for adding list of books")
public class AddListOfBooksDto {
    @NotNull(message = "Where is books that will be added")
    @Schema(description = "Addable list of books")
    private List<CreateBookDto> books;
}
