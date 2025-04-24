package maksim.reviewsservice.models.entities;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "reviews")
public class Review {
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_review_likes",
        joinColumns = @JoinColumn(name = "review_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedUsers = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @Column(name = "text")
    private String text;

    @Column(name = "rating", nullable = false)
    private Integer rating = 0;

    @Column(name = "likes", nullable = false)
    private Integer likes = 0;
}
