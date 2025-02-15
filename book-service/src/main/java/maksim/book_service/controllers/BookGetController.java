package maksim.book_service.controllers;

import jakarta.ws.rs.BadRequestException;
import maksim.book_service.models.Book;
import maksim.book_service.services.BookService;
import maksim.book_service.utils.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/books/get")
public class BookGetController {
    private final static Logger logger = LoggerFactory.getLogger(BookGetController.class);

    @Autowired
    private BookService bookService;

    @Autowired
    private Pagination pagination;

    @GetMapping("/all")
    public ResponseEntity<?> getAllBooks(@RequestParam(required = false, defaultValue = "0") int pageNum,
                                         @RequestParam(required = false, defaultValue = "20") int itemsAmount,
                                         @RequestParam(required = false, defaultValue = "rating") String sort,
                                         @RequestParam(required = false, defaultValue = "desc") String sortDir,
                                         @RequestParam(required = false) String genres) {
        logger.trace("Find all: page num {}, items amount {}, sort {}, sort dir {}, genres {}",
                pageNum, itemsAmount, sort, sortDir, genres);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sort, sortDir);
        List<Book> findBooks;

        if(genres != null) {
            findBooks = bookService.findAllBooksWithFilters(genres, pageable);
        } else {
            findBooks = bookService.findAllBooks(pageable);
        }

        logger.trace("Find (all/all by genres) successfully: selected items {}", findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }

    @GetMapping("/byId/{id}")
    public ResponseEntity<?> getBookById(@PathVariable(required = true) int id) {
        logger.trace("Try to get book by id: {}", id);

        Optional<Book> book = bookService.findById(id);

        if(book.isPresent()) {
            logger.trace("Find book: {}", book);

            return new ResponseEntity<>(book, HttpStatus.OK);
        } else {
            logger.trace("Cannot find book with such id");

            return new ResponseEntity<>("Cannot find book with such id", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/byName/{name}")
    public ResponseEntity<?> getBooksByName(@PathVariable(required = true) String name,
                                            @RequestParam(required = false, defaultValue = "0") int pageNum,
                                            @RequestParam(required = false, defaultValue = "20") int itemsAmount,
                                            @RequestParam(required = false, defaultValue = "rating") String sort,
                                            @RequestParam(required = false, defaultValue = "desc") String sortDir) {
        logger.trace("Try to find books by name: {}", name);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sort, sortDir);

        List<Book> findBooks = bookService.findByName(name, pageable);

        logger.trace("Find (by name) successfully: selected items {}", findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }

    @GetMapping("/byAuthorId/{authorId}")
    public ResponseEntity<?> getBooksByAuthorId(@PathVariable(required = true) int authorId,
                                                @RequestParam(required = false, defaultValue = "0") int pageNum,
                                                @RequestParam(required = false, defaultValue = "20") int itemsAmount,
                                                @RequestParam(required = false, defaultValue = "rating") String sort,
                                                @RequestParam(required = false, defaultValue = "desc") String sortDir) {
        logger.trace("Try to find books by author id: {}", authorId);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sort, sortDir);

        List<Book> findBooks = bookService.findAllByAuthorId(authorId, pageable);

        logger.trace("Find (by author id) successfully: selected items {}", findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }

    @GetMapping("/byAuthorName/{authorName}")
    public ResponseEntity<?> getBooksByAuthorId(@PathVariable(required = true) String authorName,
                                                @RequestParam(required = false, defaultValue = "0") int pageNum,
                                                @RequestParam(required = false, defaultValue = "20") int itemsAmount,
                                                @RequestParam(required = false, defaultValue = "rating") String sort,
                                                @RequestParam(required = false, defaultValue = "desc") String sortDir) {
        logger.trace("Try to find books by author name: {}", authorName);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sort, sortDir);

        List<Book> findBooks = bookService.findAllByAuthorName(authorName, pageable);

        logger.trace("Find (by author name) successfully: selected items {}", findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }

    @GetMapping("/byRating/{rating}/{ratingDir}")
    public ResponseEntity<?> getByRating(@PathVariable(required = true) int rating,
                                         @PathVariable(required = true) String ratingDir,
                                         @RequestParam(required = false, defaultValue = "0") int pageNum,
                                         @RequestParam(required = false, defaultValue = "20") int itemsAmount,
                                         @RequestParam(required = false, defaultValue = "rating") String sort,
                                         @RequestParam(required = false, defaultValue = "desc") String sortDir) {
        logger.trace("Try to find books by rating: rating {}, dir {}, page num {}, items amount {}, sort {}, sort dir {}",
                rating, ratingDir, pageNum, itemsAmount, sort, sortDir);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sort, sortDir);

        List<Book> findBooks = bookService.findAllByRating(rating, ratingDir, pageable);

        logger.trace("Find (by rating {}, dir {}) successfully: selected items {}", rating, ratingDir, findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }

    @GetMapping("/byDate/{date}/{dateDir}")
    public ResponseEntity<?> getByDate(@PathVariable(required = true) Date date,
                                       @PathVariable(required = true) String dateDir,
                                       @RequestParam(required = false, defaultValue = "0") int pageNum,
                                       @RequestParam(required = false, defaultValue = "20") int itemsAmount,
                                       @RequestParam(required = false, defaultValue = "rating") String sort,
                                       @RequestParam(required = false, defaultValue = "desc") String sortDir) {
        logger.trace("Try to find books by date: date {}, dir {}, page num {}, items amount {}, sort {}, sort dir {}",
                date, dateDir, pageNum, itemsAmount, sort, sortDir);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sort, sortDir);

        List<Book> findBooks = bookService.findAllByDate(date, dateDir, pageable);

        logger.trace("Find (by date {}, dir {}) successfully: selected items {}", date, dateDir, findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }

    @GetMapping("/byStatuses/{status}/{scope}/{dir}/{num}")
    public ResponseEntity<?> getByStatuses(@PathVariable(required = true) String status,
                                           @PathVariable(required = true) String scope,
                                           @PathVariable(required = true) String dir,
                                           @PathVariable(required = true) int num,
                                           @RequestParam(required = false, defaultValue = "0") int pageNum,
                                           @RequestParam(required = false, defaultValue = "20") int itemsAmount,
                                           @RequestParam(required = false, defaultValue = "rating") String sort,
                                           @RequestParam(required = false, defaultValue = "desc") String sortDir) {
        logger.trace("Try to find books by statuses: status {}, scope {}, dir {}, num {}, page num {}, items amount {}, sort {}, sort dir {}",
                status, scope, dir, num, pageNum, itemsAmount, sort, sortDir);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sort, sortDir);

        List<Book> findBooks = switch (status) {
            case "reading" -> bookService.findByStatusReading(num, scope, dir, pageable);
            case "read" -> bookService.findByStatusRead(num, scope, dir, pageable);
            case "drop" -> bookService.findByStatusDrop(num, scope, dir, pageable);
            default -> throw new BadRequestException("Invalid value for status. Supported values: reading, read, drop");
        };

        logger.trace("Find (by status {}, scope {}, dir {}, num {}) successfully: selected items {}",
                status, scope, dir, num, findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }


}
