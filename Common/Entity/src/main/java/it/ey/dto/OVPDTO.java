package it.ey.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class OVPDTO {
    private Long id;
    private String codice;
    private String descrizione;
    private String contesto;
    private String ambito;
    private String responsabilePolitico;
    private String responsabileAmministrativo;
    private String denominazione;
    private Long valoreIndice;
    private String descrizioneIndice;
    private Long sezione21Id;
    private List<OVPAreaOrganizzativaDTO> areeOrganizzative;
    private List<OVPPrioritaPoliticaDTO> prioritaPolitiche;
    private List<OVPStakeHolderDTO> stakeholders;
    private List<OVPStrategiaDTO> ovpStrategias;
    private List<OVPRisorsaFinanziariaDTO> risorseFinanziarie;

}
