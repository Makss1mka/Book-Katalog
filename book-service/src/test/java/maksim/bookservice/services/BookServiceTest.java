package maksim.bookservice.services;

import maksim.bookservice.models.Book;
import maksim.bookservice.repositories.BookRepository;
import maksim.bookservice.repositories.BookStatusesRepository;
import maksim.bookservice.utils.BookStatusScope;
import maksim.bookservice.utils.Operator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookStatusesRepository bookStatusesRepository;

    @InjectMocks
    private BookService bookService;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void testFindAllBooks() {
        Book book = new Book();
        Page<Book> bookPage = new PageImpl<>(List.of(book));
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);

        List<Book> result = bookService.findAllBooks(pageable);

        assertEquals(1, result.size());
        verify(bookRepository, times(1)).findAll(pageable);
    }

    @Test
    void testFindAllBooksWithFilters() {
        String genres = "fiction,thriller";
        List<String> genresFilter = List.of("fiction", "thriller");
        Book book = new Book();
        when(bookRepository.findAllByGenres(genresFilter, pageable)).thenReturn(List.of(book));

        List<Book> result = bookService.findAllBooksWithFilters(genres, pageable);

        assertEquals(1, result.size());
        verify(bookRepository, times(1)).findAllByGenres(genresFilter, pageable);
    }

    @Test
    void testFindAllByAuthorName() {
        String authorName = "test_author_4";
        Book book = new Book();
        when(bookRepository.findByAuthorName(authorName, pageable)).thenReturn(List.of(book));

        List<Book> result = bookService.findAllByAuthorName(authorName, pageable);

        assertEquals(1, result.size());
        verify(bookRepository, times(1)).findByAuthorName(authorName, pageable);
    }

    @Test
    void testFindAllByAuthorId() {
        int authorId = 17;
        Book book = new Book();
        when(bookRepository.findByAuthorId(authorId, pageable)).thenReturn(List.of(book));

        List<Book> result = bookService.findAllByAuthorId(authorId, pageable);

        assertEquals(1, result.size());
        verify(bookRepository, times(1)).findByAuthorId(authorId, pageable);
    }

    @Test
    void testFindAllByDate() {
        Date date = new Date();
        Book book = new Book();
        when(bookRepository.findByIssuedDateGreaterThan(date, pageable)).thenReturn(List.of(book));

        List<Book> result = bookService.findAllByDate(date, Operator.GREATER, pageable);

        assertEquals(1, result.size());
        verify(bookRepository, times(1)).findByIssuedDateGreaterThan(date, pageable);
    }

    @Test
    void testFindAllByRating() {
        int rating = 5;
        Book book = new Book();
        when(bookRepository.findByRatingGreaterThan(rating, pageable)).thenReturn(List.of(book));

        List<Book> result = bookService.findAllByRating(rating, Operator.GREATER, pageable);

        assertEquals(1, result.size());
        verify(bookRepository, times(1)).findByRatingGreaterThan(rating, pageable);
    }

    @Test
    void testFindByName() {
        String name = "Затерянный Мир";
        Book book = new Book();
        when(bookRepository.findByName(name, pageable)).thenReturn(List.of(book));

        List<Book> result = bookService.findByName(name, pageable);

        assertEquals(1, result.size());
        verify(bookRepository, times(1)).findByName(name, pageable);
    }

    @Test
    void testFindById() {
        int id = 1;
        Book book = new Book();
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        Optional<Book> result = bookService.findById(id);

        assertTrue(result.isPresent());
        verify(bookRepository, times(1)).findById(id);
    }

    @Test
    void testFindByStatusReading() {
        int value = 5;
        Book book = new Book();
        when(bookStatusesRepository.findByStatusReadingOverallGreaterThan(value, pageable)).thenReturn(List.of(book));

        List<Book> result = bookService.findByStatusReading(value, Operator.GREATER, BookStatusScope.OVERALL, pageable);

        assertEquals(1, result.size());
        verify(bookStatusesRepository, times(1)).findByStatusReadingOverallGreaterThan(value, pageable);
    }

    @Test
    void testFindByStatusRead() {
        int value = 5;
        Book book = new Book();
        when(bookStatusesRepository.findByStatusReadOverallGreaterThan(value, pageable)).thenReturn(List.of(book));

        List<Book> result = bookService.findByStatusRead(value, Operator.GREATER, BookStatusScope.OVERALL, pageable);

        assertEquals(1, result.size());
        verify(bookStatusesRepository, times(1)).findByStatusReadOverallGreaterThan(value, pageable);
    }

    @Test
    void testFindByStatusDrop() {
        int value = 5;
        Book book = new Book();
        when(bookStatusesRepository.findByStatusDropOverallGreaterThan(value, pageable)).thenReturn(List.of(book));

        List<Book> result = bookService.findByStatusDrop(value, Operator.GREATER, BookStatusScope.OVERALL, pageable);

        assertEquals(1, result.size());
        verify(bookStatusesRepository, times(1)).findByStatusDropOverallGreaterThan(value, pageable);
    }
}

