import { TipologiaAdempimento } from '../enums/tipologia-adempimento.enum';
import { UlterioriInfoDTO } from './ulteriori-info-dto';

export class AdempimentoDTO {
  id?: number;
  idSezione22?: number;
  tipologia?: TipologiaAdempimento;
  denominazione?: string;
  azione?: UlterioriInfoDTO;
  ulterioriInfo?: UlterioriInfoDTO;

}
