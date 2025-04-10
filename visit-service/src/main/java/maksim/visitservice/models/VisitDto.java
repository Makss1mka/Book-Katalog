package maksim.visitservice.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VisitDto {

    private String url;

    private String method;

    private Long count;

    public VisitDto(Visit visit) {
        this.url = visit.getUrl();
        this.method = visit.getMethod();
        this.count = visit.getCount();
    }

}
