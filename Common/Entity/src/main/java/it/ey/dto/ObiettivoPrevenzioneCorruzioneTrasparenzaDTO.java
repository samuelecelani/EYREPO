package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ObiettivoPrevenzioneCorruzioneTrasparenzaDTO  extends CampiTecniciDTO{

    private Long id;

    //Relazioni

    private Long idSezione23;
    private Long idOVP;
    private Long idStrategiaOVP;
    private Long idObbiettivoPerformance;


    private String codice;
    private String denominazione;
    private String descrizione;



     private List<ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoriDTO> indicatori;


}
