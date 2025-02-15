package maksim.book_service.services;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import maksim.book_service.models.Book;
import maksim.book_service.repositories.BookRepository;
import maksim.book_service.repositories.BookStatusesRepository;
import maksim.book_service.utils.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {
    private final static Logger logger = LoggerFactory.getLogger(BookService.class);

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookStatusesRepository bookStatusesRepository;

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

    public List<Book> findAllByDate(Date date, String mode, Pageable pageable) {
        logger.trace("Try to get all books by date: {} ; date {} ; mode {}", pageable, date, mode);

        return switch (mode) {
            case "greater" -> bookRepository.findByIssuedDateGreaterThan(date, pageable);
            case "less" -> bookRepository.findByIssuedDateLessThan(date, pageable);
            default -> throw new BadRequestException("Incorrect mode for selecting by date. Acceptable modes: more, less");
        };
    }

    public List<Book> findAllByRating(int rating, String mode, Pageable pageable) {
        logger.trace("Try to get all books by rating: {} ; rating {} ; mode {}", pageable, rating, mode);

        return switch (mode) {
            case "greater" -> bookRepository.findByRatingGreaterThan(rating, pageable);
            case "less" -> bookRepository.findByRatingLessThan(rating, pageable);
            case "exec" -> bookRepository.findByRating(rating, pageable);
            default -> throw new BadRequestException("Incorrect mode for selecting by rating. Acceptable modes: more, less, exec");
        };
    }

    @Transactional
    public List<Book> findByName(String name, Pageable pageable) {
        logger.trace("Try to get all books by rating: {} ; name {}", pageable, name);

        List<Book> books = bookRepository.findByName(name, pageable);

        return books;
    }

    public Optional<Book> findById(int id) {
        logger.trace("Try to find book by id {}", id);

        return bookRepository.findById(id);
    }

    public List<Book> findByStatusReading(int num, String mode, String scope, Pageable pageable) {
        logger.trace("Try to get books by status reading: num {}, mode {}, scope {}", num, mode, scope);

        if(mode.equals("greater")) {

            return switch (scope) {
                case "overall" -> bookStatusesRepository.findByStatusReadingOverallGreaterThan(num, pageable);
                case "year" -> bookStatusesRepository.findByStatusReadingLastYearGreaterThan(num, pageable);
                case "month" -> bookStatusesRepository.findByStatusReadingLastMonthGreaterThan(num, pageable);
                case "week" -> bookStatusesRepository.findByStatusReadingLastWeekGreaterThan(num, pageable);
                default -> throw new BadRequestException("Incorrect value for scope. Support next values: overall, year, month, week");
            };

        } else if(mode.equals("less")) {

            return switch (scope) {
                case "overall" -> bookStatusesRepository.findByStatusReadingOverallLessThan(num, pageable);
                case "year" -> bookStatusesRepository.findByStatusReadingLastYearLessThan(num, pageable);
                case "month" -> bookStatusesRepository.findByStatusReadingLastMonthLessThan(num, pageable);
                case "week" -> bookStatusesRepository.findByStatusReadingLastWeekLessThan(num, pageable);
                default -> throw new BadRequestException("Incorrect value for scope. Support next values: overall, year, month, week");
            };

        } else {
            throw new BadRequestException("Incorrect value for mode. Support next values: greater, less");
        }

    }

    public List<Book> findByStatusRead(int num, String mode, String scope, Pageable pageable) {
        logger.trace("Try to get books by status read: num {}, mode {}, scope {}", num, mode, scope);

        if(mode.equals("greater")) {

            return switch (scope) {
                case "overall" -> bookStatusesRepository.findByStatusReadOverallGreaterThan(num, pageable);
                case "year" -> bookStatusesRepository.findByStatusReadLastYearGreaterThan(num, pageable);
                case "month" -> bookStatusesRepository.findByStatusReadLastMonthGreaterThan(num, pageable);
                case "week" -> bookStatusesRepository.findByStatusReadLastWeekGreaterThan(num, pageable);
                default -> throw new BadRequestException("Incorrect value for scope. Support next values: overall, year, month, week");
            };

        } else if(mode.equals("less")) {

            return switch (scope) {
                case "overall" -> bookStatusesRepository.findByStatusReadOverallLessThan(num, pageable);
                case "year" -> bookStatusesRepository.findByStatusReadLastYearLessThan(num, pageable);
                case "month" -> bookStatusesRepository.findByStatusReadLastMonthLessThan(num, pageable);
                case "week" -> bookStatusesRepository.findByStatusReadLastWeekLessThan(num, pageable);
                default -> throw new BadRequestException("Incorrect value for scope. Support next values: overall, year, month, week");
            };

        } else {
            throw new BadRequestException("Incorrect value for mode. Support next values: greater, less");
        }

    }

    public List<Book> findByStatusDrop(int num, String mode, String scope, Pageable pageable) {
        logger.trace("Try to get books by status drop: num {}, mode {}, scope {}", num, mode, scope);

        if(mode.equals("greater")) {

            return switch (scope) {
                case "overall" -> bookStatusesRepository.findByStatusDropOverallGreaterThan(num, pageable);
                case "year" -> bookStatusesRepository.findByStatusDropLastYearGreaterThan(num, pageable);
                case "month" -> bookStatusesRepository.findByStatusDropLastMonthGreaterThan(num, pageable);
                case "week" -> bookStatusesRepository.findByStatusDropLastWeekGreaterThan(num, pageable);
                default -> throw new BadRequestException("Incorrect value for scope. Support next values: overall, year, month, week");
            };

        } else if(mode.equals("less")) {

            return switch (scope) {
                case "overall" -> bookStatusesRepository.findByStatusDropOverallLessThan(num, pageable);
                case "year" -> bookStatusesRepository.findByStatusDropLastYearLessThan(num, pageable);
                case "month" -> bookStatusesRepository.findByStatusDropLastMonthLessThan(num, pageable);
                case "week" -> bookStatusesRepository.findByStatusDropLastWeekLessThan(num, pageable);
                default -> throw new BadRequestException("Incorrect value for scope. Support next values: overall, year, month, week");
            };

        } else {
            throw new BadRequestException("Incorrect value for mode. Support next values: greater, less");
        }

    }

}
