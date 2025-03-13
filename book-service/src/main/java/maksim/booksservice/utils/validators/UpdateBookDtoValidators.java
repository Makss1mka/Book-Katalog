package maksim.booksservice.utils.validators;

import maksim.booksservice.models.dtos.CreateBookDto;
import maksim.booksservice.models.dtos.UpdateBookDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateBookDtoValidators {
    private final StringValidators stringValidators;

    @Autowired
    public UpdateBookDtoValidators(StringValidators stringValidators) {
        this.stringValidators = stringValidators;
    }

    public UpdateBookDto screenStringValue(UpdateBookDto bookData) {
        bookData.setName(stringValidators.textScreening(bookData.getName()));

        bookData.getGenres().replaceAll(stringValidators::textScreening);

        return bookData;
    }

    public boolean isSafeFromSqlInjection(UpdateBookDto bookData) {
        if (!stringValidators.isSafeFromSqlInjection(bookData.getName())) {
            return false;
        }

        for (String genre : bookData.getGenres()) {
            if (!stringValidators.isSafeFromSqlInjection(genre)) {
                return false;
            }
        }

        return true;
    }

}
