import { CampiTecniciDTO } from './campi-tecnici-dto';

export class TabellaStoricoSezioneDTO {
  id!: number;
  nomeCognome?: string;
  profilo?: string;
  codTipologiaFK?: string;
  dataModifica?: Date;
  sezione?: string;
  campiModificati?: string;
}
