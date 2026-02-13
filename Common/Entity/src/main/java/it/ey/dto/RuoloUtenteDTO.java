package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuoloUtenteDTO {

    private Long id;

    private String codiceRuolo;
    private String nomeRuolo;
    @JsonIgnore
    private UtenteRuoloPaDTO utente;
}

