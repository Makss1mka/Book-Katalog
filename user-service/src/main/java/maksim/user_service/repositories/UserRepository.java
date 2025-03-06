package maksim.user_service.repositories;

import maksim.user_service.models.Book;
import maksim.user_service.models.User;
import maksim.user_service.utils.enums.BookStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findById(int id);

    @Query("SELECT u FROM User u JOIN FETCH u.userBookStatuses s WHERE u.id = :id")
    Optional<User> findByIdWithJoinStatuses(@Param(value = "id") int id);

    @Query("SELECT u FROM User u JOIN FETCH u.userBookStatuses s JOIN FETCH s.statusBook k WHERE u.id = :id")
    Optional<User> findByIdWithJoinStatusesAndBooks(@Param(value = "id") int id);

    @Query("SELECT b FROM User u " +
            "JOIN u.userBookStatuses s " +
            "JOIN s.statusBook b " +
            "WHERE (s.statusRead = true AND (:status = 'read' OR :status = 'any')) " +
            "OR (s.statusReading = true AND (:status = 'reading' OR :status = 'any')) " +
            "OR (s.statusDrop = true AND (:status = 'drop' OR :status = 'any')) " +
            "OR (s.like = true AND (:status = 'liked' OR :status = 'any')) " +
            "AND u.id = :userId")
    List<Book> findAllBooksByUserStatus(@Param("userId") int userId, @Param("status") String status, Pageable pageable);



}
