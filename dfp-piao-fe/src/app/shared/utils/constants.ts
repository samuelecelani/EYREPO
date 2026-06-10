import { SectionEnum } from '../models/enums/section.enum';

/* Tipi Autorizzazione */
export const DFP = 'DFP';
export const PA_CAPOFILA = 'PA_CAPOFILA';
export const PA = 'PA';

/*ID Card Azioni Servizi Piao*/
export const CONSULTA = 'consulta';
export const REDIGI = 'redigi';
export const AGGIORNA = 'aggiorna';

/* TIPOLOGIA ONLINE PIAO */
export const ORDINARIO = 'ORDINARIO';
export const SEMPLIFICATO = 'SEMPLIFICATO';
export const PLUS_50 = '> 50 dipendenti';
export const MIN_50 = '≤ 50 dipendenti';

/* TIPOLOGIA PIAO */
export const ONLINE = 'ONLINE';
export const PDF = 'PDF';

/*Icon*/
export const WARNING_ICON = 'WarningCircle';
export const SHAPE_ICON = 'Shape';
export const PENCIL_ICON = 'Pencil';
export const PROFILE_ICON = 'Profile';
export const DOCS_ICON = 'Docs';
export const MONUMENT_ICON = 'Monument';
export const EXCHANGE_ICON = 'ExchangeCircle';

/*KEY SessionStorage */
export const KEY_PIAO = 'piaoDTO';
export const KEY_USER = 'userDTO';
export const KEY_PA_ATTIVA = 'paAttivaDTO';

/*Sezioni with activeId*/
export const SEZIONI_ACTIVE_ID: Record<string, SectionEnum> = {
  '1': SectionEnum.SEZIONE_1,
  '2.1': SectionEnum.SEZIONE_2_1,
  '2.2': SectionEnum.SEZIONE_2_2,
  '2.3': SectionEnum.SEZIONE_2_3,
  '3.1': SectionEnum.SEZIONE_3_1,
  '3.2': SectionEnum.SEZIONE_3_2,
  '3.3.1': SectionEnum.SEZIONE_3_3_1,
  '3.3.2': SectionEnum.SEZIONE_3_3_2,
  '4': SectionEnum.SEZIONE_4,
};

/*Semplificato Required Sections*/
export const SEZIONI_SEMPLIFICATO = ['1', '2.3', '3.1', '3.2', '3.3.1'];

/*ORDINE Sezioni Validazione*/
export const ORDINE_SEZIONI = ['0', '1', '21', '22', '23', '31', '32', '331', '332', '4'];

/*INPUT regex*/
export const INPUT_REGEX = `^[A-Za-z0-9àèéìòùÀÈÉÌÒÙ!?\\*#\\-_'".,;:()\\[\\]{}@&%€$£/\\\\+=<>°§|~\`^ ’'''""'–—\\n\\r]+$`;
export const ONLY_NUMBERS_REGEX = '^[0-9]+$';
export const HOURS_REGEX = '^(?:0|[1-9][0-9]*)(?:\\.[0-9]{1,2})?$';
export const DATE_REGEX =
  '^\\d{4}-(?:0[1-9]|1[0-2])-(?:0[1-9]|[12]\\d|3[01])(T\\d{2}:\\d{2}:\\d{2})?$';
// Regex URL safe (no catastrophic backtracking):
// - schema opzionale http(s)
// - host: lettere/cifre/trattini, almeno un'etichetta + TLD 2-24 lettere
// - path/query/fragment opzionali (qualsiasi non-spazio)
export const URL_REGEX =
  '^(https?:\\/\\/)?([A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,24}(:[0-9]{1,5})?(\\/[^\\s]*)?$';
export const EMAIL_REGEX = '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$';
export const SECTION_ID = ['1', '2.1', '2.2', '2.3', '3.1', '3.2', '3.3.1', '3.3.2', '4'];
export const TELEFONO_REGEX = '^[+]?[(]?[0-9]{1,4}[)]?[-\\s./0-9]*$';

/*Notification PDF Download*/
export const IMPOSSIBLE_PDF_DOWNLOAD = 'Non è possibile scaricare il file';

/*TEMPLATE EMAIL SOLLECITI*/
export const TEMPLATE_EMAIL_SOLLECITI = 'BODY_EMAIL_SOLLECITO';
