package maksim.booksservice.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import maksim.booksservice.exceptions.BadRequestException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import maksim.booksservice.models.dtos.BookDto;
import maksim.booksservice.models.dtos.UpdateBookDto;
import maksim.booksservice.models.dtos.CreateBookDto;
import maksim.booksservice.services.BookService;
import maksim.booksservice.services.CachingService;
import maksim.booksservice.utils.Pagination;
import maksim.booksservice.utils.bookutils.BookSearchCriteria;
import maksim.booksservice.utils.enums.JoinMode;
import maksim.booksservice.utils.validators.*;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/api/v1/books")
@Validated
public class BookController {
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;
    private final FileValidator fileValidator;
    private final StringValidator stringValidator;
    private final CreateBookDtoValidator createBookDtoValidator;
    private final UpdateBookDtoValidator updateBookDtoValidator;
    private final BookSearchCriteriaValidator bookSearchCriteriaValidator;
    private final CachingService cachingService;

    @Autowired
    public BookController(
            BookService bookService,
            FileValidator fileValidator,
            StringValidator stringValidator,
            CreateBookDtoValidator createBookDtoValidator,
            UpdateBookDtoValidator updateBookDtoValidator,
            BookSearchCriteriaValidator bookSearchCriteriaValidator,
            CachingService cachingService
    ) {
        this.bookService = bookService;
        this.fileValidator = fileValidator;
        this.stringValidator = stringValidator;
        this.createBookDtoValidator = createBookDtoValidator;
        this.updateBookDtoValidator = updateBookDtoValidator;
        this.bookSearchCriteriaValidator = bookSearchCriteriaValidator;
        this.cachingService = cachingService;
    }

    @GetMapping
    public ResponseEntity<List<BookDto>> getAllBooks(@RequestParam Map<String, String> params, HttpServletRequest request) {
        /*
        * QUERY PARAMS:
        *
        * name - str
        * authorId - int
        * authorName - str
        *
        * issuedDate - "yyyy-MM-dd"
        * issuedDateOperator - newer \ older ; default "newer"
        *
        * rating - int
        * ratingOperator - greater \ less ; default "greater"
        *
        * genres - string like "genre1,genre2,genre3"
        *
        * joinModeForAuthor - with/without
        *
        * joinModeForStatuses - with/without
        * statusMinDate - "yyyy-MM-dd"
        * statusMaxDate - "yyyy-MM-dd"
        *
        * SORTING:
        *   sortField - default "rating"
        *   sortDirection - asc \ desc ; default "desc"
        *   pageNum - default 0
        *   pageSize - default 20
        * */

        logger.trace("BookController method entrance: getAllBooks");

        String url = request.getMethod() + request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if (queryString != null) {
            url += "?" + queryString;
        }

        if (cachingService.contains(url)) {
            return new ResponseEntity<>(cachingService.getFromCache(url), HttpStatus.OK);
        }

        Pageable pageable = Pagination.getPageable(params);
        BookSearchCriteria criteria = new BookSearchCriteria(params);

        bookSearchCriteriaValidator.screenStringValues(criteria);
        if (!bookSearchCriteriaValidator.isSafeFromSqlInjection(criteria)) {
            throw new BadRequestException("Unsecured input params");
        }

        List<BookDto> findBooks = bookService.getAllBooks(criteria, pageable);

        logger.trace("BookController method end | Return: selected items {}", findBooks.size());

        cachingService.addToCache(url, findBooks, 60000);

        return new ResponseEntity<>(findBooks, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(
            @PathVariable int id,
            @RequestParam(name = "joinMode", required = false, defaultValue = "without") String strJoinMode
    ) {
        logger.trace("BookController method entrance: getBookById | Params: id {}", id);

        JoinMode joinMode = (strJoinMode != null)
                ? JoinMode.fromValue(strJoinMode) : JoinMode.WITHOUT;

        BookDto book = bookService.getById(id, joinMode);

        logger.trace("BookController method end: getBookById | Found book");

        return new ResponseEntity<>(book, HttpStatus.OK);
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



    @PostMapping
    public ResponseEntity<BookDto> addBookMetaData(@Valid @RequestBody CreateBookDto bookData) {
        logger.trace("BookController method entrance: addBookMetaData");

        createBookDtoValidator.screenStringValue(bookData);
        if (!createBookDtoValidator.isSafeFromSqlInjection(bookData)) {
            throw new BadRequestException(
                String.format("Error: book data contains not valid chars. Invalid chars: %s", stringValidator.getDangerousPatterns())
            );
        }

        BookDto book = bookService.addBookMetaData(bookData);

        logger.trace("BookController method end: addBookMetaData | Book metadata was successfully added");

        return ResponseEntity.ok(book);
    }

    @PostMapping("/{id}/file")
    public ResponseEntity<String> addBookFile(@PathVariable int id, @RequestBody MultipartFile file) {
        logger.trace("BookController method entrance: addBookFile | Params: id {}", id);

        boolean isValid = fileValidator.isValid(file);

        if (!isValid) {
            throw new BadRequestException("Error: book file is not valid. File support extensions: pdf, txt, md. FIle should be less than 2mb");
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



    @PatchMapping("/{id}")
    public ResponseEntity<BookDto> updateBook(
            @PathVariable(name = "id") int bookId,
            @Valid @RequestBody UpdateBookDto bookData
    ) {
        logger.trace("BookController method entrance: updateBook");

        updateBookDtoValidator.screenStringValue(bookData);
        if (!updateBookDtoValidator.isSafeFromSqlInjection(bookData)) {
            throw new BadRequestException(
                String.format("Error: book data contains not valid chars. Invalid chars: %s", stringValidator.getDangerousPatterns())
            );
        }

        BookDto book = bookService.updateBook(bookId, bookData);

        logger.trace("BookController method end: updateBook | Book metadata was successfully updated");

        return ResponseEntity.ok(book);
    }


}
