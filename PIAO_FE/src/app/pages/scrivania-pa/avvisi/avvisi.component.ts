import { Component, inject, Input, OnInit, Signal, computed, signal } from '@angular/core';
import { IAlert, AlertsComponent } from '../../../shared/ui/alerts/alerts.component';
import { SharedModule } from '../../../shared/module/shared/shared.module';
import { BaseComponent } from '../../../shared/components/base/base.component';
import { Router } from '@angular/router';
import { AlertsService } from '../../../shared/services/alerts.service';
import { QueryClientService } from '../../../shared/services/query-client.service';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';

@Component({
    selector: 'piao-avvisi',
    imports: [SharedModule, AlertsComponent, PaginationComponent],
    templateUrl: './avvisi.component.html',
    styleUrl: './avvisi.component.scss'
})
export class AvvisiComponent extends BaseComponent implements OnInit {
  router: Router = inject(Router);
  private alertsService = inject(AlertsService);
  private query = inject(QueryClientService);

  // Query parameter da URL: /pages/avvisi?modulo=PIAO
  @Input() modulo?: string;

  // Configurazione paginazione (può essere passata come @Input se necessario)
  @Input() itemsPerPage: number = 3;
  @Input() pagination: boolean = true; // Abilita/disabilita la paginazione
  @Input() maxItems?: number; // Numero massimo di elementi da mostrare (senza paginazione)
  currentPage = signal(1);

  // Query per gli avvisi (viene inizializzata in ngOnInit)
  alertsQuery!: {
    data: Signal<IAlert[] | null>;
    status: Signal<'idle' | 'loading' | 'success' | 'error'>;
    error: Signal<any>;
    refetch: () => void;
    invalidate: () => void;
  };

  // Signal computato per i dati paginati e ordinati
  paginatedAlerts = computed(() => {
    const allAlerts = this.alertsQuery.data();
    if (!allAlerts) return [];

    // TODO: Quando il backend fornirà il campo 'createdTs', usare quello per l'ordinamento
    // Per ora usiamo il campo 'date' (string) per ordinare
    // Ordina gli avvisi dal più recente al meno recente
    const sortedAlerts = [...allAlerts].sort((a, b) => {
      // Converte le stringhe date in Date objects per il confronto
      const dateA = new Date(a.date);
      const dateB = new Date(b.date);
      return dateB.getTime() - dateA.getTime(); // Ordine decrescente (più recente prima)
    });

    // Se la paginazione è disabilitata
    if (!this.pagination) {
      // Se maxItems è specificato, mostra solo i primi N elementi
      if (this.maxItems && this.maxItems > 0) {
        return sortedAlerts.slice(0, this.maxItems);
      }
      // Altrimenti mostra tutti i dati
      return sortedAlerts;
    }

    // Con paginazione attiva
    const startIndex = (this.currentPage() - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return sortedAlerts.slice(startIndex, endIndex);
  });

  // Signal computato per il numero totale di items
  totalItems = computed(() => {
    const allAlerts = this.alertsQuery.data();
    return allAlerts ? allAlerts.length : 0;
  });

  ngOnInit(): void {
    // Inizializza la query con il modulo corretto
    // Il modulo è disponibile qui perché @Input() viene popolato prima di ngOnInit
    const cacheKey = `alerts:list:${this.modulo || 'all'}`;

    this.alertsQuery = this.query.useQuery<IAlert[]>(
      cacheKey,
      () => this.alertsService.list(this.modulo),
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
