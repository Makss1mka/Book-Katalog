package maksim.booksservice.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import maksim.booksservice.exceptions.AcceptedException;
import maksim.booksservice.exceptions.BadRequestException;
import maksim.booksservice.exceptions.FileException;
import maksim.booksservice.exceptions.NotFoundException;
import maksim.booksservice.utils.enums.LogRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class LogsService {
    private static final Logger logger = LoggerFactory.getLogger(LogsService.class);

    private static final String LOGS_DIR = "app/logs/";
    private static final String REQUESTED_LOGS_DIR = "app/requested_logs/";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Map<String, LogRequestStatus> requests = new ConcurrentHashMap<>();

    public LogRequestStatus getStatus(String id) {
        return requests.get(id);
    }

    @Async
    public void requestLogsFile(String id, LocalDateTime minDate, LocalDateTime maxDate) {
        logger.trace("LogsService method entrance: requestLogsFile");

        requests.put(id, LogRequestStatus.IN_PROCESS);

        Path outputFilePath = Paths.get(REQUESTED_LOGS_DIR, id + ".log");

        try {
            Thread.sleep(15000);
        } catch (InterruptedException ex) {
            ex.getMessage();
        }

        try {
            Files.createDirectories(outputFilePath.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(outputFilePath,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

                try (Stream<Path> filesStream = Files.walk(Paths.get(LOGS_DIR))) {
                    filesStream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".log"))
                        .forEach(path -> processLogFile(path, writer, minDate, maxDate));
                }
            }
        } catch (IOException ex) {
            throw new FileException("Error generating log file: " + ex.getMessage());
        }

        requests.put(id, LogRequestStatus.READY);

        logger.trace("LogsService method end: requestLogsFile");
    }

    public ByteArrayOutputStream getLogsFromFile(String id) {
        if (requests.get(id) == null) {
            throw new NotFoundException("Cannot find such request");
        }

        if (requests.get(id) == LogRequestStatus.IN_PROCESS) {
            throw new AcceptedException("File is not ready");
        }

        Path filePath = Paths.get(REQUESTED_LOGS_DIR, id + ".log");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (BufferedReader reader = Files.newBufferedReader(filePath);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }

            Files.delete(filePath);
            requests.remove(id);
        } catch (IOException ex) {
            throw new FileException("Error reading generated log file: " + ex.getMessage());
        }

        return outputStream;
    }

    private void processLogFile(Path filePath, BufferedWriter writer, LocalDateTime minDate, LocalDateTime maxDate) {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;

            while ((line = reader.readLine()) != null) {
                try {
                    parseLine(line, writer, minDate, maxDate);
                } catch (DateTimeParseException e) {
                    continue;
                }
            }
        } catch (IOException ex) {
            throw new FileException("Error reading the log file: " + filePath);
        }
    }

    private void parseLine(String line, BufferedWriter writer, LocalDateTime minDate, LocalDateTime maxDate) throws IOException {
        if (line.length() >= 19) {
            String dateStr = line.substring(0, 19);

            LocalDateTime logDate = LocalDateTime.parse(dateStr, dateTimeFormatter);

            if (!logDate.isBefore(minDate) && !logDate.isAfter(maxDate)) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}