package maksim.userservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class AppConfig {
    @Value("${spring.application.users-profiles-directory}")
    private String bookFilesDirectory;

    @Value("${spring.services.book-service-url}")
    private String bookServiceUrl;

}
