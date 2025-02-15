package maksim.book_service.repositories;

import maksim.book_service.models.Book;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookStatusesRepository extends JpaRepository<Book, Integer> {

    // READING

    public List<Book> findByStatusReadingOverallGreaterThan(int num, Pageable pageable);
    public List<Book> findByStatusReadingOverallLessThan(int num, Pageable pageable);
    public List<Book> findByStatusReadingLastYearGreaterThan(int num, Pageable pageable);
    public List<Book> findByStatusReadingLastYearLessThan(int num, Pageable pageable);
    public List<Book> findByStatusReadingLastMonthGreaterThan(int num, Pageable pageable);
    public List<Book> findByStatusReadingLastMonthLessThan(int num, Pageable pageable);
    public List<Book> findByStatusReadingLastWeekGreaterThan(int num, Pageable pageable);
    public List<Book> findByStatusReadingLastWeekLessThan(int num, Pageable pageable);


    // READ

    public List<Book> findByStatusReadOverallGreaterThan(int num, Pageable pageable);
    public List<Book> findByStatusReadOverallLessThan(int num, Pageable pageable);
    public List<Book> findByStatusReadLastYearGreaterThan(int num, Pageable pageable);
    public List<Book> findByStatusReadLastYearLessThan(int num, Pageable pageable);
    public List<Book> findByStatusReadLastMonthGreaterThan(int num, Pageable pageable);
    public List<Book> findByStatusReadLastMonthLessThan(int num, Pageable pageable);
    public List<Book> findByStatusReadLastWeekGreaterThan(int num, Pageable pageable);
    public List<Book> findByStatusReadLastWeekLessThan(int num, Pageable pageable);


    // DROP

    public List<Book> findByStatusDropOverallGreaterThan(int num, Pageable pageable);
    public List<Book> findByStatusDropOverallLessThan(int num, Pageable pageable);
    public List<Book> findByStatusDropLastYearGreaterThan(int num, Pageable pageable);
    public List<Book> findByStatusDropLastYearLessThan(int num, Pageable pageable);
    public List<Book> findByStatusDropLastMonthGreaterThan(int num, Pageable pageable);
    public List<Book> findByStatusDropLastMonthLessThan(int num, Pageable pageable);
    public List<Book> findByStatusDropLastWeekGreaterThan(int num, Pageable pageable);
    public List<Book> findByStatusDropLastWeekLessThan(int num, Pageable pageable);

}
