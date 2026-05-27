import { CampiTecniciDTO } from './campi-tecnici-dto';

export class StakeHolderDTO extends CampiTecniciDTO {
  id?: number;
  idPiao?: number;
  nomeStakeHolder?: string;
  relazionePA?: string;

  override toString(): string {
    return `${this.nomeStakeHolder} - ${this.relazionePA}`;
  }
}
