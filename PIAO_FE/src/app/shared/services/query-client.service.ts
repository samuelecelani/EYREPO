import { Injectable, Signal, computed, signal, WritableSignal } from '@angular/core';
import { firstValueFrom, isObservable, Observable } from 'rxjs';

/**
 * Stati possibili di una query
 * Come in React Query: idle → loading → success/error
 */
type QueryStatus = 'idle' | 'loading' | 'success' | 'error';

/**
 * Stato completo di una query (dati + metadati)
 */
interface QueryState<T> {
  data: T | null; // Dati fetchati
  error: unknown | null; // Eventuale errore
  status: QueryStatus; // Stato corrente
  updatedAt: number | null; // Timestamp ultimo fetch (per staleness)
}

/**
 * Opzioni configurabili per una query
 */
interface QueryOptions {
  staleTimeMs?: number; // Dopo quanto i dati sono considerati "vecchi" (default: 60s)
  refetchOnMount?: boolean; // Refetch quando il componente monta (default: false)
}

/**
 * Selettori reattivi esposti al componente
 * ANALOGIA REACT: simile al return di useQuery
 */
interface QuerySelectors<T> {
  data: Signal<T | null>; // Signal reattivo con i dati
  status: Signal<QueryStatus>; // Signal con lo stato
  error: Signal<unknown | null>; // Signal con errore
  refetch: () => void; // Forza un nuovo fetch
  invalidate: () => void; // Marca i dati come stale
}

/**
 * Entry interna nella cache
 */
interface QueryEntry<T> {
  state: WritableSignal<QueryState<T>>;
  staleTimeMs: number;
}

/**
 * QueryClientService - Sistema di caching per API con Signals
 *
 * ANALOGIA REACT:
 * Simile a React Query o RTK Query, ma specifico per Angular con Signals
 *
 * COME FUNZIONA:
 * 1. Ogni query ha una chiave univoca ('users:list', 'products:detail:123')
 * 2. I dati sono cachati in memoria con timestamp
 * 3. Se i dati sono "stale" (vecchi), refetch automatico
 * 4. Espone Signals reattivi per data/status/error
 *
 * ESEMPIO USO:
 * ```typescript
 * usersQuery = this.query.useQuery<User[]>(
 *   'users:list',
 *   () => this.usersService.list(),
 *   { staleTimeMs: 60000 }
 * );
 *
 * // Nel template
 * {{ usersQuery.data() }}
 * {{ usersQuery.status() }}
 *
 * // Invalidare e refetch dopo mutation
 * this.usersQuery.invalidate();
 * this.usersQuery.refetch();
 * ```
 *
 * VANTAGGI:
 * - Evita fetch duplicati
 * - Cache automatico
 * - Loading/error states gestiti
 * - Reattività con Signals
 */
@Injectable({ providedIn: 'root' })
export class QueryClientService {
  // Cache globale: Map<chiave, entry>
  // Analogia React: simile al QueryCache interno di React Query
  private cache = new Map<string, QueryEntry<any>>();

  /**
   * Hook principale per query con cache
   *
   * ANALOGIA REACT:
   * ```typescript
   * // React Query
   * const { data, isLoading, error, refetch } = useQuery(['users'], fetchUsers);
   *
   * // RTK Query
   * const { data, isLoading, error, refetch } = useGetUsersQuery();
   * ```
   *
   * @param key - Chiave univoca per la cache (es: 'users:list', 'products:detail:123')
   * @param fetcher - Funzione che ritorna Observable o Promise con i dati
   * @param options - Configurazione (staleTimeMs, refetchOnMount)
   * @returns Selettori reattivi (data, status, error, refetch, invalidate)
   */
  useQuery<T>(
    key: string,
    fetcher: () => Observable<T> | Promise<T>,
    options?: QueryOptions
  ): QuerySelectors<T> {
    // 1. Ottieni o crea entry nella cache
    const entry = this.ensureEntry<T>(key, options);

    // 2. Decidi se fetchare (stale, idle, o refetchOnMount)
    const shouldFetch =
      this.isStale(entry) || entry.state().status === 'idle' || options?.refetchOnMount;
    if (shouldFetch) {
      void this.fetchInto<T>(key, fetcher);
    }

    // 3. Crea computed signals (reattivi)
    const data = computed(() => entry.state().data);
    const status = computed(() => entry.state().status);
    const error = computed(() => entry.state().error);

    // 4. Metodi esposti
    const refetch = () => {
      void this.fetchInto<T>(key, fetcher, true);
    };

    const invalidate = () => {
      const current = entry.state();
      entry.state.set({ ...current, updatedAt: null });
    };

    return { data, status, error, refetch, invalidate };
  }

  invalidate(key: string) {
    const entry = this.cache.get(key);
    if (entry) {
      const current = entry.state();
      entry.state.set({ ...current, updatedAt: null });
    }
  }

  private ensureEntry<T>(key: string, options?: QueryOptions): QueryEntry<T> {
    let entry = this.cache.get(key) as QueryEntry<T> | undefined;
    if (!entry) {
      entry = {
        state: signal<QueryState<T>>({ data: null, error: null, status: 'idle', updatedAt: null }),
        staleTimeMs: options?.staleTimeMs ?? 60_000,
      };
      this.cache.set(key, entry);
    } else if (options?.staleTimeMs !== undefined) {
      entry.staleTimeMs = options.staleTimeMs;
    }
    return entry;
  }

  private isStale<T>(entry: QueryEntry<T>): boolean {
    const s = entry.state();
    if (s.updatedAt == null) return true;
    return Date.now() - s.updatedAt > entry.staleTimeMs;
  }

  private async fetchInto<T>(
    key: string,
    fetcher: () => Observable<T> | Promise<T>,
    force = false
  ) {
    const entry = this.cache.get(key) as QueryEntry<T>;
    const current = entry.state();
    if (!force && current.status === 'loading') return;
    entry.state.set({ ...current, status: 'loading', error: null });
    try {
      const result = isObservable(fetcher())
        ? await firstValueFrom(fetcher() as Observable<T>)
        : await (fetcher() as Promise<T>);
      entry.state.set({ data: result, error: null, status: 'success', updatedAt: Date.now() });
    } catch (err) {
      entry.state.set({ ...current, error: err, status: 'error', updatedAt: Date.now() });
    }
  }
}
