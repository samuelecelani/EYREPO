import { CampiTecniciDTO } from './campi-tecnici-dto';
import { AttoreDTO } from './attore-dto';
import { AttivitaDTO } from './attivita-dto';

export class FaseDTO extends CampiTecniciDTO {
  id?: number;
  idSezione22?: number;
  denominazione?: string;
  descrizione?: string;
  tempi?: string;
  attore?: AttoreDTO;
  attivita?: AttivitaDTO;
}
