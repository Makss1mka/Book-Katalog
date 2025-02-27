package maksim.reviewsservice.services;

import maksim.reviewsservice.models.Review;
import maksim.reviewsservice.models.kafkadtos.DtoForBookReviewChanging;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KafkaProducerServiceTest {
    @Mock
    private KafkaTemplate<String, DtoForBookReviewChanging> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    private Review review;

    @BeforeEach
    void setUp() {
        this.review = new Review();
        review.setBookId(1);
        review.setRating(1);

        MockitoAnnotations.openMocks(this);
    }

    @Test
    void publishReviewChanges_Success() {
        when(kafkaTemplate.send(any(), any())).thenReturn(new CompletableFuture<>());

        kafkaProducerService.publishReviewChanges(this.review, 1);

        verify(kafkaTemplate, times(1)).send(any(), any());
    }

    @Test
    void publishReviewChanges_InvalidAction() {
        assertThrows(IllegalArgumentException.class, () -> {
            kafkaProducerService.publishReviewChanges(this.review, 200);
        });
    }

}
