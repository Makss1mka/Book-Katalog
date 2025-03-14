package maksim.booksservice.utils.validators;

import java.util.ArrayList;
import java.util.List;
import maksim.booksservice.utils.bookutils.BookSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookSearchCriteriaValidator {
    private final StringValidator stringValidator;

    @Autowired
    public BookSearchCriteriaValidator(StringValidator stringValidator) {
        this.stringValidator = stringValidator;
    }

    public BookSearchCriteria screenStringValues(BookSearchCriteria criteria) {
        if (criteria.getName() != null) {
            criteria.setName(
                stringValidator.textScreening(criteria.getName())
            );
        }

        if (criteria.getAuthorName() != null) {
            criteria.setAuthorName(
                stringValidator.textScreening(criteria.getAuthorName())
            );
        }

        if (criteria.getGenres() != null) {
            List<String> screenedGenres = new ArrayList<>(criteria.getGenres().size());

            for (int i = 0; i < criteria.getGenres().size(); i++) {
                screenedGenres.add(stringValidator.textScreening(criteria.getGenres().get(i)));
            }

            criteria.setGenres(screenedGenres);
        }

        return criteria;
    }

    public boolean isSafeFromSqlInjection(BookSearchCriteria criteria) {
        if (criteria.getName() != null && !stringValidator.isSafeFromSqlInjection(criteria.getName())) {
            return false;
        }

        if (criteria.getAuthorName() != null && !stringValidator.isSafeFromSqlInjection(criteria.getAuthorName())) {
            return  false;
        }

        if (criteria.getGenres() != null) {
            for (String genre : criteria.getGenres()) {
                if (!stringValidator.isSafeFromSqlInjection(genre)) {
                    return false;
                }
            }
        }

        return true;
    }

}
