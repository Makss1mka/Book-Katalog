package maksim.bookservice.controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import maksim.bookservice.models.Book;
import maksim.bookservice.services.BookService;
import maksim.bookservice.utils.BookStatus;
import maksim.bookservice.utils.BookStatusScope;
import maksim.bookservice.utils.Operator;
import maksim.bookservice.utils.Pagination;
import maksim.bookservice.utils.SortDirection;
import maksim.bookservice.utils.SortField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/books/get")
public class BookController {
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;
    private final Pagination pagination;

    @Autowired
    public BookController(BookService bookService, Pagination pagination) {
        this.bookService = bookService;
        this.pagination = pagination;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Book>> getAllBooks(@RequestParam(required = false) String genres,
                                                  @RequestParam(required = false, defaultValue = "0") int pageNum,
                                                  @RequestParam(required = false, defaultValue = "20") int itemsAmount,
                                                  @RequestParam(required = false, defaultValue = "rating") String sortStrField,
                                                  @RequestParam(required = false, defaultValue = "desc") String sortStrDirection) {
        logger.trace("Try to get all books with/without genres");

        SortField sortField = SortField.fromValue(sortStrField);

        SortDirection sortDirection = SortDirection.fromValue(sortStrDirection);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sortField, sortDirection);

        List<Book> findBooks;

        if (genres != null) {
            findBooks = bookService.findAllBooksWithFilters(genres, pageable);
        } else {
            findBooks = bookService.findAllBooks(pageable);
        }

        logger.trace("Find (all/all by genres) successfully: selected items {}", findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }

    @GetMapping("/byId/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable int id) {
        logger.trace("Try to get book by id");

        Optional<Book> book = bookService.findById(id);

        if (book.isPresent()) {
            logger.trace("Find book: {}", book);

            return new ResponseEntity<>(book.get(), HttpStatus.OK);
        } else {
            logger.trace("Cannot find book with such id");

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/byName/{name}")
    public ResponseEntity<List<Book>> getBooksByName(@PathVariable String name,
                                                     @RequestParam(required = false, defaultValue = "0") int pageNum,
                                                     @RequestParam(required = false, defaultValue = "20") int itemsAmount,
                                                     @RequestParam(required = false, defaultValue = "rating") String sortStrField,
                                                     @RequestParam(required = false, defaultValue = "desc") String sortStrDirection) {
        logger.trace("Try to get books by name");

        SortField sortField = SortField.fromValue(sortStrField);
        SortDirection sortDirection = SortDirection.fromValue(sortStrDirection);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sortField, sortDirection);

        List<Book> findBooks = bookService.findByName(name, pageable);

        logger.trace("Find (by name) successfully: selected items {}", findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }

    @GetMapping("/byAuthorId/{authorId}")
    public ResponseEntity<List<Book>> getBooksByAuthorId(@PathVariable int authorId,
                                                         @RequestParam(required = false, defaultValue = "0") int pageNum,
                                                         @RequestParam(required = false, defaultValue = "20") int itemsAmount,
                                                         @RequestParam(required = false, defaultValue = "rating") String sortStrField,
                                                         @RequestParam(required = false, defaultValue = "desc") String sortStrDirection) {
        logger.trace("Try to find books by author id");

        SortField sortField = SortField.fromValue(sortStrField);
        SortDirection sortDirection = SortDirection.fromValue(sortStrDirection);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sortField, sortDirection);

        List<Book> findBooks = bookService.findAllByAuthorId(authorId, pageable);

        logger.trace("Find (by author id) successfully: selected items {}", findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }

    @GetMapping("/byAuthorName/{authorName}")
    public ResponseEntity<List<Book>> getBooksByAuthorId(@PathVariable String authorName,
                                                         @RequestParam(required = false, defaultValue = "0") int pageNum,
                                                         @RequestParam(required = false, defaultValue = "20") int itemsAmount,
                                                         @RequestParam(required = false, defaultValue = "rating") String sortStrField,
                                                         @RequestParam(required = false, defaultValue = "desc") String sortStrDirection) {
        logger.trace("Try to find books by author name");

        SortField sortField = SortField.fromValue(sortStrField);
        SortDirection sortDirection = SortDirection.fromValue(sortStrDirection);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sortField, sortDirection);

        List<Book> findBooks = bookService.findAllByAuthorName(authorName, pageable);

        logger.trace("Find (by author name) successfully: selected items {}", findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }

    @GetMapping("/byRating/{rating}/{strOperator}")
    public ResponseEntity<List<Book>> getByRating(@PathVariable int rating,
                                                  @PathVariable String strOperator,
                                                  @RequestParam(required = false, defaultValue = "0") int pageNum,
                                                  @RequestParam(required = false, defaultValue = "20") int itemsAmount,
                                                  @RequestParam(required = false, defaultValue = "rating") String sortStrField,
                                                  @RequestParam(required = false, defaultValue = "desc") String sortStrDirection) {
        logger.trace("Try to find books by rating");

        SortField sortField = SortField.fromValue(sortStrField);
        SortDirection sortDirection = SortDirection.fromValue(sortStrDirection);
        Operator operator = Operator.fromValue(strOperator);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sortField, sortDirection);

        List<Book> findBooks = bookService.findAllByRating(rating, operator, pageable);

        logger.trace("Find by rating successfully: selected items {}", findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }

    @GetMapping("/byDate/{strDate}/{strOperator}")
    public ResponseEntity<List<Book>> getByDate(@PathVariable String strDate,
                                                @PathVariable String strOperator,
                                                @RequestParam(required = false, defaultValue = "0") int pageNum,
                                                @RequestParam(required = false, defaultValue = "20") int itemsAmount,
                                                @RequestParam(required = false, defaultValue = "rating") String sortStrField,
                                                @RequestParam(required = false, defaultValue = "desc") String sortStrDirection) {
        logger.trace("Try to find books by date");

        SortField sortField = SortField.fromValue(sortStrField);
        SortDirection sortDirection = SortDirection.fromValue(sortStrDirection);
        Operator operator = Operator.fromValue(strOperator);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sortField, sortDirection);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = formatter.parse(strDate);
        } catch (ParseException e) {
            logger.trace("Cannot format date");

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<Book> findBooks = bookService.findAllByDate(date, operator, pageable);

        logger.trace("Find by date successfully: selected items {}", findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }

    @GetMapping("/byStatuses/{strStatus}/{strScope}/{strOperator}/{value}")
    public ResponseEntity<List<Book>> getByStatuses(@PathVariable String strStatus,
                                                    @PathVariable String strScope,
                                                    @PathVariable String strOperator,
                                                    @PathVariable int value,
                                                    @RequestParam(required = false, defaultValue = "0") int pageNum,
                                                    @RequestParam(required = false, defaultValue = "20") int itemsAmount,
                                                    @RequestParam(required = false, defaultValue = "rating") String sortStrField,
                                                    @RequestParam(required = false, defaultValue = "desc") String sortStrDirection) {
        logger.trace("Try to find books by status");

        SortField sortField = SortField.fromValue(sortStrField);
        SortDirection sortDirection = SortDirection.fromValue(sortStrDirection);
        Operator operator = Operator.fromValue(strOperator);
        BookStatusScope scope = BookStatusScope.fromValue(strScope);
        BookStatus bookStatus = BookStatus.fromValue(strStatus);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sortField, sortDirection);

        List<Book> findBooks = switch (bookStatus) {
            case BookStatus.READING -> bookService.findByStatusReading(value, operator, scope, pageable);
            case BookStatus.READ -> bookService.findByStatusRead(value, operator, scope,  pageable);
            case BookStatus.DROP -> bookService.findByStatusDrop(value, operator, scope, pageable);
        };

        logger.trace("Find by status successfully: selected items {}", findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }


}
