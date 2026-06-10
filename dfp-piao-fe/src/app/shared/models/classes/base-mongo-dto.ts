import { PropertyDTO } from './property-dto';

export class BaseMongoDTO {
  id?: string;
  externalId?: number;
  properties?: PropertyDTO[];
}
