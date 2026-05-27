package it.ey.dto;

import it.ey.enums.CodTipologiaCategoria;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class CategoriaObiettiviTipDTO extends BaseDTO {
    private Long id;

    private String testo;

    private CodTipologiaCategoria codTipologiaFK;
}
