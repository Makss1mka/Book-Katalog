package maksim.auth_service.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name="id", nullable = false)
    private int id;

    @Column(name="name", nullable = false, unique = true)
    private String name;

    @Column(name="profilePicPath", nullable = true)
    private String profilePicPath;

    @Column(name="email", nullable = false, unique = true)
    private String email;

    @Column(name="role", nullable = false, columnDefinition = "Integer 1")
    private int role;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="registrationDate", nullable = false)
    private Date registrationDate;

}
