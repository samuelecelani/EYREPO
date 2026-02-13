package it.ey.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Session {

    @Id
    private Long id;
    private String sessionId;
    private String state;
    private String codeVerifier;
    private String originUri;
    private String redirectUri;
    private boolean autenticato ;
    private Instant scadenza;

    // Costruttore, getter e setter
}
