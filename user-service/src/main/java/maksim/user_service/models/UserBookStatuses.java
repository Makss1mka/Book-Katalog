package maksim.user_service.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "user_book_statuses")
public class UserBookStatuses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")  // Связь с User
    private User user;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book statusBook;

    @Transient
    private Book book = null;

//    @JsonIgnore
//    @Column(name = "user_id")
//    private Integer userId;
//
//    @JsonIgnore
//    @Column(name = "book_id")
//    private Integer bookId;

    @Column(name = "status_read")
    private Boolean statusRead;

    @Column(name = "status_reading")
    private Boolean statusReading;

    @Column(name = "status_drop")
    private Boolean statusDrop;

    @Column(name = "like")
    private Boolean like;

}
