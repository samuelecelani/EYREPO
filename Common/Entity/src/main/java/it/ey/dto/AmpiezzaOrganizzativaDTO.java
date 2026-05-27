package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class AmpiezzaOrganizzativaDTO extends BaseDTO {
    private Long id;
    private Long idSezione31;
    private String unitaOrganizzativa;
    private String numRisorseUmane;
}
