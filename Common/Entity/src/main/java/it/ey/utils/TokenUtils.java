package it.ey.utils;

import org.springframework.stereotype.Component;

import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class TokenUtils {

    private final ObjectMapper objectMapper = new ObjectMapper();


    public static Instant getRefreshTokenExpiry(long refreshExpiresInSeconds) {
        return Instant.now().plusSeconds(refreshExpiresInSeconds);
    }

}
