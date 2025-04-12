package maksim.visitservice.services;

import maksim.kafkaclient.dtos.ListOfNewVisitsKafkaDto;
import maksim.kafkaclient.dtos.VisitKafkaDto;
import maksim.visitservice.models.Visit;
import maksim.visitservice.models.VisitDto;
import maksim.visitservice.repositories.VisitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class VisitService {
    private static final Logger logger = LoggerFactory.getLogger(VisitService.class);

    private VisitRepository visitRepository;
    private List<Visit> visitEntries;

    @Autowired
    public VisitService(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;

        visitEntries = visitRepository.findAll();
    }

    synchronized public void addNewVisitEntry(String method, String serviceName, Long count) {
        logger.trace("Visit service method entrance: addNewVisitEntry");

        Visit newVisit = new Visit();
        newVisit.setServiceName(serviceName);
        newVisit.setMethod(method);
        newVisit.setCount(count);

        visitRepository.save(newVisit);

        visitEntries.add(newVisit);

        logger.trace("Visit service method end: addNewVisitEntry");
    }

    synchronized public void addListOfVisits(ListOfNewVisitsKafkaDto newVisits) {
        logger.trace("Visit service method entrance: addListOfVisits");

        boolean isFound = false;

        for (VisitKafkaDto newVisit : newVisits.getNewVisits()) {
            for (Visit visitEntry : visitEntries) {
                if (
                    visitEntry.getMethod().equals(newVisit.getMethod())
                    && visitEntry.getServiceName().equals(newVisit.getServiceName())
                ) {
                    visitEntry.setCount(
                        visitEntry.getCount() + newVisit.getCount()
                    );

                    isFound = true;

                    break;
                }
            }

            if (!isFound) {
                addNewVisitEntry(newVisit.getMethod(), newVisit.getServiceName(), newVisit.getCount());
            } else {
                isFound = false;
            }
        }

        visitRepository.saveAll(visitEntries);

        logger.trace("Visit service method end: addListOfVisits");
    }

    synchronized public List<VisitDto> getAllVisits() {
        logger.trace("Visit service method entrance: getAllVisits");

        List<VisitDto> visitDtos = new ArrayList<>(visitEntries.size());

        visitEntries.forEach(visitEntry ->
            visitDtos.add(new VisitDto(visitEntry))
        );

        logger.trace("Visit service method end: getAllVisits");

        return visitDtos;
    }


}

