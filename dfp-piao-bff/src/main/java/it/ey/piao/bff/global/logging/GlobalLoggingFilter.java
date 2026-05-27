package it.ey.piao.bff.global.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

@Component
public class GlobalLoggingFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(GlobalLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.info("Request: {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getURI());
        return chain.filter(exchange)
            .doOnSuccess(aVoid -> log.info("Response status: {}", exchange.getResponse().getStatusCode()))
            .doOnError(error -> {
                // Disconnessione client (es. chiusura tab durante SSE): log a DEBUG, non ERROR
                if (isClientDisconnect(error)) {
                    log.debug("Client disconnesso durante request {} {}: {}",
                        exchange.getRequest().getMethod(),
                        exchange.getRequest().getURI(),
                        error.getMessage());
                } else {
                    log.error("Error occurred: {}", error.getMessage(), error);
                }
            });
    }

    /**
     * Verifica se l'eccezione è causata dalla disconnessione del client
     * (IOException, ClosedChannelException o cause annidate).
     */
    private boolean isClientDisconnect(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof IOException || current instanceof ClosedChannelException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
