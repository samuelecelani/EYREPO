import { ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoriDTO } from './obiettivo-prevenzione-corruzione-trasparenza-indicatori-dto';

export class ObiettivoPrevenzioneCorruzioneTrasparenzaDTO {
  id?: number;
  idSezione23?: number;
  idOVP?: number;
  idStrategiaOVP?: number;
  idObbiettivoPerformance?: number;
  codice?: string;
  denominazione?: string;
  descrizione?: string;
  indicatori?: ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoriDTO[];
}
