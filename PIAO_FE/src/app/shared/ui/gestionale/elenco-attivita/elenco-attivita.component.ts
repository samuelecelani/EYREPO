import { Component, inject, Input, OnInit, Signal, computed, signal } from '@angular/core';
import { SharedModule } from '../../../module/shared/shared.module';
import { PaginationComponent } from '../../../components/pagination/pagination.component';
import { IListActivities } from '../../../models/interfaces/list-activities';
import { AzioniComponent } from 'src/app/shared/components/azioni/azioni.component';
import { IVerticalEllipsisActions } from '../../../models/interfaces/vertical-ellipsis-actions';
import { ButtonComponent } from '../../../components/button/button.component';
import { number } from 'echarts';

@Component({
  selector: 'piao-elenco-attivita',
  imports: [SharedModule, PaginationComponent, AzioniComponent, ButtonComponent],
  templateUrl: './elenco-attivita.component.html',
  styleUrls: ['./elenco-attivita.component.scss'],
})
export class ElencoAttivitaComponent implements OnInit {
  @Input() itemsPerPage: number = 3;
  @Input() maxItems?: number;
  @Input() pagination: boolean = true;
  currentPage = signal(1);

  activityQueries!: {
    data: Signal<IListActivities[] | null>;
    status: Signal<'idle' | 'loading' | 'success' | 'error'>;
    error: Signal<any>;
    refetch: () => void;
    invalidate: () => void;
  };
  // Signal computato per il numero totale di items
  totalItems = computed(() => {
    const allActivities = this.activityQueries.data();
    return allActivities ? allActivities.length : 0;
  });

  listActivities: IListActivities[] = [];
  baseActions: IVerticalEllipsisActions[] = [
    {
      label: 'GESTIONALE.ELENCO_ATTIVITA.TABLE.DETTAGLIO_TH',
      path: '/pages/gestionale/dettaglio-attivita',
    },
    {
      label: 'GESTIONALE.ELENCO_ATTIVITA.TABLE.PROFILO_TH',
      callback: (id) => this.handleDeleteProfile(id),
    },
  ];
  ngOnInit(): void {
    this.listActivities = [
      {
        id: 1,
        codiceAttivita: 'ATT001',
        profiloUtente: 'Admin',
        nome: 'Mario',
        cognome: 'Rossi',
        codiceFiscale: 'MRARSS80A01H501U',
      },
      {
        id: 1,
        codiceAttivita: 'ATT001',
        profiloUtente: 'Admin',
        nome: 'Mario',
        cognome: 'Rossi',
        codiceFiscale: 'MRARSS80A01H501U',
      },
      {
        id: 1,
        codiceAttivita: 'ATT001',
        profiloUtente: 'Admin',
        nome: 'Mario',
        cognome: 'Rossi',
        codiceFiscale: 'MRARSS80A01H501U',
      },
      {
        id: 1,
        codiceAttivita: 'ATT001',
        profiloUtente: 'Admin',
        nome: 'Mario',
        cognome: 'Rossi',
        codiceFiscale: 'MRARSS80A01H501U',
      },
      {
        id: 1,
        codiceAttivita: 'ATT001',
        profiloUtente: 'Admin',
        nome: 'Mario',
        cognome: 'Rossi',
        codiceFiscale: 'MRARSS80A01H501U',
      },
      {
        id: 1,
        codiceAttivita: 'ATT001',
        profiloUtente: 'Admin',
        nome: 'Mario',
        cognome: 'Rossi',
        codiceFiscale: 'MRARSS80A01H501U',
      },
      {
        id: 1,
        codiceAttivita: 'ATT001',
        profiloUtente: 'Admin',
        nome: 'Mario',
        cognome: 'Rossi',
        codiceFiscale: 'MRARSS80A01H501U',
      },
      {
        id: 1,
        codiceAttivita: 'ATT001',
        profiloUtente: 'Admin',
        nome: 'Mario',
        cognome: 'Rossi',
        codiceFiscale: 'MRARSS80A01H501U',
      },
      {
        id: 1,
        codiceAttivita: 'ATT001',
        profiloUtente: 'Admin',
        nome: 'Mario',
        cognome: 'Rossi',
        codiceFiscale: 'MRARSS80A01H501U',
      },
      {
        id: 1,
        codiceAttivita: 'ATT001',
        profiloUtente: 'Admin',
        nome: 'Mario',
        cognome: 'Rossi',
        codiceFiscale: 'MRARSS80A01H501U',
      },
      {
        id: 1,
        codiceAttivita: 'ATT001',
        profiloUtente: 'Admin',
        nome: 'Mario',
        cognome: 'Rossi',
        codiceFiscale: 'MRARSS80A01H501U',
      },
      {
        id: 1,
        codiceAttivita: 'ATT001',
        profiloUtente: 'Admin',
        nome: 'Mario',
        cognome: 'Rossi',
        codiceFiscale: 'MRARSS80A01H501U',
      },
      {
        id: 1,
        codiceAttivita: 'ATT001',
        profiloUtente: 'Admin',
        nome: 'Mario',
        cognome: 'Rossi',
        codiceFiscale: 'MRARSS80A01H501U',
      },
      {
        id: 1,
        codiceAttivita: 'ATT001',
        profiloUtente: 'Admin',
        nome: 'Mario',
        cognome: 'Rossi',
        codiceFiscale: 'MRARSS80A01H501U',
      },
    ];

    this.activityQueries = {
      data: signal(this.listActivities),
      status: signal('success'),
      error: signal(null),
      refetch: () => {
        console.log('Refetching activities...');
      },
      invalidate: () => {
        console.log('Invalidating activities cache...');
      },
    };
  }
  onPageChange(page: number): void {
    this.currentPage.set(page);
  }
  paginatedAlerts = computed(() => {
    const allAlerts = this.activityQueries.data();
    if (!allAlerts) return [];

    // Se la paginazione è disabilitata
    if (!this.pagination) {
      // Se maxItems è specificato, mostra solo i primi N elementi
      if (this.maxItems && this.maxItems > 0) {
        return allAlerts.slice(0, this.maxItems);
      }
      // Altrimenti mostra tutti i dati
      return allAlerts;
    }

    // Con paginazione attiva
    const startIndex = (this.currentPage() - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return allAlerts.slice(startIndex, endIndex);
  });

  handleNewProfile() {
    console.log('Handle new profile action');
  }
  handleDeleteProfile(userId: number) {
    console.log('Handle delete profile action');
  }

  getActionsFor(row: IListActivities): IVerticalEllipsisActions[] {
    const id = row.id;

    return this.baseActions.map((a) => ({
      ...a,
      path: a.path ? `${a.path}/${id}` : undefined,
      callback: a.callback ? () => a.callback!(id) : undefined,
    }));
  }
}
