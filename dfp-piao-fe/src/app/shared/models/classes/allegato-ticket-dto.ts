import { CampiTecniciDTO } from './campi-tecnici-dto';

export class AllegatoTicketDTO extends CampiTecniciDTO {
  id?: number;
  idTicketFk?: number;
  codDocumento?: string;
  descrizione?: string;
  sizeAllegato?: string;
  /** Esito antivirus: NO_THREATS_FOUND | THREATS_FOUND | FAILED | UNSUPPORTED */
  esitoAntivirus?: string;
  idModulo?: string;
  codiceFiscale?: string;
  codicePa?: string;
}
