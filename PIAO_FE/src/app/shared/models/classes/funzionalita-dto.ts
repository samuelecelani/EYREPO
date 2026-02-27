import { FunzionalitaRuoloDTO } from "./funzionalita-ruolo-dto";

export class FunzionalitaDTO {
  id!: number;
  nomeFunzionalita!: string;
  descrizioneFunzionalita!: string;
  codiceFunzionalita!: string;
  funzionalitaByRuoli!: FunzionalitaRuoloDTO[];
}
