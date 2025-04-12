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

    private String serviceName;

    private Long count;

    public VisitKafkaDto(String method, String serviceName, Long count) {
        this.method = method;
        this.serviceName = serviceName;
        this.count = count;
    }

}
