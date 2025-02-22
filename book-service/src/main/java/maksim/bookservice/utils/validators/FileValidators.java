package maksim.bookservice.utils.validators;

import java.util.Arrays;
import java.util.List;

import jakarta.ws.rs.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileValidators {
    private static final String[] DANGEROUS_PATTERNS = {"../", "./", "'", "\"", ";", "--", "/*", "*/", "xp_", "exec"};

    public boolean isValid(MultipartFile file) {
        return file != null && isNameAllowed(file) && isNotEmpty(file) && isFileTypeAllowed(file) && isFileExtensionAllowed(file) && isFileSizeValid(file);
    }

    public boolean isNameAllowed(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) return false;

        for (String pattern : DANGEROUS_PATTERNS) {
            if (file.getOriginalFilename().contains(pattern)) {
                return false;
            }
        }

        return true;
    }

    public boolean isNotEmpty(MultipartFile file) {
        return !file.isEmpty();
    }

    public boolean isFileTypeAllowed(MultipartFile file) {
        if (file == null) return false;

        List<String> allowedMimeTypes = Arrays.asList(
                "application/pdf",
                "text/markdown",
                "text/plain"
        );

        return allowedMimeTypes.contains(file.getContentType());
    }

    public boolean isFileExtensionAllowed(MultipartFile file) {
        List<String> allowedExtensions = Arrays.asList("txt", "pdf", "md");

        if (file == null || file.getOriginalFilename() == null) return false;

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
        if (file == null) return false;

        long maxFileSize = 2 * 1024 * 1024L; // 2 MB
        return file.getSize() <= maxFileSize;
    }

}
