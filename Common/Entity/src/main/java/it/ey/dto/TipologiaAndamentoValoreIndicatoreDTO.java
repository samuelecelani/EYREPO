package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipologiaAndamentoValoreIndicatoreDTO {

    private Long id;

    private Long idTargetFK;

    private String valore;
}

