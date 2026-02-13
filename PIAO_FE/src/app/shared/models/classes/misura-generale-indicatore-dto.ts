import { MisuraGeneraleDTO } from './misura-generale-dto';
import { IndicatoreDTO } from './indicatore-dto';

export class MisuraGeneraleIndicatoreDTO {
  id?: number;
  misuraPrevenzione?: MisuraGeneraleDTO; // @JsonIgnore nel backend
  indicatore?: IndicatoreDTO;
}
