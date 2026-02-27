import { BaseMongoDTO } from './base-mongo-dto';
import { PropertyDTO } from './property-dto';

export class ContributoreInternoDTO extends BaseMongoDTO {
  constructor(id?: string, externalId?: number, properties?: PropertyDTO[]) {
    super();
    this.id = id;
    this.externalId = externalId?.toString();
    this.properties = properties;
  }
}
