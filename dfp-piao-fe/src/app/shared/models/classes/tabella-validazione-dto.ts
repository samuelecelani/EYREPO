import { CodTipologiaSezValEnum } from '../enums/cod-tipologia-sez-val.enum';
import { CodTipologiaSezioneEnum } from '../enums/cod-tipologia-sezione.enum';
import { CampiTecniciDTO } from './campi-tecnici-dto';

export class TabellaValidazioneDTO extends CampiTecniciDTO {
  id!: number;
  numeroSezione!: string;
  testo!: string;
  statoSezione?: string;
  disabled?: boolean;
  selected?: boolean;
  triennio?: string;
  sezione?: string;
  sezioneEnum?: CodTipologiaSezValEnum;
  statoValidazione?: string;
  profUtenteInvioRichiesta?: string;
  dataInvioRichiesta?: Date;
  profUtenteValidazione?: string;
  dataValidazione?: Date;
  osservazioni?: string;
}
