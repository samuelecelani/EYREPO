package it.ey.dto;


import lombok.*;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StrutturaPiaoDTO extends CampiTecniciDTO{
    private Long id;
    private String numeroSezione;
    private String testo;
    private List<StrutturaPiaoDTO> children ;
    private String statoSezione;
}

