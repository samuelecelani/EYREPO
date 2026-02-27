import { CodTipologiaSezioneEnum } from '../enums/cod-tipologia-sezione.enum';
import { CampiTecniciDTO } from './campi-tecnici-dto';

export class TabellaValidazioneDTO extends CampiTecniciDTO {
  selected?: boolean;
  id?: number;
  triennio?: string;
  sezione?: string;
  sezioneEnum?: CodTipologiaSezioneEnum;
  statoValidazione?: string;
  profUtenteInvioRichiesta?: string;
  dataInvioRichiesta?: Date;
  profUtenteValidazione?: string;
  dataValidazione?: Date;
  osservazioni?: string;
}
