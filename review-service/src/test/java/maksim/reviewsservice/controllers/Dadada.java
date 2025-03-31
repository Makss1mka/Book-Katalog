package maksim.reviewsservice.controllers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import maksim.reviewsservice.models.dtos.*;
import maksim.reviewsservice.services.ReviewService;
import maksim.reviewsservice.utils.Pagination;
import maksim.reviewsservice.utils.validators.StringValidators;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;

class Dadada {

    @Mock
    private ReviewService reviewService;

    @Mock
    private Pagination pagination;

    @Mock
    private StringValidators stringValidators;

    @InjectMocks
    private ReviewController reviewController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
    }

    @Test
    void getReviewById_Success() throws Exception {
        when(reviewService.getById(anyInt(), any())).thenReturn(new ReviewDto());

        mockMvc.perform(get("/api/v1/reviews/1")
                        .param("linkMode", "without"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void getReviewsByUserOrBookId_Success() throws Exception {
        when(reviewService.getAllByBookOrUserId(anyInt(), any(), any(), any())).thenReturn(List.of(new ReviewDto()));
        when(pagination.getPageable(anyInt(), anyInt(), any(), any())).thenReturn(Pageable.unpaged());

        mockMvc.perform(get("/api/v1/reviews")
                        .param("id", "1")
                        .param("criteria", "userId")
                        .param("joinMode", "without")
                        .param("pageNum", "0")
                        .param("pageSize", "20")
                        .param("sortField", "rating")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    void addReview_Success() throws Exception {
        when(reviewService.addReview(any())).thenReturn(new ReviewDto());
        when(stringValidators.textScreening(any())).thenReturn("safe text");
        when(stringValidators.isSafeFromSqlInjection(any())).thenReturn(true);

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"test review\",\"userId\":1,\"bookId\":1,\"rating\":5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void addReview_InvalidText() throws Exception {
        when(stringValidators.textScreening(any())).thenReturn("safe text");
        when(stringValidators.isSafeFromSqlInjection(any())).thenReturn(false);

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"invalid text\",\"userId\":1,\"bookId\":1,\"rating\":5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addLike_Success() throws Exception {
        doNothing().when(reviewService).addLike(any());

        mockMvc.perform(post("/api/v1/reviews/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"reviewId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(content().string("Like was added successfully"));
    }

    @Test
    void deleteReview_Success() throws Exception {
        doNothing().when(reviewService).deleteReview(anyInt());

        mockMvc.perform(delete("/api/v1/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Review was successfully deleted"));
    }

    @Test
    void deleteLike_Success() throws Exception {
        doNothing().when(reviewService).deleteLike(anyInt(), anyInt());

        mockMvc.perform(delete("/api/v1/reviews/like")
                        .param("fromUser", "1")
                        .param("toReview", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Like to review was successfully deleted"));
    }

    @Test
    void updateReview_Success() throws Exception {
        ReviewDto reviewDto = new ReviewDto();
        when(reviewService.updateReview(anyInt(), any())).thenReturn(reviewDto);
        when(stringValidators.textScreening(any())).thenReturn("safe text");
        when(stringValidators.isSafeFromSqlInjection(any())).thenReturn(true);

        mockMvc.perform(patch("/api/v1/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"updated review\",\"rating\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void updateReview_InvalidText() throws Exception {
        when(stringValidators.textScreening(any())).thenReturn("safe text");
        when(stringValidators.isSafeFromSqlInjection(any())).thenReturn(false);

        mockMvc.perform(patch("/api/v1/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"invalid text\",\"rating\":4}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addReview_InvalidRating() throws Exception {
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"test\",\"userId\":1,\"bookId\":1,\"rating\":6}"))
                .andExpect(status().isBadRequest());
    }
}