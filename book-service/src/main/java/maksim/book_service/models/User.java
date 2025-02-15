package maksim.book_service.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter @Setter @ToString
@Entity
@Table(name = "users")
public class User {
//    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    @JsonManagedReference
//    private List<Book> books = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable = false)
    private int id;

    @Column(name="name", nullable = false, unique = true)
    private String name;

    @Column(name="profile_pic_path", nullable = true)
    private String profilePicPath;

    @Column(name="email", nullable = false, unique = true)
    private String email;

    @Column(name="role", nullable = false, columnDefinition = "Text USER")
    private String role;

    @JsonIgnore
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="reg_date", nullable = false)
    private Date registrationDate;

    @JsonIgnore
    @Column(name="password", nullable = false)
    private String password;
}
