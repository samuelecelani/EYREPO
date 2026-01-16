package it.ey.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Sezione1DTO extends CampiTecniciDTO{
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
    private UlterioriInfoDTO ulterioriInfoDTO;
    private List<AllegatoDTO> allegati;
    private SocialDTO social;
}

