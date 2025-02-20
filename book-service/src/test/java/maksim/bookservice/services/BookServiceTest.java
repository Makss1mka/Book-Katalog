package maksim.bookservice.services;

import jakarta.ws.rs.BadRequestException;
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
        List<Book> books = List.of(new Book());

        when(bookRepository.findAllByGenres(genresFilter, pageable)).thenReturn(books);

        assertEquals(books, bookService.findAllBooksWithFilters(genres, pageable));

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
        List<Book> books = List.of(new Book());

        when(bookRepository.findByIssuedDateGreaterThan(date, pageable)).thenReturn(books);
        when(bookRepository.findByIssuedDateLessThan(date, pageable)).thenReturn(books);

        assertEquals(books, bookService.findAllByDate(date, Operator.GREATER, pageable));
        assertEquals(books, bookService.findAllByDate(date, Operator.LESS, pageable));
        assertThrows(BadRequestException.class, () -> {
            bookService.findAllByDate(date, Operator.EQUAL, pageable);
        });

        verify(bookRepository, times(1)).findByIssuedDateGreaterThan(date, pageable);
        verify(bookRepository, times(1)).findByIssuedDateLessThan(date, pageable);
    }

    @Test
    void testFindAllByRating() {
        int rating = 5;
        List<Book> books = List.of(new Book());

        when(bookRepository.findByRatingGreaterThan(rating, pageable)).thenReturn(books);
        when(bookRepository.findByRatingLessThan(rating, pageable)).thenReturn(books);
        when(bookRepository.findByRating(rating, pageable)).thenReturn(books);

        for (Operator op : Operator.values()) {
            assertEquals(books, bookService.findAllByRating(rating, op, pageable));
        }

        verify(bookRepository, times(1)).findByRatingGreaterThan(rating, pageable);
        verify(bookRepository, times(1)).findByRatingLessThan(rating, pageable);
        verify(bookRepository, times(1)).findByRating(rating, pageable);
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
        int value = 10;
        List<Book> books = List.of(new Book());

        // WHENS
        when(bookStatusesRepository.findByStatusReadingOverallGreaterThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusReadingLastYearGreaterThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusReadingLastMonthGreaterThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusReadingLastWeekGreaterThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusReadingOverallLessThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusReadingLastYearLessThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusReadingLastMonthLessThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusReadingLastWeekLessThan(value, pageable)).thenReturn(books);

        // ASSERTS
        for (Operator op : Operator.values()) {
            if (op == Operator.EQUAL) {
                assertThrows(BadRequestException.class, () -> {
                    bookService.findByStatusReading(value, op, BookStatusScope.OVERALL, pageable);
                });
                continue;
            }

            for (BookStatusScope bookStatScope : BookStatusScope.values()) {
                assertEquals(books, bookService.findByStatusReading(value, op, bookStatScope, pageable));
            }
        }

        // VERIFIES
        verify(bookStatusesRepository, times(1)).findByStatusReadingOverallGreaterThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusReadingLastYearGreaterThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusReadingLastMonthGreaterThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusReadingLastWeekGreaterThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusReadingOverallLessThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusReadingLastYearLessThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusReadingLastMonthLessThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusReadingLastWeekLessThan(value, pageable);
    }

    @Test
    void testFindByStatusRead() {
        int value = 10;
        List<Book> books = List.of(new Book());


        // WHENS
        when(bookStatusesRepository.findByStatusReadOverallGreaterThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusReadLastYearGreaterThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusReadLastMonthGreaterThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusReadLastWeekGreaterThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusReadOverallLessThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusReadLastYearLessThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusReadLastMonthLessThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusReadLastWeekLessThan(value, pageable)).thenReturn(books);

        // ASSERTS
        for (Operator op : Operator.values()) {
            if (op == Operator.EQUAL) {
                assertThrows(BadRequestException.class, () -> {
                    bookService.findByStatusRead(value, op, BookStatusScope.OVERALL, pageable);
                });
                continue;
            }

            for (BookStatusScope bookStatScope : BookStatusScope.values()) {
                assertEquals(books, bookService.findByStatusRead(value, op, bookStatScope, pageable));
            }
        }

        // VERIFIES
        verify(bookStatusesRepository, times(1)).findByStatusReadOverallGreaterThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusReadLastYearGreaterThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusReadLastMonthGreaterThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusReadLastWeekGreaterThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusReadOverallLessThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusReadLastYearLessThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusReadLastMonthLessThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusReadLastWeekLessThan(value, pageable);
    }

    @Test
    void testFindByStatusDrop() {
        int value = 10;
        List<Book> books = List.of(new Book());

        // WHENS
        when(bookStatusesRepository.findByStatusDropOverallGreaterThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusDropLastYearGreaterThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusDropLastMonthGreaterThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusDropLastWeekGreaterThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusDropOverallLessThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusDropLastYearLessThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusDropLastMonthLessThan(value, pageable)).thenReturn(books);
        when(bookStatusesRepository.findByStatusDropLastWeekLessThan(value, pageable)).thenReturn(books);

        // ASSERTS
        for (Operator op : Operator.values()) {
            if (op == Operator.EQUAL) {
                assertThrows(BadRequestException.class, () -> {
                    bookService.findByStatusDrop(value, op, BookStatusScope.OVERALL, pageable);
                });
                continue;
            }

            for (BookStatusScope bookStatScope : BookStatusScope.values()) {
                assertEquals(books, bookService.findByStatusDrop(value, op, bookStatScope, pageable));
            }
        }

        // VERIFIES
        verify(bookStatusesRepository, times(1)).findByStatusDropOverallGreaterThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusDropLastYearGreaterThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusDropLastMonthGreaterThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusDropLastWeekGreaterThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusDropOverallLessThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusDropLastYearLessThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusDropLastMonthLessThan(value, pageable);
        verify(bookStatusesRepository, times(1)).findByStatusDropLastWeekLessThan(value, pageable);
    }
}

