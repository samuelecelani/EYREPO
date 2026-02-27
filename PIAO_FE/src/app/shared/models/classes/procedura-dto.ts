import { CampiTecniciDTO } from './campi-tecnici-dto';

export class ProceduraDTO extends CampiTecniciDTO {
  id?: number;
  denominazione?: string;
  descrizione?: string;
  unitaMisura?: string;
  misurazione?: string;
  target?: string;
  uffResponsabile?: string;
}
