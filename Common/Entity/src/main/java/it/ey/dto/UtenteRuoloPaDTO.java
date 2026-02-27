package it.ey.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtenteRuoloPaDTO {

    private Long id;

    private String codiceFiscale;
    private String nome;
    private String cognome;
    private String email;
    private String numeroTelefono;
    private List<RuoloUtenteDTO> ruoli;
    private List<UtentePaDTO> codicePA;
    private List<UtenteRuoliPaSezioneDTO> sezioni;
}

