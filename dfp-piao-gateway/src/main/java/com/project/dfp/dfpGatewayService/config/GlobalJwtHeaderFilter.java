package com.project.dfp.dfpGatewayService.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GlobalJwtHeaderFilter implements GlobalFilter, Ordered {
	
    @Autowired
    private JwtHeaderFilter jwtHeaderFilter;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        log.info(">>> GlobalJwtHeaderFilter – path={}, hasAuth={}, exchangeType={}",
                path,
                authHeader != null ? "Bearer..." : "null",
                exchange.getClass().getSimpleName());
        return jwtHeaderFilter.extractAndAddHeaders(exchange, chain);
    }

    @Override
    public int getOrder() {
        return -1 ;
    }
}