package maksim.booksservice.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UpdateBookDto {
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50, message = "Name should be 3-50 chars length")
    private String name;
    
    private List<String> genres = new ArrayList<>();
}
