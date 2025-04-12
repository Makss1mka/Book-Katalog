package maksim.visitservice.services;

import jakarta.transaction.Transactional;
import maksim.kafkaclient.dtos.ListOfNewVisitsKafkaDto;
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
public class VisitEventsConsumer {
    private static final Logger logger = LoggerFactory.getLogger(VisitEventsConsumer.class);

    private final VisitService visitService;

    @Autowired
    public VisitEventsConsumer(VisitService visitService) {
        this.visitService = visitService;
    }

    @KafkaListener(topics = "new-visit", groupId = "visit-service")
    @Transactional
    public void listenNewVisits(
            ListOfNewVisitsKafkaDto newVisits,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment ack
    ) {
        logger.trace("Kafka VisitEventsConsumer method entrance: listenNewVisits | Get mes from topic {}", topic);

        try {
            visitService.addListOfVisits(newVisits);

            ack.acknowledge();
        } catch (Exception e) {
            ack.nack(Duration.ofSeconds(1));
        }

        logger.trace("Kafka VisitEventsConsumer method end: listenNewVisits | Visits added successfully");
    }


}

