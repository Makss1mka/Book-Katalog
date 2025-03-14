package maksim.booksservice.utils.validators;

import maksim.booksservice.models.dtos.CreateBookDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateBookDtoValidator {
    private final StringValidator stringValidator;

    @Autowired
    public CreateBookDtoValidator(StringValidator stringValidator) {
        this.stringValidator = stringValidator;
    }

    public CreateBookDto screenStringValue(CreateBookDto bookData) {
        bookData.setName(stringValidator.textScreening(bookData.getName()));

        bookData.getGenres().replaceAll(stringValidator::textScreening);

        return bookData;
    }

    public boolean isSafeFromSqlInjection(CreateBookDto bookData) {
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
