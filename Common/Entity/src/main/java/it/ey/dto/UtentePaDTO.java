package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtentePaDTO {
    private Long id;
    private String codicePa;
    private String amministrazioneId;
    private String nome;
    private String phone;
    private String email;
    private String qualifica;
    @JsonIgnore
    private UtenteRuoloPaDTO utente;
}
