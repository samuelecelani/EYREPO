package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipologiaAndamentoValoreIndicatoreDTO {
    @JsonIgnore
    private Long id;

    private String denominazione;
}

