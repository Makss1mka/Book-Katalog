package maksim.booksservice.repositories;

import java.util.Optional;
import maksim.booksservice.models.Book;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {
    @EntityGraph(attributePaths = {"author"})
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdWithAuthor(@Param("id") int id);

    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdWithoutAuthor(@Param("id") int id);
}
