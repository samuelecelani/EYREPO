import { CampiTecniciDTO } from './campi-tecnici-dto';

export class AutoritaApprovatoreDTO extends CampiTecniciDTO {
  id!: number;
  codice!: string;
  testo!: string;
  idPiao?: number;
  statoSezione?: string;
}
