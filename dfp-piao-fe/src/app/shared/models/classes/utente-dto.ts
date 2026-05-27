import { PARiferimentoDTO } from './pa-riferimento-dto';

export class UtenteDTO {
  nome!: string;
  cognome?: string;
  username?: string;
  externalId?: string;
  fiscalCode?: string;
  dataNascita?: string;
  luogoDiNascita?: string;
  typeAuthority?: string;
  paRiferimento!: PARiferimentoDTO[];
}
