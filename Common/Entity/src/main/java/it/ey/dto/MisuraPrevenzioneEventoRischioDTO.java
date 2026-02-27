package it.ey.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MisuraPrevenzioneEventoRischioDTO extends CampiTecniciDTO {

    private Long id;

    private String codice;

    private String denominazione;

    private String descrizione;

    private String responsabile;

    private Long idEventoRischio;

    private Long idObiettivoPrevenzioneCorruzioneTrasparenza;

    private List<MisuraPrevenzioneEventoRischioIndicatoreDTO> indicatori;


    private List<MisuraPrevenzioneEventoRischioStakeholderDTO> stakeholder;

    private List<MonitoraggioPrevenzioneDTO> monitoraggioPrevenzione;



}

