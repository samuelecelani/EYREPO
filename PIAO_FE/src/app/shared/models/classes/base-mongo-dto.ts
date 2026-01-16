import { PropertyDTO } from "./property-dto";

export class BaseMongoDTO {
  id?: string;
  externalId?: string;
  properties?: PropertyDTO[];
}
