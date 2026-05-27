import { CampiTecniciDTO } from './campi-tecnici-dto';

export class TabellaConsultazioneDTO extends CampiTecniciDTO {
  id!: number;
  triennio?: string;
  versione?: string;
  autore?: string;
  dataApprovazione?: Date;
  dataPubblicazione?: Date;
  tipologia?: string;
}
