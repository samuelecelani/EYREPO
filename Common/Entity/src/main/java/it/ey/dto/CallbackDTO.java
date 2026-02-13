package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CallbackDTO {

    private String sessionId;
    private String originUri;
    private Instant refreshTokenExpiry;


}
