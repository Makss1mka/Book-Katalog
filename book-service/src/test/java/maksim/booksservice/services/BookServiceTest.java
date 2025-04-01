package maksim.booksservice.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import maksim.booksservice.config.AppConfig;
import maksim.booksservice.exceptions.BadRequestException;
import maksim.booksservice.exceptions.ForbiddenException;
import maksim.booksservice.exceptions.NotFoundException;
import maksim.booksservice.models.dtos.*;
import maksim.booksservice.models.entities.Book;
import maksim.booksservice.models.entities.BookStatusLog;
import maksim.booksservice.models.entities.User;
import maksim.booksservice.repositories.BookRepository;
import maksim.booksservice.repositories.UserRepository;
import maksim.booksservice.utils.bookutils.BookSearchCriteria;
import maksim.booksservice.utils.enums.JoinMode;
import maksim.booksservice.utils.validators.FileValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    @Mock
    private BookRepository bookRepository;

    @Mock
    private FileValidator fileValidator;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppConfig appConfig;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CachingService cachingService;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private User user;
    private BookStatusLog statusLog;
    private CreateBookDto createBookDto;
    private UpdateBookDto updateBookDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);

        statusLog = new BookStatusLog();
        statusLog.setStatus("READ");
        statusLog.setUserId(1);
        statusLog.setAddedDate(new Date());

        book = new Book();
        book.setId(1);
        book.setName("Test Book");
        book.setAuthor(user);
        book.setIssuedDate(new Date());
        book.setGenres(Arrays.asList("Fantasy", "Sci-Fi"));
        book.setRating(4.5f);
        book.setRatingCount(10);
        book.setFilePath("1.pdf");
        book.setStatusesLogs(Arrays.asList(statusLog, statusLog));

        createBookDto = new CreateBookDto();
        createBookDto.setName("New Book");
        createBookDto.setAuthorId(1);
        createBookDto.setGenres(Arrays.asList("Fantasy", "Sci-Fi"));

        updateBookDto = new UpdateBookDto();
        updateBookDto.setName("Updated Book");
        updateBookDto.setGenres(Arrays.asList("Fantasy", "Sci-Fi"));
    }

    @Test
    void getById_WithJoin_ShouldReturnBookDto() {
        when(bookRepository.findByIdWithJoin(1)).thenReturn(Optional.of(book));

        BookDto result = bookService.getById(1, JoinMode.WITH);

        assertNotNull(result);
        assertEquals(book.getId(), result.getId());
        verify(bookRepository).findByIdWithJoin(1);
    }

    @Test
    void getById_WithoutJoin_ShouldReturnBookDto() {
        when(bookRepository.findByIdWithoutJoin(1)).thenReturn(Optional.of(book));

        BookDto result = bookService.getById(1, JoinMode.WITHOUT);

        assertNotNull(result);
        assertEquals(book.getId(), result.getId());
        verify(bookRepository).findByIdWithoutJoin(1);
    }

    @Test
    void getById_NotFound_ShouldThrowNotFoundException() {
        when(bookRepository.findByIdWithJoin(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.getById(1, JoinMode.WITH));
    }





    @Test
    void getAllBooks_WithCriteria_ShouldReturnListOfBookDtos() {
        BookSearchCriteria criteria = new BookSearchCriteria(new HashMap<>() {{
            put("name", "book");
            put("authorId", "16");
            put("authorName", "Jora");
        }});
        criteria.setJoinModeForStatuses(JoinMode.WITH);
        criteria.setStatusMinDate(new Date(System.currentTimeMillis() - 100000));
        criteria.setStatusMaxDate(new Date());

        Pageable pageable = Pageable.unpaged();
        Page<Book> page = new PageImpl<>(List.of(book));

        when(bookRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        List<BookDto> result = bookService.getAllBooks(criteria, pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }





    @Test
    void getFile_ValidBookId_ShouldReturnFile() throws IOException {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(appConfig.getBookFilesDirectory()).thenReturn("test-files/");

        // Create test directory and file
        Files.createDirectories(Paths.get("test-files"));
        Files.createFile(Paths.get("test-files/1.pdf"));

        File result = bookService.getFile(1);

        assertNotNull(result);
        assertTrue(result.exists());

        // Cleanup
        Files.deleteIfExists(Paths.get("test-files/1.pdf"));
        Files.deleteIfExists(Paths.get("test-files"));
    }

    @Test
    void testAddBookFile_InvalidFileName() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "test",
                "application/pdf",
                "Test content".getBytes()
        );
        when(appConfig.getBookFilesDirectory()).thenReturn("/allowed/path/");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> bookService.addBookFile(file, 1));

        assertNotNull(exception);
    }

    @Test
    void testAddBookFile_BookNotFound() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "Test content".getBytes()
        );

        when(bookRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(appConfig.getBookFilesDirectory()).thenReturn("/allowed/path/");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> bookService.addBookFile(file, 1));

        assertNotNull(exception);
        verify(bookRepository, times(1)).findById(anyInt());
        verify(bookRepository, never()).save(any());
    }






    @Test
    void addBookMetaData_ValidData_ShouldReturnBookDto() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookDto result = bookService.addBookMetaData(createBookDto);

        assertNotNull(result);
        assertEquals(createBookDto.getName(), result.getName());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void addBookMetaData_AuthorNotFound_ShouldThrowBadRequestException() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> bookService.addBookMetaData(createBookDto));
    }





    @Test
    void deleteBook_ValidId_ShouldDeleteBook() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(appConfig.getBookFilesDirectory()).thenReturn("test-files/");

        bookService.deleteBook(1);

        verify(bookRepository).delete(book);
        verify(cachingService).deleteBook(1);
    }

    @Test
    void deleteBook_BookNotFound_ShouldThrowNotFoundException() {
        when(bookRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.deleteBook(1));
    }

    @Test
    void updateBook_ValidData_ShouldReturnUpdatedBookDto() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookDto result = bookService.updateBook(1, updateBookDto);

        assertNotNull(result);
        assertEquals(updateBookDto.getName(), result.getName());
        verify(cachingService).updateBook(eq(1), any(BookDto.class));
    }

    @Test
    void updateBook_BookNotFound_ShouldThrowNotFoundException() {
        when(bookRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookService.updateBook(1, updateBookDto));
    }




    @Test
    void addListOfBooks_ValidRequest_ShouldSaveBooks() {
        int authorId = 1;
        User user = new User();
        user.setId(authorId);

        List<CreateBookDto> bookDtos = List.of(
            new CreateBookDto("Book Name", Arrays.asList("gen1", "gen2"), authorId)
        );

        when(appConfig.getUserServiceUrl()).thenReturn("http://user-service");
        when(restTemplate.getForEntity(anyString(), eq(User.class)))
                .thenReturn(new ResponseEntity<>(user, HttpStatus.OK));

        bookService.addListOfBooks(authorId, bookDtos);

        verify(bookRepository).saveAll(anyList());
    }

    @Test
    void addListOfBooks_UserNotFound_ShouldThrowNotFoundException() {
        int authorId = 1;
        List<CreateBookDto> bookDtos = List.of(
            new CreateBookDto("Book Name", Arrays.asList("gen1", "gen2"), authorId)
        );

        when(appConfig.getUserServiceUrl()).thenReturn("http://user-service");
        when(restTemplate.getForEntity(anyString(), eq(User.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        assertThrows(NotFoundException.class, () ->
            bookService.addListOfBooks(authorId, bookDtos)
        );
    }

    @Test
    void addListOfBooks_AddingBooksForOtherUser_ShouldThrowForbiddenException() {
        int authorId = 1;
        int otherAuthorId = 2;
        User user = new User();
        user.setId(authorId);

        List<CreateBookDto> bookDtos = List.of(
            new CreateBookDto("Book Name", Arrays.asList("gen1", "gen2"), otherAuthorId)
        );

        when(appConfig.getUserServiceUrl()).thenReturn("http://user-service");
        when(restTemplate.getForEntity(anyString(), eq(User.class)))
                .thenReturn(new ResponseEntity<>(user, HttpStatus.OK));

        assertThrows(ForbiddenException.class, () ->
            bookService.addListOfBooks(authorId, bookDtos)
        );
    }

    @Test
    void addListOfBooks_EmptyList_ShouldNotSaveAnything() {
        List<CreateBookDto> bookDtos = new ArrayList<>();

        assertThrows(BadRequestException.class, () ->
            bookService.addListOfBooks(1, bookDtos)
        );
    }
}
