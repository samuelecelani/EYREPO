import { AllegatoDTO } from './allegato-dto';
import { AreaOrganizzativaDTO } from './area-organizzativa-dto';
import { CampiTecniciDTO } from './campi-tecnici-dto';
import { IntegrationTeamDTO } from './integration-team-dto';
import { OrganoPoliticoDTO } from './organo-politico-dto';
import { PrincipioGuidaDTO } from './principio-guida-dto';
import { PrioritaPoliticaDTO } from './priorita-politica-dto';
import { SocialDTO } from './social-dto';
import { StakeHolderDTO } from './stakeholder-dto';
import { UlterioriInfoDTO } from './ulteriori-info-dto';

export class Sezione1DTO extends CampiTecniciDTO {
  id?: number;
  idPiao?: number;
  statoSezione?: string;
  quadroNormativo?: string;
  strutturaProgrammatica?: string;
  cronoprogramma?: string;
  missione?: string;
  areeOrganizzative?: AreaOrganizzativaDTO[];
  prioritaPolitiche?: PrioritaPoliticaDTO[];
  principiGuida?: PrincipioGuidaDTO[];
  organiPolitici?: OrganoPoliticoDTO[];
  integrationTeams?: IntegrationTeamDTO[];
  ulterioriInfoDTO?: UlterioriInfoDTO;
  stakeHolders?: StakeHolderDTO[];
  allegati?: AllegatoDTO[];
  social?: SocialDTO;
}
