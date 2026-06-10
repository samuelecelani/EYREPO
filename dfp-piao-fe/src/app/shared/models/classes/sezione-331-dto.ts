import { TabellaFunzionaleDTO } from './tabella-funzionale-dto';

export class Sezione331DTO {
  id?: number;

  idPiao?: number;

  contesto?: string;
  descrizioneQualitativa?: string;
  strategiaProgrammazione?: string;
  obiettivoTrasformazione?: string;
  rimodulazione?: boolean;
  strategiaCopertura?: string;
  descrizioneStrategia?: string;
  stimaEvoluzione?: string;

  tabelleFunzionali?: TabellaFunzionaleDTO[];

  statoSezione?: string;
  campiModificati?: string;
  testoSezione?: string;
}
