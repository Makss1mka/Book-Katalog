package maksim.bookservice.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.BadRequestException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import maksim.bookservice.models.Book;
import maksim.bookservice.models.BookDtoForCreating;
import maksim.bookservice.services.BookService;
import maksim.bookservice.utils.Pagination;
import maksim.bookservice.utils.enums.BookStatus;
import maksim.bookservice.utils.enums.BookStatusScope;
import maksim.bookservice.utils.enums.Operator;
import maksim.bookservice.utils.enums.SortDirection;
import maksim.bookservice.utils.enums.SortField;
import maksim.bookservice.utils.validators.BookDtoForCreatingValidators;
import maksim.bookservice.utils.validators.FileValidators;
import maksim.bookservice.utils.validators.StringValidators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping(value = "/books")
@Validated
public class BookController {
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;
    private final Pagination pagination;
    private final FileValidators fileValidators;
    private final StringValidators stringValidators;
    private final BookDtoForCreatingValidators bookDtoForCreatingValidators;

    @Autowired
    public BookController(
            BookService bookService,
            Pagination pagination,
            FileValidators fileValidator,
            StringValidators stringValidators,
            BookDtoForCreatingValidators bookDtoForCreatingValidators
    ) {
        this.bookService = bookService;
        this.pagination = pagination;
        this.fileValidators = fileValidator;
        this.stringValidators = stringValidators;
        this.bookDtoForCreatingValidators = bookDtoForCreatingValidators;
    }

    @GetMapping("/get/all")
    public ResponseEntity<List<Book>> getAllBooks(
            @RequestParam(required = false) String genres,
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "20") int itemsAmount,
            @RequestParam(required = false, defaultValue = "rating") String sortStrField,
            @RequestParam(required = false, defaultValue = "desc") String sortStrDirection
    ) {
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

    @GetMapping("/get/byId/{id}")
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

    @GetMapping("/get/byName/{name}")
    public ResponseEntity<List<Book>> getBooksByName(
            @NotBlank @Min(3) @Max(50) @PathVariable String name,
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "20") int itemsAmount,
            @RequestParam(required = false, defaultValue = "rating") String sortStrField,
            @RequestParam(required = false, defaultValue = "desc") String sortStrDirection
    ) {
        logger.trace("Try to get books by name");

        SortField sortField = SortField.fromValue(sortStrField);
        SortDirection sortDirection = SortDirection.fromValue(sortStrDirection);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sortField, sortDirection);

        List<Book> findBooks = bookService.findByName(name, pageable);

        logger.trace("Find (by name) successfully: selected items {}", findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }

    @GetMapping("/get/byAuthorId/{authorId}")
    public ResponseEntity<List<Book>> getBooksByAuthorId(
            @PathVariable int authorId,
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "20") int itemsAmount,
            @RequestParam(required = false, defaultValue = "rating") String sortStrField,
            @RequestParam(required = false, defaultValue = "desc") String sortStrDirection
    ) {
        logger.trace("Try to find books by author id");

        SortField sortField = SortField.fromValue(sortStrField);
        SortDirection sortDirection = SortDirection.fromValue(sortStrDirection);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sortField, sortDirection);

        List<Book> findBooks = bookService.findAllByAuthorId(authorId, pageable);

        logger.trace("Find (by author id) successfully: selected items {}", findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }

    @GetMapping("/get/byAuthorName/{authorName}")
    public ResponseEntity<List<Book>> getBooksByAuthorId(
            @PathVariable String authorName,
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "20") int itemsAmount,
            @RequestParam(required = false, defaultValue = "rating") String sortStrField,
            @RequestParam(required = false, defaultValue = "desc") String sortStrDirection
    ) {
        logger.trace("Try to find books by author name");

        SortField sortField = SortField.fromValue(sortStrField);
        SortDirection sortDirection = SortDirection.fromValue(sortStrDirection);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sortField, sortDirection);

        List<Book> findBooks = bookService.findAllByAuthorName(authorName, pageable);

        logger.trace("Find (by author name) successfully: selected items {}", findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }

    @GetMapping("/get/byRating/{rating}/{strOperator}")
    public ResponseEntity<List<Book>> getByRating(
            @PathVariable int rating,
            @NotBlank @Min(1) @Max(8) @PathVariable String strOperator,
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "20") int itemsAmount,
            @RequestParam(required = false, defaultValue = "rating") String sortStrField,
            @RequestParam(required = false, defaultValue = "desc") String sortStrDirection
    ) {
        logger.trace("Try to find books by rating");

        SortField sortField = SortField.fromValue(sortStrField);
        SortDirection sortDirection = SortDirection.fromValue(sortStrDirection);
        Operator operator = Operator.fromValue(strOperator);

        Pageable pageable = pagination.getPageable(pageNum, itemsAmount, sortField, sortDirection);

        List<Book> findBooks = bookService.findAllByRating(rating, operator, pageable);

        logger.trace("Find by rating successfully: selected items {}", findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }

    @GetMapping("/get/byDate/{strDate}/{strOperator}")
    public ResponseEntity<List<Book>> getByDate(
            @PathVariable String strDate,
            @PathVariable String strOperator,
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "20") int itemsAmount,
            @RequestParam(required = false, defaultValue = "rating") String sortStrField,
            @RequestParam(required = false, defaultValue = "desc") String sortStrDirection
    ) {
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

    @GetMapping("/get/byStatuses/{strStatus}/{strScope}/{strOperator}/{value}")
    public ResponseEntity<List<Book>> getByStatuses(
            @PathVariable String strStatus,
            @PathVariable String strScope,
            @PathVariable String strOperator,
            @PathVariable int value,
            @RequestParam(required = false, defaultValue = "0") int pageNum,
            @RequestParam(required = false, defaultValue = "20") int itemsAmount,
            @RequestParam(required = false, defaultValue = "rating") String sortStrField,
            @RequestParam(required = false, defaultValue = "desc") String sortStrDirection
    ) {
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

    @GetMapping("/get/book/file/{bookId}")
    public ResponseEntity<Resource> getBookFile(@PathVariable int bookId) {
        logger.trace("Try to get book file");

        File file = bookService.getFile(bookId);

        HttpHeaders headers = new HttpHeaders();

        try {
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, Files.probeContentType(Paths.get(file.getPath())));
        } catch (IOException e) {
            logger.trace("Cannot get file path");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        logger.trace("File has successfully found");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .body(new FileSystemResource(file));
    }

    @PostMapping("/add/book/metaData")
    public ResponseEntity<String> addBookMetaData(@Valid @RequestBody BookDtoForCreating bookData) {
        logger.trace("Try to add new book meta data");

        bookDtoForCreatingValidators.screenStringValue(bookData);
        if (!bookDtoForCreatingValidators.isSafeFromSqlInjection(bookData)) {
            throw new BadRequestException(
                    String.format("Error: book data contains not valid chars. Invalid chars: %s", stringValidators.getDangerousPatterns())
            );
        }

        bookService.addBookMetaData(bookData);

        logger.trace("Book metadata was successfully added");

        return ResponseEntity.ok("Book metadata was successfully added");
    }

    @PostMapping("/add/book/file/{bookId}")
    public ResponseEntity<String> addBookFile(@PathVariable int bookId, @RequestBody MultipartFile file) {
        logger.trace("Try to add book file");

        boolean isValid = fileValidators.isValid(file);

        if (!isValid) {
            throw new BadRequestException(
                    "Error: book file is not valid. File support extensions: pdf, txt, md. FIle should be less than 2mb"
            );
        }

        bookService.addBookFile(file, bookId);

        logger.trace("Book file was successfully added");

        return ResponseEntity.ok("Book file was successfully added");
    }


}
