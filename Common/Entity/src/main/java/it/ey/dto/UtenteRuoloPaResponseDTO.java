package it.ey.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UtenteRuoloPaResponseDTO {

    private Long id;

    private String codiceFiscale;

    private String codiceRuolo;

    private String codicePA;

    private List<UtenteRuoliPaSezioneDTO> sezioni;

    private Boolean success;

    private String message;
}

