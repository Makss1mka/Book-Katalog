package maksim.booksservice.utils;

import jakarta.ws.rs.BadRequestException;
import maksim.booksservice.config.AppConfig;
import maksim.booksservice.utils.validators.FileValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FileValidatorTest {
    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private FileValidator fileValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void isNameAllowed_ValidName_ReturnsTrue() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "content".getBytes());
        assertTrue(fileValidator.isNameAllowed(file));
    }

    @Test
    void isNameAllowed_InvalidName_ReturnsFalse() {
        MultipartFile file = new MockMultipartFile("test../.txt", "test../.txt", "text/plain", "content".getBytes());
        assertFalse(fileValidator.isNameAllowed(file));
    }


    @Test
    void isPathAllowed_ValidPath_ReturnsTrue() {
        when(appConfig.getBookFilesDirectory()).thenReturn("/allowed/path/");
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "content".getBytes());
        assertTrue(fileValidator.isPathAllowed(file));
    }

    @Test
    void isPathAllowed_InvalidPath_ReturnsFalse() {
        when(appConfig.getBookFilesDirectory()).thenReturn("/allowed/path");
        MultipartFile file = new MockMultipartFile("test.txt", "../../test.txt", "text/plain", "content".getBytes());
        assertFalse(fileValidator.isPathAllowed(file));
    }



    @Test
    void isNotEmpty_NonEmptyFile_ReturnsTrue() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "content".getBytes());
        assertTrue(fileValidator.isNotEmpty(file));
    }

    @Test
    void isNotEmpty_EmptyFile_ReturnsFalse() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", new byte[0]);
        assertFalse(fileValidator.isNotEmpty(file));
    }



    @Test
    void isFileTypeAllowed_ValidType_ReturnsTrue() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "content".getBytes());
        assertTrue(fileValidator.isFileTypeAllowed(file));
    }

    @Test
    void isFileTypeAllowed_InvalidType_ReturnsFalse() {
        MultipartFile file = new MockMultipartFile("test.exe", "test.exe", "application/octet-stream", "content".getBytes());
        assertFalse(fileValidator.isFileTypeAllowed(file));
    }

    @Test
    void isFileTypeAllowed_NullFile_ReturnsFalse() {
        assertFalse(fileValidator.isFileTypeAllowed(null));
    }



    @Test
    void isFileExtensionAllowed_ValidExtension_ReturnsTrue() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "content".getBytes());
        assertTrue(fileValidator.isFileExtensionAllowed(file));
    }

    @Test
    void isFileExtensionAllowed_InvalidExtension_ReturnsFalse() {
        MultipartFile file = new MockMultipartFile("test.exe", "test.exe", "application/octet-stream", "content".getBytes());
        assertFalse(fileValidator.isFileExtensionAllowed(file));
    }

    @Test
    void isFileExtensionAllowed_NoExtension_ThrowsException() {
        MultipartFile file = new MockMultipartFile("test", "test", "text/plain", "content".getBytes());
        assertThrows(BadRequestException.class, () -> fileValidator.isFileExtensionAllowed(file));
    }



    @Test
    void isFileSizeValid_ValidSize_ReturnsTrue() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", new byte[5 * 1024 * 1024]); // 5 MB
        assertTrue(fileValidator.isFileSizeValid(file));
    }

    @Test
    void isFileSizeValid_InvalidSize_ReturnsFalse() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", new byte[15 * 1024 * 1024]); // 15 MB
        assertFalse(fileValidator.isFileSizeValid(file));
    }

    @Test
    void isFileSizeValid_NullFile_ReturnsFalse() {
        assertFalse(fileValidator.isFileSizeValid(null));
    }



    @Test
    void isValid_ValidFile_ReturnsTrue() {
        when(appConfig.getBookFilesDirectory()).thenReturn("/allowed/path/");
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "content".getBytes());
        assertTrue(fileValidator.isValid(file));
    }

    @Test
    void isValid_InvalidFile_ReturnsFalse() {
        when(appConfig.getBookFilesDirectory()).thenReturn("/allowed/path");
        MultipartFile file = new MockMultipartFile("test.exe", "test.exe", "application/octet-stream", new byte[15 * 1024 * 1024]);
        assertFalse(fileValidator.isValid(file));
    }
}