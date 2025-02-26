package maksim.booksservice.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookDtoForCreating {
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50, message = "Name should be 3-50 chars length")
    private String name;
    
    private List<String> genres = new ArrayList<>();

    @NotNull(message = "Author id is required")
    private Integer authorId;
}
