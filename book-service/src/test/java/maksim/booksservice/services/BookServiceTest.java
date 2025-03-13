package maksim.booksservice.services;

import maksim.booksservice.exceptions.BadRequestException;
import maksim.booksservice.exceptions.NotFoundException;
import maksim.booksservice.config.AppConfig;
import maksim.booksservice.models.dtos.BookDto;
import maksim.booksservice.models.entities.Book;
import maksim.booksservice.models.dtos.CreateBookDto;
import maksim.booksservice.models.entities.User;
import maksim.booksservice.repositories.BookRepository;
import maksim.booksservice.repositories.UserRepository;
import maksim.booksservice.utils.bookutils.BookSearchCriteria;
import maksim.booksservice.utils.validators.FileValidators;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceTest {
    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileValidators fileValidators;

    @Mock
    private AppConfig appConfig;

    @Mock
    private CachingService cachingService;

    @InjectMocks
    private BookService bookService;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void testGetAll() {
        Page<Book> page = new PageImpl<>(Arrays.asList(new Book(), new Book()), pageable, 2);

        when(bookRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Map<String, String> params = new HashMap<>();
        BookSearchCriteria criteria = new BookSearchCriteria(params);

        List<BookDto> result = bookService.getAllBooks(criteria, pageable);
        assertEquals(2, result.size());

        verify(bookRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }


    @Test
    void testGetFile_Success() throws Exception {
        int bookId = 1;
        String fileContent = "Test content";

        Path tempFile = Files.createTempFile("test-file", ".pdf");
        Files.write(tempFile, fileContent.getBytes());

        Book book = new Book();
        book.setId(bookId);
        book.setFilePath(tempFile.getFileName().toString());

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(appConfig.getBookFilesDirectory()).thenReturn(tempFile.getParent() + "/");

        File result = bookService.getFile(bookId);

        assertNotNull(result);
        assertEquals(tempFile.toAbsolutePath().toString(), result.getAbsolutePath());
        verify(bookRepository, times(1)).findById(bookId);

        Files.deleteIfExists(tempFile);
    }

    @Test
    void testGetFile_BookNotFound() {
        when(bookRepository.findById(anyInt())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookService.getFile(1));

        verify(bookRepository, times(1)).findById(anyInt());
    }

    @Test
    void testGetFile_FileNotFound() {
        int bookId = 1;
        Book book = new Book();
        book.setId(bookId);
        book.setFilePath("nonexistent.pdf");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(appConfig.getBookFilesDirectory()).thenReturn("/");

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookService.getFile(bookId));

        assertNotNull(exception);
        verify(bookRepository, times(1)).findById(bookId);
    }



    @Test
    void testAddBookFile_Success() throws Exception {
        MultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "Test content".getBytes()
        );

        int bookId = 1;
        Book book = new Book();
        book.setId(bookId);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(appConfig.getBookFilesDirectory()).thenReturn("target/test-files/");
        when(fileValidators.isPathAllowed(any())).thenReturn(true);

        bookService.addBookFile(file, bookId);

        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).save(book);

        Path filePath = Paths.get("target/test-files/" + bookId + ".pdf");
        assertTrue(Files.exists(filePath));

        Files.deleteIfExists(filePath);
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
    void testAddBookMetaData_Success() {
        int authorId = 1;

        CreateBookDto bookData = new CreateBookDto();
        bookData.setAuthorId(authorId);
        bookData.setName("Test Name");
        bookData.setGenres(Arrays.asList("Programming", "Software Engineering"));

        User author = new User();
        author.setId(authorId);

        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));

        bookService.addBookMetaData(bookData);

        verify(userRepository, times(1)).findById(authorId);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testAddBookMetaData_AuthorNotFound() {
        int authorId = 1;

        CreateBookDto bookData = new CreateBookDto();
        bookData.setAuthorId(authorId);
        bookData.setName("Test Name");
        bookData.setGenres(Arrays.asList("Programming", "Software Engineering"));

        when(userRepository.findById(authorId)).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> bookService.addBookMetaData(bookData));

        assertNotNull(exception);
        verify(userRepository, times(1)).findById(authorId);
        verify(bookRepository, never()).save(any(Book.class));
    }



    @Test
    void testDeleteBook_Success() throws Exception {
        int bookId = 1;

        Path tempFile = Files.createTempFile("test-file", ".pdf");
        Files.write(tempFile, "Test content".getBytes());

        Book book = new Book();
        book.setId(bookId);
        book.setFilePath(tempFile.getFileName().toString());

        doNothing().when(cachingService).deleteBook(anyInt());
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(appConfig.getBookFilesDirectory()).thenReturn(tempFile.getParent() + "/");

        bookService.deleteBook(bookId);

        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).delete(book);

        assertFalse(Files.exists(tempFile));

        Files.deleteIfExists(tempFile);
    }

    @Test
    void testDeleteBook_BookNotFound() {
        int bookId = 1;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookService.deleteBook(bookId));

        assertNotNull(exception);
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, never()).delete(any(Book.class));
    }

}

