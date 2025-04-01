package maksim.booksservice.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AppConfig {
    @Value("${spring.application.book-files-directory}")
    private String bookFilesDirectory;

    @Value("${spring.services.user-service-url}")
    private String userServiceUrl;
}
