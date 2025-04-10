package maksim.userservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class AppConfig {
    private final String kafkaPath = "spring.kafka.topics.";


    /*
        SERViCES PARAMS:
        urls
     */

    @Value("${spring.application.users-profiles-directory}")
    private String bookFilesDirectory;

    @Value("${spring.services.book-service-url}")
    private String bookServiceUrl;


    /*
        KAFKA PARAMETERS:
        topic's names
     */

    @Value("${" + kafkaPath + "status-update" + "}")
    private String kafkaStatusUpdateTopic;

    @Value("${" + kafkaPath + "status-create" + "}")
    private String kafkaStatusCreateTopic;

    @Value("${" + kafkaPath + "status-delete" + "}")
    private String kafkaStatusDeleteTopic;

    @Value("${" + kafkaPath + "like-create" + "}")
    private String kafkaLikeCreateTopic;

    @Value("${" + kafkaPath + "like-delete" + "}")
    private String kafkaLikeDeleteTopic;

}
