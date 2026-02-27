import { MisurePrevenzioneService } from '../../services/misure-prevenzione.service';
import { FattoreDTO } from './fattore-dto';
import { MisurePrevenzioneEventoRischioDTO } from './misure-prevenzione-evento-rischio-dto';
import { UlterioriInfoDTO } from './ulteriori-info-dto';

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
  fattore?: FattoreDTO;
  ulterioriInfo?: UlterioriInfoDTO;
  misure?: MisurePrevenzioneEventoRischioDTO[];
}
