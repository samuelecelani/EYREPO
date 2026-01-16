package it.ey.piao.bff.global.exception;


import com.fasterxml.jackson.databind.ObjectMapper;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBuffer;
//Questo componente è necessario per gestire l'eccezioni in contesto WebFlux ( Programmazione reattiva)
@Component
@Order(-2) // Importante: deve avere priorità alta
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;

        // Mappa le eccezioni a status specifici
        if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            status = HttpStatus.FORBIDDEN; // 403
        } else if (ex instanceof org.springframework.security.core.AuthenticationException) {
            status = HttpStatus.UNAUTHORIZED; // 401
        } else if (ex instanceof CustomBusinessException) {
            status = HttpStatus.BAD_REQUEST; // 400
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Costruisci il DTO
        GenericResponseDTO<Void> response = new GenericResponseDTO<>();
        Status s = Status.builder().isSuccess(false).build();
        it.ey.dto.Error error = it.ey.dto.Error.builder()
            .messageError(ex.getMessage() != null ? ex.getMessage() : "Errore interno")
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
}

