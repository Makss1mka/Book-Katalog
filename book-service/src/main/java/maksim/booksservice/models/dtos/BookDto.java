package maksim.booksservice.models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import maksim.booksservice.models.entities.Book;
import maksim.booksservice.utils.enums.JoinMode;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookDto {
    private Integer id = null;
    private String name = null;
    private String filePath = null;
    private Float rating = null;
    private Integer ratingsCount = null;
    private Date issuedDate = null;
    private List<String> genres = null;
    private Integer likes = null;
    private UserDto author = null;
    private List<Map<String, String>> statuses = null;

    public BookDto(Book book, JoinMode joinMode, List<Map<String, String>> statuses) {
        this.id = book.getId();
        this.name = book.getName();
        this.filePath = book.getFilePath();
        this.rating = book.getRating();
        this.ratingsCount = book.getRatingCount();
        this.issuedDate = book.getIssuedDate();
        this.genres = book.getGenres();
        this.likes = book.getLikes();

        if (joinMode == JoinMode.WITH) {
            this.author = new UserDto(book.getAuthor());
        }

        if (statuses != null) {
            this.statuses = statuses;
        }
    }

    public BookDto() {}

}
