package maksim.booksservice.models.kafkadtos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DtoForBookReviewChanging {

    // -1 - remove rate / 0 - change rate / 1 - add rate
    private Integer action;

    private Integer bookId;

    private Integer rating;

    private Integer previousRate;

}
