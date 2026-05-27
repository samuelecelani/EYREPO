import { AllegatoDTO } from './allegato-dto';
import { AmpiezzaOrganizzativaDTO } from './ampiezza-organizzativa-dto';
import { CampiTecniciDTO } from './campi-tecnici-dto';
import { TabellaFunzionaleDTO } from './tabella-funzionale-dto';
import { UlterioriInfoDTO } from './ulteriori-info-dto';

export class Sezione31DTO extends CampiTecniciDTO {
  id!: number;
  idPiao!: number;
  statoSezione!: string;
  // Struttura organizzativa al 31.12 dell'anno precedente
  strutturaOrganizzativaAP!: string;
  // Ampiezza organica
  ampiezzaOrganica!: string;
  // Incarichi dirigenziali e simili
  incarichiDirigenziali!: string;
  // Rappresentazione dei profili professionali
  profiliProfessionali!: string;
  // Linee organizzazione
  lineeOrganizzazione!: string;

  ampiezzaOrganizzative?: AmpiezzaOrganizzativaDTO[];

  tabelleFunzionali?: TabellaFunzionaleDTO[];

  ulterioriInfo?: UlterioriInfoDTO;

  //Allegati
  allegati?: AllegatoDTO[];

  campiModificati?: string;
  testoSezione?: string;

  // NUOVO FLAG
  graficoMinerva?: boolean;
}
