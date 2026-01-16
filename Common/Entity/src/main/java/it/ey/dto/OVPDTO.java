package it.ey.dto;

import lombok.*;

import java.util.List;
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OVPDTO {
    private Long id;
    private String codice;
    private String descrizione;
    private String contesto;
    private String ambito;
    private String responsabilePolitico;
    private String responsabileAmministrativo;
    private Long valoreIndice;
    private String descrizioneIndice;
    private Long sezione21Id;
    private List<OVPAreaOrganizzativaDTO> areeOrganizzative;
    private List<OVPPrioritaPoliticaDTO> prioritaPolitiche;
    private List<OVPStakeHolderDTO> stakeholders;

}
