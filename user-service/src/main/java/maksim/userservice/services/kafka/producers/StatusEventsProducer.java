package maksim.userservice.services.kafka.producers;

import maksim.kafkaclient.dtos.CreateStatusKafkaDto;
import maksim.kafkaclient.dtos.DeleteStatusKafkaDto;
import maksim.kafkaclient.dtos.UpdateStatusKafkaDto;
import maksim.userservice.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class StatusEventsProducer {
    private static final Logger logger = LoggerFactory.getLogger(StatusEventsProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplateJsonObjectValue;
    private final AppConfig appConfig;

    @Autowired
    StatusEventsProducer(
        KafkaTemplate<String, Object> kafkaTemplateJsonObjectValue,
        AppConfig appConfig
    ) {
        this.kafkaTemplateJsonObjectValue = kafkaTemplateJsonObjectValue;
        this.appConfig = appConfig;
    }

    public void publishStatusUpdate(UpdateStatusKafkaDto dto) {
        logger.trace("KafkaProducer: method enter: publishStatusUpdate");

        kafkaTemplateJsonObjectValue.send(appConfig.getKafkaStatusUpdateTopic(), dto);

        logger.trace("KafkaProducer: method end: publishStatusUpdate | Message was published");
    }

    public void publishStatusCreate(CreateStatusKafkaDto dto) {
        logger.trace("KafkaProducer: method enter: publishStatusCreate");

        kafkaTemplateJsonObjectValue.send(appConfig.getKafkaStatusCreateTopic(), dto);

        logger.trace("KafkaProducer: method end: publishStatusCreate | Message was published");
    }

    public void publishStatusDelete(DeleteStatusKafkaDto dto) {
        logger.trace("KafkaProducer: method enter: publishStatusDelete");

        kafkaTemplateJsonObjectValue.send(appConfig.getKafkaStatusDeleteTopic(), dto);

        logger.trace("KafkaProducer: method end: publishStatusDelete | Message was published");
    }

}
