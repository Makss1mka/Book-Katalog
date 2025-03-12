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

        if (criteria.getJoinMode() == JoinMode.WITH) {
            Join<Book, User> joinAuthor = root.join("author", JoinType.INNER);
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

        if (criteria.getStatusCount() != null) {
            Join<Book, BookStatusLog> joinStatusLogs = root.join("statusesLogs", JoinType.INNER);

            Predicate datePredicate = builder.between(
                joinStatusLogs.get("time"),
                criteria.getStatusDateMin(),
                criteria.getStatusDateMax()
            );

            if (criteria.getStatus() != BookStatus.ALL) {
                Predicate statusPredicate = builder.equal(joinStatusLogs.get("status"), criteria.getStatus());

                predicates.add(builder.and(statusPredicate, datePredicate));
            } else {
                predicates.add(datePredicate);
            }

        }

        return builder.and(predicates.toArray(new Predicate[0]));
    }

}
