package maksim.userservice.services.kafka.producers;

import maksim.kafkaclient.dtos.CreateLikeKafkaDto;
import maksim.kafkaclient.dtos.DeleteLikeKafkaDto;
import maksim.userservice.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeEventsProducer {
    private static final Logger logger = LoggerFactory.getLogger(LikeEventsProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplateJsonObjectValue;
    private final AppConfig appConfig;

    @Autowired
    LikeEventsProducer(
        KafkaTemplate<String, Object> kafkaTemplateJsonObjectValue,
        AppConfig appConfig
    ) {
        this.kafkaTemplateJsonObjectValue = kafkaTemplateJsonObjectValue;
        this.appConfig = appConfig;
    }

    public void publishLikeCreate(CreateLikeKafkaDto dto) {
        logger.trace("KafkaProducer: method enter: publishLikeCreate");

        kafkaTemplateJsonObjectValue.send(appConfig.getKafkaLikeCreateTopic(), dto);

        logger.trace("KafkaProducer: method end: publishLikeCreate | Message was published");
    }

    public void publishLikeDelete(DeleteLikeKafkaDto dto) {
        logger.trace("KafkaProducer: method enter: publishLikeDelete");

        kafkaTemplateJsonObjectValue.send(appConfig.getKafkaLikeDeleteTopic(), dto);

        logger.trace("KafkaProducer: method end: publishLikeDelete | Message was published");
    }

}
