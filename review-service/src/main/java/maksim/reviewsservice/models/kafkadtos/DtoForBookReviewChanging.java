package maksim.reviewsservice.models.kafkadtos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DtoForBookReviewChanging {

    // 0 - add rate | 1 - remove rate
    private int action;

    private int bookId;

    private int rating;

}
