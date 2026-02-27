import { CampiTecniciDTO } from './campi-tecnici-dto';
import { StakeHolderDTO } from './stakeholder-dto';

export class PIAODTO extends CampiTecniciDTO {
  id?: number;
  codPAFK?: string;
  denominazione?: string;
  versione?: string;
  tipologia?: string;
  tipologiaOnline?: string;
  statoPiao?: string;
  aggiornamento?: boolean;
  idSezione1?: number;
  idSezione21?: number;
  idSezione22?: number;
  idSezione23?: number;
  idSezione31?: number;
  idSezione32?: number;
  idSezione331?: number;
  idSezione332?: number;
  idSezione4?: number;

  private _stakeHolders?: StakeHolderDTO[];

  get stakeHolders(): StakeHolderDTO[] | undefined {
    return this._stakeHolders;
  }

  set stakeHolders(value: StakeHolderDTO[] | undefined) {
    this._stakeHolders = value?.sort((a, b) => (a.id ?? 0) - (b.id ?? 0));
  }
}
