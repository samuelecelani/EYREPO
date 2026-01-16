import {
  AbstractControl,
  FormControl,
  FormGroup,
  FormArray,
  FormBuilder,
  Validators,
} from '@angular/forms';
import { INPUT_REGEX } from './constants';

/**
 * Raccoglie i path di tutti i controlli in un AbstractControl (FormGroup, FormArray, FormControl)
 * che hanno valore null, undefined o stringa vuota.
 * @param control Controllo astratto da analizzare
 * @param path Percorso corrente (usato internamente per la ricorsione)
 * @returns Array di stringhe con i path dei controlli nulli
 */
export function collectNullPaths(control: AbstractControl, path: string[] = []): string[] {
  const paths: string[] = [];

  if (control instanceof FormControl) {
    if (control.value === null || control.value === '' || control.value === undefined)
      paths.push(path.join('.'));
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
 * Crea un FormArray di FormGroup basato su un array di oggetti dinamici T,
 * impostando valori di default e validatori per ogni controllo.
 * @param fb  FormBuilder instance
 * @param items  Array of items to create the FormArray from
 * @param sezione1Id  ID of the first section, used as a default value for the second key
 * @param keys  Keys to use for the form controls
 * @param maxSize1  Maximum length for the third key's value
 * @param maxSize2  Maximum length for the fourth key's value
 * @returns
 */
export function createFormArrayFromPiaoSession<T>(
  fb: FormBuilder,
  items: T[],
  sezione1Id: number | null | undefined,
  keys: (keyof T)[],
  maxSize1: number,
  maxSize2: number
): FormArray<FormGroup> | null {
  if (!Array.isArray(items) || items.length === 0) {
    return fb.array<FormGroup>([
      fb.group({
        [keys[0]]: [null, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
        [keys[1]]: [
          sezione1Id || null,
          [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
        ],
        [keys[2]]: [null, [Validators.maxLength(maxSize1), Validators.pattern(INPUT_REGEX)]],
        [keys[3]]: [null, [Validators.maxLength(maxSize2), Validators.pattern(INPUT_REGEX)]],
      }),
    ]);
  }

  return fb.array<FormGroup>(
    items.map((item) =>
      fb.group({
        [keys[0]]: [item?.[keys[0]], [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
        [keys[1]]: [
          sezione1Id ?? null,
          [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
        ],
        [keys[2]]: [
          item?.[keys[2]],
          [Validators.maxLength(maxSize1), Validators.pattern(INPUT_REGEX)],
        ],
        [keys[3]]: [
          item?.[keys[3]],
          [Validators.maxLength(maxSize2), Validators.pattern(INPUT_REGEX)],
        ],
      })
    )
  );
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
  firtsValue: boolean
): FormArray<FormGroup> | null {
  if (!Array.isArray(items) || (items.length === 0 && firtsValue)) {
    return fb.array<FormGroup>([
      fb.group({
        [keys[0]]: [null, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
        [keys[1]]: [null, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
      }),
    ]);
  }

  return fb.array<FormGroup>(
    items.map((item) =>
      fb.group({
        [keys[0]]: [
          item?.[keys[0]],
          [Validators.maxLength(maxSize1), Validators.pattern(INPUT_REGEX)],
        ],
        [keys[1]]: [
          item?.[keys[1]],
          [Validators.maxLength(maxSize2), Validators.pattern(INPUT_REGEX)],
        ],
      })
    )
  );
}
