package maksim.booksservice.services;

import org.jvnet.hk2.annotations.Service;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

@Component
public class LogsService {
    private final String LOGS_DIR = "app/logs/";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ByteArrayOutputStream getLogs(LocalDateTime minDate, LocalDateTime maxDate) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            Files.walk(Paths.get(LOGS_DIR))
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".log"))
                .forEach(path -> processLogFile(path, writer, minDate, maxDate));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return outputStream;
    }

    private void processLogFile(Path filePath, BufferedWriter writer, LocalDateTime minDate, LocalDateTime maxDate) {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.length() >= 19) { // Длина строки, чтобы извлечь дату
                    String dateStr = line.substring(0, 19);
                    try {
                        LocalDateTime logDate = LocalDateTime.parse(dateStr, dateTimeFormatter);

                        if (!logDate.isBefore(minDate) && !logDate.isAfter(maxDate)) {
                            writer.write(line);
                            writer.newLine();
                        }
                    } catch (DateTimeParseException e) {
                        continue;
                    }
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error reading the log file: " + filePath, ex);
        }
    }

}
