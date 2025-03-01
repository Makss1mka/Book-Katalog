package maksim.booksservice.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import jakarta.ws.rs.NotFoundException;
import maksim.booksservice.config.GlobalExceptionHandler;
import maksim.booksservice.models.Book;
import maksim.booksservice.services.BookService;
import maksim.booksservice.utils.*;
import maksim.booksservice.utils.enums.BookStatusScope;
import maksim.booksservice.utils.enums.NumberOperator;
import maksim.booksservice.utils.enums.SortDirection;
import maksim.booksservice.utils.enums.SortField;
import maksim.booksservice.utils.validators.BookDtoForCreatingValidators;
import maksim.booksservice.utils.validators.FileValidators;
import maksim.booksservice.utils.validators.StringValidators;
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