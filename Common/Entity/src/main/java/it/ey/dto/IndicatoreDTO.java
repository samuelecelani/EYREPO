package it.ey.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndicatoreDTO extends CampiTecniciDTO {
    private Long id;
    private String denominazione;
    private String subDimensione;
    private String dimensione;
    private String unitaMisura;
    private String formula;
    private Long peso;
    private String polarita;
    private Long baseLine;
    private Long consuntivo;
    private String fonteDati;
    private TipologiaAndamentoValoreIndicatoreDTO tipAndValAnnoCorrente;
    private TipologiaAndamentoValoreIndicatoreDTO tipipAndValAnno1;
    private TipologiaAndamentoValoreIndicatoreDTO tipipAndValAnno2;
    private Boolean rilevante;
    private UlterioriInfoDTO addInfo;
}

