package maksim.reviewsservice.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity
@Table(name = "reviews")
public class Review {
    @ManyToMany
    @JoinTable(
            name = "user_review_like",
            joinColumns = @JoinColumn(name = "review_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedUsers = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @Column(name = "text")
    private String text;

    @Column(name = "rating", nullable = false)
    private Integer rating = 0;

    @Column(name = "likes", nullable = false)
    private Integer likes = 0;
}
