export interface IGetNotificheRequest {
  idModulo: string;
  numeroPagina?: number;
  righePerPagina?: number;
  lette?: boolean;
  ruolo?: string;
  codiceFiscale?: string;
  codicePa?: string;
}
