package maksim.auth_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {
    @Value("${spring.application.secret}")
    private String secret;

    public String getSecret() {
        return secret;
    }
}
