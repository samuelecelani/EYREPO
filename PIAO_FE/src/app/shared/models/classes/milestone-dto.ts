import { CampiTecniciDTO } from './campi-tecnici-dto';

export class MilestoneDTO extends CampiTecniciDTO {
  id?: number;
  idSottofaseMonitoraggio?: number;
  descrizione?: string;
  data?: Date;
  isPromemoria?: boolean;
  dataPromemoria?: Date;
  idPromemoria?: number;
}
