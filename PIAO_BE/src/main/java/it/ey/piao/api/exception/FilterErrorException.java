package it.ey.piao.api.exception;

import lombok.Getter;

import java.io.Serial;
import java.util.Map;
@Getter
public class FilterErrorException extends RuntimeException{


    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<String, String> errorFilter;

    public FilterErrorException(String message, Map<String, String> errorFilter) {
        super(buildMessage(message, errorFilter));
        this.errorFilter = errorFilter;
    }

    private static String buildMessage(String baseMessage, Map<String, String> errorFilter) {
        return baseMessage + " -> Filtri errati: " + errorFilter;
    }
}
