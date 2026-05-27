import { CampiTecniciDTO } from './campi-tecnici-dto';
import { PIAODTO } from './piao-dto';
import { TipologiaAndamentoValoreIndicatoreDTO } from './tipologia-andamento-valore-indicatore-dto';
import { UlterioriInfoDTO } from './ulteriori-info-dto';

export class IndicatoreDTO extends CampiTecniciDTO {
  id?: number;
  idPiao?: number;
  idEntitaFK?: number;
  codTipologiaFK?: string;
  denominazione?: string;
  idSubDimensioneFK?: number;
  idDimensioneFK?: number;
  unitaMisura?: string;
  formula?: string;
  peso?: number;
  polarita?: string;
  baseLine?: number;
  consuntivo?: number;
  fonteDati?: string;
  tipAndValAnnoCorrente?: TipologiaAndamentoValoreIndicatoreDTO;
  tipAndValAnno1?: TipologiaAndamentoValoreIndicatoreDTO;
  tipAndValAnno2?: TipologiaAndamentoValoreIndicatoreDTO;
  rilevante?: boolean;
  addInfo?: UlterioriInfoDTO;
}
