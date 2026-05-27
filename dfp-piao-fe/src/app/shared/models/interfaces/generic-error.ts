import { TypeErrorEnum } from '../enums/type-error.enum';

export interface GenericError {
  messageError: string | null;
  errorCode: string | null;
  type: TypeErrorEnum | null;
}
