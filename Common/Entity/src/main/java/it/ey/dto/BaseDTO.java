package it.ey.dto;

import it.ey.enums.TypeErrorEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class BaseDTO extends CampiTecniciDTO {
    private String campiModificati;
    private String testoSezione;
    private Long idPiao;
    private String statoSezione;
    private String messageError;
    private String errorCode;
    private TypeErrorEnum typeEnum;
}
