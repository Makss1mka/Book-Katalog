package maksim.booksservice.utils.bookutils;

import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.List;
import maksim.booksservice.models.entities.Book;
import maksim.booksservice.models.entities.BookStatusLog;
import maksim.booksservice.models.entities.User;
import maksim.booksservice.utils.enums.BookStatus;
import maksim.booksservice.utils.enums.JoinMode;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecification implements Specification<Book> {

    private final transient BookSearchCriteria criteria;

    public BookSpecification(BookSearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<Book> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();

        if (criteria.getJoinModeForAuthor() == JoinMode.WITH) {
            root.join("author", JoinType.INNER);
        }

        if (criteria.getName() != null) {
            predicates.add(builder.like(root.get("name"), "%" + criteria.getName() + "%"));
        }

        if (criteria.getAuthorId() != null) {
            predicates.add(builder.equal(root.get("author").get("id"), criteria.getAuthorId()));
        }

        if (criteria.getAuthorName() != null) {
            predicates.add(builder.like(root.get("author").get("name"), "%" + criteria.getAuthorName() + "%"));
        }

        if (criteria.getGenres() != null && !criteria.getGenres().isEmpty()) {
            // Write logic later
        }

        if (criteria.getIssuedDate() != null && criteria.getIssuedDateOperator() != null) {
            predicates.add(
                switch (criteria.getIssuedDateOperator()) {
                    case OLDER -> builder.lessThan(root.get("issuedDate"), criteria.getIssuedDate());
                    case NEWER -> builder.greaterThan(root.get("issuedDate"), criteria.getIssuedDate());
                }
            );
        }

        if (criteria.getRating() != null && criteria.getRatingOperator() != null) {
            String rating = "rating";

            predicates.add(
                switch (criteria.getRatingOperator()) {
                    case GREATER -> builder.greaterThan(root.get(rating), criteria.getRating());
                    case LESS -> builder.lessThan(root.get(rating), criteria.getRating());
                    case EQUAL -> builder.equal(root.get(rating), criteria.getRating());
                }
            );
        }

        if (criteria.getJoinModeForStatuses() == JoinMode.WITH) {
            root.fetch("statusesLogs", JoinType.LEFT);

            predicates.add(builder.between(
                root.get("statusesLogs").get("addedDate"),
                criteria.getStatusMinDate(),
                criteria.getStatusMaxDate()
            ));
        }

        return builder.and(predicates.toArray(new Predicate[0]));
    }

}
