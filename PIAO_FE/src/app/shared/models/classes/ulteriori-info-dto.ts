import { SectionEnum } from '../enums/section.enum';
import { BaseMongoDTO } from './base-mongo-dto';

export class UlterioriInfoDTO extends BaseMongoDTO {
  tipoSezione?: SectionEnum;
}
