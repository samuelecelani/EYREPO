import {
  Component,
  inject,
  Input,
  OnInit,
  Signal,
  computed,
  signal,
  OnDestroy,
} from '@angular/core';
import { SharedModule } from '../../../module/shared/shared.module';
import { PaginationComponent } from '../../../components/pagination/pagination.component';
import { IListActivities } from '../../../models/interfaces/list-activities';
import { AzioniComponent } from 'src/app/shared/components/azioni/azioni.component';
import { IVerticalEllipsisActions } from '../../../models/interfaces/vertical-ellipsis-actions';
import { ButtonComponent } from '../../../components/button/button.component';
import { GestionaleService } from '../../../services/gestionale.service';
import { GestionaleStateService } from '../../../services/gestionale-state.service';
import { AccountService } from '../../../services/account.service';
import { UtenteRuoloPaDTO } from '../../../models/classes/utente-ruolo-pa-dto';
import { Router } from '@angular/router';
import { takeUntil, Subject } from 'rxjs';
import { number } from 'echarts';

@Component({
  selector: 'piao-elenco-attivita',
  imports: [SharedModule, PaginationComponent, AzioniComponent, ButtonComponent],
  templateUrl: './elenco-attivita.component.html',
  styleUrls: ['./elenco-attivita.component.scss'],
})
export class ElencoAttivitaComponent implements OnInit, OnDestroy {
  @Input() itemsPerPage: number = 3;
  @Input() maxItems?: number;
  @Input() pagination: boolean = true;
  currentPage = signal(1);

  private gestionaleService = inject(GestionaleService);
  private gestionaleStateService = inject(GestionaleStateService);
  private accountService = inject(AccountService);
  private router = inject(Router);
  private destroy$ = new Subject<void>();

  // Signals scrivibili per le attività
  private activitiesData = signal<IListActivities[] | null>(null);
  private activitiesStatus = signal<'idle' | 'loading' | 'success' | 'error'>('idle');
  private activitiesError = signal<any>(null);

  activityQueries = computed(() => ({
    data: this.activitiesData(),
    status: this.activitiesStatus(),
    error: this.activitiesError(),
    refetch: () => {
      console.log('Refetching activities...');
    },
    invalidate: () => {
      console.log('Invalidating activities cache...');
    },
  }));
  // Signal computato per il numero totale di items
  totalItems = computed(() => {
    const allActivities = this.activitiesData();
    return allActivities ? allActivities.length : 0;
  });

  listActivities: IListActivities[] = [];
  private usersMap = new Map<number, UtenteRuoloPaDTO>();
  private currentPaRiferimento: any;

  baseActions: IVerticalEllipsisActions[] = [
    {
      label: 'GESTIONALE.ELENCO_ATTIVITA.TABLE.DETTAGLIO_TH',
      // Callback gestito in getActionsFor
    },
    {
      label: 'GESTIONALE.ELENCO_ATTIVITA.TABLE.PROFILO_TH',
      callback: (id) => this.handleDeleteProfile(id),
    },
  ];
  ngOnInit(): void {
    // Ottieni il codicePa dal servizio account
    this.accountService
      .getAccount()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (user) => {
          const paAttiva = user?.paRiferimento?.find((x) => x.attiva);
          const codicePa = paAttiva?.codePA;

          if (!codicePa) {
            console.error('codicePa is required');
            this.activitiesStatus.set('error');
            this.activitiesError.set(new Error('codicePa is required'));
            return;
          }

          // Memorizza il paRiferimento per passarlo al dettaglio
          this.currentPaRiferimento = paAttiva;

          this.activitiesStatus.set('loading');
          this.fetchActivities(codicePa);
        },
        error: (err) => {
          console.error('Error fetching account', err);
          this.activitiesStatus.set('error');
          this.activitiesError.set(err);
        },
      });
  }

  private fetchActivities(codicePa: string): void {
    this.gestionaleService
      .getUserByCodicePa(codicePa)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          console.log('Fetched users from API:', data);
          // Memorizza i dati completi di UtenteRuoloPaDTO
          this.usersMap.clear();
          data.forEach((utente) => {
            if (utente.id) {
              this.usersMap.set(utente.id, utente);
              console.log('Added user to map:', utente.id, utente);
            }
          });

          // Crea la lista di IListActivities per la visualizzazione
          this.listActivities = data.map((utente) => ({
            id: utente.id || 0,
            codiceAttivita: 'A001', // Da capire  con BIP
            profiloUtente:
              (utente.ruoli && utente.ruoli.length > 0 ? utente.ruoli[0].nomeRuolo : '') || '',
            nome: utente.nome || '',
            cognome: utente.cognome || '',
            codiceFiscale: utente.codiceFiscale || '',
          }));

          this.activitiesData.set(this.listActivities);
          this.activitiesStatus.set('success');
          console.log('Activities loaded. Map size:', this.usersMap.size);
        },
        error: (err) => {
          console.error('Error fetching activities', err);
          this.activitiesStatus.set('error');
          this.activitiesError.set(err);
        },
      });
  }

  private initializeEmptyActivities(): void {
    this.activitiesData.set([]);
    this.activitiesStatus.set('error');
    this.activitiesError.set(new Error('codicePa is required'));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
  onPageChange(page: number): void {
    this.currentPage.set(page);
  }
  paginatedAlerts = computed(() => {
    const allAlerts = this.activitiesData();
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
    // Pulisci lo stato per il nuovo utente
    this.gestionaleStateService.setSelectedUser(null);
    // Naviga al form di creazione nuovo profilo
    this.router.navigate(['/pages/gestionale/dettaglio-attivita/new']);
  }

  handleDeleteProfile(userId: number) {
    console.log('Handle delete profile action for user ID:', userId);

    this.activitiesStatus.set('loading');

    this.gestionaleService
      .deleteUtentePa(userId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          console.log('User profile deleted successfully:', userId);
          // Ricarica la lista delle attività
          if (this.currentPaRiferimento) {
            this.fetchActivities(this.currentPaRiferimento.codePA);
          }
        },
        error: (err) => {
          console.error('Error deleting user profile:', err);
          this.activitiesStatus.set('error');
          this.activitiesError.set(err);
        },
      });
  }

  navigateToDetailWithUser(row: IListActivities): void {
    // Trova l'utente completo dalla mappa
    const fullUser = this.usersMap.get(Number(row.id));
    console.log('Navigating with user:', fullUser, 'ID:', row.id, 'Map size:', this.usersMap.size);
    if (fullUser && this.currentPaRiferimento) {
      // Passa l'utente e paRiferimento al servizio di stato
      this.gestionaleStateService.setSelectedUser(fullUser, this.currentPaRiferimento);
      console.log(
        'User set in state service:',
        fullUser,
        'paRiferimento:',
        this.currentPaRiferimento
      );
      // Naviga al dettaglio
      this.router.navigate(['/pages/gestionale/dettaglio-attivita', row.id]);
    } else {
      console.error('User not found:', row.id, 'Available IDs:', Array.from(this.usersMap.keys()));
    }
  }

  getActionsFor(row: IListActivities): IVerticalEllipsisActions[] {
    const id = row.id;

    return this.baseActions.map((a) => ({
      ...a,
      path: a.path ? `${a.path}/${id}` : undefined,
      callback:
        a.label === 'GESTIONALE.ELENCO_ATTIVITA.TABLE.DETTAGLIO_TH'
          ? () => {
              console.log('Callback for detail action triggered');
              this.navigateToDetailWithUser(row);
            }
          : a.callback
            ? () => a.callback!(id)
            : undefined,
    }));
  }
}
