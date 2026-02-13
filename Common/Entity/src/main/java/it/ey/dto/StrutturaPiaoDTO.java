package it.ey.dto;


import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class StrutturaPiaoDTO extends CampiTecniciDTO{
    private Long id;
    private String numeroSezione;
    private String testo;
    private List<StrutturaPiaoDTO> children ;
    private String statoSezione;
}

