package maksim.userservice.repositories;

import java.util.List;
import java.util.Optional;
import maksim.userservice.models.entities.Book;
import maksim.userservice.models.entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findById(int id);

    @Query("SELECT u FROM User u JOIN FETCH u.bookStatuses s WHERE u.id = :id")
    Optional<User> findByIdWithJoinStatuses(@Param(value = "id") int id);

    @Query("SELECT u FROM User u JOIN FETCH u.bookStatuses s JOIN FETCH s.book k WHERE u.id = :id")
    Optional<User> findByIdWithJoinStatusesAndBooks(@Param(value = "id") int id);

    @Query("SELECT b FROM User u "
            + "JOIN u.bookStatuses s "
            + "JOIN s.book b "
            + "WHERE (s.status = :status) "
            + "OR (s.like = true AND (:status = 'liked')) "
            + "OR :status = 'any' "
            + "AND u.id = :userId")
    List<Book> findAllBooksByUserStatus(@Param("userId") int userId, @Param("status") String status, Pageable pageable);

    Optional<User> findByName(String name);

    Optional<User> findByEmail(String name);

}
