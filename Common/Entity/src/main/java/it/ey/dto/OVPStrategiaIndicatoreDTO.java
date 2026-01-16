package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OVPStrategiaIndicatoreDTO extends CampiTecniciDTO{
    private Long id;
    @JsonIgnore
    private OVPDTO ovp;

    private Long idOvp;

    private OVPStrategiaDTO ovpStrategia;

    private IndicatoreDTO indicatoreDTO;
}
