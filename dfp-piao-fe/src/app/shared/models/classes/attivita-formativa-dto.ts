import { CampiTecniciDTO } from './campi-tecnici-dto';

export class AttivitaFormativeDTO extends CampiTecniciDTO {
  id?: number;
  idSezione332?: number;
  idTipologiaAttivita?: number;
  idAmbitoCompetenza?: number;
  idAreaTematica?: number;
  numeroDirigenti?: number;
  numeroNonDirigenti?: number;
  oreFormazione?: number;
  verificaApprendimento?: boolean;
}
