import { AttoreDTO } from './attore-dto';
import { MilestoneDTO } from './milestone-dto';

export class SottofaseMonitoraggioDTO {
  id?: number;
  idSezione4?: number;
  denominazione?: string;
  descrizione?: string;
  dataInizio?: Date;
  dataFine?: Date;
  strumenti?: string;
  fonteDato?: string;
  attore?: AttoreDTO;
  milestone?: MilestoneDTO[];
}
