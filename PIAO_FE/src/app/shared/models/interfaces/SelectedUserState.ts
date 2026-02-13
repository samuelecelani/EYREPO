import { PARiferimentoDTO } from '../classes/pa-riferimento-dto';
import { UtenteRuoloPaDTO } from '../classes/utente-ruolo-pa-dto';

export interface SelectedUserState {
  user: UtenteRuoloPaDTO;
  paRiferimento: PARiferimentoDTO;
}
