import { StakeHolderDTO } from './stakeholder-dto';

export class OVPStakeHolderDTO {
  id?: number;
  stakeholder?: StakeHolderDTO;

  toString(): string {
    return this.stakeholder?.nomeStakeHolder + ' - ' + this.stakeholder?.relazionePA;
  }
}
