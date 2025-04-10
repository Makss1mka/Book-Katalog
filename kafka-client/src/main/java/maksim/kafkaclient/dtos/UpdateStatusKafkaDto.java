package maksim.kafkaclient.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UpdateStatusKafkaDto {

    private int userId;

    private int bookId;

    private String newStatus;

    public UpdateStatusKafkaDto(int userId, int bookId, String newStatus) {
        this.userId = userId;
        this.bookId = bookId;
        this.newStatus = newStatus;
    }

}
