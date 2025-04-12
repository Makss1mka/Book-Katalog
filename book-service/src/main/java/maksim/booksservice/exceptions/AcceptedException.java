package maksim.booksservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AcceptedException extends ResponseStatusException {
    public AcceptedException(String message) {
        super(HttpStatus.ACCEPTED, message);
    }
}
