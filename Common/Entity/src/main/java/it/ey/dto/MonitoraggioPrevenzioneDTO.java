package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class MonitoraggioPrevenzioneDTO extends CampiTecniciDTO
{
    private Long id;
    private Long idMisuraPrevenzioneEventoRischio;
    private String tipologia;
    private String descrizione;
    private String responsabile;
    private String tempistiche;
}
