import { CampiTecniciDTO } from './campi-tecnici-dto';
import { DatiPubblicatiDTO } from './dati-pubblicati-dto';

export class ObbligoLeggeDTO extends CampiTecniciDTO {
  id?: number;
  idSezione23?: number;
  denominazione?: string;
  descrizione?: string;
  datiPubblicati?: DatiPubblicatiDTO[];
}
