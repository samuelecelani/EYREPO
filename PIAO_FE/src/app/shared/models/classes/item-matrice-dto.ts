import { OvpItemDTO } from './ovp-item-dto';

export class ItemMatriceDTO {
  politicalPriority!: string;
  organisationalAreas!: Record<string, OvpItemDTO[]>;
}
