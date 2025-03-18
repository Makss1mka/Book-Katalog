package maksim.userservice.models.dtos;

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
public class BookDto {
    private Integer id;
    private Integer authorId;
    private String name;
    private String status;

    public BookDto(Book book, BookStatus status) {
        this.id = book.getId();
        this.authorId = book.getAuthorId();
        this.name = book.getName();
        this.status = status.toString();
    }
}
