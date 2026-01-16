package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OVPStrategiaDTO extends CampiTecniciDTO{
    private Long id;
    @JsonIgnore
    private OVPDTO ovp;

    private String codStrategia;

    private String denominazioneStrategia;

    private String descrizioneStrategia;

    private String soggettoResponsabile;
}
