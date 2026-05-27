/** Stato dichiarazione (allineato all'enum BE it.ey.enums.StatoDichiarazioneEnum). */
export type StatoDichiarazione = 'INVIATA' | 'NON_INVIATA';

/** DTO ritornato dal search dei solleciti dichiarazioni DFP. */
export interface SollecitiDichiarazioniDFPDTO {
  idPiao: number;
  codePA: string;
  amministrazione: string;
  statoDichiarazione: StatoDichiarazione;
}

