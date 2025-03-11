package maksim.userservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NoContentException extends ResponseStatusException {
    public NoContentException(String message) {
        super(HttpStatus.NO_CONTENT, message);
    }
}
