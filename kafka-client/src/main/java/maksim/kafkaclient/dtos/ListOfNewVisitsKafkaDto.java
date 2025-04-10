package maksim.kafkaclient.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ListOfNewVisitsKafkaDto {

    private List<VisitKafkaDto> newVisits;

    public ListOfNewVisitsKafkaDto(List<VisitKafkaDto> newVisits) {
        this.newVisits = newVisits;
    }

}
