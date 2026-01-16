import { Component, inject, Input, OnInit, Signal, computed, signal } from '@angular/core';
import { INews, NewsComponent } from '../../../shared/ui/news/news.component';
import { SharedModule } from '../../../shared/module/shared/shared.module';
import { BaseComponent } from '../../../shared/components/base/base.component';
import { Router } from '@angular/router';
import { NewsService } from '../../../shared/services/news.service';
import { QueryClientService } from '../../../shared/services/query-client.service';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';

@Component({
    selector: 'piao-novita',
    imports: [SharedModule, NewsComponent, PaginationComponent],
    templateUrl: './novita.component.html',
    styleUrl: './novita.component.scss'
})
export class NovitaComponent extends BaseComponent implements OnInit {
  router: Router = inject(Router);
  private newsService = inject(NewsService);
  private query = inject(QueryClientService);

  // Query parameter da URL: /pages/novita?modulo=1
  @Input() modulo?: string;

  // Configurazione paginazione (può essere passata come @Input se necessario)
  @Input() itemsPerPage: number = 3;
  @Input() pagination: boolean = true; // Abilita/disabilita la paginazione
  @Input() maxItems?: number; // Numero massimo di elementi da mostrare (senza paginazione)
  currentPage = signal(1);

  // Query per le news (viene inizializzata in ngOnInit)
  newsQuery!: {
    data: Signal<INews[] | null>;
    status: Signal<'idle' | 'loading' | 'success' | 'error'>;
    error: Signal<any>;
    refetch: () => void;
    invalidate: () => void;
  };

  // Signal computato per i dati paginati e ordinati
  paginatedNews = computed(() => {
    const allNews = this.newsQuery.data();
    if (!allNews) return [];

    // TODO: Quando il backend fornirà il campo 'dataCreazione', usare quello per l'ordinamento
    // Per ora usiamo il campo 'date' (string) per ordinare
    // Ordina le news dalla più recente alla meno recente
    const sortedNews = [...allNews].sort((a, b) => {
      // Converte le stringhe date in Date objects per il confronto
      const dateA = new Date(a.date);
      const dateB = new Date(b.date);
      return dateB.getTime() - dateA.getTime(); // Ordine decrescente (più recente prima)
    });

    // Se la paginazione è disabilitata
    if (!this.pagination) {
      // Se maxItems è specificato, mostra solo i primi N elementi
      if (this.maxItems && this.maxItems > 0) {
        return sortedNews.slice(0, this.maxItems);
      }
      // Altrimenti mostra tutti i dati
      return sortedNews;
    }

    // Con paginazione attiva
    const startIndex = (this.currentPage() - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return sortedNews.slice(startIndex, endIndex);
  });

  // Signal computato per il numero totale di items
  totalItems = computed(() => {
    const allNews = this.newsQuery.data();
    return allNews ? allNews.length : 0;
  });

  ngOnInit(): void {
    // Inizializza la query con il modulo corretto
    // Il modulo è disponibile qui perché @Input() viene popolato prima di ngOnInit
    const cacheKey = `news:list:${this.modulo || 'all'}`;

    this.newsQuery = this.query.useQuery<INews[]>(
      cacheKey,
      () => this.newsService.list(this.modulo),
      {
        staleTimeMs: 60000,
        refetchOnMount: true,
      }
    );
  }

  /**
   * Handler per il cambio pagina
   */
  onPageChange(page: number): void {
    this.currentPage.set(page);
  }
}
