package it.ey.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class PiaoEmailInfoDTO {

    private Long idPiao;
    private String denominazione;
    private String versione;
    private String tipologia;
    private String tipologiaOnline;
    private String denominazionePA;
}

