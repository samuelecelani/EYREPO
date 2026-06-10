package it.ey.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmministrazioneDTO {

    private String codiceIpa;
    private String denominazione;
    private String tipologia;
}

