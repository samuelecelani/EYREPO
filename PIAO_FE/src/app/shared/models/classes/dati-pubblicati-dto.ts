import { UlterioriInfoDTO } from './ulteriori-info-dto';

export class DatiPubblicatiDTO {
  id?: number;
  idObbligoLegge?: number;
  denominazione?: string;
  tipologia?: string;
  responsabile?: string;
  terminiScadenza?: string;
  modalitaMonitoraggio?: string;
  motivazioneImpossibilita?: string;
  ulterioriInfo?: UlterioriInfoDTO;
}
