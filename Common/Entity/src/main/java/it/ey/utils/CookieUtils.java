package it.ey.utils;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

@Component
public class CookieUtils {

    private static final String SESSION_COOKIE_NAME = "SESSION_ID";
    private static final String COOKIE_PATH = "/";

    @Value("${app.cookie.secure}")
    private static boolean SECURE;

    @Value("${app.cookie.httpOnly}")
    private static boolean HTTP_ONLY;

    @Value("${app.cookie.sameSite}")
    private static String SAME_SITE;
// puoi renderlo dinamico

//    public static Cookie createCookieSession(String sessionId, Date expiry) {
//        Cookie cookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
//        cookie.setPath(COOKIE_PATH);
//        cookie.setSecure(SECURE);
//        cookie.setHttpOnly(HTTP_ONLY);
//
//        if (expiry != null) {
//            long maxAgeSeconds = (expiry.getTime() - System.currentTimeMillis()) / 1000;
//            cookie.setMaxAge((int) maxAgeSeconds);
//        }
//
//        return cookie;
//    }


    public static Cookie getSessionCookie(Cookie[] cookies) {
        if (cookies == null) return null;

        return Arrays.stream(cookies)
                .filter(c -> SESSION_COOKIE_NAME.equals(c.getName()))
                .findFirst()
                .orElse(null);
    }


    /**
     * Crea un cookie di sessione con scadenza dinamica basata sul refresh token.
     *
     * @param sessionId  ID della sessione
     * @param expiryDate Data di scadenza del refresh token
     * @return ResponseCookie configurato
     */
    public static ResponseCookie createCookieSession(String sessionId, Date expiryDate) {
        long maxAgeSeconds = (expiryDate.getTime() - System.currentTimeMillis()) / 1000;
        if (maxAgeSeconds < 0) {
            maxAgeSeconds = 0; // Evita valori negativi
        }

        return ResponseCookie.from(SESSION_COOKIE_NAME, sessionId)
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .path(COOKIE_PATH)
                .maxAge(maxAgeSeconds)
                .sameSite(SAME_SITE)
                .build();
    }
    public static ResponseCookie createCookieSession(String sessionId) {

        return ResponseCookie.from(SESSION_COOKIE_NAME, sessionId)
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .path(COOKIE_PATH)
                .sameSite(SAME_SITE)
                .build();
    }
    /**
     * Elimina il cookie di sessione.
     */
    public static ResponseCookie deleteCookieSession() {
        return ResponseCookie.from(SESSION_COOKIE_NAME, "")
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .path(COOKIE_PATH)
                .maxAge(0)
                .sameSite(SAME_SITE)
                .build();
    }
}


