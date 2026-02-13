import { AllegatoDTO } from './allegato-dto';
import { UlterioriInfoDTO } from './ulteriori-info-dto';
import { FaseDTO } from './fase-dto';
import { ObbiettivoPerformanceDTO } from './obiettivo-performance-dto';
import { AdempimentoDTO } from './adempimento-dto';

export class Sezione22DTO {
  id?: number;
  idPiao?: number;
  statoSezione?: string;
  introPerformance?: string;
  introObiettiviPerformance?: string;
  introAdempimenti?: string;
  introPerformanceOrganizzativa?: string;
  descriptionCollegamentoPerformance?: string;
  introPerformanceIndividuale?: string;
  fase?: FaseDTO[];
  obbiettiviPerformance?: ObbiettivoPerformanceDTO[];
  adempimenti?: AdempimentoDTO[];
  ulterioriInfo?: UlterioriInfoDTO;
  allegati?: AllegatoDTO[];
}
