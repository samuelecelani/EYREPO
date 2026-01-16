package it.ey.dto;


import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AreaOrganizzativaDTO extends CampiTecniciDTO {
    private Long id;
    private Long idSezione1;
    private String nomeArea;
    private String descrizioneArea;

}
