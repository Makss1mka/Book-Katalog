package maksim.visitservice.controllers;

import maksim.visitservice.models.VisitDto;
import maksim.visitservice.services.VisitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/visits")
public class VisitController {
    private static final Logger logger = LoggerFactory.getLogger(VisitController.class);

    private final VisitService visitService;

    @Autowired
    public VisitController(VisitService visitService) {
        this.visitService = visitService;
    }

    @GetMapping
    public ResponseEntity<List<VisitDto>> getAllVisits() {
        logger.trace("Visit service method entrance: addNewVisitUrl");

        List<VisitDto> visits = visitService.getAllVisits();

        logger.trace("Visit service method end: addNewVisitUrl");

        return ResponseEntity.ok(visits);
    }

}
