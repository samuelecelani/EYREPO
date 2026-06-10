import { CampiTecniciDTO } from './campi-tecnici-dto';

export class CaricaPiaoDTO extends CampiTecniciDTO {
  dataApprovazione?: Date;
  autorità?: number;
  estremiAtto?: string;
  triennio?: number;
  numDipendenti?: string;
  url?: string;
}
