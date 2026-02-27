import { inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map, shareReplay } from 'rxjs/operators';
import { BaseApiService } from './base-api.service';
import { GenericResponse } from '../models/interfaces/generic-response';
import { AlertsDTO } from '../models/classes/alerts-dto';
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

  /**
   * GET /avvisi - Recupera tutti gli avvisi
   * (mantiene compatibilità con il codice esistente)
   *
   * @param modulo - Parametro modulo (default: 'PIAO')
   * @returns Observable con la risposta generica contenente array di AlertsDTO
   */
  getAllAlerts(modulo: string = 'PIAO'): Observable<GenericResponse<AlertsDTO[]>> {
    const params: Record<string, string> = { modulo };

    return this.api.get<GenericResponse<AlertsDTO[]>>('/avvisi', { params });
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

    // Chiamata API reale
    const params: Record<string, string> = {};
    if (modulo) params['modulo'] = modulo;

    const query$ = this.api.get<GenericResponse<AlertsDTO>>('/avvisi', { params }).pipe(
      map((response) => {
        if (!response.status?.success) {
          console.error('API Error:', response.error);
          throw new Error(response.error?.messageError || 'Unknown error');
        }
        return this.mapApiToAlertDto(response.data);
      }),
      catchError((error) => {
        console.warn('API error', error);
        // In caso di errore, ritorna un array vuoto
        return of([]);
      }),
      shareReplay(1) // Cache e condividi il risultato
    );

    this.cache.set(cacheKey, query$);
    return query$;
  }

  /**
   * Invalida la cache (simile a invalidateTags di RTK Query)
   */
  invalidate(modulo?: string): void {
    if (modulo) {
      const keysToDelete: string[] = [];
      this.cache.forEach((_, key) => {
        if (key.startsWith(modulo)) keysToDelete.push(key);
      });
      keysToDelete.forEach((key) => this.cache.delete(key));
    } else {
      this.cache.clear();
    }
  }

  /**
   * Refetch: forza un nuovo fetch ignorando la cache
   */
  refetch(modulo?: string): Observable<IAlert[]> {
    this.cache.delete(`${modulo || 'all'}`);
    return this.list(modulo);
  }

  /**
   * Mappa i dati dell'API al formato AlertDto usato nel frontend
   */
  private mapApiToAlertDto(apiData: AlertsDTO[]): IAlert[] {
    return apiData.map((item) => ({
      id: item.id,
      badgeText: item.titolo,
      badgeVariant: this.getBadgeVariantByType(item.tipoAvviso),
      title: item.intro || '',
      description: item.intro || item.descrizione || '',
      date: item.createdTs || new Date(Date.now() - 12 * 24 * 60 * 60 * 1000).toISOString(), // Usa direttamente il formato dall'API
      visualizzato: item.visualizzato || false,
    }));
  }

  /**
   * Determina la variante del badge in base al tipo di avviso
   */
  private getBadgeVariantByType(tipoAvviso: string): TBadge['variant'] {
    const variant = tipoAvviso.toUpperCase();

    switch (variant) {
      case 'EXPIRY':
        return 'expiry';
      case 'ALERT':
        return 'alert';
      default:
        return 'primary';
    }
  }
}
