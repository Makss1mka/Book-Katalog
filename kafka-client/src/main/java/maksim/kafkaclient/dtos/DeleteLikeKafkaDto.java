package maksim.kafkaclient.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class DeleteLikeKafkaDto {

    private int userId;

    private int bookId;

    public DeleteLikeKafkaDto(int userId, int bookId) {
        this.userId = userId;
        this.bookId = bookId;
    }

}
