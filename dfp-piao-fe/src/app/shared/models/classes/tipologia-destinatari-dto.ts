import { CampiTecniciDTO } from './campi-tecnici-dto';

export class TipologiaDestinatariDTO extends CampiTecniciDTO {
  id!: number;
  codice!: string;
  testo!: string;
}
