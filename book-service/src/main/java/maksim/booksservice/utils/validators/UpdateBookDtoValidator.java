package maksim.booksservice.utils.validators;

import maksim.booksservice.models.dtos.UpdateBookDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateBookDtoValidator {
    private final StringValidator stringValidator;

    @Autowired
    public UpdateBookDtoValidator(StringValidator stringValidator) {
        this.stringValidator = stringValidator;
    }

    public UpdateBookDto screenStringValue(UpdateBookDto bookData) {
        bookData.setName(stringValidator.textScreening(bookData.getName()));

        bookData.getGenres().replaceAll(stringValidator::textScreening);

        return bookData;
    }

    public boolean isSafeFromSqlInjection(UpdateBookDto bookData) {
        if (!stringValidator.isSafeFromSqlInjection(bookData.getName())) {
            return false;
        }

        for (String genre : bookData.getGenres()) {
            if (!stringValidator.isSafeFromSqlInjection(genre)) {
                return false;
            }
        }

        return true;
    }

}
