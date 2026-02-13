import { BaseMongoDTO } from './base-mongo-dto';

export class EventoRischiosoDTO {
  id?: number;
  idAttivitaSensibile?: number;
  denominazione?: string;
  probabilita?: string;
  impatto?: string;
  controlli?: string;
  valutazione?: string;
  idLivelloRischio?: number;
  motivazione?: string;
  fattore?: BaseMongoDTO;
  ulterioriInfo?: BaseMongoDTO;
}
