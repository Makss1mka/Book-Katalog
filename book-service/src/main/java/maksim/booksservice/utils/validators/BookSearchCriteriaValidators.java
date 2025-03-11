package maksim.booksservice.utils.validators;

import java.util.ArrayList;
import java.util.List;
import maksim.booksservice.utils.bookutils.BookSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookSearchCriteriaValidators {
    private final StringValidators stringValidators;

    @Autowired
    public BookSearchCriteriaValidators(StringValidators stringValidators) {
        this.stringValidators = stringValidators;
    }

    public BookSearchCriteria screenStringValues(BookSearchCriteria criteria) {
        if (criteria.getName() != null) {
            criteria.setName(
                stringValidators.textScreening(criteria.getName())
            );
        }

        if (criteria.getAuthorName() != null) {
            criteria.setAuthorName(
                stringValidators.textScreening(criteria.getAuthorName())
            );
        }

        if (criteria.getGenres() != null) {
            List<String> screenedGenres = new ArrayList<>(criteria.getGenres().size());

            for (int i = 0; i < criteria.getGenres().size(); i++) {
                screenedGenres.add(stringValidators.textScreening(criteria.getGenres().get(i)));
            }

            criteria.setGenres(screenedGenres);
        }

        return criteria;
    }

    public boolean isSafeFromSqlInjection(BookSearchCriteria criteria) {
        if (criteria.getName() != null && !stringValidators.isSafeFromSqlInjection(criteria.getName())) {
            return false;
        }

        if (criteria.getAuthorName() != null && !stringValidators.isSafeFromSqlInjection(criteria.getAuthorName())) {
            return  false;
        }

        if (criteria.getGenres() != null) {
            for (String genre : criteria.getGenres()) {
                if (!stringValidators.isSafeFromSqlInjection(genre)) {
                    return false;
                }
            }
        }

        return true;
    }

}
