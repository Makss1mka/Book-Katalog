package maksim.visitservice.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "visits")
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "method")
    private String method;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "count")
    private Long count;

}
