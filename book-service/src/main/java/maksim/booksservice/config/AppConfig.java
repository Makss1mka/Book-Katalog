package maksim.booksservice.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AppConfig {
    private final String kafkaPath = "spring.kafka.topics.";


    @Value("${spring.application.book-files-directory}")
    private String bookFilesDirectory;


    /*
        SERViCES PARAMS:
        urls
     */

    @Value("${spring.services.user-service-url}")
    private String userServiceUrl;


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
