//package it.example.piao.bff.global.exception;
//
//import it.example.dto.GenericResponseDTO;
//import it.example.dto.Status;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//import reactor.core.publisher.Mono;
//
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(AuthenticationException.class)
//    public Mono<ResponseEntity<GenericResponseDTO<Void>>> handleAuthentication(AuthenticationException ex) {
//        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Autenticazione fallita: " + ex.getMessage());
//    }
//
//    @ExceptionHandler(AccessDeniedException.class)
//    public Mono<ResponseEntity<GenericResponseDTO<Void>>> handleAccessDenied(AccessDeniedException ex) {
//        return buildErrorResponse(HttpStatus.FORBIDDEN, "Accesso negato: " + ex.getMessage());
//    }
//
//    @ExceptionHandler(CustomBusinessException.class)
//    public Mono<ResponseEntity<GenericResponseDTO<Void>>> handleBusinessException(CustomBusinessException ex) {
//        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Errore di business: " + ex.getMessage());
//    }
//
//    @ExceptionHandler(Exception.class)
//    public Mono<ResponseEntity<GenericResponseDTO<Void>>> handleGenericException(Exception ex) {
//        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Errore interno: " + ex.getMessage());
//    }
//
//    private Mono<ResponseEntity<GenericResponseDTO<Void>>> buildErrorResponse(HttpStatus status, String message) {
//        GenericResponseDTO<Void> response = new GenericResponseDTO<>();
//        Status s = Status.builder().isSuccess(false).build();
//        it.example.dto.Error error = it.example.dto.Error.builder()
//            .messageError(message)
//            .build();
//        response.setStatus(s);
//        response.setError(error);
//        return Mono.just(ResponseEntity.status(status).body(response));
//    }
//}
