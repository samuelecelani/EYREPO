import { inject, Injectable } from '@angular/core';
import {
  HttpClient,
  HttpHeaders,
  HttpParams,
  HttpContext,
  HttpErrorResponse,
} from '@angular/common/http';
import { timeout, catchError, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Path } from '../utils/path';

/**
 * BaseApiService - Servizio base per tutte le chiamate HTTP
 *
 * ANALOGIA REACT:
 * Simile a creare un axios instance configurato o un fetch wrapper
 *
 * COME FUNZIONA:
 * - Centralizza la logica HTTP (headers, timeout, base URL)
 * - Tutti i servizi API lo usano come base
 * - Gli interceptor processano le richieste automaticamente
 *
 * ESEMPIO USO:
 * ```typescript
 * class UsersService {
 *   private api = inject(BaseApiService);
 *
 *   list() {
 *     return this.api.get<User[]>('/users');
 *   }
 * }
 * ```
 *
 * VANTAGGI:
 * - DRY: non ripetere configurazione in ogni servizio
 * - Type-safe: TypeScript generics per response
 * - Configurabile: headers, timeout, baseUrl personalizzabili
 */
@Injectable({ providedIn: 'root' })
export class BaseApiService {
  // Dependency Injection: come useContext in React ma type-safe
  private http = inject(HttpClient);

  private defaultHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      Accept: 'application/json',
    });
  }

  /**
   * Risolve URL relative in URL complete
   *
   * ESEMPIO:
   * - pathOrUrl = '/users' → 'http://api.example.com/users'
   * - pathOrUrl = 'http://other.com/data' → 'http://other.com/data' (assoluto)
   */
  private resolveUrl(pathOrUrl: string, baseOverride?: string): string {
    // Se è già un URL assoluto, lo uso così com'è
    if (/^https?:\/\//i.test(pathOrUrl)) return pathOrUrl;
    const base = baseOverride ?? Path.baseUrl();
    // Evita doppi slash durante la concatenazione
    const left = base.endsWith('/') ? base.slice(0, -1) : base;
    const right = pathOrUrl.startsWith('/') ? pathOrUrl : `/${pathOrUrl}`;
    return `${left}${right}`;
  }

  private mergeHeaders(custom?: HttpHeaders | Record<string, string>): HttpHeaders {
    const base = this.defaultHeaders();
    if (!custom) return base;
    if (custom instanceof HttpHeaders) {
      let merged = base;
      custom.keys().forEach((k) => {
        const v = custom.get(k);
        if (v !== null) merged = merged.set(k, v);
      });
      return merged;
    }
    return Object.entries(custom).reduce((acc, [k, v]) => acc.set(k, v), base);
  }

  /**
   * HTTP GET Request
   *
   * ANALOGIA REACT:
   * ```typescript
   * // RTK Query
   * getUsers: builder.query<User[], void>({ query: () => '/users' })
   *
   * // Fetch
   * const response = await fetch('/users');
   * ```
   *
   * USO:
   * ```typescript
   * this.api.get<User[]>('/users')
   * this.api.get<User>('/users/123')
   * this.api.get<Product[]>('/products', { params: { category: 'books' } })
   * ```
   */
  get<T>(
    pathOrUrl: string,
    options?: {
      baseUrl?: string;
      params?: HttpParams | Record<string, string | number | boolean>;
      headers?: HttpHeaders | Record<string, string>;
      context?: HttpContext;
      withCredentials?: boolean;
    }
  ) {
    const url = this.resolveUrl(pathOrUrl, options?.baseUrl);
    const obs = this.http.get<T>(url, {
      headers: this.mergeHeaders(options?.headers),
      params: options?.params as any,
      context: options?.context,
      withCredentials: options?.withCredentials ?? true,
      observe: 'response' as any,
    });
    return obs.pipe(
      map((response: any) => response.body ?? ({} as T)),
      catchError((error: unknown) => {
        if (error instanceof HttpErrorResponse && error.status >= 200 && error.status < 300) {
          return [{} as T];
        }
        throw error;
      })
    );
  }

  /**
   * HTTP POST Request
   *
   * ANALOGIA REACT:
   * ```typescript
   * // RTK Query
   * createUser: builder.mutation<User, CreateUserDto>({
   *   query: (body) => ({ url: '/users', method: 'POST', body })
   * })
   *
   * // Fetch
   * const response = await fetch('/users', {
   *   method: 'POST',
   *   body: JSON.stringify(data)
   * });
   * ```
   *
   * USO:
   * ```typescript
   * this.api.post<User, CreateUserDto>('/users', { name: 'Mario', email: 'mario@test.it' })
   * ```
   */
  post<T, B = unknown>(
    pathOrUrl: string,
    body: B,
    options?: {
      baseUrl?: string;
      params?: HttpParams | Record<string, string | number | boolean>;
      headers?: HttpHeaders | Record<string, string>;
      context?: HttpContext;
      withCredentials?: boolean;
      responseType?: 'json' | 'text';
    }
  ) {
    const url = this.resolveUrl(pathOrUrl, options?.baseUrl);
    const obs = this.http.post<T>(url, body, {
      headers: this.mergeHeaders(options?.headers),
      params: options?.params as any,
      context: options?.context,
      withCredentials: options?.withCredentials ?? true,
      responseType: (options?.responseType as any) || 'json',
      observe: 'response' as any,
    });
    return obs.pipe(
      map((response: any) => {
        // Se lo status è 2xx, è un successo
        // Ritorna il body, oppure un oggetto vuoto se il body è null
        return response.body ?? ({} as T);
      }),
      catchError((error: unknown) => {
        // Se l'errore ha status 2xx (es. errore di parsing), è comunque un successo
        if (error instanceof HttpErrorResponse && error.status >= 200 && error.status < 300) {
          // Status 2xx = successo, anche se il body non è parsabile
          return [{} as T];
        }
        // Status 4xx, 5xx o errori di rete = errore reale
        throw error;
      })
    );
  }

  /**
   * HTTP PUT Request (aggiornamento completo)
   *
   * ANALOGIA REACT: fetch con method PUT
   *
   * USO:
   * ```typescript
   * this.api.put<User, UpdateUserDto>('/users/123', { name: 'New Name', email: 'new@email.com' })
   * ```
   */
  put<T, B = unknown>(
    pathOrUrl: string,
    body: B,
    options?: {
      baseUrl?: string;
      params?: HttpParams | Record<string, string | number | boolean>;
      headers?: HttpHeaders | Record<string, string>;
      context?: HttpContext;
      withCredentials?: boolean;
    }
  ) {
    const url = this.resolveUrl(pathOrUrl, options?.baseUrl);
    const obs = this.http.put<T>(url, body, {
      headers: this.mergeHeaders(options?.headers),
      params: options?.params as any,
      context: options?.context,
      withCredentials: options?.withCredentials ?? true,
      observe: 'response' as any,
    });
    return obs.pipe(
      map((response: any) => response.body ?? ({} as T)),
      catchError((error: unknown) => {
        if (error instanceof HttpErrorResponse && error.status >= 200 && error.status < 300) {
          return [{} as T];
        }
        throw error;
      })
    );
  }

  /**
   * HTTP PATCH Request (aggiornamento parziale)
   *
   * USO:
   * ```typescript
   * this.api.patch<User>('/users/123', { name: 'New Name' }) // solo name
   * ```
   */
  patch<T, B = unknown>(
    pathOrUrl: string,
    body: B,
    options?: {
      baseUrl?: string;
      params?: HttpParams | Record<string, string | number | boolean>;
      headers?: HttpHeaders | Record<string, string>;
      context?: HttpContext;
      withCredentials?: boolean;
    }
  ) {
    const url = this.resolveUrl(pathOrUrl, options?.baseUrl);
    const obs = this.http.patch<T>(url, body, {
      headers: this.mergeHeaders(options?.headers),
      params: options?.params as any,
      context: options?.context,
      withCredentials: options?.withCredentials ?? true,
      observe: 'response' as any,
    });
    return obs.pipe(
      map((response: any) => response.body ?? ({} as T)),
      catchError((error: unknown) => {
        if (error instanceof HttpErrorResponse && error.status >= 200 && error.status < 300) {
          return [{} as T];
        }
        throw error;
      })
    );
  }

  /**
   * HTTP DELETE Request
   *
   * USO:
   * ```typescript
   * this.api.delete<void>('/users/123')
   * ```
   */
  delete<T>(
    pathOrUrl: string,
    options?: {
      baseUrl?: string;
      params?: HttpParams | Record<string, string | number | boolean>;
      headers?: HttpHeaders | Record<string, string>;
      context?: HttpContext;
      withCredentials?: boolean;
    }
  ) {
    const url = this.resolveUrl(pathOrUrl, options?.baseUrl);
    const obs = this.http.delete<T>(url, {
      headers: this.mergeHeaders(options?.headers),
      params: options?.params as any,
      context: options?.context,
      withCredentials: options?.withCredentials ?? true,
      observe: 'response' as any,
    });
    return obs.pipe(
      map((response: any) => response.body ?? ({} as T)),
      catchError((error: unknown) => {
        if (error instanceof HttpErrorResponse && error.status >= 200 && error.status < 300) {
          return [{} as T];
        }
        throw error;
      })
    );
  }

  /**
   * Factory per ottenere un client "scopato" a una base URL specifica
   *
   * USO:
   * ```typescript
   * const externalApi = this.api.forBase('https://api.external.com');
   * externalApi.get<Data>('/endpoint');
   * ```
   */
  forBase(baseUrl: string) {
    const self = this;
    return {
      get<T>(pathOrUrl: string, opts?: Omit<Parameters<BaseApiService['get']>[1], 'baseUrl'>) {
        return self.get<T>(pathOrUrl, { ...(opts ?? {}), baseUrl });
      },
      post<T, B = unknown>(
        pathOrUrl: string,
        body: B,
        opts?: Omit<Parameters<BaseApiService['post']>[2], 'baseUrl'>
      ) {
        return self.post<T, B>(pathOrUrl, body, { ...(opts ?? {}), baseUrl });
      },
      put<T, B = unknown>(
        pathOrUrl: string,
        body: B,
        opts?: Omit<Parameters<BaseApiService['put']>[2], 'baseUrl'>
      ) {
        return self.put<T, B>(pathOrUrl, body, { ...(opts ?? {}), baseUrl });
      },
      patch<T, B = unknown>(
        pathOrUrl: string,
        body: B,
        opts?: Omit<Parameters<BaseApiService['patch']>[2], 'baseUrl'>
      ) {
        return self.patch<T, B>(pathOrUrl, body, { ...(opts ?? {}), baseUrl });
      },
      delete<T>(
        pathOrUrl: string,
        opts?: Omit<Parameters<BaseApiService['delete']>[1], 'baseUrl'>
      ) {
        return self.delete<T>(pathOrUrl, { ...(opts ?? {}), baseUrl });
      },
    };
  }
}
