import { MisuraGeneraleIndicatoreDTO } from './misura-generale-indicatore-dto';
import { MisuraGeneraleStakeholderDTO } from './misura-generale-stakeholder-dto';

export class MisuraGeneraleDTO {
  id?: number;
  idObiettivoPrevenzione?: number;
  codice?: string;
  denominazione?: string;
  descrizione?: string;
  responsabileMisura?: string;
  indicatori?: MisuraGeneraleIndicatoreDTO[];
  stakeHolders?: MisuraGeneraleStakeholderDTO[];
}
