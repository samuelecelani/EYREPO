import { GenericError } from './generic-error';
import { GenericStatus } from './generic-status';
import { MetadatoDTO } from './metadato-dto';

export interface GenericResponse<T> {
  data: T;
  status: GenericStatus;
  error: GenericError | null;
  metadato: MetadatoDTO<T>[];
}
