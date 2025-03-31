package maksim.booksservice.services;

import maksim.booksservice.models.dtos.BookDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class CachingServiceTest {
    @InjectMocks
    private CachingService cachingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addToCache_ShouldAddItemAndMaintainSizeLimit() {
        List<BookDto> books = List.of(new BookDto());
        String url1 = "url";

        for (int i = 0; i < 11; i++) {
            cachingService.addToCache(url1 + i, books, 10000);
        }

        assertFalse(cachingService.contains(url1 + "0"));
        assertTrue(cachingService.contains(url1 + "10"));
    }

    @Test
    void contains_ShouldReturnFalseForExpiredCache() {
        String url = "url";

        cachingService.addToCache(url, List.of(new BookDto()), 1);

        await()
            .atMost(2, TimeUnit.SECONDS)
            .until(() -> !cachingService.contains(url));

        boolean result = cachingService.contains(url);

        assertFalse(result);
    }

    @Test
    void contains_ShouldReturnTrueForValidCache() {
        String url = "url";

        cachingService.addToCache(url, List.of(new BookDto()), 10000);

        boolean result = cachingService.contains(url);

        assertTrue(result);
    }

    @Test
    void invalidateCache_ShouldRemoveItem() {
        String url = "url";

        cachingService.addToCache(url, List.of(new BookDto()), 10000);

        cachingService.invalidateCache(url);

        assertFalse(cachingService.contains(url));
    }

    @Test
    void deleteBook_ShouldRemoveBookFromAllCaches() {
        BookDto book1 = new BookDto();
        book1.setId(1);
        BookDto book2 = new BookDto();
        book2.setId(2);

        List<BookDto> books1 = new ArrayList<>(List.of(book1, book2));
        List<BookDto> books2 = new ArrayList<>(List.of(book1));

        String url1 = "url1";
        String url2 = "url2";

        cachingService.addToCache(url1, books1, 10000);
        cachingService.addToCache(url2, books2, 10000);

        cachingService.deleteBook(1);

        assertEquals(1, cachingService.getFromCache(url1).size());
        assertEquals(0, cachingService.getFromCache(url2).size());
    }

    @Test
    void updateBook_ShouldUpdateBookInAllCaches() {
        BookDto originalBook = new BookDto();
        originalBook.setId(1);
        originalBook.setName("Old Name");

        BookDto updatedBook = new BookDto();
        updatedBook.setId(1);
        updatedBook.setName("New Name");

        String url = "url";

        cachingService.addToCache(url, new ArrayList<>(List.of(originalBook)), 10000);

        cachingService.updateBook(1, updatedBook);

        assertEquals(updatedBook.getName(), cachingService.getFromCache(url).getFirst().getName());
    }

    @Test
    void checkAndDeleteInvalidCaches_ShouldRemoveExpiredItems() {
        List<BookDto> books = List.of(new BookDto());

        cachingService.addToCache("expired", books, 1);
        cachingService.addToCache("valid", books, 10000);

        await()
            .atMost(2, TimeUnit.SECONDS)
            .until(() -> !cachingService.contains("expired"));

        cachingService.checkAndDeleteInvalidCaches();

        assertFalse(cachingService.contains("expired"));
        assertTrue(cachingService.contains("valid"));
    }

    @Test
    void printStorage_ShouldLogCacheState() {
        assertDoesNotThrow(() -> {
            cachingService.printStorage();
        });
    }
}