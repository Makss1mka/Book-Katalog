package maksim.reviewsservice.models.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import maksim.reviewsservice.models.entities.User;

import java.util.Set;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewDto {
    private Set<User> likedUsers

}
