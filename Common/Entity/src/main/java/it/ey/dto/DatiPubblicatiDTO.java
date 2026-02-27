package it.ey.dto;

import it.ey.entity.UlterioriInfo;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatiPubblicatiDTO {

    private Long id;

    // Relazione con ObbligoLegge
    private Long idObbligoLegge;

    private String denominazione;

    private String tipologia;

    private String responsabile;

    private String terminiScadenza;

    private String modalitaMonitoraggio;

    private String motivazioneImpossibilita;

    // Questo è il campo NoSQL, valorizzato dal service
    private UlterioriInfoDTO ulterioriInfo;

}
