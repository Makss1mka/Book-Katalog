package maksim.booksservice.services;

import maksim.booksservice.exceptions.FileException;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;

@Component
public class LogsService {
    private final String logsDir = "app/logs/";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ByteArrayOutputStream getLogs(LocalDateTime minDate, LocalDateTime maxDate) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            try (Stream<Path> filesStream = Files.walk(Paths.get(logsDir))) {
                filesStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".log"))
                    .forEach(path -> processLogFile(path, writer, minDate, maxDate));
            }
        } catch (IOException ex) {
            throw new FileException(ex.getMessage());
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
