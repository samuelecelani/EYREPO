package it.ey.dto;

import it.ey.entity.EventoRischio;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Sezione23DTO extends  CampiTecniciDTO {

    private Long id;
    private Long idPiao;
    private Long idStato;
    private String statoSezione;
    private String introAdempimentiNormativi;
    private String impattoContestoExt;
    private String impattoContestoInt;
    private String descrGestioneRischio;
    private String descrIdentificazioneRischio;
    private String descrAnalisiRischio;
    private String descrMisurazioneRischio;
    private String descrTrattamentoRischio;
    private String descrMonitoraggioRischio;
    private String introObiettivoPrevenzione;
    private String introMisurePrevenzione;
    private String introValorePubblico;
    private String introAttivitaSensibili;
    private String introValutazioneRischio;
    private String introGestioneRischio;
    private String introMonitoraggio;
    private String descrTrasparenza;
    private  UlterioriInfoDTO ulterioriInfo;
    private List<AllegatoDTO> allegati;
    private List <ObiettivoPrevenzioneDTO> obiettivoPrevenzione;
    private List<AdempimentiNormativiDTO> adempimentiNormativi;
    private List <AttivitaSensibileDTO> attivitaSensibile;
    private List<AttoreDTO> attore;
    private List <ObbligoLeggeDTO> obblighiLegge;
    private List <ObiettivoPrevenzioneCorruzioneTrasparenzaDTO> obiettivoPrevenzioneCorruzioneTrasparenza;
    private List<EventoRischioDTO> eventoRischio;
    private List<MisuraPrevenzioneDTO> misuraPrevenzione;

}
