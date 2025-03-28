package maksim.booksservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import maksim.booksservice.exceptions.BadRequestException;
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

@RestController
@RequestMapping(value = "/api/v1/logs")
public class LogsController {
    private static final Logger logger = LoggerFactory.getLogger(LogsController.class);

    private final LogsService logsService;

    @Autowired
    public LogsController(LogsService logsService) {
        this.logsService = logsService;
    }

    @GetMapping
    @Operation(
        summary = "Get all logs by some period",
        description = "Get all logs by some inputted dates period"
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Get logs",
                content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(example = "Some log")
                )
            ),
        @ApiResponse(
                responseCode = "400",
                description = "Bad request (validation failed)",
                content = @Content(
                    mediaType = "plain/text",
                    schema = @Schema(example = "Invalid date format")
                )
            ),
        @ApiResponse(
                responseCode = "500",
                description = "Server error",
                content = @Content(
                    mediaType = "plain/text",
                    schema = @Schema(example = "Something goes wrong, Sorry my bad :(")
                )
            )
    })
    public ResponseEntity<InputStreamResource> getLogs(
        @Parameter(
            description = "(type: String in format 'yyyy-MM-dd') - min date edge from which logs will be found",
            required = false
        )
        @RequestParam(name = "minDate")
        String strMinDate,

        @Parameter(
            description = "(type: String in format 'yyyy-MM-dd') - max date edge till which logs will be found",
            required = false
        )
        @RequestParam(name = "maxDate")
        String strMaxDate
    ) {
        logger.trace("LogsController method entrance: getLogs");

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

        ByteArrayOutputStream outputStream = logsService.getLogs(minDate, maxDate);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=filtered_logs.log");
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);

        InputStreamResource resource = new InputStreamResource(inputStream);

        logger.trace("LogsController method end: getLogs | File has successfully created");

        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(outputStream.size())
            .body(resource);
    }

}
