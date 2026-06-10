package it.ey.piao.api.handler;

import it.ey.enums.TypeErrorEnum;
import it.ey.piao.api.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {

        // corpo di risposta FE
        Map<String, Object> body = new HashMap<>();
        body.put("data", null);
        body.put("status", Map.of("success", false));

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("errorCode", ex.getErrorCode());
        errorBody.put("messageError", ex.getMessage());
        errorBody.put("typeEnum", ex.getTypeEnum() != null ? ex.getTypeEnum().name() : null);

        body.put("error", errorBody);

        HttpStatus status =
            ex.getTypeEnum() == TypeErrorEnum.WARNING
                ? HttpStatus.OK
                : HttpStatus.CONFLICT;

        return ResponseEntity.status(status).body(body);
    }
}
