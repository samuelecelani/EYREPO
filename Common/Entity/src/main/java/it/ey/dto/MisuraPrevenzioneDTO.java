package it.ey.dto;


import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class MisuraPrevenzioneDTO extends CampiTecniciDTO{

    private Long id;

    private Long idObiettivoPrevenzione;

    private Long idSezione23;

    private String denominazione;

    private String descrizione;

    private String codice;

    private String responsabileMisura;

    private List<MisuraPrevenzioneIndicatoreDTO> indicatori;

    private List<MisuraPrevenzioneStakeholderDTO> stakeholder;
}
