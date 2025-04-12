package maksim.userservice.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import maksim.userservice.services.kafka.producers.VisitEventsProducer;
import maksim.kafkaclient.dtos.VisitKafkaDto;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class ControllerVisitPushingAspect {
    private static final Logger logger = LoggerFactory.getLogger(ControllerVisitPushingAspect.class);

    private final VisitEventsProducer visitEventsProducer;

    @Autowired
    public ControllerVisitPushingAspect(VisitEventsProducer visitEventsProducer) {
        this.visitEventsProducer = visitEventsProducer;
    }

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}

    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void pushVisitAfterControllerMethodEnd(JoinPoint joinPoint, Object result) {
        logger.trace("ControllerVisitPushingAspect entrance");

        HttpServletRequest request =
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();

        visitEventsProducer.addAndPublishVisit("user-service", methodName);

        logger.trace("ControllerVisitPushingAspect end");
    }
}
