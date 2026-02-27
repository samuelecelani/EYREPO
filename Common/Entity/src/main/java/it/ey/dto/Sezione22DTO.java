package it.ey.dto;

import it.ey.entity.campiTecnici.CampiTecnici;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Sezione22DTO extends CampiTecniciDTO{

    private  Long id;
    private  Long idPiao;
    private String statoSezione;
    private  String  introPerformance;
    private  String  introObiettiviPerformance;
    private  String  introAdempimenti;
    private  String  introPerformanceOrganizzativa;
    private  String descriptionCollegamentoPerformance;
    private  String introPerformanceIndividuale;
    private List<FaseDTO> fase;
    private List<ObbiettivoPerformanceDTO> obbiettiviPerformance;
    private UlterioriInfoDTO ulterioriInfo;
    private List<AllegatoDTO> allegati;
    private List<AdempimentoDTO> adempimenti;

}

