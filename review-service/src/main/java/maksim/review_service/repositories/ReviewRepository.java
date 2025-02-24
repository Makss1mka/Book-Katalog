package maksim.review_service.repositories;

import jakarta.ws.rs.QueryParam;
import maksim.review_service.models.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    @Query("SELECT r FROM Review r WHERE r.id = :id")
    Optional<Review> findByIdWithoutLinkingTables(@QueryParam("id") int id);

    Optional<Review> findById(int id);


    @Query("SELECT r FROM Review r WHERE r.userId = :userId")
    List<Review> findByUserIdWithoutLinkingTables(@QueryParam("userId") int userId, Pageable pageable);

    List<Review> findByUserId(int id, Pageable pageable);


    @Query("SELECT r FROM Review r WHERE r.bookId = :bookId")
    List<Review> findByBookIdWithoutLinkingTables(@QueryParam("bookId") int bookId, Pageable pageable);

    List<Review> findByBookId(int id, Pageable pageable);

}
