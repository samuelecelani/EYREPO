package it.ey.dto.external;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PiaoExcelDTO extends PiaoExternalDTO {

    private String codPAFK;

    private String denominazione;

    private String tipologia;
}

