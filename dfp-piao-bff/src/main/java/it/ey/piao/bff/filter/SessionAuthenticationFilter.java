//package it.example.piao.bff.filter;
//
//import it.example.dto.LoginSessionDTO;
//import it.example.dto.TokenDTO;
//import it.example.piao.bff.cache.SpringCacheService;
//import it.example.piao.bff.configuration.security.KeycloakRealmRoleConverter;
//import it.example.piao.bff.httpClient.OAuth2Service;
//import it.example.piao.bff.service.impl.JwtClaimsService;
//import it.example.utils.CookieUtils;
//import it.example.utils.TokenUtils;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.cache.CacheProperties;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.ResponseCookie;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
//import org.springframework.security.oauth2.jwt.JwtException;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.Collection;
//import java.util.Date;
//import java.util.List;
//
////@Component
//public class SessionAuthenticationFilter extends OncePerRequestFilter {
//
//
//    private final SpringCacheService springCacheService;
//    private final CacheProperties cacheProperties;
//    private final JwtDecoder jwtDecoder;
//    private final JwtClaimsService  jwtClaimsService;
//    private final OAuth2Service oAuth2Service;
//    private final Logger log = LoggerFactory.getLogger(SessionAuthenticationFilter.class);
//   public  SessionAuthenticationFilter(SpringCacheService springCacheService, CacheProperties cacheProperties, JwtDecoder jwtDecoder, JwtClaimsService jwtClaimsService, OAuth2Service oAuth2Service){
//
//       this.springCacheService = springCacheService;
//       this.cacheProperties = cacheProperties;
//       this.jwtDecoder = jwtDecoder;
//       this.jwtClaimsService = jwtClaimsService;
//       this.oAuth2Service = oAuth2Service;
//   }
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        // Non applicare su /auth/*
//        if (request.getRequestURI().startsWith("/auth/")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        String sessionId = getSessionIdFromCookie(request.getCookies());
//        if (sessionId != null) {
//
//            LoginSessionDTO loginSession = springCacheService.get(
//                cacheProperties.getCacheNames().getFirst(),
//                sessionId,
//                LoginSessionDTO.class
//            );
//
//            if (loginSession != null && loginSession.isAuthenticated()) {
//                TokenDTO token = loginSession.getToken();
//
//                try {
//                    // Prova a decodificare il token (verifica scadenza)
//                    Jwt jwt = jwtDecoder.decode(token.getAccessToken());
//
//                    setAuthentication(jwt);
//
//                } catch (JwtException e) {
//                    // Token scaduto → tenta refresh
//                    log.warn("Access token scaduto, tentativo di refresh per sessionId {}", sessionId);
//
//                    TokenDTO newTokens = oAuth2Service.refreshTokens(token.getRefreshToken()).block();
//                    if (newTokens != null) {
//                        // Aggiorna sessione in cache
//                        loginSession.setToken(newTokens);
//                        springCacheService.put(cacheProperties.getCacheNames().getFirst(), sessionId, loginSession);
//
//
//                        // Decodifica nuovo token e setta autenticazione
//                        Jwt jwt = jwtDecoder.decode(newTokens.getAccessToken());
//                        setAuthentication(jwt);
//
//                        // Aggiorna cookie con nuova scadenza refresh token
//                        ResponseCookie newCookie = CookieUtils.createCookieSession(
//                            sessionId,
//                            Date.from(TokenUtils.getRefreshTokenExpiry(newTokens.getRefreshExpiresIn()))
//                        );
//                        response.addHeader(HttpHeaders.SET_COOKIE, newCookie.toString());
//
//                        log.info("Refresh token completato per sessionId {}", sessionId);
//                    } else {
//                        log.error("Refresh token non valido, logout forzato.");
//                        ResponseCookie removeCookie = CookieUtils.deleteCookieSession();
//                        response.addHeader(HttpHeaders.SET_COOKIE, removeCookie.toString());
//                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                        return;
//                    }
//                }
//            }
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//
//
//    private void setAuthentication(Jwt jwt) {
//        // Recupera le authorities dai ruoli del JWT
//        Collection<GrantedAuthority> authorities = new KeycloakRealmRoleConverter().convert(jwt);
//        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities);
//        // Imposta nel SecurityContext
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//    }
//
//
//    private String getSessionIdFromCookie(Cookie[] cookies) {
//        if (cookies != null) {
//            for (Cookie cookie : cookies) {
//                if ("SESSION_ID".equals(cookie.getName())) {
//                    return cookie.getValue();
//                }
//            }
//        }
//        return null;
//    }
//
//
//}
