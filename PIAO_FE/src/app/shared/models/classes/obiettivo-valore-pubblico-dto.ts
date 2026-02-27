import { CampiTecniciDTO } from './campi-tecnici-dto';

// TODO: Confermare struttura con backend - DTO creato da Figma
export class ObiettivoValorePubblicoDTO extends CampiTecniciDTO {
  id?: number;
  idSezione?: number;
  nomeObiettivo?: string;
  descrizioneObiettivo?: string;
  strategiaAttuativa?: string;
}
