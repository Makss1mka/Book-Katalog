package maksim.booksservice.services;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import maksim.booksservice.models.dtos.result.BookDto;
import maksim.booksservice.utils.CacheObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CachingService {
    private static final Logger logger = LoggerFactory.getLogger(CachingService.class);

    private static final int STORAGE_MAX_SIZE = 10;

    private final Map<String, CacheObject<List<BookDto>>> storage = new ConcurrentHashMap<>();
    private final List<String> history = new LinkedList<>();
    private int storageSize = 0;

    synchronized public void invalidateCache(String url) {
        storage.remove(url);
        history.remove(url);

        storageSize--;
    }

    synchronized public List<BookDto> getFromCache(String url) {
        storage.get(url).setCreationDate(new Date());

        logger.trace("Caching service method: getFromCache | Get values from cache");

        return storage.get(url).getValue();
    }

    synchronized public boolean contains(String url) {
        if (!storage.containsKey(url)) {
            return false;
        }

        if (new Date(storage.get(url).getCreationDate().getTime() + storage.get(url).getExpirationTime()).before(new Date())) {
            invalidateCache(url);

            return false;
        }

        return true;
    }

    synchronized public void addToCache(String url, List<BookDto> dtos, long expirationTime) {
        logger.trace("Caching service method: addToCache | Add value to cache");

        if (storageSize >= STORAGE_MAX_SIZE) {
            String deletedUrl = history.removeFirst();
            storage.remove(deletedUrl);
        } else {
            storageSize++;
        }

        CacheObject<List<BookDto>> cacheObject = new CacheObject<>(dtos, new Date(), expirationTime);

        storage.put(url, cacheObject);
        history.addLast(url);
    }

    synchronized public void deleteBook(int bookId) {
        for (int i = 0; i < storageSize; i++) {
            List<BookDto> value = storage.get(history.get(i)).getValue();

            for (int j = 0; j < value.size(); j++) {
                if (value.get(j).getId() == bookId) {
                    value.remove(j);

                    break;
                }
            }
        }
    }

    synchronized public void updateBook(int bookId, BookDto updatedBook) {
        for (int i = 0; i < storageSize; i++) {
            List<BookDto> value = storage.get(history.get(i)).getValue();

            for (BookDto bookDto : value) {
                if (bookDto.getId() == bookId) {
                    bookDto.setName(updatedBook.getName());
                    bookDto.setGenres(updatedBook.getGenres());

                    break;
                }
            }
        }
    }

    @Scheduled(fixedRate = 120000)
    public void printStorage() {
        logger.info("---------CURRENT CACHE STORAGE STATE--------------------");

        for (Map.Entry<String, CacheObject<List<BookDto>>> entry : storage.entrySet()) {
            logger.info("    {} : {} : {} : {}", entry.getKey(), entry.getValue().getCreationDate(), entry.getValue().getExpirationTime(), entry.getValue().getValue().size());
        }

        logger.info("--------------------------------------------------------");
    }

    @Scheduled(fixedRate = 120000)
    public void checkAndDeleteInvalidCaches() {
        logger.trace("CachingService method: checkAndDeleteInvalidCaches | STARTING CLEANING");

        int cleanedCaches = 0;

        CacheObject<List<BookDto>> temporalCacheObject;

        int ind = 0;
        while (ind < storageSize) {
            temporalCacheObject = storage.get(history.get(ind));

            if (new Date(temporalCacheObject.getCreationDate().getTime() + temporalCacheObject.getExpirationTime()).before(new Date())) {
                storage.remove(history.get(ind));
                history.remove(ind);

                storageSize--;
                ind--;
                cleanedCaches++;
            }

            ind++;
        }

        logger.trace("CachingService method: checkAndDeleteInvalidCaches | END CLEANING | cleaned {} caches", cleanedCaches);
    }

}
