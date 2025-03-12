package maksim.booksservice.utils.validators;

import maksim.booksservice.models.dtos.CreateBookDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookDtoForCreatingValidators {
    private final StringValidators stringValidators;

    @Autowired
    public BookDtoForCreatingValidators(StringValidators stringValidators) {
        this.stringValidators = stringValidators;
    }

    public CreateBookDto screenStringValue(CreateBookDto bookData) {
        bookData.setName(stringValidators.textScreening(bookData.getName()));

        bookData.getGenres().replaceAll(stringValidators::textScreening);

        return bookData;
    }

    public boolean isSafeFromSqlInjection(CreateBookDto bookData) {
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
