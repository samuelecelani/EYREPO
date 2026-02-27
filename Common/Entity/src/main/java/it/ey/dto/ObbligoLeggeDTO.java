package it.ey.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ObbligoLeggeDTO extends CampiTecniciDTO{

    private Long id;

    private Long idSezione23;

    private String denominazione;

    private String descrizione;
    private List<DatiPubblicatiDTO> datiPubblicati;

}
