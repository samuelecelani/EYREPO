import { inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map, shareReplay } from 'rxjs/operators';
import { BaseApiService } from './base-api.service';
import {
  INewsDocumentDTO,
  INewsDetailDTO,
  INewsSearchItemDTO,
  INewsSearchResponseDTO,
  INewsTipologieResponseDTO,
} from '../models/classes/news-dto';
import { INews, INewsDocument } from '../ui/news/news.component';
import { TBadge } from '../components/badge/badge.component';

export interface INewsTipologia {
  id: string;
  label: string;
  count: number;
}

export interface INewsSearchResult {
  page: number;
  limit: number;
  total: number;
  items: INews[];
}

interface IBffEnvelope<T> {
  data?: T | null;
  status?: {
    success?: boolean;
  };
  error?: unknown;
  metadato?: unknown;
}

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

  private readonly DEFAULT_LOCALE = 'it';
  private readonly DEFAULT_SORT = 'date_desc';
  private readonly MAX_LIMIT = 50;

  // Cache delle query (chiave = parametri della query)
  private cache = new Map<string, Observable<INews[]>>();
  private detailCache = new Map<string, Observable<INews | null>>();

  listTipologie(): Observable<INewsTipologia[]> {
    return this.api.get<INewsTipologieResponseDTO | IBffEnvelope<INewsTipologieResponseDTO>>('/novita/tipologie').pipe(
      map((response) => {
        const payload = this.unwrapData(response);
        return payload?.items || [];
      }),
      catchError((error) => {
        console.warn('Tipologie API error', error);
        return of([]);
      })
    );
  }

  search(params?: {
    keyword?: string;
    tipologia?: string;
    inEvidenza?: boolean;
    startDate?: string;
    sort?: 'date_desc' | 'date_asc' | 'title_asc' | 'title_desc' | string;
    page?: number;
    limit?: number;
    locale?: string;
  }): Observable<INewsSearchResult> {
    const request = this.buildSearchRequest(params);
    const queryParams = this.toSearchQueryParams(request);

    return this.api
      .post<INewsSearchResponseDTO | IBffEnvelope<INewsSearchResponseDTO>, Record<string, never>>(
        '/novita/search',
        {},
        { params: queryParams }
      )
      .pipe(
        map((response) => {
          const payload = this.unwrapData(response);
          return this.mapSearchResponse(payload || ({} as INewsSearchResponseDTO), request.page, request.limit);
        }),
        catchError((error) => {
          console.warn('Search API error', error);
          return of({ page: request.page, limit: request.limit, total: 0, items: [] });
        })
      );
  }

  /**
   * Compatibilità: carica la lista news con default ricerca
   *
   * @param modulo - Parametro legacy (non usato)
   */
  list(modulo?: string): Observable<INews[]> {
    const cacheKey = `${modulo || 'all'}`;

    // Se è in cache, ritorna dalla cache
    if (this.cache.has(cacheKey)) {
      return this.cache.get(cacheKey)!;
    }

    const query$ = this.search({ page: 1, limit: this.MAX_LIMIT }).pipe(
      map((response) => response.items),
      catchError((error) => {
        console.warn('API error', error);
        return of([]);
      }),
      shareReplay(1)
    );

    this.cache.set(cacheKey, query$);
    return query$;
  }

  getById(
    id: string | number,
    modulo?: string,
    _options?: { locale?: string; debug?: boolean }
  ): Observable<INews | null> {
    const normalizedId = String(id);
    const cacheKey = `${modulo || 'all'}:${normalizedId}`;

    if (this.detailCache.has(cacheKey)) {
      return this.detailCache.get(cacheKey)!;
    }

    const params = {
      id: normalizedId,
    };

    const detail$ = this.api
      .get<INewsDetailDTO | IBffEnvelope<INewsDetailDTO>>('/novita/detail', { params })
      .pipe(
        map((response) => this.mapDetailItem(this.unwrapData(response))),
        catchError(() => {
          return this.list(modulo).pipe(
            map((items) => items.find((item) => String(item.id) === normalizedId) ?? null)
          );
        }),
        shareReplay(1)
      );

    this.detailCache.set(cacheKey, detail$);
    return detail$;
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

      const detailKeysToDelete: string[] = [];
      this.detailCache.forEach((_, key) => {
        if (key.startsWith(`${modulo}:`)) detailKeysToDelete.push(key);
      });
      detailKeysToDelete.forEach((key) => this.detailCache.delete(key));
    } else {
      this.cache.clear();
      this.detailCache.clear();
    }
  }

  /**
   * Refetch: forza un nuovo fetch ignorando la cache
   */
  refetch(modulo?: string): Observable<INews[]> {
    this.cache.delete(`${modulo || 'all'}`);
    return this.list(modulo);
  }

  private mapSearchResponse(
    response: INewsSearchResponseDTO,
    fallbackPage: number,
    fallbackLimit: number
  ): INewsSearchResult {
    return {
      page: response?.page ?? fallbackPage,
      limit: response?.limit ?? fallbackLimit,
      total: response?.total ?? 0,
      items: (response?.items || []).map((item) => this.mapSearchItem(item)),
    };
  }

  private mapSearchItem(item: INewsSearchItemDTO): INews {
    const type = this.getNewsType(item.tipologia);
    const dateIso = this.resolveDateIso(item.data);

    return {
      id: item.id,
      type,
      icon: this.getIconByType(type),
      badgeText: item.tipologia || this.getBadgeLabelByType(type),
      badgeVariant: this.getBadgeVariantByType(type),
      title: item.titolo || '',
      titleLink: `/novita/dettaglio/${item.id}`,
      description: item.abstract || item.abstractText || '',
      date: this.formatDate(new Date(dateIso)),
      dateIso,
      readMoreLink: `/novita/dettaglio/${item.id}`,
      content: '',
      imageBase64: null,
      keywords: this.getDefaultKeywords(type),
      documents: [],
    };
  }

  private mapDetailItem(item: INewsDetailDTO | null | undefined): INews | null {
    if (!item || !item.id) {
      return null;
    }

    const type = this.getNewsType(item.tipologia);
    const dateIso = this.resolveDateIso(item.data);

    return {
      id: item.id,
      type,
      icon: this.getIconByType(type),
      badgeText: item.tipologia || this.getBadgeLabelByType(type),
      badgeVariant: this.getBadgeVariantByType(type),
      title: item.titolo || '',
      titleLink: `/novita/dettaglio/${item.id}`,
      description: item.abstract || item.abstractText || '',
      date: this.formatDate(new Date(dateIso)),
      dateIso,
      readMoreLink: `/novita/dettaglio/${item.id}`,
      content: item.testoHtml || '',
      imageBase64: this.resolveDetailImageBase64(item),
      keywords: this.getDefaultKeywords(type),
      documents: this.mapDetailDocuments(item),
    };
  }

  /**
   * Determina l'icona in base al tipo di novità
   */
  private getIconByType(type: INews['type']): string {
    switch (type) {
      case 'CIRCOLARE':
        return 'Docs';
      case 'NOVITA':
        return 'Horn';
      case 'ALTRO':
        return 'SearchDocs';
      default:
        return 'Horn';
    }
  }

  private getBadgeLabelByType(type: INews['type']): string {
    switch (type) {
      case 'CIRCOLARE':
        return 'Circolare del Dipartimento di Funzione Pubblica';
      case 'NOVITA':
        return 'Novita del Dipartimento di Funzione Pubblica';
      case 'ALTRO':
        return 'Altro';
      default:
        return 'Novita';
    }
  }

  /**
   * Determina la variante del badge in base al tipo di novità
   */
  private getBadgeVariantByType(type: INews['type']): TBadge['variant'] {
    switch (type) {
      case 'NOVITA':
        return 'success-light';
      case 'CIRCOLARE':
        return 'primary';
      case 'ALTRO':
        return 'secondary';
      default:
        return 'primary';
    }
  }

  private getNewsType(tipoNovita: string): INews['type'] {
    const normalized = (tipoNovita || '').toUpperCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');

    if (normalized.includes('CIRCOLARE')) return 'CIRCOLARE';
    if (normalized.includes('NOVITA') || normalized.includes('NEWS') || normalized.includes('NOTIZIE')) return 'NOVITA';
    return 'ALTRO';
  }

  private resolveDateIso(rawDate?: string): string {
    if (!rawDate) {
      return new Date().toISOString();
    }

    const parsed = Date.parse(rawDate);
    return Number.isNaN(parsed) ? new Date().toISOString() : new Date(parsed).toISOString();
  }

  private buildSearchRequest(params?: {
    keyword?: string;
    tipologia?: string;
    inEvidenza?: boolean;
    startDate?: string;
    sort?: string;
    page?: number;
    limit?: number;
    locale?: string;
  }): {
    keyword?: string;
    tipologia?: string;
    inEvidenza?: boolean;
    startDate?: string;
    sort: string;
    page: number;
    limit: number;
    locale: string;
  } {
    const page = Math.max(1, Number(params?.page || 1));
    const requestedLimit = Math.max(1, Number(params?.limit || 10));
    const limit = Math.min(this.MAX_LIMIT, requestedLimit);

    return {
      keyword: params?.keyword?.trim() || undefined,
      tipologia: params?.tipologia?.trim() || undefined,
      inEvidenza: params?.inEvidenza,
      startDate: params?.startDate?.trim() || undefined,
      sort: params?.sort || this.DEFAULT_SORT,
      page,
      limit,
      locale: params?.locale || this.DEFAULT_LOCALE,
    };
  }

  private toSearchQueryParams(request: {
    keyword?: string;
    tipologia?: string;
    inEvidenza?: boolean;
    startDate?: string;
    sort: string;
    page: number;
    limit: number;
    locale: string;
  }): Record<string, string | number | boolean> {
    const queryParams: Record<string, string | number | boolean> = {
      sort: request.sort,
      page: request.page,
      limit: request.limit,
      locale: request.locale,
    };

    if (request.keyword !== undefined) {
      queryParams['keyword'] = request.keyword;
    }
    if (request.tipologia !== undefined) {
      queryParams['tipologia'] = request.tipologia;
    }
    if (request.inEvidenza !== undefined) {
      queryParams['inEvidenza'] = request.inEvidenza;
    }
    if (request.startDate !== undefined) {
      queryParams['startDate'] = request.startDate;
    }

    return queryParams;
  }

  private unwrapData<T>(response: T | IBffEnvelope<T> | null | undefined): T | null {
    if (!response) {
      return null;
    }

    if (typeof response === 'object' && 'data' in (response as Record<string, unknown>)) {
      return ((response as IBffEnvelope<T>).data as T | null | undefined) ?? null;
    }

    return response as T;
  }

  private mapDetailDocuments(item: INewsDetailDTO): INewsDocument[] {
    const source = item.documenti || item.allegati || [];

    return source
      .filter((doc): doc is INewsDocumentDTO => !!doc && !!doc.nome)
      .map((doc, index) => ({
        id: doc.id || `${item.id}-doc-${index + 1}`,
        nome: doc.nome || '',
        tipo: doc.tipo || this.inferDocumentType(doc.nome),
        sizeMb: this.normalizeDocumentSize(doc.sizeMb),
        downloadUrl: doc.downloadUrl || '#',
      }));
  }

  private normalizeDocumentSize(sizeMb?: number | string): number {
    if (typeof sizeMb === 'number' && Number.isFinite(sizeMb) && sizeMb >= 0) {
      return sizeMb;
    }

    const parsed = Number(sizeMb);
    if (Number.isFinite(parsed) && parsed >= 0) {
      return parsed;
    }

    return 0;
  }

  private inferDocumentType(fileName?: string): string {
    if (!fileName || !fileName.includes('.')) {
      return 'PDF';
    }

    const ext = fileName.split('.').pop();
    return (ext || 'PDF').toUpperCase();
  }

  private resolveDetailImageBase64(item: INewsDetailDTO): string | null {
    const rawValue = item.imageBase64 || item.immagineBase64 || item.backgroundImageBase64;
    if (!rawValue) {
      return null;
    }

    const trimmed = rawValue.trim();
    return trimmed.length > 0 ? trimmed : null;
  }

  private getDefaultKeywords(type: INews['type']): string[] {
    switch (type) {
      case 'CIRCOLARE':
        return ['PIAO', 'Monitoraggio', 'Normativa'];
      case 'NOVITA':
        return ['Portale PIAO', 'Programmazione', 'Aggiornamento'];
      case 'ALTRO':
        return ['Approfondimento'];
      default:
        return ['PIAO'];
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
