import { IndicatoreDTO } from '../classes/indicatore-dto';

export interface LabelValue {
  label: string;
  value?: string | boolean | null | any;
  formControlName?: string;
}
