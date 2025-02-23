package maksim.bookservice.utils.validators;

import jakarta.ws.rs.BadRequestException;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import maksim.bookservice.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileValidators {
    private final AppConfig appConfig;
    private static final String[] DANGEROUS_PATTERNS = {"../", "./", "'", "\"", ";", "--", "/*", "*/", "xp_", "exec"};

    @Autowired
    FileValidators(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public boolean isValid(MultipartFile file) {
        return
                file != null
                && isNotEmpty(file)
                && isNameAllowed(file)
                && isPathAllowed(file)
                && isFileTypeAllowed(file)
                && isFileExtensionAllowed(file)
                && isFileSizeValid(file);
    }

    public boolean isNameAllowed(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            return false;
        }

        for (String pattern : DANGEROUS_PATTERNS) {
            if (file.getOriginalFilename() == null || file.getOriginalFilename().contains(pattern)) {
                return false;
            }
        }

        return true;
    }

    public boolean isPathAllowed(MultipartFile file) {
        Path targetPath = new File(appConfig.getBookFilesDirectory()).toPath().normalize();
        File targetFile = new File(appConfig.getBookFilesDirectory() + file.getOriginalFilename());

        return targetFile.toPath().normalize().startsWith(targetPath);
    }

    public boolean isNotEmpty(MultipartFile file) {
        return !file.isEmpty();
    }

    public boolean isFileTypeAllowed(MultipartFile file) {
        if (file == null) {
            return false;
        }

        List<String> allowedMimeTypes = Arrays.asList(
                "application/pdf",
                "text/markdown",
                "text/plain"
        );

        return allowedMimeTypes.contains(file.getContentType());
    }

    public boolean isFileExtensionAllowed(MultipartFile file) {
        List<String> allowedExtensions = Arrays.asList("txt", "pdf", "md");

        if (file == null || file.getOriginalFilename() == null) {
            return false;
        }

        String fileName = file.getOriginalFilename();

        if (fileName.lastIndexOf(".") == -1) {
            throw new BadRequestException("File doesn't contains extension");
        }

        String fileExtension = fileName
                .substring(fileName.lastIndexOf(".") + 1)
                .toLowerCase();

        return allowedExtensions.contains(fileExtension);
    }

    public boolean isFileSizeValid(MultipartFile file) {
        if (file == null) {
            return false;
        }

        long maxFileSize = 10 * 1024 * 1024L; // 20 MB
        return file.getSize() <= maxFileSize;
    }

}
