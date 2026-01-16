package it.ey.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StakeHolderDTO extends CampiTecniciDTO {
    private Long id;
    private Long idPiao;
    private String nomeStakeHolder;
    private String relazionePA;
}