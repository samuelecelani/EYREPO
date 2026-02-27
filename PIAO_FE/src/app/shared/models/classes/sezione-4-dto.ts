import { CampiTecniciDTO } from './campi-tecnici-dto';
import { UlterioriInfoDTO } from './ulteriori-info-dto';
import { AttoreDTO } from './attore-dto';
import { MilestoneDTO } from './milestone-dto';
import { AllegatoDTO } from './allegato-dto';

// DTO per sottofase monitoraggio (allineato con backend)
export class SottofaseMonitoraggioDTO extends CampiTecniciDTO {
  id?: number;
  idSezione4?: number;
  denominazione?: string;
  descrizione?: string;
  dataInizio?: string;
  dataFine?: string;
  strumenti?: string;
  fonteDato?: string;
  milestone?: MilestoneDTO[];
  // NoSQL
  attore?: AttoreDTO;
}

export class Sezione4DTO extends CampiTecniciDTO {
  id?: number;
  idPiao?: number;
  statoSezione?: string;

  // Strumenti e modalità del monitoraggio integrato del PIAO
  descrStrumenti?: string;

  // Modalità di rilevazione della soddisfazione degli utenti
  descrModalitaRilevazione?: string;

  // Introduzione attività monitoraggio
  intro?: string;

  // === MONITORAGGIO SEZIONE 2 ===

  // 2.1 Valore Pubblico - Introduzione
  intro21?: string;

  // 2.2 Performance - Introduzione
  intro22?: string;

  // 2.2 Performance Individuale - Descrizione
  descr22?: string;

  // 2.3 Rischi corruttivi e trasparenza - Descrizione
  descr23?: string;

  // === MONITORAGGIO SEZIONE 3 ===

  // 3.1 Organizzazione - Descrizione
  descr31?: string;

  // 3.2 Lavoro agile - Descrizione
  descr32?: string;

  // 3.3.1 Fabbisogno del personale - Descrizione
  descr331?: string;

  // 3.3.2 Formazione del personale - Descrizione
  descr332?: string;

  // Il monitoraggio del PIAO - Descrizione
  descrMonitoraggio?: string;

  // Sottofasi di monitoraggio
  sottofaseMonitoraggio?: SottofaseMonitoraggioDTO[];

  // Ulteriori info (NoSQL)
  ulterioriInfo?: UlterioriInfoDTO;

  // Attore
  attore?: AttoreDTO;

  allegati!: AllegatoDTO[];
}
