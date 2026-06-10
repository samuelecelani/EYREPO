import { CampiTecniciDTO } from './campi-tecnici-dto';

export class StrutturaIndicePiaoDTO extends CampiTecniciDTO {
  id!: number;
  numeroSezione!: string;
  testo!: string;
  statoSezione?: string;
  disabled?: boolean;
  children!: StrutturaIndicePiaoDTO[];
  revocato?: boolean;
  rifiutato?: boolean;
  annullato?: boolean;
  statoPiao?: string;
}
