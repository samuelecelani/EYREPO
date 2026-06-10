import {
  Component,
  Input,
  OnChanges,
  OnInit,
  Signal,
  SimpleChanges,
  computed,
  inject,
  signal,
} from '@angular/core';
import { INews, NewsComponent } from '../../../shared/ui/news/news.component';
import { SharedModule } from '../../../shared/module/shared/shared.module';
import { BaseComponent } from '../../../shared/components/base/base.component';
import { INewsSearchResult, NewsService } from '../../../shared/services/news.service';
import { QueryClientService } from '../../../shared/services/query-client.service';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';

@Component({
  selector: 'piao-novita',
  imports: [SharedModule, NewsComponent, PaginationComponent],
  templateUrl: './novita.component.html',
  styleUrl: './novita.component.scss',
})
export class NovitaComponent extends BaseComponent implements OnInit, OnChanges {
  private readonly newsService = inject(NewsService);
  private readonly query = inject(QueryClientService);
  private initialized = false;
  private readonly maxFetchLimit = 50;

  @Input() modulo?: string;
  @Input() searchTerm: string = '';
  @Input() typologyFilter: string = '';
  @Input() selectedDate: string = '';
  @Input() orderBy: string = '';
  @Input() showSearchSummary: boolean = false;

  @Input() itemsPerPage: number = 3;
  @Input() pagination: boolean = true;
  @Input() maxItems?: number;

  currentPage = signal(1);

  newsQuery!: {
    data: Signal<INewsSearchResult | null>;
    status: Signal<'idle' | 'loading' | 'success' | 'error'>;
    error: Signal<any>;
    refetch: () => void;
    invalidate: () => void;
  };

  private searchTermSignal = signal('');
  private typologyFilterSignal = signal('');
  private selectedDateSignal = signal('');
  private orderBySignal = signal('');

  hasSearchTerm = computed(() => this.searchTermSignal().trim().length > 0);

  private filteredAndSortedNews = computed(() => {
    const allNews = this.newsQuery.data()?.items || [];

    const searchTerm = this.normalize(this.searchTermSignal());
    const selectedTypology = this.normalize(this.typologyFilterSignal());
    const selectedDate = (this.selectedDateSignal() || '').trim();
    const orderBy = this.orderBySignal();

    const filtered = allNews.filter((news) => {
      const searchable = `${news.badgeText} ${news.title} ${news.description}`;
      const normalizedBadge = this.normalize(news.badgeText || '');
      const normalizedType = this.normalize(news.type || '');
      const newsDate = (news.dateIso || '').slice(0, 10);

      const matchSearch = !searchTerm || this.normalize(searchable).includes(searchTerm);
      const matchTypology =
        !selectedTypology ||
        normalizedBadge === selectedTypology ||
        normalizedType === selectedTypology;
      // startDate semantics: include news from selected date onward.
      const matchDate = !selectedDate || (!newsDate ? false : newsDate >= selectedDate);

      return matchSearch && matchTypology && matchDate;
    });

    return filtered.sort((a, b) => {
      const dateA = Date.parse(a.dateIso || '');
      const dateB = Date.parse(b.dateIso || '');
      const tsA = Number.isNaN(dateA) ? 0 : dateA;
      const tsB = Number.isNaN(dateB) ? 0 : dateB;

      return orderBy === 'oldest' ? tsA - tsB : tsB - tsA;
    });
  });

  safeCurrentPage = computed(() => {
    if (!this.pagination) return 1;

    const total = this.totalItems();
    if (total === 0) return 1;

    const pages = Math.max(1, Math.ceil(total / this.itemsPerPage));
    return Math.min(Math.max(this.currentPage(), 1), pages);
  });

  paginatedNews = computed(() => {
    const news = this.filteredAndSortedNews();

    if (!this.pagination) {
      if (this.maxItems && this.maxItems > 0) {
        return news.slice(0, this.maxItems);
      }
      return news;
    }

    const startIndex = (this.safeCurrentPage() - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return news.slice(startIndex, endIndex);
  });

  totalItems = computed(() => this.filteredAndSortedNews().length);

  visibleItems = computed(() => this.paginatedNews().length);

  visibleResultsText = computed(() => `${this.visibleItems()} di ${this.totalItems()} risultati`);

  ngOnChanges(changes: SimpleChanges): void {
    if (
      changes['searchTerm'] ||
      changes['typologyFilter'] ||
      changes['selectedDate'] ||
      changes['orderBy'] ||
      changes['itemsPerPage'] ||
      changes['pagination'] ||
      changes['maxItems'] ||
      changes['modulo']
    ) {
      this.searchTermSignal.set(this.searchTerm ?? '');
      this.typologyFilterSignal.set(this.typologyFilter ?? '');
      this.selectedDateSignal.set(this.selectedDate ?? '');
      this.orderBySignal.set(this.orderBy ?? '');
      this.currentPage.set(1);

      if (this.initialized) {
        this.refreshQuery(true);
      }
    }
  }

  ngOnInit(): void {
    this.searchTermSignal.set(this.searchTerm ?? '');
    this.typologyFilterSignal.set(this.typologyFilter ?? '');
    this.selectedDateSignal.set(this.selectedDate ?? '');
    this.orderBySignal.set(this.orderBy ?? '');

    this.initialized = true;
    this.refreshQuery(true);
  }

  onPageChange(page: number): void {
    if (!this.pagination || page === this.currentPage()) {
      return;
    }

    this.currentPage.set(page);
  }

  private refreshQuery(refetchOnMount = false): void {
    // --- Implementazione reale: chiamata BE tramite QueryClientService ---
    const limit = this.resolveFetchLimit();
    const cacheKey = this.buildCacheKey(limit);

    this.newsQuery = this.query.useQuery<INewsSearchResult>(
      cacheKey,
      () =>
        this.newsService.search({
          keyword: this.searchTermSignal().trim() || undefined,
          tipologia: this.toApiTipologia(this.typologyFilterSignal()),
          startDate: this.selectedDateSignal() || undefined,
          sort: this.toApiSort(this.orderBySignal()),
          page: 1,
          limit,
        }),
      {
        staleTimeMs: 60000,
        refetchOnMount,
      }
    );

    // --- Stub mockato (BE disabilitato): da riattivare se serve testare l'UI senza chiamate HTTP ---
    // void refetchOnMount;
    // this.newsQuery = {
    //   data: signal<INewsSearchResult | null>({
    //     items: [],
    //     page: 1,
    //     limit: 0,
    //     total: 0,
    //   } as INewsSearchResult),
    //   status: signal<'idle' | 'loading' | 'success' | 'error'>('success'),
    //   error: signal<any>(null),
    //   refetch: () => {},
    //   invalidate: () => {},
    // };
  }

  private buildCacheKey(limit: number): string {
    const keyParts = [
      'news:search',
      this.modulo || 'all',
      this.searchTermSignal().trim(),
      this.typologyFilterSignal().trim(),
      this.selectedDateSignal().trim(),
      this.toApiSort(this.orderBySignal()),
      String(limit),
    ];

    return keyParts.map((part) => encodeURIComponent(part)).join(':');
  }

  private resolveFetchLimit(): number {
    if (!this.pagination && this.maxItems && this.maxItems > 0) {
      return Math.min(this.maxFetchLimit, this.maxItems);
    }

    return this.maxFetchLimit;
  }

  private toApiSort(value: string): 'date_desc' | 'date_asc' {
    return value === 'oldest' ? 'date_asc' : 'date_desc';
  }

  private toApiTipologia(value: string): string | undefined {
    const raw = (value || '').trim();
    if (!raw) return undefined;

    const normalized = value
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim();

    if (!normalized) return undefined;
    if (normalized === 'circolare') return 'Circolare';
    if (normalized === 'novita' || normalized === 'news') return 'Novita';
    if (normalized === 'notizie') return 'Notizie';
    if (normalized === 'altro') return 'Altro';

    // Preserve backend-provided labels exactly when already selected from API options.
    return raw;
  }

  private normalize(value: string): string {
    return (value || '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .trim();
  }
}
