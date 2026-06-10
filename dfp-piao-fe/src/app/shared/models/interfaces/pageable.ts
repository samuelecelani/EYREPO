/**
 * Modelli equivalenti a Spring Data Pageable / Page<T>.
 * Utilizzabili lato FE per qualunque endpoint paginato.
 */

/** Direzione di ordinamento. */
export type SortDirection = 'asc' | 'desc';

/** Singolo criterio di ordinamento (es. { property: 'amministrazione', direction: 'asc' }). */
export interface SortCriteria {
  property: string;
  direction: SortDirection;
}

/** Richiesta di paginazione/ordinamento da inviare al BE. */
export interface Pageable {
  /** Indice pagina 0-based (Spring Data convention). */
  page: number;
  /** Numero di elementi per pagina. */
  size: number;
  /** Ordinamenti opzionali (più criteri ammessi). */
  sort?: SortCriteria[];
}

/** Risposta paginata standard Spring Data. */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  /** Indice pagina corrente 0-based. */
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

/** Helper: serializza il Pageable in query params per HttpClient. */
export function pageableToParams(p: Pageable): Record<string, string | string[]> {
  const params: Record<string, string | string[]> = {
    page: String(p.page),
    size: String(p.size),
  };
  if (p.sort && p.sort.length > 0) {
    // Spring accetta più parametri 'sort=property,direction'
    params['sort'] = p.sort.map((s) => `${s.property},${s.direction}`);
  }
  return params;
}

