import { AttoreDTO } from './attore-dto';
import { EventoRischiosoDTO } from './evento-rischioso-dto';
import { UlterioriInfoDTO } from './ulteriori-info-dto';

export class AttivitaSensibileDTO {
  id!: number;
  idSezione23!: number;
  denominazione!: string;
  descrizione!: string;
  processoCollegato!: string;
  attore!: AttoreDTO;
  ulterioriInfo!: UlterioriInfoDTO;
  eventoRischio!: EventoRischiosoDTO[];
}
