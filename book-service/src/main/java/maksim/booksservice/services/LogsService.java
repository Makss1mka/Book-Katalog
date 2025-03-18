package maksim.booksservice.services;

import org.jvnet.hk2.annotations.Service;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class LogsService {
    private final String LOGS_DIR = "app/logs/";

    public ByteArrayOutputStream getLogs(Date minDate, Date maxDate) {
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

    private void processLogFile(Path filePath, BufferedWriter writer, Date minDate, Date maxDate) {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            while ((line = reader.readLine()) != null) {
                if (line.length() >= 19) {
                    String dateStr = line.substring(0, 19);
                    try {
                        Date logDate = dateFormat.parse(dateStr);

                        if (!logDate.before(minDate) && !logDate.after(maxDate)) {
                            writer.write(line);
                            writer.newLine();
                        }
                    } catch (ParseException e) {
                        continue;
                    }
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
