package it.sogei.dfp.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class GlobalJwtHeaderFilter implements GlobalFilter, Ordered {
	
    @Autowired
    private JwtHeaderFilter jwtHeaderFilter;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return jwtHeaderFilter.extractAndAddHeaders(exchange, chain);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}