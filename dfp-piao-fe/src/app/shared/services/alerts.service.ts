import { inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map, shareReplay, tap } from 'rxjs/operators';
import { BaseApiService } from './base-api.service';
import { GenericResponse } from '../models/interfaces/generic-response';
import { AlertsDTO } from '../models/classes/alerts-dto';
import { CreateAvvisoDTO, UpdateAvvisoDTO } from '../models/classes/avviso-dto';
import { IAlert } from '../ui/alerts/alerts.component';
import { TBadge } from '../components/badge/badge.component';

/**
 * Alerts Service con pattern RTK Query-like
 *
 * ✅ Caching automatico
 * ✅ Invalidazione cache
 * ✅ Refetch on demand
 *
 * Analogia React: simile a RTK Query endpoints
 */
@Injectable({ providedIn: 'root' })
export class AlertsService {
  private api = inject(BaseApiService);

  // Cache delle query (chiave = parametri della query)
  private cache = new Map<string, Observable<IAlert[]>>();
  private rawListCache = new Map<string, Observable<AlertsDTO[]>>();
  private detailCache = new Map<string, Observable<AlertsDTO | null>>();

  /**
   * GET /avvisi - Recupera tutti gli avvisi
   * (mantiene compatibilita con il codice esistente)
   *
   * @returns Observable con la risposta generica contenente array di AlertsDTO
   */
  getAllAlerts(_modulo: string = 'PIAO'): Observable<GenericResponse<AlertsDTO[]>> {
    return this.api.get<GenericResponse<AlertsDTO[]>>('/avvisi');
  }

  /**
   * GET /avvisi - Recupera la lista DTO completa per la pagina CRUD.
   */
  getAllAvvisi(modulo?: string): Observable<AlertsDTO[]> {
    const cacheKey = `${modulo || 'all'}`;

    if (this.rawListCache.has(cacheKey)) {
      return this.rawListCache.get(cacheKey)!;
    }

    const query$ = this.getAllAlerts(modulo).pipe(
      map((response) => this.unwrapData(response, [])),
      catchError((error) => {
        console.warn('Avvisi list API error', error);
        return of([]);
      }),
      shareReplay(1)
    );

    this.rawListCache.set(cacheKey, query$);
    return query$;
  }

  /**
   * GET /avvisi/{id}
   */
  getById(id: number | string): Observable<GenericResponse<AlertsDTO>> {
    return this.api.get<GenericResponse<AlertsDTO>>(`/avvisi/${id}`);
  }

  /**
   * GET /avvisi/{id} - restituisce solo il DTO con cache locale.
   */
  getByIdData(id: number | string): Observable<AlertsDTO | null> {
    const normalizedId = String(id);

    if (this.detailCache.has(normalizedId)) {
      return this.detailCache.get(normalizedId)!;
    }

    const query$ = this.getById(normalizedId).pipe(
      map((response) => this.unwrapData(response, null)),
      catchError((error) => {
        console.warn('Avviso detail API error', error);
        return of(null);
      }),
      shareReplay(1)
    );

    this.detailCache.set(normalizedId, query$);
    return query$;
  }

  /**
   * POST /avvisi
   */
  create(body: CreateAvvisoDTO): Observable<GenericResponse<AlertsDTO>> {
    return this.api.post<GenericResponse<AlertsDTO>, CreateAvvisoDTO>('/avvisi', body).pipe(
      tap(() => this.invalidate())
    );
  }

  /**
   * PUT /avvisi/{id}
   */
  update(id: number | string, body: UpdateAvvisoDTO): Observable<GenericResponse<AlertsDTO>> {
    return this.api
      .put<GenericResponse<AlertsDTO>, UpdateAvvisoDTO>(`/avvisi/${id}`, body)
      .pipe(tap(() => this.invalidate(undefined, String(id))));
  }

  /**
   * DELETE /avvisi/{id}
   */
  delete(id: number | string): Observable<GenericResponse<Record<string, never>>> {
    return this.api
      .delete<GenericResponse<Record<string, never>>>(`/avvisi/${id}`)
      .pipe(tap(() => this.invalidate(undefined, String(id))));
  }

  /**
   * GET /avvisi - Recupera la lista degli avvisi con caching automatico
   *
   * @param modulo - Parametro modulo per filtrare gli avvisi
   * @returns Observable con array di AlertDto
   */
  list(modulo?: string): Observable<IAlert[]> {
    const cacheKey = `${modulo || 'all'}`;

    // Se è in cache, ritorna dalla cache
    if (this.cache.has(cacheKey)) {
      return this.cache.get(cacheKey)!;
    }

    const query$ = this.getAllAvvisi(modulo).pipe(
      map((response) => this.mapApiToAlertDto(response)),
      catchError((error) => {
        console.warn('API error', error);
        return of([]);
      }),
      shareReplay(1)
    );

    this.cache.set(cacheKey, query$);
    return query$;
  }

  /**
   * Invalida la cache (simile a invalidateTags di RTK Query)
   */
  invalidate(modulo?: string, id?: string): void {
    if (modulo) {
      const keysToDelete: string[] = [];
      this.cache.forEach((_, key) => {
        if (key.startsWith(modulo)) keysToDelete.push(key);
      });
      keysToDelete.forEach((key) => this.cache.delete(key));

      const rawKeysToDelete: string[] = [];
      this.rawListCache.forEach((_, key) => {
        if (key.startsWith(modulo)) rawKeysToDelete.push(key);
      });
      rawKeysToDelete.forEach((key) => this.rawListCache.delete(key));
    } else {
      this.cache.clear();
      this.rawListCache.clear();
    }

    if (id) {
      this.detailCache.delete(id);
    } else if (!modulo) {
      this.detailCache.clear();
    }
  }

  /**
   * Refetch: forza un nuovo fetch ignorando la cache
   */
  refetch(modulo?: string): Observable<IAlert[]> {
    this.cache.delete(`${modulo || 'all'}`);
    this.rawListCache.delete(`${modulo || 'all'}`);
    return this.list(modulo);
  }

  /**
   * Mappa i dati dell'API al formato AlertDto usato nel frontend
   */
  private mapApiToAlertDto(apiData: AlertsDTO[]): IAlert[] {
    return apiData.map((item) => ({
      id: item.id || '',
      badgeText: item.tipologiaContenuto || item.tipoAvviso || 'Avviso',
      badgeVariant: this.getBadgeVariantByType(item.tipologiaContenuto || item.tipoAvviso),
      title: item.oggetto || item.titolo || '',
      description: item.messaggio || item.intro || item.descrizione || '',
      date: item.dataPubblicazione || item.createdTs?.toString() || new Date().toISOString(),
      visualizzato: item.visualizzato || false,
    }));
  }

  private unwrapData<T>(response: GenericResponse<T>, fallback: T): T {
    if (!response.status?.success) {
      console.error('API Error:', response.error);
      throw new Error(response.error?.messageError || 'Unknown error');
    }

    return response.data ?? fallback;
  }

  /**
   * Determina la variante del badge in base al tipo di avviso
   */
  private getBadgeVariantByType(tipoAvviso?: string): TBadge['variant'] {
    const variant = (tipoAvviso || '').toUpperCase();

    switch (variant) {
      case 'COMUNICAZIONE':
        return 'secondary';
      case 'PUBBLICATO':
        return 'success';
      case 'EXPIRY':
        return 'expiry';
      case 'ALERT':
        return 'alert';
      default:
        return 'primary';
    }
  }
}
