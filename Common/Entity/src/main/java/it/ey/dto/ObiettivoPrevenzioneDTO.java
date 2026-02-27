package it.ey.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class ObiettivoPrevenzioneDTO extends CampiTecniciDTO {
    private Long id;

    private Long idSezione23;

    private String denominazione;

    private String descrizione;

    private String codice;

    private List<ObiettivoPrevenzioneIndicatoreDTO> indicatori;

    @JsonIgnore
    private List<MisuraPrevenzioneDTO> misurePrevenzione;

}
