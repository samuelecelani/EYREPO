package it.ey.piao.api.service.event;


import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class BeforeUpdateEvent<T> {
    private final List<T> previousState;
    private final Class<T> type;

    public BeforeUpdateEvent(Class<T> type, T previousState) {
        this.type = type;
        this.previousState= Collections.singletonList(previousState);
    }
    public BeforeUpdateEvent(Class<T> type, List<T> previousState) {
        this.type = type;
        this.previousState = previousState;
    }


}

