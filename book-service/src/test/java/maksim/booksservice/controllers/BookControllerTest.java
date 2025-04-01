package maksim.booksservice.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import maksim.booksservice.exceptions.NotFoundException;
import maksim.booksservice.config.GlobalExceptionHandler;
import maksim.booksservice.models.dtos.BookDto;
import maksim.booksservice.models.dtos.CreateBookDto;
import maksim.booksservice.services.BookService;
import maksim.booksservice.services.CachingService;
import maksim.booksservice.utils.bookutils.BookSearchCriteria;
import maksim.booksservice.utils.enums.JoinMode;
import maksim.booksservice.utils.validators.*;
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
import static org.mockito.Mockito.*;

class BookControllerTest {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private BookService bookService;

    @Mock
    private CreateBookDtoValidator createBookDtoValidator;

    @Mock
    private FileValidator fileValidator;

    @Mock
    private StringValidator stringValidator;

    @Mock
    private BookSearchCriteriaValidator bookSearchCriteriaValidator;

    @Mock
    private CachingService cachingService;

    @Mock
    private QueryParamsValidator queryParamsValidator;

    @InjectMocks
    private BookController bookController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .setControllerAdvice(new GlobalExceptionHandler()) // Добавляем глобальный обработчик
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetBookById() {
        BookDto book = new BookDto();

        when(bookService.getById(1, JoinMode.WITHOUT)).thenReturn(book);
        when(bookService.getById(1, JoinMode.WITH)).thenReturn(book);

        ResponseEntity<BookDto> result;

        result = bookController.getBookById(1, "with");
        assertNotNull(result);
        assertEquals(result.getBody(), book);

        result = bookController.getBookById(1, "without");
        assertNotNull(result);
        assertEquals(result.getBody(), book);

        verify(bookService, times(2)).getById(anyInt(), any(JoinMode.class));
    }


    @Test
    void testGet() throws Exception {
        List<BookDto> books = Arrays.asList(new BookDto(), new BookDto(), new BookDto());

        when(cachingService.contains(any(String.class))).thenReturn(false);
        doNothing().when(cachingService).addToCache(any(String.class), any(), anyInt());
        doNothing().when(queryParamsValidator).queryAsMapValidating(any(Map.class));

        when(bookSearchCriteriaValidator.isSafeFromSqlInjection(any(BookSearchCriteria.class))).thenReturn(true);
        when(bookService.getAllBooks(any(BookSearchCriteria.class), any(Pageable.class))).thenReturn(books);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/books"))
                    .andExpect(status().isOk());

        verify(bookService, times(1)).getAllBooks(any(BookSearchCriteria.class), any(Pageable.class));
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

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }



    @Test
    void testAddBookMetaData_Success() throws Exception {
        when(createBookDtoValidator.isSafeFromSqlInjection(any())).thenReturn(true);

        mockMvc.perform(post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"TEST NAME\", \"authorId\": 17}"))
            .andExpect(status().isOk());

        verify(bookService, times(1)).addBookMetaData(any());
    }

    @Test
    void testAddBookMetaData_InvalidData() throws Exception {
        when(createBookDtoValidator.isSafeFromSqlInjection(any())).thenReturn(false);
        when(stringValidator.isSafeFromSqlInjection(any())).thenReturn(false);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"TEST NAME\", \"authorId\": 17}"))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).addBookMetaData(any());
    }



    @Test
    void testAddBookFile_Success() throws Exception {
        when(fileValidator.isValid(any())).thenReturn(true);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Test content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/books/1/file")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Book file was successfully added"));

        verify(bookService, times(1)).addBookFile(any(), eq(1));
    }

    @Test
    void testAddBookFile_InvalidFile() throws Exception {
        when(fileValidator.isValid(any())).thenReturn(false);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Test content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/books/1/file")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).addBookFile(any(), anyInt());
    }



    @Test
    void addAllBooksFromList_ValidRequest_ShouldReturnOk() throws Exception {
        List<CreateBookDto> bookDtos = Arrays.asList(
                new CreateBookDto("Book Name", Arrays.asList("gen1", "gen2"), 1)
        );

        mockMvc.perform(post("/api/v1/books/all")
                        .param("authorId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDtos)))
                .andExpect(status().isOk());

        verify(bookService).addListOfBooks(eq(1), any(List.class));
    }

}