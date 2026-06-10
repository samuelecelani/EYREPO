import { Component, DestroyRef, Input, OnInit, Signal, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { SharedModule } from '../../../shared/module/shared/shared.module';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { AzioniComponent } from '../../../shared/components/azioni/azioni.component';
import { IVerticalEllipsisActions } from '../../../shared/models/interfaces/vertical-ellipsis-actions';
import { AlertsService } from '../../../shared/services/alerts.service';
import { QueryClientService } from '../../../shared/services/query-client.service';
import { AlertsComponent, IAlert } from '../../../shared/ui/alerts/alerts.component';
import { AlertsDTO } from '../../../shared/models/classes/alerts-dto';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { StatusComponent } from 'src/app/shared/components/status/status.component';
import { CodStatoAvvisi } from '../../../shared/models/enums/cod-stato-avvisi.enum';
import { AssetService } from '../../../shared/services/asset.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

interface IAvvisoRow {
  idDB: number;
  id: string;
  tipologia: string;
  data: string;
  anteprima: string;
  stato: string;
  raw: AlertsDTO;
}

@Component({
  selector: 'piao-avvisi',
  imports: [
    SharedModule,
    PaginationComponent,
    ButtonComponent,
    AzioniComponent,
    StatusComponent,
    AlertsComponent,
    ModalComponent,
  ],
  templateUrl: './avvisi.component.html',
  styleUrl: './avvisi.component.scss',
})
export class AvvisiComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  @Input() modulo?: string;
  @Input() itemsPerPage: number = 5;
  @Input() pagination: boolean = true;
  @Input() maxItems?: number;

  private readonly router = inject(Router);
  private readonly alertsService = inject(AlertsService);
  private readonly query = inject(QueryClientService);
  protected readonly asset = inject(AssetService);

  currentPage = signal(1);
  alertsQuery!: {
    data: Signal<IAlert[] | null>;
    status: Signal<'idle' | 'loading' | 'success' | 'error'>;
    error: Signal<unknown>;
    refetch: () => void;
    invalidate: () => void;
  };
  avvisiPageQuery!: {
    data: Signal<AlertsDTO[] | null>;
    status: Signal<'idle' | 'loading' | 'success' | 'error'>;
    error: Signal<unknown>;
    refetch: () => void;
    invalidate: () => void;
  };
  selectedRowToDelete = signal<IAvvisoRow | null>(null);
  isDeleting = signal(false);

  get isPageView(): boolean {
    return this.router.url.includes('/avvisi');
  }

  readonly pageRows = computed(() => {
    const allAvvisi = this.avvisiPageQuery?.data() || [];

    return allAvvisi
      .map((item) => this.mapAvvisoRow(item))
      .sort((left, right) => {
        const leftDate = new Date(
          left.raw.dataPubblicazione || left.raw.createdTs?.toString() || 0
        );
        const rightDate = new Date(
          right.raw.dataPubblicazione || right.raw.createdTs?.toString() || 0
        );
        return rightDate.getTime() - leftDate.getTime();
      });
  });

  readonly totalItems = computed(() => this.pageRows().length);
  readonly totalAlerts = computed(() => {
    const allAlerts = this.alertsQuery?.data();
    return allAlerts ? allAlerts.length : 0;
  });

  readonly paginatedAvvisi = computed(() => {
    const allRows = this.pageRows();

    if (!this.pagination) {
      if (this.maxItems && this.maxItems > 0) {
        return allRows.slice(0, this.maxItems);
      }

      return allRows;
    }

    const startIndex = (this.currentPage() - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return allRows.slice(startIndex, endIndex);
  });

  readonly paginatedAlerts = computed(() => {
    const allAlerts = this.alertsQuery?.data();
    if (!allAlerts) {
      return [];
    }

    const sortedAlerts = [...allAlerts].sort((a, b) => {
      const dateA = new Date(a.date);
      const dateB = new Date(b.date);
      return dateB.getTime() - dateA.getTime();
    });

    if (!this.pagination) {
      if (this.maxItems && this.maxItems > 0) {
        return sortedAlerts.slice(0, this.maxItems);
      }

      return sortedAlerts;
    }

    const startIndex = (this.currentPage() - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return sortedAlerts.slice(startIndex, endIndex);
  });

  readonly rowActions: IVerticalEllipsisActions[] = [
    { label: 'Modifica' },
    { label: 'Vedi dettaglio' },
    //{ label: 'Elimina' },
  ];

  ngOnInit(): void {
    // Safety net: force defaults if inputs arrive as undefined (e.g. when component
    // is loaded directly via router without parent bindings).
    if (this.itemsPerPage == null || this.itemsPerPage <= 0) {
      this.itemsPerPage = 5;
    }
    if (this.pagination == null) {
      this.pagination = true;
    }

    const cacheKey = `alerts:list:${this.modulo || 'all'}`;
    const avvisiPageCacheKey = `avvisi:page:list:${this.modulo || 'all'}`;

    this.alertsQuery = this.query.useQuery<IAlert[]>(
      cacheKey,
      () => this.alertsService.list(this.modulo),
      {
        staleTimeMs: 60000,
        refetchOnMount: true,
      }
    );

    this.avvisiPageQuery = this.query.useQuery<AlertsDTO[]>(
      avvisiPageCacheKey,
      () => this.alertsService.getAllAvvisi(this.modulo),
      {
        staleTimeMs: 60000,
        refetchOnMount: true,
      }
    );
  }

  onPageChange(page: number): void {
    this.currentPage.set(page);
  }

  getActionsFor(row: IAvvisoRow): IVerticalEllipsisActions[] {
    const actions =
      row.stato === CodStatoAvvisi.PUBBLICATO
        ? this.rowActions.filter((a) => a.label === 'Vedi dettaglio')
        : this.rowActions;

    return actions.map((action) => ({
      ...action,
      callback: () => this.handleRowAction(action.label, row),
    }));
  }

  handleNewAvviso(): void {
    this.router.navigate(['/avvisi/pubblica-avviso']);
  }

  closeDeleteModal(): void {
    this.selectedRowToDelete.set(null);
    this.isDeleting.set(false);
  }

  confirmDelete(): void {
    const selectedRow = this.selectedRowToDelete();
    if (!selectedRow) {
      return;
    }

    this.isDeleting.set(true);
    this.alertsService.delete(selectedRow.idDB).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.avvisiPageQuery.refetch();
        this.alertsQuery.refetch();
        this.closeDeleteModal();
      },
      error: (error) => {
        console.error('Delete avviso failed', error);
        this.isDeleting.set(false);
      },
    });
  }

  private handleRowAction(actionLabel: string, row: IAvvisoRow): void {
    switch (actionLabel) {
      case 'Modifica':
        this.router.navigate(['/avvisi/modifica-avviso', row.idDB]);
        break;
      case 'Vedi dettaglio':
        this.router.navigate(['/avvisi/dettaglio-avviso', row.idDB]);
        break;
      /*
      case 'Elimina':
        this.selectedRowToDelete.set(row);
        break;
      */
      default:
        break;
    }
  }

  private mapAvvisoRow(item: AlertsDTO): IAvvisoRow {
    const id = item.id ? `A${String(item.id).padStart(3, '0')}` : '-';

    return {
      id,
      idDB: item.id || -1,
      tipologia: item.tipologiaContenuto || item.tipoAvviso || '-',
      data: this.formatDisplayDate(item.dataPubblicazione || item.createdTs?.toString()),
      anteprima: item.messaggio || item.oggetto || '-',
      stato: item.stato || '-',
      raw: item,
    };
  }

  private formatDisplayDate(value?: string): string {
    if (!value) {
      return '-';
    }

    const parsedDate = new Date(value);
    if (Number.isNaN(parsedDate.getTime())) {
      return value;
    }

    return parsedDate.toLocaleDateString('it-IT');
  }
}
