package maksim.booksservice.services.kafka.consumers;

import maksim.booksservice.exceptions.NotFoundException;
import maksim.booksservice.services.BookService;
import maksim.kafkaclient.dtos.CreateStatusKafkaDto;
import maksim.kafkaclient.dtos.UpdateStatusKafkaDto;
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
public class StatusEventsConsumer {
    private static final Logger logger = LoggerFactory.getLogger(LikeEventsConsumer.class);

    private final BookService bookService;

    @Autowired
    public StatusEventsConsumer(BookService bookService) {
        this.bookService = bookService;
    }

    @KafkaListener(topics = "status-create", groupId = "book-service")
    public void listenStatusCreate(
            CreateStatusKafkaDto statusDto,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment ack
    ) {
        logger.trace("Kafka LikeEventsConsumer method entrance: listenStatusCreate | Get mes from topic {}", topic);

        try {
            bookService.createStatus(statusDto);

            ack.acknowledge();
        } catch (NotFoundException e) {
            logger.trace("Kafka LikeEventsConsumer method exception: listenStatusCreate | Exception: {}", e.getMessage());
            return;
        } catch (Exception e) {
            ack.nack(Duration.ofSeconds(1));
        }

        logger.trace("Kafka LikeEventsConsumer method end: listenStatusCreate | Status created successfully");
    }

    @KafkaListener(topics = "status-update", groupId = "book-service")
    public void listenStatusUpdate(
            UpdateStatusKafkaDto statusDto,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment ack
    ) {
        logger.trace("Kafka LikeEventsConsumer method entrance: listenStatusUpdate | Get mes from topic {}", topic);

        try {
            bookService.updateStatus(statusDto);

            ack.acknowledge();
        } catch (NotFoundException e) {
            logger.trace("Kafka LikeEventsConsumer method exception: listenStatusUpdate | Exception: {}", e.getMessage());
            return;
        } catch (Exception e) {
            ack.nack(Duration.ofSeconds(1));
        }

        logger.trace("Kafka LikeEventsConsumer method end: listenStatusUpdate | Like deleted successfully");
    }


}


