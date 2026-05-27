package it.ey.piao.bff.global.exception;


import com.fasterxml.jackson.databind.ObjectMapper;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBuffer;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

//Questo componente è necessario per gestire l'eccezioni in contesto WebFlux ( Programmazione reattiva)
@Component
@Order(-2) // Importante: deve avere priorità alta
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    private final ObjectMapper objectMapper;

    public GlobalErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // Se la response è già stata inviata (es. SSE stream già aperto con 200 OK),
        // non possiamo più modificare headers/status → ignoriamo l'errore.
        if (exchange.getResponse().isCommitted()) {
            if (isClientDisconnectException(ex)) {
                log.debug("Client disconnesso durante SSE stream: {}", ex.getMessage());
            } else {
                log.warn("Errore su response già committed ({}): {}",
                    exchange.getRequest().getPath(), ex.getMessage());
            }
            return Mono.empty();
        }

        HttpStatus status;
        String errorMessage;

        // Disconnessione client: log a livello WARN, non ERROR
        if (isClientDisconnectException(ex)) {
            log.warn("Client disconnesso: {}", ex.getMessage());
            return Mono.empty();
        }

        // Log dell'errore
        log.error("Global error handler caught exception: {}", ex.getMessage(), ex);

        // Mappa le eccezioni a status specifici
        if (ex instanceof WebClientResponseException) {
            // Preserva lo status code originale dalle chiamate HTTP
            WebClientResponseException webClientEx = (WebClientResponseException) ex;
            status = HttpStatus.resolve(webClientEx.getStatusCode().value());
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            errorMessage = "Error calling external service: " + webClientEx.getMessage();
            log.error("WebClient error - Status: {}, Message: {}", status, webClientEx.getMessage());
        } else if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            status = HttpStatus.FORBIDDEN; // 403
            errorMessage = "Access denied";
        } else if (ex instanceof org.springframework.security.core.AuthenticationException) {
            status = HttpStatus.UNAUTHORIZED; // 401
            errorMessage = "Authentication failed";
        } else if (ex instanceof CustomBusinessException) {
            status = HttpStatus.BAD_REQUEST; // 400
            errorMessage = ex.getMessage() != null ? ex.getMessage() : "Bad request";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
            errorMessage = ex.getMessage() != null ? ex.getMessage() : "Internal server error";
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Costruisci il DTO
        GenericResponseDTO<Void> response = new GenericResponseDTO<>();
        Status s = Status.builder().isSuccess(false).build();
        it.ey.dto.Error error = it.ey.dto.Error.builder()
            .messageError(errorMessage)
            .build();
        response.setStatus(s);
        response.setError(error);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(response);
        } catch (Exception e) {
            bytes = "{\"error\":\"Internal Server Error\"}".getBytes();
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    /**
     * Verifica se l'eccezione è causata dalla disconnessione del client
     * (es. chiusura tab, navigazione, abort della connessione SSE).
     */
    private boolean isClientDisconnectException(Throwable ex) {
        if (ex instanceof IOException || ex instanceof ClosedChannelException) {
            return true;
        }
        // Controlla anche le cause annidate
        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof IOException || cause instanceof ClosedChannelException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}

