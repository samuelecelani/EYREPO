package it.ey.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class RichiestaApprovazioneDTO extends BaseDTO {

    private Long id;
    private Long idPiao;

    private String statoPiao;

    private String mail;
    private String oggetto;
    private String testo;
}