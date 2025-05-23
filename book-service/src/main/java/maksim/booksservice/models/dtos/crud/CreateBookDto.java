package maksim.booksservice.models.dtos.crud;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Schema for creating book")
public class CreateBookDto {
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50, message = "Name should be 3-50 chars length")
    @Schema(description = "Book name", example = "Some name")
    private String name;

    @Schema(description = "List of genres", example = "[ \"Adventure\", \"Horror\", \"Action\" ]")
    private List<String> genres = new ArrayList<>();

    @NotNull(message = "Author id is required")
    @Min(value = 0, message = "Author id should be greater than 0")
    @Schema(description = "Author id", example = "16")
    private Integer authorId;

    public CreateBookDto(String name, List<String> genres, Integer authorId) {
        this.name = name;
        this.authorId = authorId;
        this.genres = genres;
    }

    public CreateBookDto() {}
}
