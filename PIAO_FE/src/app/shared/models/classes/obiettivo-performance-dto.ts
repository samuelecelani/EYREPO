import { CampiTecniciDTO } from './campi-tecnici-dto';
import { TipologiaObbiettivo } from '../enums/tipologia-obbiettivo.enum';
import { ContributoreInternoDTO } from './contributore-interno-dto';
import { ObiettivoStakeHolderDTO } from './obiettivo-stakeholder-dto';
import { ObiettivoIndicatoriDTO } from './obiettivo-indicatori-dto';

export class ObbiettivoPerformanceDTO extends CampiTecniciDTO {
  id?: number;
  idSezione22?: number;
  idOvp?: number;
  idStrategiaOvp?: number;
  codice?: string;
  tipologia?: TipologiaObbiettivo;
  denominazione?: string;
  responsabileAmministrativo?: string;
  risorseUmane?: string;
  risorseEconomicaFinanziaria?: string;
  risorseStrumentali?: string;
  tipologiaRisorsa?: string;
  idObiettivoPeformance?: number;
  contributoreInterno?: ContributoreInternoDTO;
  stakeholders?: ObiettivoStakeHolderDTO[];
  indicatori?: ObiettivoIndicatoriDTO[];
}
