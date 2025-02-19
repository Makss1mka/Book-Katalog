package maksim.bookservice.controllers;

import maksim.bookservice.models.Book;
import maksim.bookservice.services.BookService;
import maksim.bookservice.utils.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BookControllerTest {

    @Mock
    private BookService bookService;

    @Mock
    private Pagination pagination;

    @InjectMocks
    private BookController bookController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllBooks_WithGenres() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        when(pagination.getPageable(anyInt(), anyInt(), any(SortField.class), any(SortDirection.class))).thenReturn(pageable);
        when(bookService.findAllBooksWithFilters(anyString(), any(Pageable.class))).thenReturn(Arrays.asList(new Book(), new Book()));

        // Act
        ResponseEntity<List<Book>> response = bookController.getAllBooks("fantasy", 0, 20, "rating", "desc");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(bookService, times(1)).findAllBooksWithFilters("fantasy", pageable);
    }

    @Test
    void testGetAllBooks_WithoutGenres() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        when(pagination.getPageable(anyInt(), anyInt(), any(SortField.class), any(SortDirection.class))).thenReturn(pageable);
        when(bookService.findAllBooks(any(Pageable.class))).thenReturn(Arrays.asList(new Book(), new Book()));

        // Act
        ResponseEntity<List<Book>> response = bookController.getAllBooks(null, 0, 20, "rating", "desc");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(bookService, times(1)).findAllBooks(pageable);
    }

    @Test
    void testGetBookById_Found() {
        // Arrange
        Book book = new Book();
        when(bookService.findById(anyInt())).thenReturn(Optional.of(book));

        // Act
        ResponseEntity<Book> response = bookController.getBookById(1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(book, response.getBody());
        verify(bookService, times(1)).findById(1);
    }

    @Test
    void testGetBookById_NotFound() {
        // Arrange
        when(bookService.findById(anyInt())).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Book> response = bookController.getBookById(1);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(bookService, times(1)).findById(1);
    }

    @Test
    void testGetBooksByName() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        when(pagination.getPageable(anyInt(), anyInt(), any(SortField.class), any(SortDirection.class))).thenReturn(pageable);
        when(bookService.findByName(anyString(), any(Pageable.class))).thenReturn(Arrays.asList(new Book(), new Book()));

        // Act
        ResponseEntity<List<Book>> response = bookController.getBooksByName("Book Name", 0, 20, "rating", "desc");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(bookService, times(1)).findByName("Book Name", pageable);
    }

    @Test
    void testGetBooksByAuthorId() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        when(pagination.getPageable(anyInt(), anyInt(), any(SortField.class), any(SortDirection.class))).thenReturn(pageable);
        when(bookService.findAllByAuthorId(anyInt(), any(Pageable.class))).thenReturn(Arrays.asList(new Book(), new Book()));

        // Act
        ResponseEntity<List<Book>> response = bookController.getBooksByAuthorId(1, 0, 20, "rating", "desc");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(bookService, times(1)).findAllByAuthorId(1, pageable);
    }

    @Test
    void testGetBooksByAuthorName() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        when(pagination.getPageable(anyInt(), anyInt(), any(SortField.class), any(SortDirection.class))).thenReturn(pageable);
        when(bookService.findAllByAuthorName(anyString(), any(Pageable.class))).thenReturn(Arrays.asList(new Book(), new Book()));

        // Act
        ResponseEntity<List<Book>> response = bookController.getBooksByAuthorId("Author Name", 0, 20, "rating", "desc");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(bookService, times(1)).findAllByAuthorName("Author Name", pageable);
    }

    @Test
    void testGetByRating() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        when(pagination.getPageable(anyInt(), anyInt(), any(SortField.class), any(SortDirection.class))).thenReturn(pageable);
        when(bookService.findAllByRating(anyInt(), any(Operator.class), any(Pageable.class))).thenReturn(Arrays.asList(new Book(), new Book()));

        // Act
        ResponseEntity<List<Book>> response = bookController.getByRating(5, "greater", 0, 20, "rating", "desc");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(bookService, times(1)).findAllByRating(5, Operator.GREATER, pageable);
    }

    @Test
    void testGetByDate_Success() throws ParseException {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        when(pagination.getPageable(anyInt(), anyInt(), any(SortField.class), any(SortDirection.class))).thenReturn(pageable);
        when(bookService.findAllByDate(any(Date.class), any(Operator.class), any(Pageable.class))).thenReturn(Arrays.asList(new Book(), new Book()));

        // Act
        ResponseEntity<List<Book>> response = bookController.getByDate("2023-01-01", "greater", 0, 20, "rating", "desc");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(bookService, times(1)).findAllByDate(any(Date.class), eq(Operator.GREATER), any(Pageable.class));
    }


    @Test
    void testGetByDate_InvalidDate() {
        // Act
        ResponseEntity<List<Book>> response = bookController.getByDate("invalid-date", "greater", 0, 20, "rating", "desc");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(bookService, never()).findAllByDate(any(Date.class), any(Operator.class), any(Pageable.class));
    }

    @Test
    void testGetByStatuses() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        when(pagination.getPageable(anyInt(), anyInt(), any(SortField.class), any(SortDirection.class))).thenReturn(pageable);
        when(bookService.findByStatusReading(anyInt(), any(Operator.class), any(BookStatusScope.class), any(Pageable.class))).thenReturn(Arrays.asList(new Book(), new Book()));

        // Act
        ResponseEntity<List<Book>> response = bookController.getByStatuses("reading", "overall", "greater", 5, 0, 20, "rating", "desc");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(bookService, times(1)).findByStatusReading(5, Operator.GREATER, BookStatusScope.OVERALL, pageable);
    }
}