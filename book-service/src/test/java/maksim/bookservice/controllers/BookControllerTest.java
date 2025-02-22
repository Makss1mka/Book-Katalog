package maksim.bookservice.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import jakarta.ws.rs.NotFoundException;
import maksim.bookservice.config.GlobalExceptionHandler;
import maksim.bookservice.models.Book;
import maksim.bookservice.services.BookService;
import maksim.bookservice.utils.*;
import maksim.bookservice.utils.enums.BookStatusScope;
import maksim.bookservice.utils.enums.Operator;
import maksim.bookservice.utils.enums.SortDirection;
import maksim.bookservice.utils.enums.SortField;
import maksim.bookservice.utils.validators.BookDtoForCreatingValidators;
import maksim.bookservice.utils.validators.FileValidators;
import maksim.bookservice.utils.validators.StringValidators;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BookControllerTest {
    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @Mock
    private BookDtoForCreatingValidators bookDtoForCreatingValidators;

    @Mock
    private FileValidators fileValidators;

    @Mock
    private StringValidators stringValidators;

    @Mock
    private Pagination pagination;

    @InjectMocks
    private BookController bookController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .setControllerAdvice(new GlobalExceptionHandler()) // Добавляем глобальный обработчик
                .build();
    }

    @Test
    void testGetAllBooks_WithGenres() {
        Pageable pageable = mock(Pageable.class);
        when(pagination.getPageable(anyInt(), anyInt(), any(SortField.class), any(SortDirection.class))).thenReturn(pageable);
        when(bookService.findAllBooksWithFilters(anyString(), any(Pageable.class))).thenReturn(Arrays.asList(new Book(), new Book()));

        ResponseEntity<List<Book>> response = bookController.getAllBooks("fantasy", 0, 20, "rating", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(bookService, times(1)).findAllBooksWithFilters("fantasy", pageable);
    }

    @Test
    void testGetAllBooks_WithoutGenres() {
        Pageable pageable = mock(Pageable.class);
        when(pagination.getPageable(anyInt(), anyInt(), any(SortField.class), any(SortDirection.class))).thenReturn(pageable);
        when(bookService.findAllBooks(any(Pageable.class))).thenReturn(Arrays.asList(new Book(), new Book()));

        ResponseEntity<List<Book>> response = bookController.getAllBooks(null, 0, 20, "rating", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(bookService, times(1)).findAllBooks(pageable);
    }

    @Test
    void testGetBookById_Found() {
        Book book = new Book();
        when(bookService.findById(anyInt())).thenReturn(Optional.of(book));

        ResponseEntity<Book> response = bookController.getBookById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(book, response.getBody());
        verify(bookService, times(1)).findById(1);
    }

    @Test
    void testGetBookById_NotFound() {
        when(bookService.findById(anyInt())).thenReturn(Optional.empty());

        ResponseEntity<Book> response = bookController.getBookById(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(bookService, times(1)).findById(1);
    }

    @Test
    void testGetBooksByName() {
        Pageable pageable = mock(Pageable.class);
        when(pagination.getPageable(anyInt(), anyInt(), any(SortField.class), any(SortDirection.class))).thenReturn(pageable);
        when(bookService.findByName(anyString(), any(Pageable.class))).thenReturn(Arrays.asList(new Book(), new Book()));

        ResponseEntity<List<Book>> response = bookController.getBooksByName("Book Name", 0, 20, "rating", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(bookService, times(1)).findByName("Book Name", pageable);
    }

    @Test
    void testGetBooksByAuthorId() {
        Pageable pageable = mock(Pageable.class);
        when(pagination.getPageable(anyInt(), anyInt(), any(SortField.class), any(SortDirection.class))).thenReturn(pageable);
        when(bookService.findAllByAuthorId(anyInt(), any(Pageable.class))).thenReturn(Arrays.asList(new Book(), new Book()));

        ResponseEntity<List<Book>> response = bookController.getBooksByAuthorId(1, 0, 20, "rating", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(bookService, times(1)).findAllByAuthorId(1, pageable);
    }

    @Test
    void testGetBooksByAuthorName() {
        Pageable pageable = mock(Pageable.class);
        when(pagination.getPageable(anyInt(), anyInt(), any(SortField.class), any(SortDirection.class))).thenReturn(pageable);
        when(bookService.findAllByAuthorName(anyString(), any(Pageable.class))).thenReturn(Arrays.asList(new Book(), new Book()));

        ResponseEntity<List<Book>> response = bookController.getBooksByAuthorId("Author Name", 0, 20, "rating", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(bookService, times(1)).findAllByAuthorName("Author Name", pageable);
    }

    @Test
    void testGetByRating() {
        Pageable pageable = mock(Pageable.class);
        when(pagination.getPageable(anyInt(), anyInt(), any(SortField.class), any(SortDirection.class))).thenReturn(pageable);
        when(bookService.findAllByRating(anyInt(), any(Operator.class), any(Pageable.class))).thenReturn(Arrays.asList(new Book(), new Book()));

        ResponseEntity<List<Book>> response = bookController.getByRating(5, "greater", 0, 20, "rating", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(bookService, times(1)).findAllByRating(5, Operator.GREATER, pageable);
    }

    @Test
    void testGetByDate_Success() {
        Pageable pageable = mock(Pageable.class);
        when(pagination.getPageable(anyInt(), anyInt(), any(SortField.class), any(SortDirection.class))).thenReturn(pageable);
        when(bookService.findAllByDate(any(Date.class), any(Operator.class), any(Pageable.class))).thenReturn(Arrays.asList(new Book(), new Book()));

        ResponseEntity<List<Book>> response = bookController.getByDate("2023-01-01", "greater", 0, 20, "rating", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(bookService, times(1)).findAllByDate(any(Date.class), eq(Operator.GREATER), any(Pageable.class));
    }

    @Test
    void testGetByDate_InvalidDate() {
        ResponseEntity<List<Book>> response = bookController.getByDate("invalid-date", "greater", 0, 20, "rating", "desc");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(bookService, never()).findAllByDate(any(Date.class), any(Operator.class), any(Pageable.class));
    }

    @Test
    void testGetByStatuses() {
        Pageable pageable = mock(Pageable.class);
        when(pagination.getPageable(anyInt(), anyInt(), any(SortField.class), any(SortDirection.class))).thenReturn(pageable);
        when(bookService.findByStatusReading(anyInt(), any(Operator.class), any(BookStatusScope.class), any(Pageable.class))).thenReturn(Arrays.asList(new Book(), new Book()));

        ResponseEntity<List<Book>> response = bookController.getByStatuses("reading", "overall", "greater", 5, 0, 20, "rating", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(bookService, times(1)).findByStatusReading(5, Operator.GREATER, BookStatusScope.OVERALL, pageable);
    }



    @Test
    void testGetBookFile_Success() throws IOException {
        Path tempFile = Files.createTempFile("test-file", ".txt");
        Files.write(tempFile, "Test content".getBytes());
        File file = tempFile.toFile();

        when(bookService.getFile(anyInt())).thenReturn(file);

        ResponseEntity<Resource> response = bookController.getBookFile(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(file.length(), response.getHeaders().getContentLength());
        assertEquals("attachment; filename=\"" + file.getName() + "\"", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));

        Files.delete(tempFile);
    }

    @Test
    void testGetBookFile_FileNotFound() {
        when(bookService.getFile(anyInt())).thenThrow(new NotFoundException("Not found"));

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> bookController.getBookFile(1)
        );

        assertEquals("Not found", exception.getMessage());
    }



    @Test
    void testAddBookMetaData_Success() throws Exception {
        when(bookDtoForCreatingValidators.isSafeFromSqlInjection(any())).thenReturn(true);

        mockMvc.perform(post("/books/add/book/metaData")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"TEST NAME\", \"authorId\": 17}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Book metadata was successfully added"));

        verify(bookService, times(1)).addBookMetaData(any());
    }

    @Test
    void testAddBookMetaData_InvalidData() throws Exception {
        when(bookDtoForCreatingValidators.isSafeFromSqlInjection(any())).thenReturn(false);
        when(stringValidators.isSafeFromSqlInjection(any())).thenReturn(false);

        mockMvc.perform(post("/books/add/book/metaData")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"TEST NAME\", \"authorId\": 17}"))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).addBookMetaData(any());
    }



    @Test
    void testAddBookFile_Success() throws Exception {
        when(fileValidators.isValid(any())).thenReturn(true);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Test content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/books/add/book/file/1")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Book file was successfully added"));

        verify(bookService, times(1)).addBookFile(any(), eq(1));
    }

    @Test
    void testAddBookFile_InvalidFile() throws Exception {
        when(fileValidators.isValid(any())).thenReturn(false);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Test content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/books/add/book/file/1")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: book file is not valid. File support extensions: pdf, txt, md. FIle should be less than 2mb"));

        verify(bookService, never()).addBookFile(any(), anyInt());
    }

}