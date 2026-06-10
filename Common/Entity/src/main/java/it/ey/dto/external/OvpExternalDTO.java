package it.ey.dto.external;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvpExternalDTO {

    private Long id;
    private String codice;
    private String denominazione;
    private List<OvpStrategiaExternalDTO> ovpStrategias;
}
