package it.ey.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Sezione1DTO extends SezioneBaseDTO{
    private Long id;
    private Long idPiao;
    private String statoSezione;

    private String quadroNormativo;
    private String strutturaProgrammatica;
    private String cronoprogramma;
    private String missione;
    private List<AreaOrganizzativaDTO> areeOrganizzative;
    private List<PrioritaPoliticaDTO> prioritaPolitiche;
    private List<PrincipioGuidaDTO> principiGuida;
    private List<OrganoPoliticoDTO> organiPolitici;
    private List<IntegrationTeamDTO> integrationTeams;
    private AnagraficaDTO anagrafica;
    private List<StakeHolderDTO> stakeHolders;



}

