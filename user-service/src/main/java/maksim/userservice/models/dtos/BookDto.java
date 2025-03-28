package maksim.userservice.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import maksim.userservice.models.entities.Book;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import maksim.userservice.utils.enums.BookStatus;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Returning book object")
public class BookDto {
    @Schema(description = "Book id", example = "16")
    private Integer id;

    @Schema(description = "Author id", example = "16")
    private Integer authorId;

    @Schema(description = "Book name", example = "Some Name")
    private String name;

    @Schema(description = "Status", example = "READ")
    private String status;

    public BookDto(Book book, BookStatus status) {
        this.id = book.getId();
        this.authorId = book.getAuthorId();
        this.name = book.getName();

        if (status != null) {
            this.status = status.toString();
        }
    }
}
