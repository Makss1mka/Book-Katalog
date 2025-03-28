package maksim.booksservice.models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import maksim.booksservice.models.entities.Book;
import maksim.booksservice.utils.enums.JoinMode;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Returning book object")
public class BookDto {
    @Schema(description = "book id", example = "1")
    private Integer id = null;

    @Schema(description = "book name", example = "Властелин колец. Братсво кольца")
    private String name = null;

    @Schema(description = "file path", example = "/some/file/path")
    private String filePath = null;

    @Schema(description = "rating", example = "5.0")
    private Float rating = null;

    @Schema(description = "amount or ratings", example = "10000")
    private Integer ratingsCount = null;

    @Schema(description = "date when book was added", example = "23.03.2006")
    private Date issuedDate = null;

    @Schema(description = "list of book genres", example = "[ \"Adventure\", \"Horror\", \"Action\" ]")
    private List<String> genres = null;

    @Schema(description = "amount of likes", example = "10000")
    private Integer likes = null;

    @Schema(description = "author object", example = "{ \"id\": 17, \"name\": \"Talkin\", \"profilePicPath\": \"some/path\" }")
    private UserDto author = null;

    @Schema(
        description = "statuses object",
        example = """
            [
                {
                    "count": "2",
                    "max_date": "Tue Jan 01 00:00:00 UTC 2222",
                    "status_name": "READ",
                    "min_date": "Mon Jan 01 00:00:00 UTC 1990"
                },
                {
                    "count": "4",
                    "max_date": "Tue Jan 01 00:00:00 UTC 2222",
                    "status_name": "READING",
                    "min_date": "Mon Jan 01 00:00:00 UTC 1990"
                },
                {
                    "count": "5",
                    "max_date": "Tue Jan 01 00:00:00 UTC 2222",
                    "status_name": "DROP",
                    "min_date": "Mon Jan 01 00:00:00 UTC 1990"
                }
            ]
            """)
    private List<Map<String, String>> statuses = null;

    public BookDto(Book book, JoinMode joinModeForAuthor, List<Map<String, String>> statuses) {
        this.id = book.getId();
        this.name = book.getName();
        this.filePath = book.getFilePath();
        this.rating = book.getRating();
        this.ratingsCount = book.getRatingCount();
        this.issuedDate = book.getIssuedDate();
        this.genres = book.getGenres();
        this.likes = book.getLikes();

        if (joinModeForAuthor == JoinMode.WITH) {
            this.author = new UserDto(book.getAuthor());
        }

        if (statuses != null) {
            this.statuses = statuses;
        }
    }

    public BookDto() {}

}
