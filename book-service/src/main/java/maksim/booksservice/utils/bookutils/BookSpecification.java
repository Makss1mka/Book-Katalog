package maksim.booksservice.utils.bookutils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import maksim.booksservice.models.Book;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecification implements Specification<Book> {

    private final transient BookSearchCriteria criteria;

    public BookSpecification(BookSearchCriteria criteria) {
        this.criteria = criteria;
    }

    @Override
    public Predicate toPredicate(Root<Book> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();

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

        if (criteria.getStatusCount() != null && criteria.getStatusOperator() != null
            && criteria.getStatus() != null && criteria.getStatusScope() != null) {
            String status = "status" + criteria.getStatus().getValue() + criteria.getStatusScope().getValue();

            predicates.add(
                switch (criteria.getStatusOperator()) {
                    case GREATER -> builder.greaterThan(root.get(status), criteria.getStatusCount());
                    case LESS -> builder.lessThan(root.get(status), criteria.getStatusCount());
                    case EQUAL -> builder.equal(root.get(status), criteria.getStatusCount());
                }
            );
        }

        return builder.and(predicates.toArray(new Predicate[0]));
    }

}
