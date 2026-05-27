import { CampiTecniciDTO } from './campi-tecnici-dto';
import { AllegatoTicketEsternoDTO } from './allegato-ticket-esterno-dto';

export class TicketDTO extends CampiTecniciDTO {
  id?: number;
  nome?: string;
  cognome?: string;
  mail?: string;
  oggetto?: string;
  messaggio?: string;
  idModulo?: string;
  codiceFiscale?: string;
  codicePa?: string;
  idTicketEsterno?: number;
  stato?: string;
  messaggioRisposta?: string;
  idCategoriaTicket?: number;
  allegatiEsterni?: AllegatoTicketEsternoDTO[];
}
