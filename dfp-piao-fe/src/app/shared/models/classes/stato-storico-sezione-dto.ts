import { CampiTecniciDTO } from './campi-tecnici-dto';

export class StatoSezioneDTO {
  id?: number;
  testo?: string;
}

export class StatoStoricoSezioneDTO extends CampiTecniciDTO {
  id?: number;
  statoSezioneDTO?: StatoSezioneDTO;
  codTipologiaFK?: string;
  idEntitaFK?: number;
  testo?: string;
  rifiutato?: boolean;
  revocato?: boolean;
  annullato?: boolean;
  osservazioni?: string;
}
