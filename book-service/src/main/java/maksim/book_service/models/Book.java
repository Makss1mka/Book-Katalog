package maksim.book_service.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter @Setter
@Entity
@Table(name = "books")
public class Book {
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "author_id", nullable = false)
    private int authorId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "filePath", nullable = false)
    private String filePath;

    @Column(name = "rating", nullable = false, columnDefinition = "integer 0")
    private int rating;

    @Column(name = "rating_count", nullable = false, columnDefinition = "integer 0")
    private int ratingCount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "issued_date", nullable = false)
    private Date issuedDate;

    @Column(name = "genres", nullable = true)
    private List<String> genres;

}
