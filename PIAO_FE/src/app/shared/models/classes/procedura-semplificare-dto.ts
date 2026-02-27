import { CampiTecniciDTO } from './campi-tecnici-dto';

// TODO: Confermare struttura con backend - DTO creato da Figma
export class ProceduraSemplificareDTO extends CampiTecniciDTO {
  id?: number;
  idSezione?: number;
  procedura?: string;
  descrizione?: string;
  unitaMisura?: string;
  misurazioneAttuale?: string;
  target?: string;
  ufficioResponsabile?: string;
}
