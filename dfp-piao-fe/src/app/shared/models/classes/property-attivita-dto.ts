import { PropertyDTO } from './property-dto';

export class PropertyAttivitaDTO extends PropertyDTO {
  keyDateInizio?: string;
  keyDateFine?: string;
  valueDateInizio?: Date;
  valueDateFine?: Date;
}
