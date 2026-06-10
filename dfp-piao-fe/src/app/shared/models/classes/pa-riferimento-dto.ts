import { RuoloUtenteDTO } from './ruolo-utente-dto';

export class PARiferimentoDTO {
  attiva!: boolean;
  externalId?: string;
  codePA!: string;
  email?: string;
  fiscalCode?: string;
  numeroTelefono?: string;
  qualifica?: string;
  denominazionePA!: string;
  ruoli!: RuoloUtenteDTO[];
}
