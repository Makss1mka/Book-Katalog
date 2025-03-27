package maksim.booksservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
        summary = "Get all books by filters",
        description = "Get all books by filters, that are specified in query params"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Book updated",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(implementation = BookDto.class)
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Book bad request (validation failed)",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Some fields contains invalid chars")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Book not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Book not found")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Server error",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Something goes wrong, Sorry my bad :(")
            )
        )
    })
    public ResponseEntity<List<BookDto>> getAllBooks(
        @Parameter(
            description = """
                Get all books by filters
                Input filtering params:
                   - name (type: String) - book name
                   - author id (type: int) - author id
                   - author name (type: String) - author name
                   - issuedDate (type: String in format 'yyyy-MM-dd') - date were book was published on app
                   - issuedDateOperation - (type: String (values: newer/older)) - in which direction will book was sorted by date field
                   - rating (type: int)
                   - ratingOperator (type: String (values: greater/less)) - in which direction will book was sorted by rating field
                   - genres (type: String in format "genre1,genre2,genre3") - book genres
                   - joinModeForAuthor (type: String (values: with/without)) - will author data included
                   - joinModeForStatus (type: String (values: with/without)) - will statuses data include
                   - statusMinDate (type: String in format 'yyyy-MM-dd') - min date edge from which statuses will be counted
                   - statusMaxDate (type: String in format 'yyyy-MM-dd') - max date edge till which statuses will be counted
                   - sortField (type: String (values: rating/name/ratingsCount/issuedDate)) - sorting field
                   - sortDirection (type: String (values: asc/desc)) - sorting direction
                   - pageNum (type: int) - page number
                   - pageSize (type: int) - how many books should be on one page
                """,
            required = false
        )
        @RequestParam(required = false)
        Map<String, String> params, HttpServletRequest request
    ) {
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
    @Operation(
        summary = "Get book by it id",
        description = "Get book by it id, which is specified as path variable"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Get book by id",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BookDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Book bad request (validation failed)",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Book id is invalid")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Server error",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Something goes wrong, Sorry my bad :(")
            )
        )
    })
    public ResponseEntity<BookDto> getBookById(
        @Parameter(description = "book id", required = true)
        @PathVariable int id,

        @Parameter(
            description = "(type: String , values: with/without) Indicates whether the book should be linked to the author object",
            required = false
        )
        @RequestParam(name = "joinMode", required = false, defaultValue = "without")
        String strJoinMode
    ) {
        logger.trace("BookController method entrance: getBookById | Params: id {}", id);

        JoinMode joinMode = (strJoinMode != null)
                ? JoinMode.fromValue(strJoinMode) : JoinMode.WITHOUT;

        BookDto book = bookService.getById(id, joinMode);

        logger.trace("BookController method end: getBookById | Found book");

        return new ResponseEntity<>(book, HttpStatus.OK);
    }



    @GetMapping("/{id}/file")
    @Operation(
        summary = "Get book file by book id",
        description = "Get book file by book id"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Get book file",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(example = "Some .txt book")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Book bad request (validation failed)",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Book id is invalid")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Book not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Book not found")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Server error",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Something goes wrong, Sorry my bad :(")
            )
        )
    })
    public ResponseEntity<Resource> getBookFile(
        @Parameter(description = "book id", required = true)
        @PathVariable int id
    ) {
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
    @Operation(
        summary = "Create book",
        description = "Add book metadata to db"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Book metaData adding",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BookDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Book bad request (validation failed)",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Some fields contains invalid chars")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Book not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Book not found")
            )
        ),
        @ApiResponse(
            responseCode = "407",
            description = "Book data contains conflicted data (when it is ruins db uniques)",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Book data contains conflicted data")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Server error",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Something goes wrong, Sorry my bad :(")
            )
        )
    })
    public ResponseEntity<BookDto> addBookMetaData(
        @Parameter(
            description = "Data for book creating",
            required = true
        )
        @Valid @RequestBody CreateBookDto bookData
    ) {
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
    @Operation(
        summary = "Add file to the book by it id",
        description = "Add file to the book by it id"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Book file adding",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(example = "File was added")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Book bad request (validation failed)",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Invalid file type/format")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Book not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Book not found")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Server error",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Something goes wrong, Sorry my bad :(")
            )
        )
    })
    public ResponseEntity<String> addBookFile(
        @Parameter(description = "book id", required = true)
        @PathVariable int id,

        @Parameter(
            description = "Book file, should be in next formats: .txt , .pdf , .md",
            required = true
        )
        @RequestBody MultipartFile file
    ) {
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
    @Operation(
        summary = "Delete book data by it id",
        description = "Delete book data (db data and file) by it id"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Book deletion",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(example = "Book was deleted")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Book bad request (validation failed)",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Invalid id value")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Book not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Book not found")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Server error",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Something goes wrong, Sorry my bad :(")
            )
        )
    })
    public ResponseEntity<String> deleteBook(
        @Parameter(description = "book id", required = true)
        @PathVariable int id
    ) {
    logger.trace("BookController method entrance: deleteBook | Params: book id {}", id);

    bookService.deleteBook(id);

    logger.trace("BookController method end: deleteBook | Book has successfully deleted");

    return ResponseEntity.ok("Book was successfully deleted");
}



    @PatchMapping("/{id}")
    @Operation(
        summary = "Update books data",
        description = "Update some book fields"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Book updated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BookDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Book bad request (validation failed)",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "SomeFields contains invalid chars")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Book not found",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Book not found")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Server error",
            content = @Content(
                mediaType = "plain/text",
                schema = @Schema(example = "Something goes wrong, Sorry my bad :(")
            )
        )
    })
    public ResponseEntity<BookDto> updateBook(
            @Parameter(description = "book id", required = true)
            @PathVariable(name = "id") int bookId,

            @Parameter(
                description = "New book data",
                required = true
            )
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
