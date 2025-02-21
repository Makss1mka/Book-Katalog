package maksim.bookservice.utils.validators;

import maksim.bookservice.models.BookDtoForCreating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookDtoForCreatingValidators {
    private final StringValidators stringValidators;

    @Autowired
    public BookDtoForCreatingValidators(StringValidators stringValidators) {
        this.stringValidators = stringValidators;
    }

    public BookDtoForCreating screenStringValue(BookDtoForCreating bookData) {
        bookData.setName(stringValidators.textScreening(bookData.getName()));

        bookData.getGenres().replaceAll(stringValidators::textScreening);

        return bookData;
    }

    public boolean isSafeFromSqlInjection(BookDtoForCreating bookData) {
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
