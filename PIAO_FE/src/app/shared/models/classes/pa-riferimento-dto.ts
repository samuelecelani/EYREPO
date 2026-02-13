import { RuoloUtenteDTO } from "./ruolo-utente-dto";

export class PARiferimentoDTO {
  attiva!: boolean;
  codePA!: string;
  denominazionePA!: string;
  ruoli!: RuoloUtenteDTO[];
}
