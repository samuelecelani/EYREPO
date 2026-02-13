import { AllegatoDTO } from './allegato-dto';
import { CampiTecniciDTO } from './campi-tecnici-dto';
import { FondiEuropeiDTO } from './fondi-europei-dto';
import { OVPDTO } from './ovp-dto';
import { ProceduraDTO } from './procedura-dto';
import { SocialDTO } from './social-dto';
import { SwotMinacceDTO } from './swot-minacce-dto';
import { SwotOpportunitaDTO } from './swot-opportunita-dto';
import { SwotPuntiDebolezzaDTO } from './swot-punti-debolezza-dto';
import { SwotPuntiForzaDTO } from './swot-punti-forza-dto';
import { UlterioriInfoDTO } from './ulteriori-info-dto';

export class Sezione21DTO extends CampiTecniciDTO {
  id?: number;
  idPiao?: number;
  idStato?: number;
  statoSezione?: string;

  // Parte Generale - Analisi contesto
  contestoInt?: string;
  contestoExt?: string;

  // Parte Generale - Analisi SWOT
  swotPuntiForza?: SwotPuntiForzaDTO;
  swotPuntiDebolezza?: SwotPuntiDebolezzaDTO;
  swotOpportunita?: SwotOpportunitaDTO;
  swotMinacce?: SwotMinacceDTO;

  // Parte Generale - Concetto generale di valore pubblico
  descrizioneValorePubblico?: string;

  // Parte Funzionale - Obiettivi di valore pubblico
  ovp?: OVPDTO[];

  // Parte Funzionale - Interventi sovvenzionati con fondi europei
  fondiEuropei?: FondiEuropeiDTO[];

  // Parte Funzionale - Obiettivi trasversali
  descrizioneAccessiDigitale?: string;
  descrizioneAccessiFisica?: string;
  descrizioneSemplificazione?: string;
  descrizionePariOpportunita?: string;

  // intro risorseFinanziarie, procedura, fondiEuropei
  introRisorseFinanziarie?: string;
  introFondiEuropei?: string;
  introProcedure?: string;

  // Parte Funzionale - Elenco procedure da semplificare
  procedure?: ProceduraDTO[];

  // Ulteriori informazioni (oggetto singolo, non array)
  ulterioriInfo?: UlterioriInfoDTO;

  //Allegati
  allegati?: AllegatoDTO[];

  social?: SocialDTO;
}
