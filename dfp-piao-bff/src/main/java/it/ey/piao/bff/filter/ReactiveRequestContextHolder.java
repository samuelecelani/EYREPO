package it.ey.piao.bff.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.util.context.Context;

import java.util.function.Function;

/**
 * Holder reattivo che permette di salvare/recuperare la ServerHttpRequest
 * nel contesto Reactor, rendendola disponibile a qualsiasi operatore
 * nella catena reattiva (es. ExchangeFilterFunction del WebClient).
 */
public class ReactiveRequestContextHolder {

    private static final Class<ServerHttpRequest> CONTEXT_KEY = ServerHttpRequest.class;

    /**
     * Restituisce una Function da usare con .contextWrite() per salvare la request nel contesto.
     */
    public static Function<Context, Context> withRequest(ServerHttpRequest request) {
        return ctx -> ctx.put(CONTEXT_KEY, request);
    }

    /**
     * Recupera la ServerHttpRequest dal contesto Reactor.
     */
    public static reactor.core.publisher.Mono<ServerHttpRequest> getRequest() {
        return reactor.core.publisher.Mono.deferContextual(ctx -> {
            if (ctx.hasKey(CONTEXT_KEY)) {
                return reactor.core.publisher.Mono.just(ctx.get(CONTEXT_KEY));
            }
            return reactor.core.publisher.Mono.empty();
        });
    }
}
