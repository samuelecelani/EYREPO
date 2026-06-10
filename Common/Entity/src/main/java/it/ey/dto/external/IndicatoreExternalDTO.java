package it.ey.dto.external;

import it.ey.dto.TipologiaAndamentoValoreIndicatoreDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndicatoreExternalDTO {

    private String codTipologiaFK;
    private String denominazione;
    private String unitaMisura;
    private Long peso;
    private String polarita;
    private Long baseLine;
    private String fonteDati;
    private TipologiaAndamentoValoreIndicatoreDTO tipAndValAnnoCorrente;
    private TipologiaAndamentoValoreIndicatoreDTO tipAndValAnno1;
    private TipologiaAndamentoValoreIndicatoreDTO tipAndValAnno2;
}
