package it.sogei.dfp.gateway.config;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtHeaderFilter extends AbstractGatewayFilterFactory<JwtHeaderFilter.Config> {
	
	@Value("${gateway.filter.header.useridKey:'X-User-Sub'}")
	private String USER_ID;
	@Value("${gateway.filter.header.rolesKey:'X-User-Roles'}")
	private String ROLES;

    public JwtHeaderFilter() {
        super(Config.class);
    }

    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> extractAndAddHeaders(exchange, chain);
    }

	protected Mono<Void> extractAndAddHeaders(ServerWebExchange exchange, GatewayFilterChain chain) {
		return exchange.getPrincipal()
            .ofType(Authentication.class)
            .flatMap(auth -> {

                if (!(auth.getPrincipal() instanceof Jwt jwt)) {
                    return chain.filter(exchange);
                }

                String sub = jwt.getSubject();
                String roles = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));

                ServerHttpRequest request = exchange.getRequest()
                        .mutate()
                        .header(USER_ID, sub)
                        .header(ROLES, roles)
                        .build();

                log.debug("Header: {}={}, {}=[{}]", USER_ID, sub, ROLES, roles);
                return chain.filter(exchange.mutate().request(request).build());
            })
            .switchIfEmpty(chain.filter(exchange));
	}
}