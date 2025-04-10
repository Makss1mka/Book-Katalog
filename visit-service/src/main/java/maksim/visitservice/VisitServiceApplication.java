package maksim.visitservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VisitServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisitServiceApplication.class, args);
    }

}
