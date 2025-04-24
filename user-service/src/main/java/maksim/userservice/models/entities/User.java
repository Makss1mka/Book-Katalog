package maksim.userservice.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.*;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "users")
public class User {
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.PERSIST)
    private List<UserBookStatus> bookStatuses = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_book_likes",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    private Set<Book> likedBooks = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "profile_pic_path")
    private String profilePicPath;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "role", nullable = false, columnDefinition = "Text USER")
    private String role = "USER";

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "reg_date", nullable = false)
    private Date registrationDate;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;
}
