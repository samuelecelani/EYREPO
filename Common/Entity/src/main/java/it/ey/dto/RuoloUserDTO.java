package it.ey.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RuoloUserDTO {
    private String codice;
    private String descrizione;
    private boolean ruoloAttivo;
    private List<String> sezioneAssociata;

}
