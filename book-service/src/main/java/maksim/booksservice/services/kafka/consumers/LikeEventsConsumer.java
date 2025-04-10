package maksim.booksservice.services.kafka.consumers;

import maksim.booksservice.exceptions.NotFoundException;
import maksim.booksservice.services.BookService;
import maksim.kafkaclient.dtos.CreateLikeKafkaDto;
import maksim.kafkaclient.dtos.DeleteLikeKafkaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LikeEventsConsumer {
    private static final Logger logger = LoggerFactory.getLogger(LikeEventsConsumer.class);

    private final BookService bookService;

    @Autowired
    public LikeEventsConsumer(BookService bookService) {
        this.bookService = bookService;
    }

    @KafkaListener(topics = "like-create", groupId = "book-service")
    public void listenLikeCreate(
        CreateLikeKafkaDto likeDto,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        Acknowledgment ack
    ) {
        logger.trace("Kafka LikeEventsConsumer method entrance: listenLikeCreate | Get mes from topic {}", topic);

        try {
            bookService.addLike(likeDto);

            ack.acknowledge();
        } catch (NotFoundException e) {
            logger.trace("Kafka LikeEventsConsumer method exception: listenLikeCreate | Exception: {}", e.getMessage());
            return;
        } catch (Exception e) {
            ack.nack(Duration.ofSeconds(1));
        }

        logger.trace("Kafka LikeEventsConsumer method end: listenLikeCreate | Like created successfully");
    }

    @KafkaListener(topics = "like-delete", groupId = "book-service")
    public void listenLikeDelete(
        DeleteLikeKafkaDto likeDto,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        Acknowledgment ack
    ) {
        logger.trace("Kafka LikeEventsConsumer method entrance: listenLikeDelete | Get mes from topic {}", topic);

        try {
            bookService.deleteLike(likeDto);

            ack.acknowledge();
        } catch (NotFoundException e) {
            logger.trace("Kafka LikeEventsConsumer method exception: listenLikeDelete | Exception: {}", e.getMessage());
            return;
        } catch (Exception e) {
            ack.nack(Duration.ofSeconds(1));
        }

        logger.trace("Kafka LikeEventsConsumer method end: listenLikeDelete | Like deleted successfully");
    }

}


