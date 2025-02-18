package maksim.bookservice.repositories;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import maksim.bookservice.models.Book;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface BookRepository extends JpaRepository<Book, Integer> {
    public Optional<Book> findById(int id);

    @EntityGraph(attributePaths = {"author"})
    public List<Book> findByName(String name, Pageable pageable);

    public List<Book> findByRating(int rating, Pageable pageable);

    public List<Book> findByRatingCount(int ratingCount, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN FETCH b.author a WHERE a.name = :authorName")
    public List<Book> findByAuthorName(@Param("authorName") String authorName, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN FETCH b.author a WHERE a.id = :id")
    public List<Book> findByAuthorId(@Param("id") int id, Pageable pageable);

    public List<Book> findByRatingGreaterThan(int rating, Pageable pageable);

    public List<Book> findByRatingLessThan(int rating, Pageable pageable);

    public List<Book> findByIssuedDateGreaterThan(Date issuedDate, Pageable pageable);

    public List<Book> findByIssuedDateLessThan(Date issuedDate, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN FETCH b.author a WHERE b.genres IN :genres")
    public List<Book> findAllByGenres(@Param("genres") List<String> genres, Pageable pageable);

}
