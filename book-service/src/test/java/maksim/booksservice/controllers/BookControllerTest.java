package maksim.booksservice.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import jakarta.ws.rs.NotFoundException;
import maksim.booksservice.config.GlobalExceptionHandler;
import maksim.booksservice.models.Book;
import maksim.booksservice.services.BookService;
import maksim.booksservice.utils.bookutils.BookSearchCriteria;
import maksim.booksservice.utils.enums.JoinMode;
import maksim.booksservice.utils.validators.BookDtoForCreatingValidators;
import maksim.booksservice.utils.validators.FileValidators;
import maksim.booksservice.utils.validators.StringValidators;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
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
    void testGetBookById() {
        Book book = new Book();

        when(bookService.getById(1, JoinMode.WITHOUT_JOIN)).thenReturn(Optional.of(book));
        when(bookService.getById(1, JoinMode.WITH_JOIN)).thenReturn(Optional.of(book));

        ResponseEntity<Book> result;

        result = bookController.getBookById(1, "with");
        assertNotNull(result);
        assertEquals(result.getBody(), book);

        result = bookController.getBookById(1, "without");
        assertNotNull(result);
        assertEquals(result.getBody(), book);

        verify(bookService, times(2)).getById(anyInt(), any(JoinMode.class));
    }


    @Test
    void testGet() {
        List<Book> books = Arrays.asList(new Book(), new Book(), new Book());

        Map<String, String> params = new HashMap<>();

        when(bookService.getAllBooks(any(BookSearchCriteria.class), any(JoinMode.class), any(Pageable.class))).thenReturn(books);

        ResponseEntity<List<Book>> result = bookController.getAllBooks(params);
        assertNotNull(result);
        assertEquals(result.getBody(), books);

        verify(bookService, times(1)).getAllBooks(any(BookSearchCriteria.class), any(JoinMode.class), any(Pageable.class));
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

        mockMvc.perform(post("/books/metaData")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"TEST NAME\", \"authorId\": 17}"))
                .andExpect(status().isOk());

        verify(bookService, times(1)).addBookMetaData(any());
    }

    @Test
    void testAddBookMetaData_InvalidData() throws Exception {
        when(bookDtoForCreatingValidators.isSafeFromSqlInjection(any())).thenReturn(false);
        when(stringValidators.isSafeFromSqlInjection(any())).thenReturn(false);

        mockMvc.perform(post("/books/metaData")
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

        mockMvc.perform(MockMvcRequestBuilders.multipart("/books/1/file")
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

        mockMvc.perform(MockMvcRequestBuilders.multipart("/books/1/file")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: book file is not valid. File support extensions: pdf, txt, md. FIle should be less than 2mb"));

        verify(bookService, never()).addBookFile(any(), anyInt());
    }

}