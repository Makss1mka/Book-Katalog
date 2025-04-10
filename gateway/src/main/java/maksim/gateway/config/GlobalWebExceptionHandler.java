package maksim.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class GlobalWebExceptionHandler implements ErrorWebExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalWebExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        logger.trace("Error occurred: {}", ex.getMessage());

        HttpStatusCode status;
        String message;

        if (ex instanceof ResponseStatusException) {
            status = ((ResponseStatusException) ex).getStatusCode();
            message = "Route not found or method not supported";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "Something goes wrong, Sorry my bad :(";
        }

        exchange.getResponse().setStatusCode(status);
        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(message.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}

