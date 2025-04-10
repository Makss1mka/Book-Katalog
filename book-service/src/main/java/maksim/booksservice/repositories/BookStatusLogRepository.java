package maksim.booksservice.repositories;

import maksim.booksservice.models.entities.BookStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookStatusLogRepository extends JpaRepository<BookStatusLog, Integer> {
}
