export interface IListActivities {
  id: number | string;
  codiceAttivita: string;
  profiloUtente: string;
  profiloUtenteSecondario?: string;
  nome: string;
  cognome: string;
  codiceFiscale: string;
  codiceRuolo: string;
  codiceRuoloSecondario?: string;
}
