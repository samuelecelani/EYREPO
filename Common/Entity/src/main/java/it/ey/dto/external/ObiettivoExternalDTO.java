package it.ey.dto.external;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObiettivoExternalDTO {

    private Long id;
    private String codice;
    private String tipologia;
    private String denominazione;
    private Long idObiettivoPeformance;
    private String risorseEconomicaFinanziaria;
    private String risorseStrumentali;
    private String tipologiaRisorsa;
    private List<IndicatoreExternalDTO> indicatori;
}
