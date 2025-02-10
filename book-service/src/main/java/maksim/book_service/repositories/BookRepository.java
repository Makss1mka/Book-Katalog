package maksim.book_service.repositories;

import maksim.book_service.models.Book;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {
    public Optional<Book> findById(int id);

    public List<Book> findByName(String name, Pageable pageable);
    public List<Book> findByStatusReading(int count, Pageable pageable);
    public List<Book> findByStatusAlreadyRead(int count, Pageable pageable);
    public List<Book> findByStatusDrop(int count, Pageable pageable);
    public List<Book> findByRating(int rating, Pageable pageable);
    public List<Book> findByRatingCount(int ratingCount, Pageable pageable);

    @Query("SELECT b FROM books b WHERE b.author.name = :authorName")
    public List<Book> findByAuthorName(String authorName, Pageable pageable);
    @Query("SELECT b FROM books b WHERE b.author.id = :id")
    public List<Book> findByAuthorId(int id, Pageable pageable);

    @Query("SELECT b FROM books b WHERE b.rating >= :rating")
    public List<Book> findByRatingMoreThan(int rating, Pageable pageable);
    @Query("SELECT b FROM books b WHERE b.rating <= :rating")
    public List<Book> findByRatingLessThan(int rating, Pageable pageable);

    @Query("SELECT b FROM books b WHERE b.issued_date >= :date")
    public List<Book> findByDateMoreThan(Date date, Pageable pageable);
    @Query("SELECT b FROM books b WHERE b.issued_date <= :date")
    public List<Book> findByDateLessThan(Date date, Pageable pageable);

    @Query("SELECT b FROM books b WHERE :genres <@ b.genres")
    public List<Book> findAllByGenres(String[] genres, Pageable pageable);

}
