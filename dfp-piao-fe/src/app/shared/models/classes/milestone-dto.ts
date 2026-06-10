import { CampiTecniciDTO } from './campi-tecnici-dto';

export class MilestoneDTO extends CampiTecniciDTO {
  id?: number;
  idSottofaseMonitoraggio?: number;
  descrizione?: string;
  data?: string;
  isPromemoria?: boolean;
  dataPromemoria?: string;
  idPromemoria?: number;
}
