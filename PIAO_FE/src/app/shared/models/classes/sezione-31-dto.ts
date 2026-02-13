import { CampiTecniciDTO } from './campi-tecnici-dto';

export class Sezione31DTO extends CampiTecniciDTO {
  id!: number;
  idPiao!: number;
  statoSezione!: string;
  // Struttura organizzativa al 31.12 dell'anno precedente
  strutturaOrganizzativa!: string;
  // Ulteriori dettagli ampiezza organizzativa
  ulterioriDettagli!: string;
  // Incarichi dirigenziali e simili
  incarichiDirigenziali!: string;
  // Rappresentazione dei profili professionali
  profiliProfessionali!: string;
  // Linee strategiche dell'organizzazione
  lineeStrategiche!: string;
}
