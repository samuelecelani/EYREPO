package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class OVPStrategiaDTO extends CampiTecniciDTO{
    private Long id;

    private String codStrategia;

    private String denominazioneStrategia;

    private String descrizioneStrategia;

    private String soggettoResponsabile;

    private List<OVPStrategiaIndicatoreDTO> indicatori;

}
