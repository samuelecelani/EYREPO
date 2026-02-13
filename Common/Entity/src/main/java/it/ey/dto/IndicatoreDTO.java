package it.ey.dto;

import it.ey.enums.CodTipologiaIndicatoreEnum;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)

public class IndicatoreDTO extends CampiTecniciDTO {
    private Long id;
    private Long idPiao;
    private Long idEntitaFK;
    private CodTipologiaIndicatoreEnum codTipologiaFK;
    private String denominazione;
    private Long idSubDimensioneFK;
    private Long idDimensioneFK;
    private String unitaMisura;
    private String formula;
    private Long peso;
    private String polarita;
    private Long baseLine;
    private Long consuntivo;
    private String fonteDati;
    private TipologiaAndamentoValoreIndicatoreDTO tipAndValAnnoCorrente;
    private TipologiaAndamentoValoreIndicatoreDTO tipAndValAnno1;
    private TipologiaAndamentoValoreIndicatoreDTO tipAndValAnno2;
    private Boolean rilevante;
    private UlterioriInfoDTO addInfo;
}

