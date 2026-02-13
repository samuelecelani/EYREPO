import { CampiTecniciDTO } from './campi-tecnici-dto';
import { Sezione1DTO } from './sezione-1-dto';
import { Sezione21DTO } from './sezione-21-dto';
import { Sezione22DTO } from './sezione-22-dto';
import { Sezione23DTO } from './sezione-23-dto';
import { Sezione31DTO } from './sezione-31-dto';
import { StakeHolderDTO } from './stakeholder-dto';

export class PIAODTO extends CampiTecniciDTO {
  id?: number;
  codPAFK?: string;
  denominazione?: string;
  versione?: string;
  tipologia?: string;
  tipologiaOnline?: string;
  statoPiao?: string;
  aggiornamento?: boolean;
  sezione1?: Sezione1DTO;
  sezione21?: Sezione21DTO;
  sezione22?: Sezione22DTO;
  sezione23?: Sezione23DTO;
  sezione31?: Sezione31DTO;
  sezione32?: any;
  sezione331?: any;
  sezione332?: any;
  stakeHolders?: StakeHolderDTO[];
}
