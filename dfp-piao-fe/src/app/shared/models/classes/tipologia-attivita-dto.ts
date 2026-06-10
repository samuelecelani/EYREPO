import { CampiTecniciDTO } from './campi-tecnici-dto';

export class TipologiaAttivitaDTO extends CampiTecniciDTO {
  id!: number;
  codice!: string;
  testo!: string;
}
