package maksim.booksservice.utils;

import jakarta.ws.rs.BadRequestException;
import maksim.booksservice.config.AppConfig;
import maksim.booksservice.utils.validators.FileValidators;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FileValidatorsTest {
    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private FileValidators fileValidators;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void isNameAllowed_ValidName_ReturnsTrue() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "content".getBytes());
        assertTrue(fileValidators.isNameAllowed(file));
    }

    @Test
    void isNameAllowed_InvalidName_ReturnsFalse() {
        MultipartFile file = new MockMultipartFile("test../.txt", "test../.txt", "text/plain", "content".getBytes());
        assertFalse(fileValidators.isNameAllowed(file));
    }


    @Test
    void isPathAllowed_ValidPath_ReturnsTrue() {
        when(appConfig.getBookFilesDirectory()).thenReturn("/allowed/path/");
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "content".getBytes());
        assertTrue(fileValidators.isPathAllowed(file));
    }

    @Test
    void isPathAllowed_InvalidPath_ReturnsFalse() {
        when(appConfig.getBookFilesDirectory()).thenReturn("/allowed/path");
        MultipartFile file = new MockMultipartFile("test.txt", "../../test.txt", "text/plain", "content".getBytes());
        assertFalse(fileValidators.isPathAllowed(file));
    }



    @Test
    void isNotEmpty_NonEmptyFile_ReturnsTrue() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "content".getBytes());
        assertTrue(fileValidators.isNotEmpty(file));
    }

    @Test
    void isNotEmpty_EmptyFile_ReturnsFalse() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", new byte[0]);
        assertFalse(fileValidators.isNotEmpty(file));
    }



    @Test
    void isFileTypeAllowed_ValidType_ReturnsTrue() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "content".getBytes());
        assertTrue(fileValidators.isFileTypeAllowed(file));
    }

    @Test
    void isFileTypeAllowed_InvalidType_ReturnsFalse() {
        MultipartFile file = new MockMultipartFile("test.exe", "test.exe", "application/octet-stream", "content".getBytes());
        assertFalse(fileValidators.isFileTypeAllowed(file));
    }

    @Test
    void isFileTypeAllowed_NullFile_ReturnsFalse() {
        assertFalse(fileValidators.isFileTypeAllowed(null));
    }



    @Test
    void isFileExtensionAllowed_ValidExtension_ReturnsTrue() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "content".getBytes());
        assertTrue(fileValidators.isFileExtensionAllowed(file));
    }

    @Test
    void isFileExtensionAllowed_InvalidExtension_ReturnsFalse() {
        MultipartFile file = new MockMultipartFile("test.exe", "test.exe", "application/octet-stream", "content".getBytes());
        assertFalse(fileValidators.isFileExtensionAllowed(file));
    }

    @Test
    void isFileExtensionAllowed_NoExtension_ThrowsException() {
        MultipartFile file = new MockMultipartFile("test", "test", "text/plain", "content".getBytes());
        assertThrows(BadRequestException.class, () -> fileValidators.isFileExtensionAllowed(file));
    }



    @Test
    void isFileSizeValid_ValidSize_ReturnsTrue() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", new byte[5 * 1024 * 1024]); // 5 MB
        assertTrue(fileValidators.isFileSizeValid(file));
    }

    @Test
    void isFileSizeValid_InvalidSize_ReturnsFalse() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", new byte[15 * 1024 * 1024]); // 15 MB
        assertFalse(fileValidators.isFileSizeValid(file));
    }

    @Test
    void isFileSizeValid_NullFile_ReturnsFalse() {
        assertFalse(fileValidators.isFileSizeValid(null));
    }



    @Test
    void isValid_ValidFile_ReturnsTrue() {
        when(appConfig.getBookFilesDirectory()).thenReturn("/allowed/path/");
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "content".getBytes());
        assertTrue(fileValidators.isValid(file));
    }

    @Test
    void isValid_InvalidFile_ReturnsFalse() {
        when(appConfig.getBookFilesDirectory()).thenReturn("/allowed/path");
        MultipartFile file = new MockMultipartFile("test.exe", "test.exe", "application/octet-stream", new byte[15 * 1024 * 1024]);
        assertFalse(fileValidators.isValid(file));
    }
}