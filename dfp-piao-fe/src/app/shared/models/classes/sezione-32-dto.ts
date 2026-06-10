import { TabellaFunzionaleDTO } from './tabella-funzionale-dto';

export class Sezione32DTO {
  id?: number;
  idPiao?: number;
  statoSezione?: string;
  descrizioneContestoRiferimento?: string;
  descrizioneObiettiviLavoroAgile?: string;
  descrizioneStatoAttuazione?: string;
  descrizioneFattoriAbilitanti?: string;
  descrizionePersonaleAgile?: string;
  descrizioneGiornateLavorate?: string;
  descrizioneLivelloSoddisfazione?: string;
  descrizioneContributi?: string;
  descrizioneImpatti?: string;

  tabelleFunzionali?: TabellaFunzionaleDTO[];

  campiModificati?: string;
  testoSezione?: string;
}
