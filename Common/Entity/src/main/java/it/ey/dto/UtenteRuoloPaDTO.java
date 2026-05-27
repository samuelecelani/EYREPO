package it.ey.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtenteRuoloPaDTO {

    private String id;

    private String codiceFiscale;
    private String nome;
    private String cognome;
    private String email;
    private String numeroTelefono;
    private LocalDate dataNascita;
    private String luogoNascita;
    private List<RuoloUtenteDTO> ruoli;
    private List<UtentePaDTO> codicePA;
    private List<UtenteRuoliPaSezioneDTO> sezioni;
}

