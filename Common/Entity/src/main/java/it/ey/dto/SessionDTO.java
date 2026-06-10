package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {
    private Long id;
    private String sessionId;
    private String state;
    private String codeVerifier;
    private String originUri;
    private String redirectUri;
    private boolean autenticato ;
    private Instant scadenza;



}