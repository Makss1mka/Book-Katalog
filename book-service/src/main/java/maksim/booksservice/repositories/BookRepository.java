package maksim.booksservice.repositories;

import java.util.List;
import java.util.Optional;
import maksim.booksservice.models.entities.Book;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {
    @Query("SELECT b FROM Book b JOIN FETCH b.author LEFT JOIN FETCH b.statusesLogs s WHERE b.id = :id")
    Optional<Book> findByIdWithJoin(@Param("id") int id);

    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdWithoutJoin(@Param("id") int id);

    @Query(value = "SELECT b.* FROM books b JOIN users a ON b.author_id = a.id " +
            " WHERE (:keyWords IS NULL OR (' ' || b.name || ' ' LIKE '% ' || :keyWords || '% %' " +
            " OR ' ' || a.name || ' ' LIKE '% ' || :keyWords || '% %'))" +
            " AND (:genres_len = 0 OR b.genres @> CAST(:genres AS VARCHAR[])) ",
            nativeQuery = true)
    List<Book> searchBooks(@Param("genres") String[] genres, @Param("genres_len") int genresLen, @Param("keyWords") String keyWords, Pageable pageable);


}
