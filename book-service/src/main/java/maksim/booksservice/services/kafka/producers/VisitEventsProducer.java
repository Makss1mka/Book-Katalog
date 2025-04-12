package maksim.booksservice.services.kafka.producers;

import maksim.booksservice.config.AppConfig;
import maksim.kafkaclient.dtos.ListOfNewVisitsKafkaDto;
import maksim.kafkaclient.dtos.VisitKafkaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class VisitEventsProducer {
    private static final Logger logger = LoggerFactory.getLogger(VisitEventsProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplateJsonObjectValue;
    private final AppConfig appConfig;
    private final CopyOnWriteArrayList<VisitKafkaDto> visits;
    private final AtomicLong count;
    private final Long uploadVisitsSize;

    @Autowired
    public VisitEventsProducer(
            KafkaTemplate<String, Object> kafkaTemplateJsonObjectValue,
            AppConfig appConfig
    ) {
        this.kafkaTemplateJsonObjectValue = kafkaTemplateJsonObjectValue;
        this.appConfig = appConfig;

        visits = new CopyOnWriteArrayList<>();
        count = new AtomicLong(0L);
        uploadVisitsSize = appConfig.getVisitsUploadSize();
    }

    synchronized public void addAndPublishVisit(String serviceName, String methodName) {
        logger.trace("KafkaProducer: method enter: addAndPublishVisit");
        logger.info("KafkaProducer: method: addAndPublishVisit | Current list size {}", count.get() + 1);

        boolean isFound = false;

        for (VisitKafkaDto visit : visits) {
            if (visit.getMethod().equals(methodName)) {
                visit.setCount(visit.getCount() + 1L);
                isFound = true;
            }
        }

        if (!isFound) {
            visits.add(new VisitKafkaDto(methodName, serviceName, 1L));
        }

        long updatedCount = count.incrementAndGet();

        if (updatedCount >= uploadVisitsSize) {
            kafkaTemplateJsonObjectValue.send(appConfig.getKafkaNewVisitTopic(), new ListOfNewVisitsKafkaDto(visits));
            visits.clear();
            count.set(0L);

            logger.info("KafkaProducer: method: addAndPublishVisit | Message was published");
        } else {
            logger.trace("KafkaProducer: method end: addAndPublishVisit | Visit was added");
        }
    }
}
