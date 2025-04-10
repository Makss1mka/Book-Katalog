package maksim.kafkaclient.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CreateStatusKafkaDto {

    private int userId;

    private int bookId;

    private String status;

    public CreateStatusKafkaDto(int userId, int bookId, String status) {
        this.userId = userId;
        this.bookId = bookId;
        this.status = status;
    }

}
