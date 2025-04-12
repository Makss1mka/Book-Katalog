package maksim.reviewsservice.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AppConfig {
    private final String kafkaPath = "spring.kafka.topics.";


    /*
        KAFKA PARAMETERS:
        topic's names
     */

    @Value("${" + kafkaPath + "new-visit" + "}")
    private String kafkaNewVisitTopic;

}
