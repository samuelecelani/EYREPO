/**
 * Enum centralizzato per la gestione dei codici errore funzionali relativi
 * ai vincoli di cancellazione e modifica tra le entità del PIAO.
 *
 * Ogni voce dell'enum contiene un codice numerico e un messaggio descrittivo user‑friendly.
 *
 * - Il nome della costante segue lo schema ENTITÀ_USO_SEZIONE.
 * - Il codice numerico aumenta di 10 per categorie funzionali omogenee.
 */
export enum ErrorCodEnum {
  OVP_USATO_IN_SEZIONE22 = 'OVP_USATO_IN_SEZIONE22',
  OVP_USATO_IN_SEZIONE23 = 'OVP_USATO_IN_SEZIONE23',
  OVP_USATO_IN_TABELLA_FUNZIONALE = 'OVP_USATO_IN_TABELLA_FUNZIONALE',

  OBIETTIVO_PERFORMANCE_USATO_SEZIONE23 = 'OBIETTIVO_PERFORMANCE_USATO_SEZIONE23',

  STRATEGIA_USATA_SEZIONE22 = 'STRATEGIA_USATA_SEZIONE22',
  STRATEGIA_USATA_SEZIONE23 = 'STRATEGIA_USATA_SEZIONE23',

  STAKEHOLDER_USATO_OVP = 'STAKEHOLDER_USATO_OVP',
  STAKEHOLDER_USATO_OBIETTIVO_PERFORMANCE = 'STAKEHOLDER_USATO_OBIETTIVO_PERFORMANCE',
  STAKEHOLDER_USATO_MISURA_PREVENZIONE = 'STAKEHOLDER_USATO_MISURA_PREVENZIONE',
  STAKEHOLDER_USATO_MISURA_PREVENZIONE_EVENTO_RISCHIOSO = 'STAKEHOLDER_USATO_MISURA_PREVENZIONE_EVENTO_RISCHIOSO',
  STAKEHOLDER_USATO_IN_TABELLA_FUNZIONALE = 'STAKEHOLDER_USATO_IN_TABELLA_FUNZIONALE',

  AREA_ORGANIZZATIVA_USATA_OVP = 'AREA_ORGANIZZATIVA_USATA_OVP',
  PRIORITA_POLITICA_USATA_OVP = 'PRIORITA_POLITICA_USATA_OVP',

  DELETE_WITH_IMPACT = 'DELETE_WITH_IMPACT',

  // Nuovi errori per Tabella Funzionale
  OVP_USATO_IN_SEZIONE31 = 'OVP_USATO_IN_SEZIONE31',
  OVP_USATO_IN_SEZIONE32 = 'OVP_USATO_IN_SEZIONE32',
  OVP_USATO_IN_SEZIONE331 = 'OVP_USATO_IN_SEZIONE331',
  OVP_USATO_IN_SEZIONE332 = 'OVP_USATO_IN_SEZIONE332',
  STAKEHOLDER_USATO_SEZIONE31 = 'STAKEHOLDER_USATO_SEZIONE31',
  STAKEHOLDER_USATO_SEZIONE32 = 'STAKEHOLDER_USATO_SEZIONE32',
  STAKEHOLDER_USATO_SEZIONE331 = 'STAKEHOLDER_USATO_SEZIONE331',
  STAKEHOLDER_USATO_SEZIONE332 = 'STAKEHOLDER_USATO_SEZIONE332',
}

export const ERROR_COD_MESSAGES: Record<ErrorCodEnum, string> = {
  [ErrorCodEnum.OVP_USATO_IN_SEZIONE22]:
    'Impossibile eliminare o modificare OVP: utilizzato nella Sezione 2.2 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO',

  [ErrorCodEnum.OVP_USATO_IN_SEZIONE23]:
    'Impossibile eliminare o modificare OVP: utilizzato nella Sezione 2.3 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO',

  [ErrorCodEnum.OVP_USATO_IN_TABELLA_FUNZIONALE]:
    'Impossibile eliminare o modificare OVP: utilizzato nella Tabella funzionale',

  [ErrorCodEnum.OBIETTIVO_PERFORMANCE_USATO_SEZIONE23]:
    'Impossibile eliminare o modificare Obiettivo Performance: collegato alla Sezione 2.3 IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO',

  [ErrorCodEnum.STRATEGIA_USATA_SEZIONE22]:
    'Impossibile eliminare o modificare la Strategia: collegata a Obiettivi Performance in Sezione 2.2 IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO',

  [ErrorCodEnum.STRATEGIA_USATA_SEZIONE23]:
    'Impossibile eliminare o modificare la Strategia: collegata a Obiettivi Corruzione/Trasparenza in Sezione 2.3 IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO',

  [ErrorCodEnum.STAKEHOLDER_USATO_OVP]:
    'Impossibile eliminare lo Stakeholder: collegato a OVP in Sezione2.1 IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO',

  [ErrorCodEnum.STAKEHOLDER_USATO_OBIETTIVO_PERFORMANCE]:
    'Impossibile eliminare lo Stakeholder: collegato a Obiettivo Performance in Sezione2.2 IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO',

  [ErrorCodEnum.STAKEHOLDER_USATO_MISURA_PREVENZIONE]:
    'Impossibile eliminare lo Stakeholder: collegato a Misura di prevenzione in Sezione2.3 IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO',

  [ErrorCodEnum.STAKEHOLDER_USATO_MISURA_PREVENZIONE_EVENTO_RISCHIOSO]:
    'Impossibile eliminare lo Stakeholder: collegato a Misura di prevenzione Evento Rischioso in Sezione2.3 IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO',

  [ErrorCodEnum.STAKEHOLDER_USATO_IN_TABELLA_FUNZIONALE]:
    'Impossibile eliminare lo Stakeholder: utilizzato nella Tabella funzionale',

  [ErrorCodEnum.AREA_ORGANIZZATIVA_USATA_OVP]:
    "Impossibile eliminare l'Area Organizzativa: collegata a OVP in Sezione2.1 IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO",

  [ErrorCodEnum.PRIORITA_POLITICA_USATA_OVP]:
    'Impossibile eliminare la Priorita Politica: collegata a OVP in Sezione2.1 IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO',

  [ErrorCodEnum.DELETE_WITH_IMPACT]:
    "L'eliminazione di questo elemento può avere impatti su altre sezioni, confermare per l'eliminazione dell'elemento.",

  // Nuovi messaggi per Tabella Funzionale
  [ErrorCodEnum.OVP_USATO_IN_SEZIONE31]:
    'Impossibile eliminare o modificare OVP: utilizzato nella Sezione 3.1 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO',

  [ErrorCodEnum.OVP_USATO_IN_SEZIONE32]:
    'Impossibile eliminare o modificare OVP: utilizzato nella Sezione 3.2 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO',

  [ErrorCodEnum.OVP_USATO_IN_SEZIONE331]:
    'Impossibile eliminare o modificare OVP: utilizzato nella Sezione 3.3.1 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO',

  [ErrorCodEnum.OVP_USATO_IN_SEZIONE332]:
    'Impossibile eliminare o modificare OVP: utilizzato nella Sezione 3.3.2 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO',
  [ErrorCodEnum.STAKEHOLDER_USATO_SEZIONE31]:
    'Impossibile eliminare lo Stakeholder: collegato alla Sezione 3.1 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO',

  [ErrorCodEnum.STAKEHOLDER_USATO_SEZIONE32]:
    'Impossibile eliminare lo Stakeholder: collegato alla Sezione 3.2 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO',

  [ErrorCodEnum.STAKEHOLDER_USATO_SEZIONE331]:
    'Impossibile eliminare lo Stakeholder: collegato alla Sezione 3.3.1 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO',

  [ErrorCodEnum.STAKEHOLDER_USATO_SEZIONE332]:
    'Impossibile eliminare lo Stakeholder: collegato alla Sezione 3.3.2 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO',
};

export function getErrorMessage(code: string): string | undefined {
  return ERROR_COD_MESSAGES[code as ErrorCodEnum];
}

export function isErrorCod(code: string): boolean {
  return code in ErrorCodEnum;
}
