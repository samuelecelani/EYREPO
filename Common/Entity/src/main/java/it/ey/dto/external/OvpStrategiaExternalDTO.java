package it.ey.dto.external;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OvpStrategiaExternalDTO {

    private Long id;
    private String codStrategia;
    private String denominazioneStrategia;
    private List<IndicatoreExternalDTO> indicatori;
    private List<ObiettivoExternalDTO> obbiettivoPerformance;
    private List<ObiettivoExternalDTO> obbiettivoDiPienaAccessibilitaDigitale;
    private List<ObiettivoExternalDTO> obbiettivoDiPienaAccessibilitaFisica;
    private List<ObiettivoExternalDTO> obbiettivoDiSemplificazione;
    private List<ObiettivoExternalDTO> obbiettivoDiPariOpportunita;
    private List<ObiettivoExternalDTO> obbiettivoDiPerformanceOrganizzativa;
    private List<ObiettivoExternalDTO> obbiettivoDiPerformanceIndividuale;
}
