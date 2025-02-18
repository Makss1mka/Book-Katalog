package maksim.book_service.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter @Setter @ToString
@Entity
@Table(name = "books")
public class Book {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    private User author;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "rating", nullable = false)
    private int rating = 0;

    @Column(name = "ratings_count", nullable = false)
    private int ratingCount = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "issued_date", nullable = false)
    private Date issuedDate;

    @Column(name = "genres")
    private List<String> genres;


    // STATUSES: READING

    @Column(name = "status_reading_overall", nullable = false)
    private int statusReadingOverall;

    @Column(name = "status_reading_last_year", nullable = false)
    private int statusReadingLastYear;

    @Column(name = "status_reading_last_month", nullable = false)
    private int statusReadingLastMonth;

    @Column(name = "status_reading_last_week", nullable = false)
    private int statusReadingLastWeek;

    @JsonIgnore
    @Column(name = "status_reading_current_year", nullable = false)
    private int statusReadingCurrentYear;

    @JsonIgnore
    @Column(name = "status_reading_current_month", nullable = false)
    private int statusReadingCurrentMonth;

    @JsonIgnore
    @Column(name = "status_reading_current_week", nullable = false)
    private int statusReadingCurrentWeek;

    // STATUSES: READ

    @Column(name = "status_read_overall", nullable = false)
    private int statusReadOverall;

    @Column(name = "status_read_last_year", nullable = false)
    private int statusReadLastYear;

    @Column(name = "status_read_last_month", nullable = false)
    private int statusReadLastMonth;

    @Column(name = "status_read_last_week", nullable = false)
    private int statusReadLastWeek;

    @Column(name = "status_read_current_year", nullable = false)
    private int statusReadCurrentYear;

    @Column(name = "status_read_current_month", nullable = false)
    private int statusReadCurrentMonth;

    @Column(name = "status_read_current_week", nullable = false)
    private int statusReadCurrentWeek;


    // STATUSES: DROP

    @Column(name = "status_drop_overall", nullable = false)
    private int statusDropOverall;

    @Column(name = "status_drop_last_year", nullable = false)
    private int statusDropLastYear;

    @Column(name = "status_drop_last_month", nullable = false)
    private int statusDropLastMonth;

    @Column(name = "status_drop_last_week", nullable = false)
    private int statusDropLastWeek;

    @Column(name = "status_drop_current_year", nullable = false)
    private int statusDropCurrentYear;

    @Column(name = "status_drop_current_month", nullable = false)
    private int statusDropCurrentMonth;

    @Column(name = "status_drop_current_week", nullable = false)
    private int statusDropCurrentWeek;

}
