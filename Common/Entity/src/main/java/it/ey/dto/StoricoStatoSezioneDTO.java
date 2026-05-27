package it.ey.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class StoricoStatoSezioneDTO extends CampiTecniciDTO {

    private Long id;

    private String testoStato;

    private Long idEntitaFK;

    private String codTipologiaFK;

    private String testo;

    private Boolean rifiutato;

    private Boolean revocato;

    private Boolean annullato;

    private String osservazioni;
}
