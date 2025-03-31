package maksim.booksservice.controllers;

import maksim.booksservice.exceptions.BadRequestException;
import maksim.booksservice.services.LogsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class LogsControllerTest {
    @Mock
    private LogsService logsService;

    @InjectMocks
    private LogsController logsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getLogs_SuccessWithFullDateTimeFormat() throws Exception {
        String minDate = "2023-01-01 00:00:00";
        String maxDate = "2023-01-31 23:59:59";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write("test logs".getBytes());

        when(logsService.getLogs(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(outputStream);

        ResponseEntity<InputStreamResource> response = logsController.getLogs(minDate, maxDate);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());

        HttpHeaders headers = response.getHeaders();
        assertEquals("attachment; filename=filtered_logs.log",
                headers.getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals("text/plain", headers.getFirst(HttpHeaders.CONTENT_TYPE));
        assertEquals(outputStream.size(), headers.getContentLength());
    }

    @Test
    void getLogs_SuccessWithShortDateFormat() throws Exception {
        String minDate = "2023-01-01";
        String maxDate = "2023-01-31";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write("test logs".getBytes());

        when(logsService.getLogs(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(outputStream);

        ResponseEntity<InputStreamResource> response = logsController.getLogs(minDate, maxDate);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void getLogs_InvalidMinDateFormat() {
        String invalidMinDate = "01-01-2023";
        String maxDate = "2023-01-31";

        assertThrows(BadRequestException.class, () -> {
            logsController.getLogs(invalidMinDate, maxDate);
        });
    }

    @Test
    void getLogs_InvalidMaxDateFormat() {
        String minDate = "2023-01-01";
        String invalidMaxDate = "31-01-2023";

        assertThrows(BadRequestException.class, () -> {
            logsController.getLogs(minDate, invalidMaxDate);
        });
    }

    @Test
    void getLogs_MinDateAfterMaxDate() {
        String minDate = "2023-01-31";
        String maxDate = "2023-01-01";

        assertThrows(BadRequestException.class, () -> {
            logsController.getLogs(minDate, maxDate);
        });
    }

    @Test
    void getLogs_EmptyDates() {
        assertThrows(BadRequestException.class, () -> {
            logsController.getLogs(null, null);
        });
    }
}