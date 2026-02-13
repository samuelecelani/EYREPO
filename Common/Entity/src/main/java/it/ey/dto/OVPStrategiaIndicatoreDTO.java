package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class OVPStrategiaIndicatoreDTO extends CampiTecniciDTO{
    private Long id;
    @JsonIgnore
    private OVPDTO ovp;

    @JsonIgnore
    private OVPStrategiaDTO ovpStrategia;

    private IndicatoreDTO indicatore;
}
