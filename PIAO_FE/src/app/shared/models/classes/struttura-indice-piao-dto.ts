export class StrutturaIndicePiaoDTO {
  id!: number;
  numeroSezione!: string;
  testo!: string;
  statoSezione?: string;
  validity?: boolean;
  createdBy?: string;
  updatedBy?: string;
  createdTs?: Date;
  updatedTs?: Date;
  disabled?: boolean;
  children!: StrutturaIndicePiaoDTO[];
}
