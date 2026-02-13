package it.ey.piao.api.service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter

public class TransactionFailureEvent<T> extends ApplicationEvent {
    private final Exception exception;
    public TransactionFailureEvent(T source, Exception exception) {
        super(source);
        this.exception = exception;
    }
}
