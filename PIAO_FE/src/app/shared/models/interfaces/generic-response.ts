import { GenericError } from "./generic-error";
import { GenericStatus } from "./generic-status";

export interface GenericResponse<T> {
  data: T;
  status: GenericStatus;
  error: GenericError | null;
}

