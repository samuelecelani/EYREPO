package it.ey.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaRiferimentoDTO {

    private boolean attiva;
    private String codePA;
    private String denominazionePA;
    private List<RuoloUserDTO> ruoli;

}
