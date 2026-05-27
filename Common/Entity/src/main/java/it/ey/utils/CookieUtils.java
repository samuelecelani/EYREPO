package it.ey.utils;

import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseCookie;

import java.util.Arrays;
import java.util.Date;

public class CookieUtils {

    public static final String SESSION_COOKIE_NAME = "SESSION_ID";
    private static final String COOKIE_PATH = "/";

    // Per sviluppo locale HTTP: Secure=false, SameSite=Lax
    // Il FE deve usare il proxy per le chiamate API (same-origin)
    private static final boolean SECURE = false;
    private static final boolean HTTP_ONLY = true;  // false per debug
    private static final String SAME_SITE = "Lax";
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


