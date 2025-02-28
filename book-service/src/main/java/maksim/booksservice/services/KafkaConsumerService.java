package maksim.booksservice.services;

import maksim.booksservice.models.kafkadtos.DtoForBookReviewChanging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

@Configuration
public class KafkaConsumerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final BookService bookService;

    @Autowired
    KafkaConsumerService(BookService bookService) {
        this.bookService = bookService;
    }

    @KafkaListener(topics = "review-changes", groupId = "book-service")
    public void listenReviewChanges(DtoForBookReviewChanging receivedReviewData) {
        logger.trace("Kafka: receive messaged | Method enter: listenReviewChanges | Message: {}", receivedReviewData);

        bookService.changeOneRate(receivedReviewData);

        logger.trace("Kafka method end: listenReviewChanges");
    }

}
