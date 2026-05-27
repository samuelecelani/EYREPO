import { CodTipologiaCategoriaEnum } from '../enums/cod-tipologia-categoria.enum';
import { AttoreDTO } from './attore-dto';
import { BaseMongoAttivitaDTO } from './base-mongo-attivita-dto';
import { UlterioriInfoDTO } from './ulteriori-info-dto';

export class CategoriaObiettiviDTO {
  id?: number;
  idSezione4?: number;
  idSottofase?: number;
  idCategoriaObiettivi?: number;
  codTipologiaFk?: CodTipologiaCategoriaEnum;
  attivita?: BaseMongoAttivitaDTO;
  attore?: AttoreDTO;
  ulterioriInfo?: UlterioriInfoDTO;
}
