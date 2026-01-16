import { CampiTecniciDTO } from './campi-tecnici-dto';
import { Sezione1DTO } from './sezione-1-dto';

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
  sezione21?: any;
  sezione22?: any;
  sezione23?: any;
  sezione31?: any;
  sezione32?: any;
  sezione331?: any;
  sezione332?: any;
}
