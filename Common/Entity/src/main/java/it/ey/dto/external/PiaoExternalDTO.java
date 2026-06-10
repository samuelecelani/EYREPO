package it.ey.dto.external;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PiaoExternalDTO {

    private AnagraficaExternalDTO anagrafica;

    private List<OvpExternalDTO> ovp;

    private List<PopolazioneSuddivisaEtaExternalDTO> popolazioneSuddivisaEta;
}
