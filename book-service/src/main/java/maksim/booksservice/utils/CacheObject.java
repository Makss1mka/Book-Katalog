package maksim.booksservice.utils;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CacheObject<T> {
    private T value;
    private Date creationDate;
    private long expirationTime;

    public CacheObject(T value, Date creationDate, long expirationTime) {
        this.value = value;
        this.creationDate = creationDate;
        this.expirationTime = expirationTime;
    }
}

