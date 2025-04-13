package maksim.authservice.config;

import jakarta.validation.ConstraintViolationException;
import maksim.authservice.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.trace("Method argument validation error {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(field ->
            errors.put(field.getField(), field.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler({
        ConstraintViolationException.class,
        BadRequestException.class,
        IllegalArgumentException.class
    })
    public ResponseEntity<String> handleBadRequest(Exception ex) {
        logger.trace(ex.getMessage());

        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler({
        NotFoundException.class,
        HttpRequestMethodNotSupportedException.class,
        NoHandlerFoundException.class
    })
    public ResponseEntity<String> handleNotFound(Exception ex) {
        logger.trace(ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<String> handleConflictExceptions(ConflictException ex) {
        logger.trace(ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleAcceptedExceptions(UnauthorizedException ex) {
        logger.error(ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }


    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<String> handleFprbiddenExceptions(ForbiddenException ex) {
        logger.trace(ex.getMessage());

        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception ex) {
        logger.trace(ex.getMessage());

        return ResponseEntity.internalServerError().body("Something goes wrong, Sorry my bad :(");
    }

}
