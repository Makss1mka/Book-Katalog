package maksim.booksservice.controllers;

import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.ws.rs.NotFoundException;
import maksim.booksservice.models.Book;
import maksim.booksservice.models.BookDtoForCreating;
import maksim.booksservice.services.BookService;
import maksim.booksservice.utils.Pagination;
import maksim.booksservice.utils.bookutils.BookSearchCriteria;
import maksim.booksservice.utils.enums.*;
import maksim.booksservice.utils.validators.BookDtoForCreatingValidators;
import maksim.booksservice.utils.validators.BookSearchCriteriaValidators;
import maksim.booksservice.utils.validators.FileValidators;
import maksim.booksservice.utils.validators.StringValidators;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final FileValidators fileValidators;
    private final StringValidators stringValidators;
    private final BookDtoForCreatingValidators bookDtoForCreatingValidators;
    private final BookSearchCriteriaValidators bookSearchCriteriaValidators;

    @Autowired
    public BookController(
            BookService bookService,
            FileValidators fileValidator,
            StringValidators stringValidators,
            BookDtoForCreatingValidators bookDtoForCreatingValidators,
            BookSearchCriteriaValidators bookSearchCriteriaValidators
    ) {
        this.bookService = bookService;
        this.fileValidators = fileValidator;
        this.stringValidators = stringValidators;
        this.bookDtoForCreatingValidators = bookDtoForCreatingValidators;
        this.bookSearchCriteriaValidators = bookSearchCriteriaValidators;
    }

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks(@RequestParam Map<String, String> params) {
        /*
        * QUERY PARAMS:
        *
        * name - str
        * authorId - int
        * authorName - str
        *
        * issuedDate - str
        * issuedDateOperator - newer \ older ; default "newer"
        *
        * rating - int
        * ratingOperator - greater \ less ; default "greater"
        *
        * genres - string like "genre1,genre2,genre3"
        *
        * status - read \ reading \ drop ; default "read"
        * statusCount - int
        * statusScope - overall \ last_year \ last_month \ last_week ; default "overall"
        * statusOperator - greater \ less ; default "greater"
        *
        * SORTING:
        *   sortField - default "rating"
        *   sortDirection - asc \ desc ; default "desc"
        *   pageNum - default 0
        *   pageSize - default 20
        * */

        logger.trace("BookController method entrance: getAllBooks");

        Pageable pageable = Pagination.getPageable(params);
        BookSearchCriteria criteria = new BookSearchCriteria(params);

        bookSearchCriteriaValidators.screenStringValues(criteria);
        if (!bookSearchCriteriaValidators.isSafeFromSqlInjection(criteria)) {
            throw new BadRequestException("Unsecured input params");
        }

        List<Book> findBooks = bookService.getAllBooks(criteria, pageable);

        logger.trace("BookController method end | Return: selected items {}", findBooks.size());

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(
            @PathVariable int id,
            @RequestParam(name = "joinMode", required = false, defaultValue = "without") String strJoinMode
    ) {
        logger.trace("BookController method entrance: getBookById | Params: id {}", id);

        JoinMode joinMode = (strJoinMode != null) ?
                JoinMode.fromValue(strJoinMode) : JoinMode.WITHOUT_JOIN;

        Optional<Book> book = bookService.getById(id, joinMode);

        if (book.isPresent()) {
            if (joinMode == JoinMode.WITH_JOIN) {
                book.get().setBookAuthor(book.get().getAuthor());
            }

            logger.trace("BookController method end: getBookById | Found book");

            return new ResponseEntity<>(book.get(), HttpStatus.OK);
        } else {
            logger.trace("BookController method end: getBookById | Book not found");

            throw new NotFoundException("Cannot find such book");
        }
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getBookFile(@PathVariable int id) {
        logger.trace("BookController method entrance: getFile | Params: book id {}", id);

        File file = bookService.getFile(id);

        HttpHeaders headers = new HttpHeaders();

        try {
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, Files.probeContentType(Paths.get(file.getPath())));
        } catch (IOException e) {
            logger.trace("Cannot get file path");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        logger.trace("BookController method end: getFile | File has successfully found");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .body(new FileSystemResource(file));
    }

    @PostMapping("/metaData")
    public ResponseEntity<Book> addBookMetaData(@Valid @RequestBody BookDtoForCreating bookData) {
        logger.trace("BookController method entrance: addBookMetaData");

        bookDtoForCreatingValidators.screenStringValue(bookData);

        if (!bookDtoForCreatingValidators.isSafeFromSqlInjection(bookData)) {
            throw new BadRequestException(
                    String.format("Error: book data contains not valid chars. Invalid chars: %s", stringValidators.getDangerousPatterns())
            );
        }

        Book book = bookService.addBookMetaData(bookData);

        logger.trace("BookController method end: addBookMetaData | Book metadata was successfully added");

        return ResponseEntity.ok(book);
    }

    @PostMapping("/{id}/file")
    public ResponseEntity<String> addBookFile(@PathVariable int id, @RequestBody MultipartFile file) {
        logger.trace("BookController method entrance: addBookFile | Params: id {}", id);

        boolean isValid = fileValidators.isValid(file);

        if (!isValid) {
            throw new BadRequestException(
                    "Error: book file is not valid. File support extensions: pdf, txt, md. FIle should be less than 2mb"
            );
        }

        bookService.addBookFile(file, id);

        logger.trace("BookController method entrance: addBookFile |Book file was successfully added");

        return ResponseEntity.ok("Book file was successfully added");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable int id) {
        logger.trace("BookController method entrance: deleteBook | Params: book id {}", id);

        bookService.deleteBook(id);

        logger.trace("BookController method end: deleteBook | Book has successfully deleted");

        return ResponseEntity.ok("Book was successfully deleted");
    }



}
