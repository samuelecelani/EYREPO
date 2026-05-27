package it.ey.piao.bff.configuration.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

public class CookieBearerTokenResolver implements BearerTokenResolver {

    private static final String SESSION_COOKIE_NAME = "SESSION_ID";

    @Override
    public String resolve(HttpServletRequest request) {

        if (request.getRequestURI().startsWith("/auth/")) {
            return null; // Non applicare JWT
        }

        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (SESSION_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue(); // JWT dal cookie
                }
            }
        }
        return null; // Nessun token trovato
    }
}
