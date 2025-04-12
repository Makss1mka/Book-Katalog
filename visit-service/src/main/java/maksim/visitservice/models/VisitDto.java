package maksim.visitservice.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VisitDto {

    private String serviceName;

    private String method;

    private Long count;

    public VisitDto(Visit visit) {
        this.serviceName = visit.getServiceName();
        this.method = visit.getMethod();
        this.count = visit.getCount();
    }

}
