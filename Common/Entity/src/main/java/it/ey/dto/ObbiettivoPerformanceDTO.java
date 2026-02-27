package it.ey.dto;

import it.ey.enums.TipologiaObbiettivo;
import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ObbiettivoPerformanceDTO extends CampiTecniciDTO {

    private Long id;

    private Long idObiettivoPeformance;

    private Long idSezione22;

    private Long idOvp;

    private Long idStrategiaOvp;

    private String codice;

    private TipologiaObbiettivo tipologia;

    private String denominazione;

    private String responsabileAmministrativo;

    private String risorseUmane;

    private String risorseEconomicaFinanziaria;

    private String risorseStrumentali;

    private String tipologiaRisorsa;

    private ContributoreInternoDTO contributoreInterno;

    private List<ObiettivoStakeHolderDTO> stakeholders;

    private List<ObiettivoIndicatoriDTO> indicatori;


}
