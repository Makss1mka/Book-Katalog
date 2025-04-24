package maksim.reviewsservice.repositories;

import jakarta.ws.rs.QueryParam;
import java.util.List;
import java.util.Optional;
import maksim.reviewsservice.models.entities.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    @Query("SELECT r FROM Review r WHERE r.id = :id")
    Optional<Review> findByIdWithoutJoin(@QueryParam("id") int id);

    @Query("SELECT r FROM Review r JOIN FETCH r.author a LEFT JOIN FETCH r.likedUsers u WHERE r.id = :id")
    Optional<Review> findByIdWithJoin(@QueryParam("id") int id);


    @Query("SELECT r FROM Review r JOIN r.author a WHERE a.id = :userId")
    List<Review> findByUserIdWithoutJoin(@QueryParam("userId") int userId, Pageable pageable);

    @Query("SELECT r FROM Review r JOIN FETCH r.author a LEFT JOIN FETCH r.likedUsers u WHERE a.id = :userId")
    List<Review> findByUserIdWithJoin(@QueryParam("userId") int userId, Pageable pageable);


    @Query("SELECT r FROM Review r WHERE r.bookId = :bookId")
    List<Review> findByBookIdWithoutJoin(@QueryParam("bookId") int bookId, Pageable pageable);

    @Query("SELECT r FROM Review r JOIN FETCH r.author a LEFT JOIN FETCH r.likedUsers u WHERE r.bookId = :bookId")
    List<Review> findByBookIdWithJoin(@QueryParam("bookId") int bookId, Pageable pageable);

}
