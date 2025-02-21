package maksim.bookservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {
    @Value("${spring.application.book-files-directory}")
    private String bookFilesDirectory;

    public String getBookFilesDirectory() {
        return bookFilesDirectory;
    }
}
