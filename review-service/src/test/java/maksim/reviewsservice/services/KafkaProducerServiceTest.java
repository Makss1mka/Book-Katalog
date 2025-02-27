package maksim.reviewsservice.services;

import maksim.reviewsservice.models.Review;
import maksim.reviewsservice.models.kafkadtos.DtoForBookReviewChanging;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KafkaProducerServiceTest {
    @Mock
    private KafkaTemplate<String, DtoForBookReviewChanging> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void publishReviewChanges_Success() {
        Review review = new Review();

        doNothing().when(kafkaTemplate).send(any(), any());

        kafkaProducerService.publishReviewChanges(review, 1);

        verify(kafkaTemplate, times(1)).send(any(), any());
    }

    @Test
    void publishReviewChanges_InvalidAction() {
        Review review = new Review();

        doNothing().when(kafkaTemplate).send(any(), any());

        assertThrows(RuntimeException.class, () -> {
            kafkaProducerService.publishReviewChanges(review, 200);
        });
    }

}
