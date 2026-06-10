import { ObiettiviRisultatiFotografiaDTO } from './obiettivi-risultati-fotografia-dto';
import { TabellaFunzionaleDTO } from './tabella-funzionale-dto';
import { AttivitaFormativeDTO } from './attivita-formativa-dto';

export class Sezione332DTO {
  id?: number;
  idPiao?: number;
  statoSezione?: string;
  contestoNormativo?: string;
  descrizioneQualitativa?: string;
  descrizioneStrategia?: string;
  descrizioneRisorse?: string;
  descrizioneIncentivi?: string;

  obiettiviRisultatiFotografia?: ObiettiviRisultatiFotografiaDTO[];

  tabelleFunzionali?: TabellaFunzionaleDTO[];

  attivitaFormative?: AttivitaFormativeDTO[];

  campiModificati?: string;
  testoSezione?: string;
}
