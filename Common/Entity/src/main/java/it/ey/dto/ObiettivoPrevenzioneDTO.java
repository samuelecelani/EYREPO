package it.ey.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObiettivoPrevenzioneDTO {
    private Long id;

    private Long idSezione23;

    private String denominazione;

    private String descrizione;

    private String codice;

    private List<ObiettivoPrevenzioneIndicatoreDTO> indicatori;

    @JsonIgnore
    private List<MisuraPrevenzioneDTO> misurePrevenzione;

}
