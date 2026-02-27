import { inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map, shareReplay } from 'rxjs/operators';
import { BaseApiService } from './base-api.service';
import { GenericResponse } from '../models/interfaces/generic-response';
import { INewsDTO } from '../models/classes/news-dto';
import { INews } from '../ui/news/news.component';
import { TBadge } from '../components/badge/badge.component';

/**
 * News Service con pattern RTK Query-like
 *
 * ✅ Caching automatico
 * ✅ Invalidazione cache
 * ✅ Refetch on demand
 *
 * Analogia React: simile a RTK Query endpoints
 */
@Injectable({ providedIn: 'root' })
export class NewsService {
  private api = inject(BaseApiService);

  // Cache delle query (chiave = parametri della query)
  private cache = new Map<string, Observable<INews[]>>();

  /**
   * GET /novita - Recupera la lista delle news con caching automatico
   *
   * @param modulo - Parametro modulo per filtrare le news
   * @returns Observable con array di NewsDto
   */
  list(modulo?: string): Observable<INews[]> {
    const cacheKey = `${modulo || 'all'}`;

    // Se è in cache, ritorna dalla cache
    if (this.cache.has(cacheKey)) {
      return this.cache.get(cacheKey)!;
    }

    // Chiamata API reale
    const params: Record<string, string> = {};
    if (modulo) params['modulo'] = modulo;

    const query$ = this.api.get<GenericResponse<INewsDTO>>('/novita', { params }).pipe(
      map((response) => {
        if (!response.status?.success) {
          console.error('API Error:', response.error);
          throw new Error(response.error?.messageError || 'Unknown error');
        }
        return this.mapApiToNewsDto(response.data);
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
  refetch(modulo?: string): Observable<INews[]> {
    this.cache.delete(`${modulo || 'all'}`);
    return this.list(modulo);
  }

  /**
   * Mappa i dati dell'API al formato NewsDto usato nel frontend
   */
  private mapApiToNewsDto(apiData: INewsDTO[]): INews[] {
    return apiData.map((item) => ({
      id: item.id,
      icon: this.getIconByType(item.tipoNovita),
      badgeText: item.titolo,
      badgeVariant: this.getBadgeVariantByType(item.tipoNovita),
      title: item.intro,
      titleLink: `/pages/novita/${item.id}`,
      description: item.intro || item.descrizione,
      date: this.formatDate(new Date()), // Puoi aggiungere un campo data nell'API se necessario
      readMoreLink: `/pages/novita/${item.id}`,
    }));
  }

  /**
   * Determina l'icona in base al tipo di novità
   */
  private getIconByType(tipoNovita: string): string {
    const variant = tipoNovita.toUpperCase();

    switch (variant) {
      case 'INFO':
        return 'Horn';
      case 'AGGIORNAMENTO':
        return 'Docs';
      case 'NEWS':
        return 'SearchDocs';
      default:
        return 'Horn';
    }
  }

  /**
   * Determina la variante del badge in base al tipo di novità
   */
  private getBadgeVariantByType(tipoNovita: string): TBadge['variant'] {
    const variant = tipoNovita.toUpperCase();

    switch (variant) {
      case 'INFO':
        return 'success-light';
      case 'AGGIORNAMENTO':
        return 'primary';
      case 'NEWS':
        return 'secondary';
      default:
        return 'primary';
    }
  }

  /**
   * Formatta la data in formato italiano
   */
  private formatDate(date: Date): string {
    const months = [
      'Gennaio',
      'Febbraio',
      'Marzo',
      'Aprile',
      'Maggio',
      'Giugno',
      'Luglio',
      'Agosto',
      'Settembre',
      'Ottobre',
      'Novembre',
      'Dicembre',
    ];

    const day = date.getDate().toString().padStart(2, '0');
    const month = months[date.getMonth()];
    const year = date.getFullYear();

    return `${day} ${month} ${year}`;
  }
}
