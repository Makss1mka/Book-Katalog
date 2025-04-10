package maksim.booksservice.models.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "books")
public class Book {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author = null;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "book_id", referencedColumnName = "id")
    private List<BookStatusLog> statusesLogs = null;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "rating", nullable = false)
    private float rating = 0;

    @Column(name = "ratings_count", nullable = false)
    private int ratingCount = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "issued_date", nullable = false)
    private Date issuedDate;

    @Column(name = "genres")
    private List<String> genres = new ArrayList<>();

    @Column(name = "likes", nullable = false)
    private int likes = 0;
}
