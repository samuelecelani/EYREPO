import { ObiettivoPrevenzioneIndicatoreDTO } from './obiettivo-prevenzione-indicatore-dto';

export class ObiettivoPrevenzioneDTO {
  id!: number;

  idSezione23!: number;

  codice!: string;
  denominazione!: string;
  descrizione!: string;

  indicatori!: ObiettivoPrevenzioneIndicatoreDTO[];
}
