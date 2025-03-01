package maksim.booksservice.repositories;

import java.util.Optional;
import maksim.booksservice.models.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {
    @EntityGraph(value = "Book.author", type = EntityGraph.EntityGraphType.FETCH)
    public Optional<Book> findByIdWithJoin(int id);

    @EntityGraph(value = "Book.author", type = EntityGraph.EntityGraphType.FETCH)
    public Page<Book> findByAllWithJoin(Specification<Book> spec, Pageable pageable);
}
