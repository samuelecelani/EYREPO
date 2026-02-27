package it.ey.dto;

import it.ey.enums.TipologiaAdempimento;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class AdempimentiNormativiDTO extends CampiTecniciDTO
{
    private Long id;

    private Long idSezione23;

    private String normativa;

    private String azione;
}
