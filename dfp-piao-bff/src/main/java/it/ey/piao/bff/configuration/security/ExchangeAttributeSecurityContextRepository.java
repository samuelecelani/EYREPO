package it.ey.piao.bff.configuration.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Repository che salva il SecurityContext come attributo dell'exchange.
 * Questo permette ai filtri custom di impostare l'autenticazione e renderla
 * disponibile a Spring Security per l'autorizzazione.
 */
@Component
public class ExchangeAttributeSecurityContextRepository implements ServerSecurityContextRepository {

    private static final Logger log = LoggerFactory.getLogger(ExchangeAttributeSecurityContextRepository.class);
    private static final String SECURITY_CONTEXT_ATTR = "SPRING_SECURITY_CONTEXT";

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.fromRunnable(() -> {
            if (context != null) {
                exchange.getAttributes().put(SECURITY_CONTEXT_ATTR, context);
                log.debug("SecurityContext SAVED to exchange attributes");
            } else {
                exchange.getAttributes().remove(SECURITY_CONTEXT_ATTR);
                log.debug("SecurityContext REMOVED from exchange attributes");
            }
        });
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        log.info(">>>>> SecurityContextRepository.load() for path: {} <<<<<", path);

        SecurityContext ctx = (SecurityContext) exchange.getAttributes().get(SECURITY_CONTEXT_ATTR);
        log.info("Exchange attributes keys: {}", exchange.getAttributes().keySet());

        if (ctx != null) {
            log.info("✓ SecurityContext FOUND - authenticated: {}, principal: {}",
                ctx.getAuthentication() != null && ctx.getAuthentication().isAuthenticated(),
                ctx.getAuthentication() != null ? ctx.getAuthentication().getName() : "null");
        } else {
            log.warn("✗ SecurityContext NOT FOUND in exchange attributes for path: {}", path);
        }
        return Mono.justOrEmpty(ctx);
    }
}
