import { TypeErrorEnum } from '../enums/type-error.enum';
import { CampiTecniciDTO } from './campi-tecnici-dto';

export class AvvisoDTO extends CampiTecniciDTO {
  active?: boolean;
  deactivationTime?: string;
  campiModificati?: string;
  testoSezione?: string;
  idPiao?: number;
  statoSezione?: string;
  messageError?: string;
  errorCode?: string;
  typeEnum?: TypeErrorEnum;
  id?: number;
  tipologiaContenuto?: string;
  dataPubblicazione?: string;
  oggetto?: string;
  tipologiaAmministrazione?: string;
  amministrazione?: string;
  messaggio?: string;
  stato?: string;

  // Campi legacy mantenuti per compatibilita durante la transizione API/UI.
  visualizzato?: boolean;
  tipoAvviso?: string;
  titolo?: string;
  intro?: string;
  descrizione?: string;
}

export type CreateAvvisoDTO = AvvisoDTO;
export type UpdateAvvisoDTO = AvvisoDTO;