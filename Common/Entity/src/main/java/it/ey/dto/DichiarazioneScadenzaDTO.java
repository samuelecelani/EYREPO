package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DichiarazioneScadenzaDTO extends CampiTecniciDTO
{
    private Long id;
    private Long annoRiferimento;
    private LocalDate dataPubblicazione;
    private String note;
    private Long idMotivazioneDichiarazione;
    private String descrizione;
    private String responsabile;
    private Long idPiao;
}
