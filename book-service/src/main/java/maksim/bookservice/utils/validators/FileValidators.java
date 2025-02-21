package maksim.bookservice.utils.validators;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileValidators {

    public boolean isValid(MultipartFile file) {
        return file != null && isNotEmpty(file) && isFileTypeAllowed(file) && isFileExtensionAllowed(file) && isFileSizeValid(file);
    }

    public boolean isNotEmpty(MultipartFile file) {
        return !file.isEmpty();
    }

    public boolean isFileTypeAllowed(MultipartFile file) {
        List<String> allowedMimeTypes = Arrays.asList(
                "application/pdf",
                "text/markdown",
                "text/plain"
        );

        return allowedMimeTypes.contains(file.getContentType());
    }

    public boolean isFileExtensionAllowed(MultipartFile file) {
        List<String> allowedExtensions = Arrays.asList("txt", "pdf", "md");

        String fileName = file.getOriginalFilename();
        String fileExtension = fileName
                .substring(fileName.lastIndexOf(".") + 1)
                .toLowerCase();

        return allowedExtensions.contains(fileExtension);
    }

    public boolean isFileSizeValid(MultipartFile file) {
        long maxFileSize = 2 * 1024 * 1024; // 2 MB
        return file.getSize() <= maxFileSize;
    }

}
