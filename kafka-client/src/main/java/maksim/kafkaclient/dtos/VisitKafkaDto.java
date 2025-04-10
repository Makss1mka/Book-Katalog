package maksim.kafkaclient.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class VisitKafkaDto {

    private String method;

    private String url;

    public VisitKafkaDto(String method, String url) {
        this.method = method;
        this.url = url;
    }

}
