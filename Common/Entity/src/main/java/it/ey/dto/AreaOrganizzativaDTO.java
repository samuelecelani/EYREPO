package it.ey.dto;


import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder(toBuilder = true)
public class AreaOrganizzativaDTO extends CampiTecniciDTO {
    private Long id;
    private Long idSezione1;
    private String nomeArea;
    private String descrizioneArea;

}
