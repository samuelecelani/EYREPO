import { PARiferimentoDTO } from "./pa-riferimento-dto";

export class UtenteDTO {
  nome!: string;
  cognome?: string;
  email?: string;
  username?: string;
  codiceFiscale?: string;
  dataNascita?: Date
  luogoDiNascita?: string
  typeAuthority?: string;
  paRiferimento!: PARiferimentoDTO[];
}


