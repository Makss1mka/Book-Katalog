package maksim.book_service.services;

import jakarta.ws.rs.BadRequestException;
import maksim.book_service.models.Book;
import maksim.book_service.repositories.BookRepository;
import maksim.book_service.repositories.BookStatusesRepository;
import maksim.book_service.utils.Operator;
import maksim.book_service.utils.BookStatusScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final BookStatusesRepository bookStatusesRepository;

    private static final String errorOperatorMessage = "Incorrect value for mode. Support next values: greater, less";

    @Autowired
    public BookService(BookRepository bookRepository, BookStatusesRepository bookStatusesRepository) {
        this.bookRepository = bookRepository;
        this.bookStatusesRepository = bookStatusesRepository;
    }

    public List<Book> findAllBooks(Pageable pageable) {
        logger.trace("Try to get books without filters: {}", pageable);

        return bookRepository.findAll(pageable).toList();
    }

    public List<Book> findAllBooksWithFilters(String rawGenresFilter, Pageable pageable) {
        logger.trace("Try to get books with filters: {} ; genres {}", pageable, rawGenresFilter);

        List<String> genresFilter = Arrays.stream(rawGenresFilter.split(",")).toList();

        return bookRepository.findAllByGenres(genresFilter, pageable);
    }

    public List<Book> findAllByAuthorName(String authorName, Pageable pageable) {
        logger.trace("Try to get all books by author name: {} ; author name {}", pageable, authorName);

        return bookRepository.findByAuthorName(authorName, pageable);
    }

    public List<Book> findAllByAuthorId(int authorId, Pageable pageable) {
        logger.trace("Try to get all books by author id: {} ; author id {}", pageable, authorId);

        return bookRepository.findByAuthorId(authorId, pageable);
    }

    public List<Book> findAllByDate(Date date, Operator operator, Pageable pageable) {
        logger.trace("Try to get all books by date: {} ; date {} ; operator {}", pageable, date, operator);

        return switch (operator) {
            case Operator.GREATER -> bookRepository.findByIssuedDateGreaterThan(date, pageable);
            case Operator.LESS -> bookRepository.findByIssuedDateLessThan(date, pageable);
            default -> throw new BadRequestException("Incorrect mode for selecting by date. Acceptable modes: more, less");
        };
    }

    public List<Book> findAllByRating(int rating, Operator operator, Pageable pageable) {
        logger.trace("Try to get all books by rating: {} ; rating {} ; mode {}", pageable, rating, operator);

        return switch (operator) {
            case Operator.GREATER -> bookRepository.findByRatingGreaterThan(rating, pageable);
            case Operator.LESS -> bookRepository.findByRatingLessThan(rating, pageable);
            case Operator.EQUAL -> bookRepository.findByRating(rating, pageable);
        };
    }

    public List<Book> findByName(String name, Pageable pageable) {
        logger.trace("Try to get all books by rating: {} ; name {}", pageable, name);

        return bookRepository.findByName(name, pageable);
    }

    public Optional<Book> findById(int id) {
        logger.trace("Try to find book by id {}", id);

        return bookRepository.findById(id);
    }

    public List<Book> findByStatusReading(int value, Operator operator, BookStatusScope scope, Pageable pageable) {
        logger.trace("Try to get books by status reading: value {}, mode {}, scope {}", value, operator, scope);

        if(operator == Operator.GREATER) {

            return switch (scope) {
                case BookStatusScope.OVERALL -> bookStatusesRepository.findByStatusReadingOverallGreaterThan(value, pageable);
                case BookStatusScope.LAST_YEAR -> bookStatusesRepository.findByStatusReadingLastYearGreaterThan(value, pageable);
                case BookStatusScope.LAST_MONTH -> bookStatusesRepository.findByStatusReadingLastMonthGreaterThan(value, pageable);
                case BookStatusScope.LAST_WEEK -> bookStatusesRepository.findByStatusReadingLastWeekGreaterThan(value, pageable);
            };

        } else if(operator == Operator.LESS) {

            return switch (scope) {
                case BookStatusScope.OVERALL -> bookStatusesRepository.findByStatusReadingOverallLessThan(value, pageable);
                case BookStatusScope.LAST_YEAR -> bookStatusesRepository.findByStatusReadingLastYearLessThan(value, pageable);
                case BookStatusScope.LAST_MONTH -> bookStatusesRepository.findByStatusReadingLastMonthLessThan(value, pageable);
                case BookStatusScope.LAST_WEEK -> bookStatusesRepository.findByStatusReadingLastWeekLessThan(value, pageable);
            };

        } else {
            throw new BadRequestException(errorOperatorMessage);
        }

    }

    public List<Book> findByStatusRead(int value, Operator operator, BookStatusScope scope, Pageable pageable) {
        logger.trace("Try to get books by status read: value {}, mode {}, scope {}", value, operator, scope);

        if(operator == Operator.GREATER) {

            return switch (scope) {
                case BookStatusScope.OVERALL -> bookStatusesRepository.findByStatusReadOverallGreaterThan(value, pageable);
                case BookStatusScope.LAST_YEAR -> bookStatusesRepository.findByStatusReadLastYearGreaterThan(value, pageable);
                case BookStatusScope.LAST_MONTH -> bookStatusesRepository.findByStatusReadLastMonthGreaterThan(value, pageable);
                case BookStatusScope.LAST_WEEK -> bookStatusesRepository.findByStatusReadLastWeekGreaterThan(value, pageable);
            };

        } else if(operator == Operator.LESS) {

            return switch (scope) {
                case BookStatusScope.OVERALL -> bookStatusesRepository.findByStatusReadOverallLessThan(value, pageable);
                case BookStatusScope.LAST_YEAR -> bookStatusesRepository.findByStatusReadLastYearLessThan(value, pageable);
                case BookStatusScope.LAST_MONTH -> bookStatusesRepository.findByStatusReadLastMonthLessThan(value, pageable);
                case BookStatusScope.LAST_WEEK -> bookStatusesRepository.findByStatusReadLastWeekLessThan(value, pageable);
            };

        } else {
            throw new BadRequestException(errorOperatorMessage);
        }

    }

    public List<Book> findByStatusDrop(int value, Operator operator, BookStatusScope scope, Pageable pageable) {
        logger.trace("Try to get books by status drop: value {}, mode {}, scope {}", value, operator, scope);

        if(operator == Operator.GREATER) {

            return switch (scope) {
                case BookStatusScope.OVERALL -> bookStatusesRepository.findByStatusDropOverallGreaterThan(value, pageable);
                case BookStatusScope.LAST_YEAR -> bookStatusesRepository.findByStatusDropLastYearGreaterThan(value, pageable);
                case BookStatusScope.LAST_MONTH -> bookStatusesRepository.findByStatusDropLastMonthGreaterThan(value, pageable);
                case BookStatusScope.LAST_WEEK -> bookStatusesRepository.findByStatusDropLastWeekGreaterThan(value, pageable);
            };

        } else if(operator == Operator.LESS) {

            return switch (scope) {
                case BookStatusScope.OVERALL -> bookStatusesRepository.findByStatusDropOverallLessThan(value, pageable);
                case BookStatusScope.LAST_YEAR -> bookStatusesRepository.findByStatusDropLastYearLessThan(value, pageable);
                case BookStatusScope.LAST_MONTH -> bookStatusesRepository.findByStatusDropLastMonthLessThan(value, pageable);
                case BookStatusScope.LAST_WEEK -> bookStatusesRepository.findByStatusDropLastWeekLessThan(value, pageable);
            };

        } else {
            throw new BadRequestException(errorOperatorMessage);
        }

    }

}
