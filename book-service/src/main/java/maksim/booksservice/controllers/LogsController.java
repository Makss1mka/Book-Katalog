package maksim.booksservice.controllers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import maksim.booksservice.exceptions.BadRequestException;
import maksim.booksservice.services.LogsService;
import maksim.booksservice.utils.enums.LogRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/books/logs")
public class LogsController {
    private static final Logger logger = LoggerFactory.getLogger(LogsController.class);

    private final LogsService logsService;

    @Autowired
    public LogsController(LogsService logsService) {
        this.logsService = logsService;
    }

    @PostMapping("/request")
    public ResponseEntity<String> requestLogs(
        @RequestParam(name = "minDate")
        String strMinDate,

        @RequestParam(name = "maxDate")
        String strMaxDate
    ) {
        logger.trace("LogsController method entrance: requestLogs");

        DateTimeFormatter fullDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter shortDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime minDate;
        LocalDateTime maxDate;

        try {
            minDate  = LocalDateTime.parse(strMinDate, fullDateFormatter);
        } catch (Exception ex1) {
            try {
                minDate = LocalDate.parse(strMinDate, shortDateFormatter).atStartOfDay();
            } catch (Exception ex2) {
                throw new BadRequestException("Date format for min date is invalid. Accepted formats: 'yyyy-MM-dd HH:mm:ss' or 'yyyy-MM-dd'");
            }
        }

        try {
            maxDate = LocalDateTime.parse(strMaxDate, fullDateFormatter);
        } catch (Exception ex1) {
            try {
                maxDate = LocalDate.parse(strMaxDate, shortDateFormatter).atStartOfDay();
            } catch (Exception ex2) {
                throw new BadRequestException("Date format for max date is invalid. Accepted formats: 'yyyy-MM-dd HH:mm:ss' or 'yyyy-MM-dd'");
            }
        }

        if (minDate.isAfter(maxDate)) {
            throw new BadRequestException("Min date should be before max date");
        }

        String id = UUID.randomUUID().toString();
        logsService.requestLogsFile(id, minDate, maxDate);

        logger.trace("LogsController method end: requestLogs | File has successfully requested");

        return ResponseEntity.ok(id);
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<String> getStatus(
        @PathVariable(name = "id") String id
    ) {
        logger.trace("LogsController method entrance: getStatus");

        LogRequestStatus status = logsService.getStatus(id);

        logger.trace("LogsController method end: getStatus");

        return ResponseEntity.ok(status.toString());
    }

    @GetMapping("/file/{id}")
    public ResponseEntity<Resource> getLogFile(
        @PathVariable(name = "id") String id
    ) {
        logger.trace("LogsController method entrance: getLogFile");

        ByteArrayOutputStream logBuffer = logsService.getLogsFromFile(id);
        ByteArrayResource resource = new ByteArrayResource(logBuffer.toByteArray());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"logs_" + id + ".log\"");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(logBuffer.size()));

        logger.trace("LogsController method end: getLogFile");

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

}
