package maksim.user_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {
    @Value("${spring.application.users-profiles-directory}")
    private String bookFilesDirectory;

    public String getBookFilesDirectory() {
        return bookFilesDirectory;
    }
}
