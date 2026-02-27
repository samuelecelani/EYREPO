import { CampiTecniciDTO } from './campi-tecnici-dto';

export class RisorsaFinanziariaDTO {
  id?: number;
  idOvp?: number; // FK all'obiettivo di valore pubblico
  iniziativa?: string;
  descrizione?: string;
  dotazioneFinanziaria?: number;
  fonteFinanziamento?: string;
}
