package maksim.booksservice.controllers;

import jakarta.ws.rs.BadRequestException;
import maksim.booksservice.services.LogsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping(value = "/api/v1/logs")
public class LogsController {
    private final static Logger logger = LoggerFactory.getLogger(LogsController.class);

    private final LogsService logsService;

    @Autowired
    public LogsController(LogsService logsService) {
        this.logsService = logsService;
    }

    @GetMapping
    public ResponseEntity<InputStreamResource> getLogs(
            @RequestParam(name = "minDate") String strMinDate,
            @RequestParam(name = "maxDate") String strMaxDate
    ) {
        logger.trace("LogsController method entrance: getLogs");

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date minDate;
        Date maxDate;

        try {
            minDate = formatter.parse(strMinDate);
            maxDate = formatter.parse(strMaxDate);
        } catch (ParseException e) {
            throw new BadRequestException("Date format is invalid. Date should be in format 'yyyy-MM-dd HH:mm:ss'");
        }

        ByteArrayOutputStream outputStream = logsService.getLogs(minDate, maxDate);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        InputStreamResource resource = new InputStreamResource(inputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=filtered_logs.log");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);

        logger.trace("LogsController method end: getLogs | File has successfully created");

        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(outputStream.size())
            .body(resource);
    }

}
