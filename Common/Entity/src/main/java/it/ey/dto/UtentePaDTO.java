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
    private String nome;
    @JsonIgnore
    private UtenteRuoloPaDTO utente;
}
