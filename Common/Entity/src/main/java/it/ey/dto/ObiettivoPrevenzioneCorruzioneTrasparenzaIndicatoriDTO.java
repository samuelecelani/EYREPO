package it.ey.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoriDTO extends CampiTecniciDTO{

    private Long id;
    private Long idObiettivoPrevenzioneCorruzioneTrasparenza;


    private IndicatoreDTO indicatore;
}
