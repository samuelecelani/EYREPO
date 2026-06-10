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
    private String externalId;
    private String codePA;
    private String fiscalCode;
    private String email;
    private String numeroTelefono;
    private String qualifica;
    private String denominazionePA;
    private List<RuoloUserDTO> ruoli;

}
