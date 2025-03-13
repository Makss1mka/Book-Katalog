package maksim.booksservice.services;

import maksim.booksservice.models.dtos.BookDto;
import maksim.booksservice.utils.CacheObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CachingService {
    private static final Logger logger = LoggerFactory.getLogger(CachingService.class);

    private final int STORAGE_MAX_SIZE = 10;

    private final Map<String, CacheObject<List<BookDto>>> storage = new HashMap<String, CacheObject<List<BookDto>>>();

    private final List<String> history = new ArrayList<>(STORAGE_MAX_SIZE);
    private int oldestCacheIHistoryInd = 0;
    private int storageSize = 0;

    public List<BookDto> checkAndGetFromCache(String url) {
        logger.trace("CachingService method: checkAndGetFromCache");

        if (new Date(storage.get(url).getCreationDate().getTime() + storage.get(url).getExpirationTime()).before(new Date())) {
            storage.remove(url);



            history.set(oldestCacheIHistoryInd, url);
            oldestCacheIHistoryInd = (oldestCacheIHistoryInd + 1) % STORAGE_MAX_SIZE;
        }

        return null;
    }

    public void addToCache(String url, List<BookDto> dtos) {
        if (storageSize >= STORAGE_MAX_SIZE) {
            storage.remove(history.get(oldestCacheIHistoryInd));

            history.set(oldestCacheIHistoryInd, url);
            oldestCacheIHistoryInd = (oldestCacheIHistoryInd + 1) % STORAGE_MAX_SIZE;
        } else {
            storageSize++;

            history.set(storageSize - 1, url);
        }

        CacheObject<List<BookDto>> cacheObject = new CacheObject<List<BookDto>>(dtos, new Date(), 60000);

        storage.put(url, cacheObject);
    }

    @Scheduled(fixedRate = 60000)
    public void checkAndDeleteInvalidCache() {

    }

}
