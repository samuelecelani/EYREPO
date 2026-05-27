export interface MetadatoDTO<T> {
  key?: string;
  idFK?: number; // id oggetto
  value?: T; // nome classe
  idPiao?: number;
}
