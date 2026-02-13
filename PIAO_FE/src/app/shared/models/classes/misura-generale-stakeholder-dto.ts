import { MisuraGeneraleDTO } from './misura-generale-dto';
import { StakeHolderDTO } from './stakeholder-dto';

export class MisuraGeneraleStakeholderDTO {
  id?: number;
  misuraPrevenzione?: MisuraGeneraleDTO; // @JsonIgnore nel backend
  stakeholder?: StakeHolderDTO;
}
