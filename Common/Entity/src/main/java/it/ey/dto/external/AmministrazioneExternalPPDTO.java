package it.ey.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmministrazioneExternalPPDTO {

    private String codiceIpa;
    private String denominazione;
    private String tipologia;
}

