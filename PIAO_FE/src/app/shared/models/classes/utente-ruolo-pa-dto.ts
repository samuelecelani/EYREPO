import { UtenteRuoliPaSezioneDTO } from './utente-ruoli-pa-sezione-dto';
import { UtenteRuoloDTO } from './utente-ruolo-dto';
import { UtentePaDTO } from './utente-pa-dto';

export class UtenteRuoloPaDTO {
  id?: number;
  codiceFiscale?: string;
  nome?: string;
  cognome?: string;
  email?: string;
  numeroTelefono?: string;
  ruoli?: UtenteRuoloDTO[];
  codicePA?: UtentePaDTO[];
  sezioni?: UtenteRuoliPaSezioneDTO[];
}
