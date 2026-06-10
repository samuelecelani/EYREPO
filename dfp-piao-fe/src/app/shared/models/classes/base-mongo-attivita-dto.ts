import { PropertyAttivitaDTO } from './property-attivita-dto';

export class BaseMongoAttivitaDTO {
  id?: string;
  externalId?: string;
  propertyAttivita?: PropertyAttivitaDTO[];
}
