import { CampiTecniciDTO } from './campi-tecnici-dto';

export class ObiettiviRisultatiFotografiaDTO extends CampiTecniciDTO {
  id?: number;

  idSezione332?: number;
  idTipologiaAttivita?: number;
  idAmbitoCompetenza?: number;
  idAreaTematica?: number;
  idTipologiaDestinatari?: number;

  codTipologiaFK?: string;

  codice?: string;
  titolo?: string;
  carattereObbligatorio?: boolean;
  riferimentoNormativo?: string;
  targetDirigenti?: string;
  targetNonDirigenti?: string;

  numeroDirigenti?: number;
  numeroNonDirigenti?: number;

  oreFormazione?: number;

  verificaApprendimento?: boolean;
  creditiFormativi?: number;

  modalitaGestioneFormazione?: string;
  enteErogatore?: string;

  costoAttivita?: string;

  dataInizio?: string; // formato ISO: "YYYY-MM-DD"
  dataFine?: string; // formato ISO: "YYYY-MM-DD"
}
