package it.ey.dto;


import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class StakeHolderDTO extends CampiTecniciDTO {
    private Long id;
    private Long idPiao;
    private String nomeStakeHolder;
    private String relazionePA;
}