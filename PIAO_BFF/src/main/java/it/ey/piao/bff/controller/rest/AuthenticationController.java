package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.LoginDataDTO;
import it.ey.piao.bff.property.PropertyAuthentication;
import it.ey.piao.bff.service.impl.AuthenticationService;
import it.ey.utils.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.security.auth.login.LoginException;
import java.net.URI;
import java.util.Date;

@ApiV1Controller("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final PropertyAuthentication props;


    public AuthenticationController(AuthenticationService authenticationService){
        this.authenticationService = authenticationService;
        this.props = new PropertyAuthentication();
}
    @GetMapping("/login")
    public Mono<ResponseEntity<Void>> login(
        @RequestParam("origin_uri") @URL @NotBlank String originUri,
        @RequestParam("redirect_uri") @URL @NotBlank String redirectUri) {

        return Mono.fromSupplier(() -> {
            LoginDataDTO loginData = authenticationService.login(originUri, redirectUri);
            ResponseCookie sessionCookie = CookieUtils.createCookieSession(loginData.getSessionId());
            return ResponseEntity.status(302)
                .location(URI.create(loginData.getKeycloakAuthUri()))
                .header(HttpHeaders.SET_COOKIE, sessionCookie.toString())
                .build();
        });
    }

    @GetMapping("/callback")
    public Mono<ResponseEntity<Object>> callback(
        HttpServletRequest request,
        @RequestParam("state") @NotBlank String state,
        @RequestParam(value = "session_state", required = false) String sessionState,
        @RequestParam(value = "iss", required = false) String iss,
        @RequestParam("code") @NotBlank String code) {

        Cookie sessionCookie = CookieUtils.getSessionCookie(request.getCookies() != null ? request.getCookies() : new Cookie[0]);


        if (sessionCookie == null || sessionCookie.getValue() == null) {
            return Mono.just(ResponseEntity.status(401).body("Cookie di sessione non trovato o nullo."));
        }

        return authenticationService.callback(code, sessionCookie.getValue(), state)
            .map(callbackDTO -> {
                ResponseCookie newCookie = CookieUtils.createCookieSession(
                    callbackDTO.getSessionId(),
                    Date.from(callbackDTO.getRefreshTokenExpiry()));

                return ResponseEntity.status(302)
                    .location(URI.create(callbackDTO.getOriginUri()))
                    .header(HttpHeaders.SET_COOKIE, newCookie.toString())
                    .build();
            })
            .onErrorResume(LoginException.class, e -> {
                ResponseCookie removeCookie = CookieUtils.deleteCookieSession();
                return Mono.just(ResponseEntity.status(401)
                    .header(HttpHeaders.SET_COOKIE, removeCookie.toString())
                    .body(e.getMessage() != null ? e.getMessage() : ""));
            });
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(
        HttpServletRequest request,
        @RequestParam("redirect_uri") @URL @NotBlank String redirectUri) {

        // Recupera il cookie SESSION_ID
        Cookie sessionCookie = CookieUtils.getSessionCookie(request.getCookies() != null ? request.getCookies() : new Cookie[0]);

        String sessionId = sessionCookie != null ? sessionCookie.getValue() : null;


        if (sessionId == null) {
            // Nessuna sessione -> redirect diretto
            return ResponseEntity.status(302)
                .location(URI.create(redirectUri))
                .header(HttpHeaders.SET_COOKIE, CookieUtils.deleteCookieSession().toString())
                .build();
        }



        String logoutUri = authenticationService.logout( sessionId, redirectUri).block();

        if (logoutUri == null) {
            // Fallback: redirect diretto
            logoutUri = redirectUri;
        }

        return ResponseEntity.status(302)
            .location(URI.create(logoutUri))
            .header(HttpHeaders.SET_COOKIE, CookieUtils.deleteCookieSession().toString())
            .build();
    }

}
