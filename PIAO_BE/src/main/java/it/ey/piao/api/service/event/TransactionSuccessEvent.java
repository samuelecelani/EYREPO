package it.ey.piao.api.service.event;

import org.springframework.context.ApplicationEvent;


public class TransactionSuccessEvent<T> extends ApplicationEvent {
    public TransactionSuccessEvent(T source) {
        super(source);
    }
}

