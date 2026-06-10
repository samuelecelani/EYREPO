package com.project.dfp.dfpGatewayService.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Global filter che logga tutte le request in ingresso e le response in uscita dal gateway.
 * Order molto basso (Ordered.HIGHEST_PRECEDENCE) per essere eseguito per primo.
 */
@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        String originalPath = request.getURI().toString();
        String queryParams = request.getQueryParams().isEmpty() ? "" : request.getQueryParams().toString();
        HttpHeaders headers = request.getHeaders();

        log.info(">>> GATEWAY REQUEST: {} {} | Query: {} | Host: {} | Content-Type: {}",
                method,
                originalPath,
                queryParams,
                headers.getFirst(HttpHeaders.HOST),
                headers.getFirst(HttpHeaders.CONTENT_TYPE));

        // Log della rotta matchata (disponibile dopo il routing)
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            URI routeUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);

            log.info("<<< GATEWAY RESPONSE: {} {} | Status: {} | Route: {} | Target: {}",
                    method,
                    request.getPath(),
                    response.getStatusCode(),
                    route != null ? route.getId() : "N/A",
                    routeUri != null ? routeUri : "N/A");
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
