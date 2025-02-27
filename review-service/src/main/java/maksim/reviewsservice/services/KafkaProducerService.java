package maksim.reviewsservice.services;

import maksim.reviewsservice.models.Review;
import maksim.reviewsservice.models.kafkadtos.DtoForBookReviewChanging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, DtoForBookReviewChanging> kafkaTemplate;

    KafkaProducerService(KafkaTemplate<String, DtoForBookReviewChanging> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishReviewChanges(Review review, int action) {
        logger.trace("Kafka: publish message | Method enter: publishReviewChanges | Params: {} ; action {}", review, action);

        if (action != -1 && action != 0 && action != 1) {
            logger.trace("Kafka method: publishReviewChanges | Invalid action value");

            throw new IllegalArgumentException("Invalid action value, action can be -1, 0 or 1 for remove/change/add rate");
        }

        DtoForBookReviewChanging transferringData = new DtoForBookReviewChanging();

        transferringData.setBookId(review.getBookId());
        transferringData.setBookId(review.getRating());
        transferringData.setAction(action);

        kafkaTemplate.send("review-changes", transferringData);

        logger.trace("Kafka method return: publishReviewChanges | Result: message has published");
    }
}
