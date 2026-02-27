import { CampiTecniciDTO } from './campi-tecnici-dto';

export class AllegatoDTO extends CampiTecniciDTO {
  id?: number;
  idEntitaFK?: number;
  codDocumento?: string;
  codDocumentoFE?: string;
  codTipologiaFK?: string;
  codTipologiaAllegato?: string;
  descrizione?: string;
  downloadUrl?: string;
  sizeAllegato?: string;
  type?: string;
  isDoc?: boolean;
  base64?: string;
}
