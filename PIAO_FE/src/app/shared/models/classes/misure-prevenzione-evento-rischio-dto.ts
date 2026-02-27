import { MisuraPrevenzioneEventoRischioIndicatoreDTO } from './misura-prevenzione-evento-rischio-indicatore-dto';
import { MisuraPrevenzioneEventoRischioStakeholderDTO } from './misura-prevenzione-evento-rischio-stakeholder-dto';
import { MonitoraggioPrevenzioneDTO } from './monitoraggio-prevenzione-dto';

export class MisurePrevenzioneEventoRischioDTO {
  id!: number;

  codice!: string;

  denominazione!: string;

  descrizione!: string;

  responsabile!: string;

  idEventoRischio!: number;

  idObiettivoPrevenzioneCorruzioneTrasparenza!: number;

  indicatori!: MisuraPrevenzioneEventoRischioIndicatoreDTO[];

  stakeholder!: MisuraPrevenzioneEventoRischioStakeholderDTO[];

  monitoraggioPrevenzione!: MonitoraggioPrevenzioneDTO[];
}
