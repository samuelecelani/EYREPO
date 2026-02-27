import {
  AbstractControl,
  FormControl,
  FormGroup,
  FormArray,
  FormBuilder,
  Validators,
} from '@angular/forms';
import { INPUT_REGEX, ONLY_NUMBERS_REGEX } from './constants';
import { OVPDTO } from '../models/classes/ovp-dto';
import { OVPAreaOrganizzativaDTO } from '../models/classes/ovp-area-organizzativa-dto';
import { AreaOrganizzativaDTO } from '../models/classes/area-organizzativa-dto';
import { OVPPrioritaPoliticaDTO } from '../models/classes/ovp-priorita-politica-dto';
import { PrioritaPoliticaDTO } from '../models/classes/priorita-politica-dto';
import { OVPStakeHolderDTO } from '../models/classes/ovp-stakeholder-dto';
import { StakeHolderDTO } from '../models/classes/stakeholder-dto';
import { OVPStrategiaIndicatoreDTO } from '../models/classes/ovp-strategia-indicatore-dto';
import { OVPStrategiaDTO } from '../models/classes/ovp-strategia-dto';
import { IndicatoreDTO } from '../models/classes/indicatore-dto';
import { TipologiaAndamentoValoreIndicatoreDTO } from '../models/classes/tipologia-andamento-valore-indicatore-dto';
import { PropertyDTO } from '../models/classes/property-dto';
import { UlterioriInfoDTO } from '../models/classes/ulteriori-info-dto';
import { RisorsaFinanziariaDTO } from '../models/classes/risorsa-finanziaria-dto';
import { ObbiettivoPerformanceDTO } from '../models/classes/obiettivo-performance-dto';
import { ContributoreInternoDTO } from '../models/classes/contributore-interno-dto';
import { ObiettivoStakeHolderDTO } from '../models/classes/obiettivo-stakeholder-dto';
import { ObiettivoIndicatoriDTO } from '../models/classes/obiettivo-indicatori-dto';
import { AdempimentoDTO } from '../models/classes/adempimento-dto';
import { FaseDTO } from '../models/classes/fase-dto';
import { AttoreDTO } from '../models/classes/attore-dto';
import { AttivitaDTO } from '../models/classes/attivita-dto';
import { LabelValue } from '../models/interfaces/label-value';
import { AttivitaSensibileDTO } from '../models/classes/attivita-sensibile-dto';
import { MonitoraggioPrevenzioneDTO } from '../models/classes/monitoraggio-prevenzione-dto';
import { ObiettivoPrevenzioneCorruzioneTrasparenzaDTO } from '../models/classes/obiettivo-prevenzione-corruzione-trasparenza-dto';
import { ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoriDTO } from '../models/classes/obiettivo-prevenzione-corruzione-trasparenza-indicatori-dto';
import { EventoRischiosoDTO } from '../models/classes/evento-rischioso-dto';
import { MisurePrevenzioneEventoRischioDTO } from '../models/classes/misure-prevenzione-evento-rischio-dto';
import { MisuraPrevenzioneEventoRischioIndicatoreDTO } from '../models/classes/misura-prevenzione-evento-rischio-indicatore-dto';
import { MisuraPrevenzioneEventoRischioStakeholderDTO } from '../models/classes/misura-prevenzione-evento-rischio-stakeholder-dto';
import { FattoreDTO } from '../models/classes/fattore-dto';

/**
 * Estrae gli anni da una denominazione tipo "PIAO 25-27" o "PIAO 2025-2027".
 * Restituisce un array di numeri [2025, 2026, 2027].
 *
 * Regole:
 *  - Se sono 2 cifre (es. 25), assume il secolo 2000 → 2025.
 *  - Accetta separatori "-" o "–" e spazi opzionali.
 *  - Valida che l'anno finale sia >= dell'iniziale.
 *  - Genera tutti gli anni nel range inclusivo.
 */
export function estraiAnniDaDenominazione(input: string): number[] {
  if (!input || typeof input !== 'string') {
    throw new Error('Input non valido: è richiesta una stringa.');
  }

  // Cattura "aa-aa" o "aaaa-aaaa" con spazi/separatori vari
  const match = input.trim().match(/(\d{2}|\d{4})\s*[–-]\s*(\d{2}|\d{4})/);

  if (!match) {
    throw new Error(
      'Formato non riconosciuto. Atteso qualcosa come "PIAO 25-27" o "PIAO 2025-2027".'
    );
  }

  let [_, startRaw, endRaw] = match;

  const toFullYear = (s: string): number => {
    if (s.length === 4) return parseInt(s, 10);
    // 2 cifre → assume 2000-2099. Se ti serve logica diversa, cambiala qui.
    const n = parseInt(s, 10);
    // Puoi anche decidere un cut-off: es. 00-79 → 2000-2079, 80-99 → 1980-1999.
    return 2000 + n;
  };

  const startYear = toFullYear(startRaw);
  const endYear = toFullYear(endRaw);

  if (isNaN(startYear) || isNaN(endYear)) {
    throw new Error('Impossibile interpretare gli anni.');
  }
  if (endYear < startYear) {
    throw new Error(`Range anni non valido: ${startYear} > ${endYear}.`);
  }
  // Per sicurezza, evita range "sospetti" (es. troppo ampi)
  const maxSpan = 50;
  if (endYear - startYear > maxSpan) {
    throw new Error(`Range troppo ampio (${startYear}-${endYear}). Controlla la denominazione.`);
  }

  const years: number[] = [];
  for (let y = startYear; y <= endYear; y++) {
    years.push(y);
  }
  return years;
}

/**
 * Raccoglie i nomi di tutti i controlli in un AbstractControl (FormGroup, FormArray, FormControl)
 * che hanno valore null, undefined o stringa vuota.
 * @param control Controllo astratto da analizzare
 * @param path Percorso corrente (usato internamente per la ricorsione)
 * @returns Array di stringhe con i nomi degli attributi dei controlli nulli
 */
export function collectNullPaths(control: AbstractControl, path: string[] = []): string[] {
  const paths: string[] = [];

  if (control instanceof FormControl) {
    if (control.value === null || control.value === '' || control.value === undefined) {
      const attributeName = path[path.length - 1];
      if (attributeName !== undefined) {
        paths.push(attributeName);
      }
    }
    return paths;
  }

  if (control instanceof FormGroup) {
    for (const [key, child] of Object.entries(control.controls)) {
      paths.push(...collectNullPaths(child, [...path, key]));
    }
    return paths;
  }

  if (control instanceof FormArray) {
    control.controls.forEach((child, idx) => {
      paths.push(...collectNullPaths(child, [...path, String(idx)]));
    });
    return paths;
  }

  return paths;
}

/**
 *  Verifica se tutti i valori all'interno di un AbstractControl sono null, undefined o stringa vuota.
 * @param control  Controllo astratto da verificare
 * @returns true se tutti i valori sono nulli, false altrimenti
 */
export function areAllValuesNull(control: AbstractControl): boolean {
  if (control instanceof FormControl) {
    return control.value === null || control.value === '' || control.value === undefined;
  }

  if (control instanceof FormGroup) {
    const controls = Object.values(control.controls);
    // Se non ci sono controlli, lo consideriamo "tutto null" per definizione
    return controls.every((child) => areAllValuesNull(child));
  }

  if (control instanceof FormArray) {
    // Un array vuoto è considerato "tutto null"
    return control.controls.every((child) => areAllValuesNull(child));
  }

  // Caso di fallback (non dovrebbe servire)
  return true;
}

/**
 * Verifica ricorsivamente se ci sono errori di tipo 'required' o 'minlength' in un FormGroup o FormArray.
 * Controlla solo gli errori required e minlength, ignorando altri validatori come maxLength, pattern, etc.
 * @param formGroup Il FormGroup o FormArray da verificare
 * @returns true se esiste almeno un errore required o minlength, false altrimenti
 */
export function hasRequiredErrors(formGroup: FormGroup | FormArray): boolean {
  // Controlla prima se il controllo stesso ha errori required o minlength
  if (
    formGroup?.errors?.['required'] ||
    formGroup?.errors?.['minlength'] ||
    formGroup?.errors?.['minArrayLength']
  ) {
    return true;
  }

  for (const key in formGroup.controls) {
    const control = formGroup.get(key);

    if (control instanceof FormGroup || control instanceof FormArray) {
      if (hasRequiredErrors(control)) {
        return true;
      }
    } else if (
      control?.errors?.['required'] ||
      control?.errors?.['minlength'] ||
      control?.errors?.['minArrayLength']
    ) {
      return true;
    }
  }

  return false;
}

/**
 * Validatore custom per verificare che un FormArray abbia un numero minimo di elementi
 * @param minLength Numero minimo di elementi richiesti
 * @returns Funzione validatore
 */
export function minArrayLength(minLength: number) {
  return (control: AbstractControl): { [key: string]: any } | null => {
    if (control instanceof FormArray) {
      return control.length >= minLength
        ? null
        : { minArrayLength: { requiredLength: minLength, actualLength: control.length } };
    }
    return null;
  };
}

/**
 * Verifica se un valore è considerato "vuoto"
 * @param v Valore da verificare
 * @returns true se il valore è null, undefined o stringa vuota, false altrimenti
 */
export const isEmpty = (v: unknown) =>
  v === null || v === undefined || (typeof v === 'string' && v.trim() === '');

/**
 * Filtra un array, mantenendo solo gli oggetti per cui TUTTE le chiavi indicate
 * hanno valore != null
 * @param items Array di oggetti da filtrare
 * @param keysToCheck Chiavi da controllare per ogni oggetto
 * @returns Array filtrato
 */
export function filterNonNullFields<T extends Record<string, any>>(
  items: T[] | null | undefined,
  keysToCheck: (keyof T)[]
): T[] {
  if (!Array.isArray(items)) return [];

  const considerEmptyStringAsNull = true;

  const isNullish = (v: unknown) => {
    if (v === null || v === undefined) return true;
    if (considerEmptyStringAsNull && typeof v === 'string' && v.trim() === '') return true;
    return false;
  };

  return items.filter((item) => keysToCheck.every((k) => !isNullish(item[k as string])));
}

/**
 * Filtra un array, mantenendo solo gli oggetti per cui ALMENO UNA delle chiavi indicate
 * ha valore != null
 * @param items Array di oggetti da filtrare
 * @param keysToCheck Chiavi da controllare per ogni oggetto
 * @returns Array filtrato
 */
export function filterAtLeastOneNonNullField<T extends Record<string, any>>(
  items: T[] | null | undefined,
  keysToCheck: (keyof T)[]
): T[] {
  if (!Array.isArray(items)) return [];

  const considerEmptyStringAsNull = true;

  const isNullish = (v: unknown) => {
    if (v === null || v === undefined) return true;
    if (considerEmptyStringAsNull && typeof v === 'string' && v.trim() === '') return true;
    return false;
  };

  return items.filter((item) => keysToCheck.some((k) => !isNullish(item[k as string])));
}

/**
 * Crea un FormArray di FormGroup basato su un array di oggetti dinamici T,
 * impostando valori di default e validatori per ogni controllo.
 * @param fb  FormBuilder instance
 * @param items  Array of items to create the FormArray from
 * @param id  ID of the first section, used as a default value for the second key
 * @param keys  Keys to use for the form controls
 * @param maxSize1  Maximum length for the third key's value
 * @param maxSize2  Maximum length for the fourth key's value
 * @returns
 */
export function createFormArrayFromPiaoSession<T>(
  fb: FormBuilder,
  items: T[],
  id: number | null | undefined,
  keys: (keyof T)[],
  maxSize1: number,
  maxSize2: number,
  pattern: string = INPUT_REGEX,
  firstValue: boolean = true
): FormArray<FormGroup> {
  if (!Array.isArray(items) || (items.length === 0 && firstValue)) {
    return fb.array<FormGroup>([
      fb.group({
        [keys[0]]: [null, [Validators.maxLength(20), Validators.pattern(pattern)]],
        [keys[1]]: [id || null, [Validators.maxLength(20), Validators.pattern(pattern)]],
        [keys[2]]: [null, [Validators.maxLength(maxSize1), Validators.pattern(pattern)]],
        [keys[3]]: [null, [Validators.maxLength(maxSize2), Validators.pattern(pattern)]],
      }),
    ]);
  }

  return fb.array<FormGroup>(
    items.map((item) =>
      fb.group({
        [keys[0]]: [item?.[keys[0]], [Validators.maxLength(20), Validators.pattern(pattern)]],
        [keys[1]]: [id ?? null, [Validators.maxLength(20), Validators.pattern(pattern)]],
        [keys[2]]: [item?.[keys[2]], [Validators.maxLength(maxSize1), Validators.pattern(pattern)]],
        [keys[3]]: [item?.[keys[3]], [Validators.maxLength(maxSize2), Validators.pattern(pattern)]],
      })
    )
  );
}

/**
 * Crea un Form di FormGroup basato su un array di oggetti dinamici T,
 * impostando valori di default e validatori per ogni controllo.
 * @param fb  FormBuilder instance
 * @param items  Array of items to create the FormArray from
 * @param idSezione1OrPiao ID of the first section or PIAO, used as a default value for the second key
 * @param keys  Keys to use for the form controls
 * @param maxSize1  Maximum length for the third key's value
 * @param maxSize2  Maximum length for the fourth key's value
 * @returns
 */
export function createFormFromPiaoSession<T>(
  fb: FormBuilder,
  item: T,
  idSezione1OrPiao: number | null | undefined,
  keys: (keyof T)[],
  maxSize1: number,
  maxSize2: number,
  pattern: string = INPUT_REGEX
): FormGroup {
  console.log('itemsForm', item);

  return fb.group({
    [keys[0]]: [item?.[keys[0]], [Validators.maxLength(20), Validators.pattern(pattern)]],
    [keys[1]]: [idSezione1OrPiao ?? null, [Validators.maxLength(20), Validators.pattern(pattern)]],
    [keys[2]]: [item?.[keys[2]], [Validators.maxLength(maxSize1), Validators.pattern(pattern)]],
    [keys[3]]: [item?.[keys[3]], [Validators.maxLength(maxSize2), Validators.pattern(pattern)]],
  });
}

/**
 * Crea un FormArray di FormGroup basato su un array di oggetti dinamici T di Mongo,
 * impostando valori di default e validatori per ogni controllo.
 * @param fb  FormBuilder instance
 * @param items  Array of items to create the FormArray from
 * @param sezione1Id  ID of the first section, used as a default value for the second key
 * @param keys  Keys to use for the form controls
 * @param maxSize1  Maximum length for the third key's value
 * @param maxSize2  Maximum length for the fourth key's value
 * @returns
 */
export function createFormArrayFromPiaoSessionMongo<T>(
  fb: FormBuilder,
  items: T[],
  keys: (keyof T)[],
  maxSize1: number,
  maxSize2: number,
  firstValue: boolean,
  pattern: string = INPUT_REGEX,
  keyValue: string
): FormArray<FormGroup> | null {
  if (!Array.isArray(items) || (items.length === 0 && firstValue)) {
    return fb.array<FormGroup>([
      fb.group({
        [keys[0]]: [keyValue, [Validators.maxLength(20), Validators.pattern(pattern)]],
        [keys[1]]: [null, [Validators.maxLength(20), Validators.pattern(pattern)]],
      }),
    ]);
  }

  return fb.array<FormGroup>(
    items.map((item) =>
      fb.group({
        [keys[0]]: [item?.[keys[0]], [Validators.maxLength(maxSize1), Validators.pattern(pattern)]],
        [keys[1]]: [item?.[keys[1]], [Validators.maxLength(maxSize2), Validators.pattern(pattern)]],
      })
    )
  );
}

export function createFormArrayOVPFromPiaoSession(
  fb: FormBuilder,
  items: OVPDTO[],
  pattern: string = INPUT_REGEX,
  idSezione1OrPiao: Record<string, number>
): FormArray<FormGroup> | null {
  if (!Array.isArray(items) || items.length === 0) {
    return fb.array<FormGroup>([]);
  }
  console.log('itemsOVP', items);

  return fb.array<FormGroup>(
    items.map((item) =>
      fb.group({
        id: [item?.id, [Validators.maxLength(20), Validators.pattern(pattern)]],
        denominazione: [
          item?.denominazione,
          [Validators.maxLength(250), Validators.pattern(pattern)],
        ],
        codice: [item?.codice, [Validators.maxLength(250), Validators.pattern(pattern)]],
        descrizione: [item?.descrizione, [Validators.maxLength(500), Validators.pattern(pattern)]],
        contesto: [item?.contesto, [Validators.maxLength(500), Validators.pattern(pattern)]],
        ambito: [item?.ambito, [Validators.maxLength(250), Validators.pattern(pattern)]],
        responsabilePolitico: [
          item?.responsabilePolitico,
          [Validators.maxLength(250), Validators.pattern(pattern)],
        ],
        responsabileAmministrativo: [
          item?.responsabileAmministrativo,
          [Validators.maxLength(250), Validators.pattern(pattern)],
        ],
        valoreIndice: [
          item?.valoreIndice,
          [Validators.maxLength(20), Validators.pattern(ONLY_NUMBERS_REGEX)],
        ],
        descrizioneIndice: [
          item?.descrizioneIndice,
          [Validators.maxLength(500), Validators.pattern(pattern)],
        ],
        sezione21Id: [item?.sezione21Id, [Validators.maxLength(20), Validators.pattern(pattern)]],
        areeOrganizzative: createFormArrayOBJInOBJFromPiaoSession<
          OVPAreaOrganizzativaDTO,
          AreaOrganizzativaDTO
        >(
          fb,
          item?.areeOrganizzative || [],
          ['id', 'areaOrganizzativa'],
          ['id', 'idSezione1', 'nomeArea', 'descrizioneArea'] as (keyof AreaOrganizzativaDTO)[],
          INPUT_REGEX,
          idSezione1OrPiao['idSezione1']
        ),
        prioritaPolitiche: createFormArrayOBJInOBJFromPiaoSession<
          OVPPrioritaPoliticaDTO,
          PrioritaPoliticaDTO
        >(
          fb,
          item?.prioritaPolitiche || [],
          ['id', 'prioritaPolitica'],
          [
            'id',
            'idSezione1',
            'nomePrioritaPolitica',
            'descrizionePrioritaPolitica',
          ] as (keyof PrioritaPoliticaDTO)[],
          INPUT_REGEX,
          idSezione1OrPiao['idSezione1']
        ),
        stakeholders: createFormArrayOBJInOBJFromPiaoSession<OVPStakeHolderDTO, StakeHolderDTO>(
          fb,
          item?.stakeholders || [],
          ['id', 'stakeholder'],
          ['id', 'idPiao', 'nomeStakeHolder', 'relazionePA'] as (keyof StakeHolderDTO)[],
          INPUT_REGEX,
          idSezione1OrPiao['idPiao']
        ),
        ovpStrategias: (() => {
          const formArray = createFormArrayOVPStrategiaFromPiaoSession(
            fb,
            item?.ovpStrategias || [],
            pattern
          );
          formArray?.setValidators(Validators.required);
          return formArray;
        })(),
        risorseFinanziarie: (() => {
          const formArray = createFormArrayWithNControlsFromPiaoSession<RisorsaFinanziariaDTO>(
            fb,
            item?.risorseFinanziarie || [],
            [
              'id',
              'idOvp',
              'iniziativa',
              'descrizione',
              'dotazioneFinanziaria',
              'fonteFinanziamento',
            ],
            [
              ONLY_NUMBERS_REGEX,
              ONLY_NUMBERS_REGEX,
              pattern,
              pattern,
              ONLY_NUMBERS_REGEX,
              ONLY_NUMBERS_REGEX,
            ],
            [20, 20, 250, 500, 20, 50],
            false,
            [false, true, true, true, true, false]
          );
          return formArray;
        })(),
      })
    )
  );
}

function createFormArrayOBJInOBJFromPiaoSession<T, N>(
  fb: FormBuilder,
  items: T[],
  keys: (keyof T)[],
  keys2: (keyof N)[],
  pattern: string = INPUT_REGEX,
  idSezione1OrPiao: number | null | undefined
): FormArray<FormGroup> | null {
  if (!Array.isArray(items) || items.length === 0) {
    return fb.array<FormGroup>([]);
  }
  return fb.array<FormGroup>(
    items.map((item) =>
      fb.group({
        [keys[0]]: [item?.[keys[0]], [Validators.maxLength(20), Validators.pattern(pattern)]],
        [keys[1]]: createFormFromPiaoSession<N>(
          fb,
          item?.[keys[1]] as N,
          idSezione1OrPiao,
          keys2,
          50,
          2000,
          pattern
        ),
      })
    )
  );
}

export function createFormArrayGenericIndicatoreFromPiaoSession<T>(
  fb: FormBuilder,
  items: T[],
  keys: (keyof T)[],
  pattern: string = INPUT_REGEX
): FormArray<FormGroup> | null {
  if (!Array.isArray(items) || items.length === 0) {
    return fb.array<FormGroup>([]);
  }

  return fb.array<FormGroup>(
    items.map((item) =>
      fb.group({
        [keys[0]]: [item?.[keys[0]], [Validators.maxLength(20), Validators.pattern(pattern)]],
        [keys[1]]: createFormIndicatoreFromPiaoSession(
          fb,
          item?.[keys[1]] as IndicatoreDTO,
          pattern
        ),
      })
    )
  );
}

function createFormArrayOVPStrategiaFromPiaoSession(
  fb: FormBuilder,
  items: OVPStrategiaDTO[],
  pattern: string = INPUT_REGEX
): FormArray<FormGroup> | null {
  if (!Array.isArray(items) || items.length === 0) {
    return fb.array<FormGroup>([]);
  }

  return fb.array<FormGroup>(
    items.map((item) =>
      fb.group({
        id: [item?.id, [Validators.maxLength(20), Validators.pattern(pattern)]],
        codStrategia: [item?.codStrategia, [Validators.maxLength(20), Validators.pattern(pattern)]],
        denominazioneStrategia: [
          item?.denominazioneStrategia,
          [Validators.maxLength(250), Validators.pattern(pattern), Validators.required],
        ],
        descrizioneStrategia: [
          item?.descrizioneStrategia,
          [Validators.maxLength(500), Validators.pattern(pattern)],
        ],
        soggettoResponsabile: [
          item?.soggettoResponsabile,
          [Validators.maxLength(100), Validators.pattern(pattern)],
        ],
        indicatori: (() => {
          const formArray =
            createFormArrayGenericIndicatoreFromPiaoSession<OVPStrategiaIndicatoreDTO>(
              fb,
              item?.indicatori || [],
              ['id', 'indicatore'],
              pattern
            );
          formArray?.setValidators(Validators.required);
          return formArray;
        })(),
      })
    )
  );
}

export function createFormArrayObiettivoPrevenzioneCorruzioneTrasparenzaFromPiaoSession(
  fb: FormBuilder,
  items: ObiettivoPrevenzioneCorruzioneTrasparenzaDTO[],
  pattern: string = INPUT_REGEX
): FormArray<FormGroup> | null {
  if (!Array.isArray(items) || items.length === 0) {
    const formArray = fb.array<FormGroup>([]);
    formArray.setValidators(minArrayLength(1));
    return formArray;
  }

  const formArray = fb.array<FormGroup>(
    items.map((item) =>
      fb.group({
        id: [item?.id, [Validators.maxLength(20), Validators.pattern(pattern)]],
        idSezione23: [item?.idSezione23, [Validators.maxLength(20), Validators.pattern(pattern)]],
        idOVP: [
          item?.idOVP,
          [Validators.required, Validators.maxLength(20), Validators.pattern(pattern)],
        ],
        idStrategiaOVP: [
          item?.idStrategiaOVP,
          [Validators.required, Validators.maxLength(20), Validators.pattern(pattern)],
        ],
        idObbiettivoPerformance: [
          item?.idObbiettivoPerformance,
          [Validators.required, Validators.maxLength(20), Validators.pattern(pattern)],
        ],
        codice: [
          item?.codice,
          [Validators.required, Validators.maxLength(50), Validators.pattern(pattern)],
        ],
        denominazione: [
          item?.denominazione,
          [Validators.required, Validators.maxLength(250), Validators.pattern(pattern)],
        ],
        descrizione: [item?.descrizione, [Validators.maxLength(100), Validators.pattern(pattern)]],
        indicatori: (() => {
          const indicatoriFormArray =
            createFormArrayGenericIndicatoreFromPiaoSession<ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoriDTO>(
              fb,
              item?.indicatori || [],
              ['id', 'indicatore'],
              pattern
            );
          indicatoriFormArray?.setValidators(Validators.required);
          return indicatoriFormArray;
        })(),
      })
    )
  );

  formArray.setValidators(minArrayLength(1));
  return formArray;
}

export function createFormArrayObiettivoPerformanceFromPiaoSession(
  fb: FormBuilder,
  items: ObbiettivoPerformanceDTO[],
  pattern: string = INPUT_REGEX,
  idPiao?: number
): FormArray<FormGroup> | null {
  if (!Array.isArray(items) || items.length === 0) {
    return fb.array<FormGroup>([]);
  }

  return fb.array<FormGroup>(
    items.map((item) =>
      fb.group({
        id: [item?.id, [Validators.maxLength(20), Validators.pattern(pattern)]],
        idSezione22: [item?.idSezione22, [Validators.maxLength(20), Validators.pattern(pattern)]],
        idOvp: [
          item?.idOvp,
          item?.tipologia?.toString() !== 'PERFORMANCE_INDIVIDUALE'
            ? [Validators.required, Validators.maxLength(20), Validators.pattern(pattern)]
            : [Validators.maxLength(20), Validators.pattern(pattern)],
        ],
        idObiettivoPeformance: [
          item?.idObiettivoPeformance,
          item?.tipologia?.toString() === 'PERFORMANCE_INDIVIDUALE'
            ? [Validators.required, Validators.maxLength(20), Validators.pattern(pattern)]
            : [Validators.maxLength(20), Validators.pattern(pattern)],
        ],
        idStrategiaOvp: [
          item?.idStrategiaOvp,
          item?.tipologia?.toString() !== 'PERFORMANCE_INDIVIDUALE'
            ? [Validators.required, Validators.maxLength(20), Validators.pattern(pattern)]
            : [Validators.maxLength(20), Validators.pattern(pattern)],
        ],
        codice: [
          item?.codice,
          [Validators.required, Validators.maxLength(50), Validators.pattern(pattern)],
        ],
        tipologia: [
          item?.tipologia,
          [Validators.maxLength(50), Validators.pattern(pattern), Validators.required],
        ],
        tipologiaRisorsa: [
          item?.tipologiaRisorsa,
          item?.tipologia?.toString() === 'PERFORMANCE_INDIVIDUALE'
            ? [Validators.required, Validators.maxLength(20), Validators.pattern(pattern)]
            : [Validators.maxLength(20), Validators.pattern(pattern)],
        ],
        denominazione: [
          item?.denominazione,
          [Validators.maxLength(20), Validators.pattern(pattern), Validators.required],
        ],
        responsabileAmministrativo: [
          item?.responsabileAmministrativo,
          [Validators.maxLength(100), Validators.pattern(pattern)],
        ],
        risorseUmane: [
          item?.risorseUmane,
          [Validators.maxLength(100), Validators.pattern(pattern)],
        ],
        risorseEconomicaFinanziaria: [
          item?.risorseEconomicaFinanziaria,
          [Validators.maxLength(100), Validators.pattern(pattern)],
        ],
        risorseStrumentali: [
          item?.risorseStrumentali,
          [Validators.maxLength(100), Validators.pattern(pattern)],
        ],
        contributoreInterno: createFormMongoFromPiaoSession<ContributoreInternoDTO>(
          fb,
          item?.contributoreInterno || ({} as ContributoreInternoDTO),
          ['id', 'externalId', 'properties'],
          pattern,
          100,
          false
        ),
        stakeholders: [
          // Estrae solo gli ID degli stakeholders per il dropdown multi-select
          item?.stakeholders?.map((sh) => sh.stakeholder?.id).filter((id) => id != null) || [],
          [],
        ],
        indicatori: (() => {
          const formArray = createFormArrayGenericIndicatoreFromPiaoSession<ObiettivoIndicatoriDTO>(
            fb,
            item?.indicatori || [],
            ['id', 'indicatore'],
            pattern
          );
          formArray?.setValidators(Validators.required);
          return formArray;
        })(),
      })
    )
  );
}

export function createFormArrayAdempimentoFromPiaoSession(
  fb: FormBuilder,
  items: AdempimentoDTO[],
  pattern: string = INPUT_REGEX,
  idPiao?: number
): FormArray<FormGroup> | null {
  if (!Array.isArray(items) || items.length === 0) {
    return fb.array<FormGroup>([]);
  }

  return fb.array<FormGroup>(
    items.map((item) =>
      fb.group({
        id: [item?.id, [Validators.maxLength(20), Validators.pattern(pattern)]],
        idSezione22: [item?.idSezione22, [Validators.maxLength(20), Validators.pattern(pattern)]],
        tipologia: [item?.tipologia, [Validators.maxLength(50), Validators.pattern(pattern)]],
        denominazione: [
          item?.denominazione,
          [Validators.required, Validators.maxLength(500), Validators.pattern(pattern)],
        ],
        azione: createFormMongoFromPiaoSession<UlterioriInfoDTO>(
          fb,
          item?.azione || new UlterioriInfoDTO(),
          ['id', 'externalId', 'properties'],
          pattern,
          100,
          false
        ),
        ulterioriInfo: createFormMongoFromPiaoSession<UlterioriInfoDTO>(
          fb,
          item?.ulterioriInfo || new UlterioriInfoDTO(),
          ['id', 'externalId', 'properties'],
          pattern,
          100,
          false
        ),
      })
    )
  );
}

export function createFormArrayFaseFromPiaoSession(
  fb: FormBuilder,
  items: FaseDTO[],
  pattern: string = INPUT_REGEX,
  idPiao?: number
): FormArray<FormGroup> | null {
  if (!Array.isArray(items) || items.length === 0) {
    return fb.array<FormGroup>([]);
  }

  return fb.array<FormGroup>(
    items.map((item) =>
      fb.group({
        id: [item?.id, [Validators.maxLength(20), Validators.pattern(pattern)]],
        idSezione22: [item?.idSezione22, [Validators.maxLength(20), Validators.pattern(pattern)]],
        denominazione: [
          item?.denominazione,
          [Validators.required, Validators.maxLength(500), Validators.pattern(pattern)],
        ],
        descrizione: [item?.descrizione, [Validators.maxLength(2000), Validators.pattern(pattern)]],
        tempi: [item?.tempi, [Validators.maxLength(500), Validators.pattern(pattern)]],
        attore: createFormMongoFromPiaoSession<AttoreDTO>(
          fb,
          item?.attore || new AttoreDTO(),
          ['id', 'externalId', 'properties'],
          pattern,
          100,
          false
        ),
        attivita: createFormMongoFromPiaoSession<AttivitaDTO>(
          fb,
          item?.attivita || new AttivitaDTO(),
          ['id', 'externalId', 'properties'],
          pattern,
          100,
          false
        ),
      })
    )
  );
}

export function createFormArrayAttivitaSensibileFromPiaoSession(
  fb: FormBuilder,
  items: AttivitaSensibileDTO[],
  pattern: string = INPUT_REGEX
): FormArray<FormGroup> | null {
  if (!Array.isArray(items) || items.length === 0) {
    const formArray = fb.array<FormGroup>([]);
    formArray.setValidators(minArrayLength(1));
    return formArray;
  }

  const formArray = fb.array<FormGroup>(
    items.map((item) =>
      fb.group({
        id: [item?.id, [Validators.maxLength(20), Validators.pattern(pattern)]],
        idSezione23: [item?.idSezione23, [Validators.maxLength(20), Validators.pattern(pattern)]],
        denominazione: [
          item?.denominazione,
          [Validators.required, Validators.maxLength(250), Validators.pattern(pattern)],
        ],
        descrizione: [item?.descrizione, [Validators.maxLength(500), Validators.pattern(pattern)]],
        processoCollegato: [
          item?.processoCollegato,
          [Validators.maxLength(250), Validators.pattern(pattern)],
        ],
        attore: createFormMongoFromPiaoSession<AttoreDTO>(
          fb,
          item?.attore || new AttoreDTO(),
          ['id', 'externalId', 'properties'],
          pattern,
          100,
          false
        ),
        ulterioriInfo: createFormMongoFromPiaoSession<UlterioriInfoDTO>(
          fb,
          item?.ulterioriInfo || new UlterioriInfoDTO(),
          ['id', 'externalId', 'properties'],
          pattern,
          100,
          false
        ),
        eventoRischio: createFormArrayEventoRischiosoFromPiaoSession(
          fb,
          item?.eventoRischio || [],
          pattern
        ),
      })
    )
  );

  formArray.setValidators(minArrayLength(1));
  return formArray;
}

/**
 * Crea un FormArray di FormGroup per MisuraPrevenzioneEventoRischioIndicatoreDTO
 * @param fb FormBuilder instance
 * @param items Array di indicatori
 * @param pattern Pattern di validazione
 * @returns FormArray di FormGroup
 */
function createFormArrayMisuraPrevenzioneEventoRischioIndicatoriFromPiaoSession(
  fb: FormBuilder,
  items: MisuraPrevenzioneEventoRischioIndicatoreDTO[],
  pattern: string = INPUT_REGEX
): FormArray<FormGroup> | null {
  if (!Array.isArray(items) || items.length === 0) {
    const formArray = fb.array<FormGroup>([]);
    formArray.setValidators(minArrayLength(1));
    return formArray;
  }

  const formArray = fb.array<FormGroup>(
    items.map((item) =>
      fb.group({
        id: [item?.id, [Validators.maxLength(20), Validators.pattern(pattern)]],
        indicatore: createFormIndicatoreFromPiaoSession(
          fb,
          item?.indicatore as IndicatoreDTO,
          pattern
        ),
      })
    )
  );

  formArray.setValidators(minArrayLength(1));
  return formArray;
}

/**
 * Crea un FormArray di FormGroup per MisuraPrevenzioneEventoRischioStakeholderDTO
 * @param fb FormBuilder instance
 * @param items Array di stakeholder
 * @param pattern Pattern di validazione
 * @returns FormArray di FormGroup
 */
function createFormArrayMisuraPrevenzioneEventoRischioStakeholderFromPiaoSession(
  fb: FormBuilder,
  items: MisuraPrevenzioneEventoRischioStakeholderDTO[],
  pattern: string = INPUT_REGEX
): FormArray<FormGroup> | null {
  if (!Array.isArray(items) || items.length === 0) {
    return fb.array<FormGroup>([]);
  }

  return fb.array<FormGroup>(
    items.map((item) =>
      fb.group({
        id: [item?.id, [Validators.maxLength(20), Validators.pattern(pattern)]],
        stakeholder: fb.group({
          id: [item?.stakeholder?.id, [Validators.maxLength(20), Validators.pattern(pattern)]],
          idPiao: [
            item?.stakeholder?.idPiao,
            [Validators.maxLength(20), Validators.pattern(pattern)],
          ],
          nomeStakeHolder: [
            item?.stakeholder?.nomeStakeHolder,
            [Validators.maxLength(250), Validators.pattern(pattern)],
          ],
          relazionePA: [
            item?.stakeholder?.relazionePA,
            [Validators.maxLength(500), Validators.pattern(pattern)],
          ],
        }),
      })
    )
  );
}

/**
 * Crea un FormArray di FormGroup per MisurePrevenzioneEventoRischioDTO
 * @param fb FormBuilder instance
 * @param items Array di misure di prevenzione
 * @param pattern Pattern di validazione
 * @returns FormArray di FormGroup
 */
export function createFormArrayMisuraPrevenzioneEventoRischioFromPiaoSession(
  fb: FormBuilder,
  items: MisurePrevenzioneEventoRischioDTO[],
  pattern: string = INPUT_REGEX
): FormArray<FormGroup> | null {
  if (!Array.isArray(items) || items.length === 0) {
    const formArray = fb.array<FormGroup>([]);
    return formArray;
  }

  const formArray = fb.array<FormGroup>(
    items.map((item) =>
      fb.group({
        id: [item?.id, [Validators.maxLength(20), Validators.pattern(pattern)]],
        codice: [
          item?.codice,
          [Validators.required, Validators.maxLength(50), Validators.pattern(pattern)],
        ],
        denominazione: [
          item?.denominazione,
          [Validators.required, Validators.maxLength(250), Validators.pattern(pattern)],
        ],
        descrizione: [item?.descrizione, [Validators.maxLength(1000), Validators.pattern(pattern)]],
        responsabile: [
          item?.responsabile,
          [Validators.maxLength(250), Validators.pattern(pattern)],
        ],
        idEventoRischio: [
          item?.idEventoRischio,
          [Validators.maxLength(20), Validators.pattern(pattern), Validators.required],
        ],
        idObiettivoPrevenzioneCorruzioneTrasparenza: [
          item?.idObiettivoPrevenzioneCorruzioneTrasparenza,
          [Validators.maxLength(20), Validators.pattern(pattern), Validators.required],
        ],
        indicatori: createFormArrayMisuraPrevenzioneEventoRischioIndicatoriFromPiaoSession(
          fb,
          item?.indicatori || [],
          pattern
        ),
        stakeholder: createFormArrayMisuraPrevenzioneEventoRischioStakeholderFromPiaoSession(
          fb,
          item?.stakeholder || [],
          pattern
        ),
        monitoraggioPrevenzione: createFormArrayMonitoraggioPrevenzioneFromPiaoSession(
          fb,
          item?.monitoraggioPrevenzione || [],
          pattern
        ),
      })
    )
  );

  formArray.setValidators(minArrayLength(1));
  return formArray;
}

/**
 * Crea un FormArray di FormGroup per EventoRischiosoDTO
 * @param fb FormBuilder instance
 * @param items Array di eventi rischiosi
 * @param pattern Pattern di validazione
 * @returns FormArray di FormGroup
 */
export function createFormArrayEventoRischiosoFromPiaoSession(
  fb: FormBuilder,
  items: EventoRischiosoDTO[],
  pattern: string = INPUT_REGEX
): FormArray<FormGroup> | null {
  if (!Array.isArray(items) || items.length === 0) {
    const formArray = fb.array<FormGroup>([]);
    return formArray;
  }

  const formArray = fb.array<FormGroup>(
    items.map((item) =>
      fb.group({
        id: [item?.id, [Validators.maxLength(20), Validators.pattern(pattern)]],
        idAttivitaSensibile: [
          item?.idAttivitaSensibile,
          [Validators.maxLength(20), Validators.pattern(pattern), Validators.required],
        ],
        denominazione: [
          item?.denominazione,
          [Validators.required, Validators.maxLength(250), Validators.pattern(pattern)],
        ],
        probabilita: [item?.probabilita, [Validators.maxLength(100), Validators.pattern(pattern)]],
        impatto: [item?.impatto, [Validators.maxLength(100), Validators.pattern(pattern)]],
        controlli: [item?.controlli, [Validators.maxLength(500), Validators.pattern(pattern)]],
        valutazione: [item?.valutazione, [Validators.maxLength(500), Validators.pattern(pattern)]],
        idLivelloRischio: [
          item?.idLivelloRischio,
          [Validators.maxLength(20), Validators.pattern(pattern)],
        ],
        motivazione: [
          item?.motivazione,
          [Validators.maxLength(500), Validators.pattern(pattern), Validators.required],
        ],
        fattore: createFormMongoFromPiaoSession<FattoreDTO>(
          fb,
          item?.fattore || new FattoreDTO(),
          ['id', 'externalId', 'properties'],
          pattern,
          100,
          false
        ),
        ulterioriInfo: createFormMongoFromPiaoSession<UlterioriInfoDTO>(
          fb,
          item?.ulterioriInfo || new UlterioriInfoDTO(),
          ['id', 'externalId', 'properties'],
          pattern,
          100,
          false
        ),
        misure: createFormArrayMisuraPrevenzioneEventoRischioFromPiaoSession(
          fb,
          item?.misure || [],
          pattern
        ),
      })
    )
  );

  formArray.setValidators(minArrayLength(1));
  return formArray;
}

export function createFormArrayMonitoraggioPrevenzioneFromPiaoSession(
  fb: FormBuilder,
  items: MonitoraggioPrevenzioneDTO[],
  pattern: string = INPUT_REGEX
): FormArray<FormGroup> | null {
  if (!Array.isArray(items) || items.length === 0) {
    return fb.array<FormGroup>([]);
  }

  return fb.array<FormGroup>(
    items.map((item) =>
      fb.group({
        id: [item?.id, [Validators.maxLength(20), Validators.pattern(pattern)]],
        idSezione23: [item?.idSezione23, [Validators.maxLength(20), Validators.pattern(pattern)]],
        idMisuraPrevenzioneEventoRischio: [
          item?.idMisuraPrevenzioneEventoRischio,
          [Validators.required, Validators.maxLength(20), Validators.pattern(ONLY_NUMBERS_REGEX)],
        ],
        tipologia: [item?.tipologia, [Validators.maxLength(250), Validators.pattern(pattern)]],
        descrizione: [
          item?.descrizione,
          [Validators.maxLength(500), Validators.pattern(pattern), Validators.required],
        ],
        responsabile: [
          item?.responsabile,
          [Validators.maxLength(250), Validators.pattern(pattern)],
        ],
        tempistiche: [item?.tempistiche, [Validators.maxLength(250), Validators.pattern(pattern)]],
      })
    )
  );
}

function createFormIndicatoreFromPiaoSession(
  fb: FormBuilder,
  item: IndicatoreDTO,
  pattern: string = INPUT_REGEX
): FormGroup | null | undefined {
  if (!item || item === null || item === undefined) {
    return undefined;
  }

  console.log('itemIndicatore', item);

  return fb.group({
    id: [item?.id, [Validators.maxLength(20), Validators.pattern(pattern)]],
    denominazione: [item?.denominazione, [Validators.maxLength(100), Validators.pattern(pattern)]],
    idSubDimensioneFK: [
      item?.idSubDimensioneFK,
      [Validators.maxLength(100), Validators.pattern(pattern)],
    ],
    idDimensioneFK: [
      item?.idDimensioneFK,
      [Validators.maxLength(100), Validators.pattern(pattern)],
    ],
    unitaMisura: [item?.unitaMisura, [Validators.maxLength(100), Validators.pattern(pattern)]],
    formula: [item?.formula, [Validators.maxLength(100), Validators.pattern(pattern)]],
    peso: [item?.peso, [Validators.maxLength(100), Validators.pattern(ONLY_NUMBERS_REGEX)]],
    polarita: [item?.polarita, [Validators.maxLength(100), Validators.pattern(pattern)]],
    baseLine: [item?.baseLine, [Validators.maxLength(100), Validators.pattern(ONLY_NUMBERS_REGEX)]],
    consuntivo: [
      item?.consuntivo,
      [Validators.maxLength(100), Validators.pattern(ONLY_NUMBERS_REGEX)],
    ],
    fonteDati: [item?.fonteDati, [Validators.maxLength(100), Validators.pattern(pattern)]],
    tipAndValAnnoCorrente:
      createFormWithNControlsFromPiaoSession<TipologiaAndamentoValoreIndicatoreDTO>(
        fb,
        item?.tipAndValAnnoCorrente as TipologiaAndamentoValoreIndicatoreDTO,
        ['id', 'idTargetFK', 'valore'],
        pattern,
        [20, 200]
      ),
    tipAndValAnno1: createFormWithNControlsFromPiaoSession<TipologiaAndamentoValoreIndicatoreDTO>(
      fb,
      item?.tipAndValAnno1 as TipologiaAndamentoValoreIndicatoreDTO,
      ['id', 'idTargetFK', 'valore'],
      pattern,
      [20, 200]
    ),
    tipAndValAnno2: createFormWithNControlsFromPiaoSession<TipologiaAndamentoValoreIndicatoreDTO>(
      fb,
      item?.tipAndValAnno2 as TipologiaAndamentoValoreIndicatoreDTO,
      ['id', 'idTargetFK', 'valore'],
      pattern,
      [20, 200]
    ),
    rilevante: [item?.rilevante],
    addInfo: createFormMongoFromPiaoSession<UlterioriInfoDTO>(
      fb,
      item?.addInfo as UlterioriInfoDTO,
      ['id', 'externalId', 'properties'],
      pattern,
      50,
      false
    ),
  });
}

export function createFormMongoFromPiaoSession<T>(
  fb: FormBuilder,
  item: T,
  keys: (keyof T)[],
  pattern: string = INPUT_REGEX,
  maxSize1: number,
  firstValue: boolean = false,
  keyValue: string = ''
): FormGroup | null | undefined {
  if (
    (item?.[keys[0]] === undefined || item?.[keys[0]] === null) &&
    (item?.[keys[2]] === undefined || item?.[keys[2]] === null) &&
    !firstValue
  ) {
    return fb.group({
      [keys[0]]: [item?.[keys[0]], [Validators.maxLength(maxSize1), Validators.pattern(pattern)]],
      [keys[1]]: [item?.[keys[1]], [Validators.maxLength(maxSize1), Validators.pattern(pattern)]],
      [keys[2]]: fb.array<FormGroup>([]),
    });
  }

  return fb.group({
    [keys[0]]: [item?.[keys[0]], [Validators.maxLength(maxSize1), Validators.pattern(pattern)]],
    [keys[1]]: [item?.[keys[1]], [Validators.maxLength(maxSize1), Validators.pattern(pattern)]],
    [keys[2]]: createFormArrayFromPiaoSessionMongo<PropertyDTO>(
      fb,
      (item?.[keys[2]] as PropertyDTO[]) || [],
      ['key', 'value'],
      50,
      50,
      firstValue,
      pattern,
      keyValue
    ),
  });
}

export function createFormWithNControlsFromPiaoSession<T>(
  fb: FormBuilder,
  item: T,
  keys: (keyof T)[],
  pattern: string = INPUT_REGEX,
  maxSizes: number | number[]
): FormGroup | null | undefined {
  if (item?.[keys[0]] === undefined || item?.[keys[0]] === null) {
    return undefined;
  }

  const formConfig: Record<string, any> = {};

  keys.forEach((key, index) => {
    const maxLength = Array.isArray(maxSizes) ? maxSizes[index] : maxSizes;
    formConfig[key as string] = [
      item?.[key],
      [Validators.maxLength(maxLength), Validators.pattern(pattern)],
    ];
  });

  return fb.group(formConfig);
}

/**
 * Crea un FormArray di FormGroup basato su un array di oggetti dinamici T con N controlli.
 * @param fb FormBuilder instance
 * @param items Array di oggetti da trasformare in FormArray
 * @param keys Chiavi da usare per i controlli del form
 * @param pattern Pattern regex per la validazione (singolo o array di pattern per ogni campo)
 * @param maxSizes Lunghezze massime per ogni campo (numero singolo o array di numeri)
 * @param createEmptyForm Se true, crea un FormGroup con valori null quando items è vuoto; se false, crea un FormArray vuoto
 * @param requiredFields Array di boolean per indicare quali campi sono required (opzionale)
 * @returns FormArray di FormGroup
 */
export function createFormArrayWithNControlsFromPiaoSession<T>(
  fb: FormBuilder,
  items: T[],
  keys: (keyof T)[],
  pattern: string | string[] = INPUT_REGEX,
  maxSizes: number | number[],
  createEmptyForm: boolean = true,
  requiredFields?: boolean[]
): FormArray<FormGroup> {
  if (!Array.isArray(items) || items.length === 0) {
    if (!createEmptyForm) {
      // Ritorna un FormArray vuoto
      return fb.array<FormGroup>([]);
    }

    // Crea un FormGroup vuoto con valori null
    const formConfig: Record<string, any> = {};
    keys.forEach((key, index) => {
      const maxLength = Array.isArray(maxSizes) ? maxSizes[index] : maxSizes;
      const validatorPattern = Array.isArray(pattern) ? pattern[index] : pattern;
      const isRequired = requiredFields && requiredFields[index];

      const validators = [Validators.maxLength(maxLength), Validators.pattern(validatorPattern)];
      if (isRequired) {
        validators.unshift(Validators.required);
      }

      formConfig[key as string] = [null, validators];
    });
    return fb.array<FormGroup>([fb.group(formConfig)]);
  }

  return fb.array<FormGroup>(
    items.map((item) => {
      const formConfig: Record<string, any> = {};

      keys.forEach((key, index) => {
        const maxLength = Array.isArray(maxSizes) ? maxSizes[index] : maxSizes;
        const validatorPattern = Array.isArray(pattern) ? pattern[index] : pattern;
        const isRequired = requiredFields && requiredFields[index];

        const validators = [Validators.maxLength(maxLength), Validators.pattern(validatorPattern)];
        if (isRequired) {
          validators.unshift(Validators.required);
        }

        formConfig[key as string] = [item?.[key], validators];
      });

      return fb.group(formConfig);
    })
  );
}

/**
 * Calcola lo stato di una sezione in base ai valori del form.
 * @param form FormGroup della sezione
 * @param sectionId ID della sezione (es. '1', '2.1', etc.)
 * @param requiredFieldsConfig Configurazione dei campi obbligatori per sezione
 * @param statusEnum Enum con i valori di stato (DA_COMPILARE, IN_COMPILAZIONE, COMPILATA)
 * @returns Lo stato della sezione
 */
export function getSectionStatus(
  form: FormGroup,
  sectionId: string,
  requiredFieldsConfig: Record<string, string[]>,
  statusEnum: { DA_COMPILARE: string; IN_COMPILAZIONE: string; COMPILATA: string }
): string {
  const formIsNull = areAllValuesNull(form);

  if (formIsNull) {
    return statusEnum.DA_COMPILARE;
  }

  const fieldNull = collectNullPaths(form);
  const requiredFields = requiredFieldsConfig[sectionId] || [];
  const hasNullRequiredFields = requiredFields.some((field) => fieldNull.includes(field));

  if (hasNullRequiredFields) {
    return statusEnum.IN_COMPILAZIONE;
  }

  return statusEnum.COMPILATA;
}

/**
 * Verifica se è possibile aggiungere un nuovo elemento a un FormArray controllando
 * che l'ultimo elemento abbia determinati campi compilati e FormArray nested non vuoti.
 * @param formArray FormArray da verificare
 * @param requiredFields Array di nomi di campi che devono essere compilati
 * @param requiredArrays Array di nomi di FormArray nested che devono essere non vuoti
 * @returns true se è possibile aggiungere un nuovo elemento, false altrimenti
 */
export function canAddToFormArray(
  formArray: FormArray,
  requiredFields: string[] = [],
  requiredArrays: string[] = []
): boolean {
  if (formArray.length === 0) {
    return true;
  }

  const lastElement = formArray.at(formArray.length - 1) as FormGroup;

  // Verifica che tutti i campi required siano compilati
  const allFieldsFilled = requiredFields.every((fieldName) => {
    const value = lastElement.get(fieldName)?.value;
    return value && (typeof value !== 'string' || value.trim() !== '');
  });

  // Verifica che tutti i FormArray nested siano non vuoti
  const allArraysNotEmpty = requiredArrays.every((arrayName) => {
    const array = lastElement.get(arrayName) as FormArray;
    return array && array.length > 0;
  });

  return allFieldsFilled && allArraysNotEmpty;
}

/**
 * Pulisce un array di oggetti BaseMongoDTO rimuovendo:
 * - Oggetti null o undefined
 * - Oggetti con properties null/undefined
 * - Oggetti con properties contenenti elementi con key o value null/undefined/empty
 *
 * @param items Array di oggetti che estendono BaseMongoDTO
 * @returns Array pulito o array vuoto se l'input è null/undefined
 */
export function cleanMongoDTO<T extends { properties?: { key?: string; value?: string }[] }>(
  items: T[] | null | undefined
): T[] {
  if (!Array.isArray(items)) return [];

  return items.filter((item) => {
    // Rimuovi oggetti null o undefined
    if (item === null || item === undefined) return false;

    // Se properties è null o undefined, rimuovi l'oggetto
    if (item.properties === null || item.properties === undefined) return false;

    // Se properties è un array vuoto, lo consideriamo valido
    if (!Array.isArray(item.properties)) return false;

    // Filtra le properties rimuovendo quelle con key o value null/undefined/empty
    item.properties = item.properties.filter((prop) => {
      if (prop === null || prop === undefined) return false;

      const keyIsValid =
        prop.key !== null &&
        prop.key !== undefined &&
        (typeof prop.key !== 'string' || prop.key.trim() !== '');
      const valueIsValid =
        prop.value !== null &&
        prop.value !== undefined &&
        (typeof prop.value !== 'string' || prop.value.trim() !== '');

      return keyIsValid && valueIsValid;
    });

    // Se dopo il filtraggio properties è vuoto, rimuovi l'oggetto
    // (opzionale: puoi commentare questa riga se vuoi mantenere oggetti con properties vuoto)
    if (item.properties.length === 0) return false;

    return true;
  });
}

/**
 * Pulisce un singolo oggetto BaseMongoDTO rimuovendo properties con key o value null/undefined/empty
 *
 * @param item Oggetto che estende BaseMongoDTO
 * @returns Oggetto pulito o null se l'input è null/undefined o non ha properties valide
 */
export function cleanSingleMongoDTO<T extends { properties?: { key?: string; value?: string }[] }>(
  item: T | null | undefined
): T | undefined {
  // Se l'oggetto è null o undefined, ritorna null
  if (item === null || item === undefined) return undefined;

  // Se properties è null o undefined, ritorna null
  if (item.properties === null || item.properties === undefined) return undefined;

  // Se properties non è un array, ritorna null
  if (!Array.isArray(item.properties)) return undefined;

  // Filtra le properties rimuovendo quelle con key o value null/undefined/empty
  item.properties = item.properties.filter((prop) => {
    if (prop === null || prop === undefined) return false;

    const keyIsValid =
      prop.key !== null &&
      prop.key !== undefined &&
      (typeof prop.key !== 'string' || prop.key.trim() !== '');
    const valueIsValid =
      prop.value !== null &&
      prop.value !== undefined &&
      (typeof prop.value !== 'string' || prop.value.trim() !== '');

    return keyIsValid && valueIsValid;
  });

  return item;
}

/**
 * Mappa un array di oggetti con proprietà value e id in formato LabelValue
 */
export function mapToLabelValue(items: Array<{ value?: string; id?: number }>): LabelValue[] {
  return items.map((item) => ({
    label: item.value || '',
    value: item.id || 0,
  }));
}

export function printFormErrors(control: FormGroup | FormArray, path: string = ''): void {
  if (control instanceof FormGroup) {
    Object.keys(control.controls).forEach((key) => {
      const childControl = control.get(key);
      const currentPath = path ? `${path}.${key}` : key;

      if (childControl?.errors?.['required']) {
        console.log(`❌ REQUIRED: ${currentPath}`);
      }
      if (childControl?.errors?.['minlength']) {
        console.log(
          `❌ MINLENGTH: ${currentPath} (required: ${childControl.errors['minlength'].requiredLength}, actual: ${childControl.errors['minlength'].actualLength})`
        );
      }
      if (childControl?.errors?.['minArrayLength']) {
        console.log(
          `❌ MIN_ARRAY_LENGTH: ${currentPath} (required: ${childControl.errors['minArrayLength'].requiredLength}, actual: ${childControl.errors['minArrayLength'].actualLength})`
        );
      }

      if (childControl instanceof FormGroup || childControl instanceof FormArray) {
        printFormErrors(childControl, currentPath);
      }
    });
  } else if (control instanceof FormArray) {
    if (control.errors?.['required']) {
      console.log(`❌ REQUIRED: ${path} (FormArray)`);
    }
    if (control.errors?.['minlength']) {
      console.log(
        `❌ MINLENGTH: ${path} (FormArray - required: ${control.errors['minlength'].requiredLength}, actual: ${control.errors['minlength'].actualLength})`
      );
    }
    if (control.errors?.['minArrayLength']) {
      console.log(
        `❌ MIN_ARRAY_LENGTH: ${path} (FormArray - required: ${control.errors['minArrayLength'].requiredLength}, actual: ${control.errors['minArrayLength'].actualLength})`
      );
    }

    control.controls.forEach((childControl, index) => {
      const currentPath = `${path}[${index}]`;
      if (childControl instanceof FormGroup || childControl instanceof FormArray) {
        printFormErrors(childControl, currentPath);
      }
    });
  }
}

export function getTodayISO(): string {
  return new Date().toISOString().split('T')[0];
}
