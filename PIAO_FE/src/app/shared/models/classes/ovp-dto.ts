import { CampiTecniciDTO } from './campi-tecnici-dto';
import { OVPAreaOrganizzativaDTO } from './ovp-area-organizzativa-dto';
import { OVPPrioritaPoliticaDTO } from './ovp-priorita-politica-dto';
import { OVPStakeHolderDTO } from './ovp-stakeholder-dto';
import { OVPStrategiaDTO } from './ovp-strategia-dto';
import { RisorsaFinanziariaDTO } from './risorsa-finanziaria-dto';

export class OVPDTO extends CampiTecniciDTO {
  id?: number;
  denominazione?: string;
  codice?: string;
  descrizione?: string;
  contesto?: string;
  ambito?: string;
  responsabilePolitico?: string;
  responsabileAmministrativo?: string;
  valoreIndice?: number;
  descrizioneIndice?: string;
  sezione21Id?: number;
  areeOrganizzative?: OVPAreaOrganizzativaDTO[];
  prioritaPolitiche?: OVPPrioritaPoliticaDTO[];
  stakeholders?: OVPStakeHolderDTO[];
  ovpStrategias?: OVPStrategiaDTO[];
  risorseFinanziarie?: RisorsaFinanziariaDTO[];
}
