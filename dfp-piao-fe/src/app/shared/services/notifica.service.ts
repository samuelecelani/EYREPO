import { computed, inject, Injectable, Signal, signal, WritableSignal } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { EMPTY, lastValueFrom, map, Observable, of, Subject, Subscription, switchMap } from 'rxjs';
import { OAuthService } from 'angular-oauth2-oidc';
import { IGetNotificheRequest } from '../models/interfaces/get-notifiche-request';
import { INotification } from '../models/interfaces/notification';
import { IAllNotificationsResponse } from '../models/interfaces/all-notifications-response';
import { Path } from '../utils/path';
import { INotificationRead } from '../models/interfaces/notification-read';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { GenericResponse } from '../models/interfaces/generic-response';
import { BaseComponent } from '../components/base/base.component';
import { UserSessionService } from './user-session-service.service';
import { RoleRoutingService } from './role-routing.service';
import { request } from 'https';
import { getAuthTokenForEnvironment } from '../utils/utils';

@Injectable({
  providedIn: 'root',
})
export class NotificaService extends BaseComponent {
  constructor(private http: HttpClient) {
    super();
  }

  private readonly oAuthService = inject(OAuthService);
  private readonly userSessionService = inject(UserSessionService);
  private readonly roleRoutingService = inject(RoleRoutingService);

  serviceIsReady: boolean = false;
  notificationsToReadIndexes: number[] = [];
  maxNotificationNumber: number = 20;

  notificationsList: WritableSignal<INotification[]> = signal<INotification[]>([]);
  unreadNotifications: Signal<number> = computed(
    () => this.notificationsList().filter((not) => !not.read).length
  );
  totalNotificationsCount: WritableSignal<number> = signal<number>(0);

  // Set di ID notifiche marcate come lette localmente (per evitare che SSE sovrascriva)
  private recentlyMarkedAsRead: Set<number> = new Set();
  // Set di ID notifiche marcate come "da leggere" localmente (per evitare che SSE sovrascriva)
  private recentlyMarkedAsUnread: Set<number> = new Set();

  // Helper per ordinare le notifiche per creationDate (più recenti prima)
  private sortByCreationDate(notifications: INotification[]): INotification[] {
    return [...notifications].sort((a, b) => {
      const dateA = new Date(a.creationDate).getTime();
      const dateB = new Date(b.creationDate).getTime();
      return dateB - dateA;
    });
  }

  private eventSourceObj: EventSource | undefined;
  private abortController?: AbortController;
  private notificationsToReadSubject$:
    | Subject<{ indexes: number[]; fromDropdown?: boolean }>
    | undefined;
  private eventSourceListenerSubscription: Subscription | undefined;
  private userContextSubscription?: Subscription;

  getNotifiche(request: IGetNotificheRequest): Observable<IAllNotificationsResponse> {
    return this.getUserContext$().pipe(
      switchMap((user) => {
        request.codiceFiscale = user.cf;
        let userDFP = this.roleRoutingService.isDfpAuthority(user.user.typeAuthority);
        if (!userDFP) {
          request.codicePa = user.paRiferimento?.codePA;
        }
        request.userDfp = userDFP;

        return this.http
          .get<GenericResponse<IAllNotificationsResponse>>(Path.url('/notification/list'), {
            params: { ...request },
          })
          .pipe(map((res) => res.data));
      })
    );
  }

  /** Versione silenziosa (senza spinner) usata per il refresh triggato da SSE */
  private getNotificheSilent(request: IGetNotificheRequest): Observable<IAllNotificationsResponse> {
    return this.getUserContext$().pipe(
      switchMap((user) => {
        request.codiceFiscale = user.cf;
        let userDFP = this.roleRoutingService.isDfpAuthority(user.user.typeAuthority);
        if (!userDFP) {
          request.codicePa = user.paRiferimento?.codePA;
        }
        request.userDfp = userDFP;

        return this.http
          .get<GenericResponse<IAllNotificationsResponse>>(Path.url('/notification/list'), {
            params: { ...request },
            headers: new HttpHeaders({ 'id-spinner': 'none' }),
          })
          .pipe(map((res) => res.data));
      })
    );
  }

  patchNotifiche(request: INotificationRead): Observable<string> {
    return this.http.put(Path.url('/notification/readNotify'), request, {
      responseType: 'text',
      headers: new HttpHeaders({ 'id-spinner': 'none' }),
    });
  }

  // Endpoint per marcare una notifica come "da leggere" (unread)
  patchUnreadNotifiche(request: INotificationRead): Observable<string> {
    return this.http.put(Path.url('/notification/unreadNotify'), request, {
      responseType: 'text',
      headers: new HttpHeaders({ 'id-spinner': 'none' }),
    });
  }

  generatePdf(idPiao: number, sezione: string, codicePa: string): Observable<GenericResponse<any>> {
    const params = new HttpParams()
      .set('idPiao', idPiao)
      .set('sezione', sezione)
      .set('codicePa', codicePa);

    return this.http.post<GenericResponse<any>>(
      Path.url('/notification/pdf/generation'),
      {},
      {
        params,
      }
    );
  }

  async initNotificationsService(): Promise<boolean> {
    try {
      if (this.serviceIsReady) return true;

      // Inizializza EventSource che restituisce sia dati iniziali che aggiornamenti
      await this.setAllNotifications();
      this.listenForNewNotifications();
      this.initNotificationsReader();
      this.serviceIsReady = true;
      return true;
    } catch (err) {
      console.error(err);
      this.serviceIsReady = false;
      return false;
    }
  }

  notifyMouseLeave(index: number, fromDropdown: boolean = false): void {
    if (this.notificationsToReadIndexes.includes(index)) return;

    this.notificationsToReadIndexes.push(index);
    this.notificationsToReadSubject$?.next({
      indexes: this.notificationsToReadIndexes,
      fromDropdown,
    });
  }

  private async setAllNotifications(): Promise<void> {
    const params: IGetNotificheRequest = {
      idModulo: 'PIAO',
      numeroPagina: 1,
      righePerPagina: this.maxNotificationNumber,
    };

    const obs$: Observable<IAllNotificationsResponse> = this.getNotifiche(params);

    const res: IAllNotificationsResponse = await lastValueFrom(obs$);

    // Gestisce il caso in cui la risposta sia null o risultati sia undefined
    // Ordina per creationDate (più recenti prima)
    this.notificationsList.set(this.sortByCreationDate([...(res?.risultati || [])]));

    // Aggiorna il conteggio totale se presente nella risposta
    if (res?.totaleElementi !== undefined) {
      this.totalNotificationsCount.set(res.totaleElementi);
    }
  }

  /** Refresh silenzioso delle notifiche (senza spinner), usato come reazione al segnale SSE */
  private refreshNotificationsSilent(): void {
    const params: IGetNotificheRequest = {
      idModulo: 'PIAO',
      numeroPagina: 1,
      righePerPagina: this.maxNotificationNumber,
    };

    this.getNotificheSilent(params).subscribe({
      next: (res) => {
        const notifications = this.sortByCreationDate([...(res?.risultati || [])]);
        // Preserva stato read locale per notifiche marcate di recente
        const finalList = notifications.map((n) => {
          if (this.recentlyMarkedAsRead.has(n.id)) return { ...n, read: true };
          if (this.recentlyMarkedAsUnread.has(n.id)) return { ...n, read: false };
          return n;
        });
        this.notificationsList.set(finalList);
        if (res?.totaleElementi !== undefined) {
          this.totalNotificationsCount.set(res.totaleElementi);
        }
      },
      error: (err) => console.error('Errore refresh notifiche silenzioso:', err),
    });
  }

  private listenForNewNotifications(): void {
    // Chiudi eventuale connessione precedente
    this.abortController?.abort();
    this.abortController = new AbortController();
    this.userContextSubscription?.unsubscribe();

    this.userContextSubscription = this.getUserContext$().subscribe((user) => {
      const params: IGetNotificheRequest = {
        idModulo: 'PIAO',
        codiceFiscale: user.cf,
        codicePa: user.paRiferimento?.codePA,
      };

      // Costruisce URL con parametri per EventSource
      const baseUrl = Path.url('/notification/subscribe');
      const queryParams = new URLSearchParams();

      if (params.idModulo) {
        queryParams.append('idModulo', params.idModulo);
        queryParams.append('codiceFiscale', params.codiceFiscale || '');
        let userDFP = this.roleRoutingService.isDfpAuthority(user.user.typeAuthority);
        if (!userDFP) {
          queryParams.append('codicePa', params.codicePa || '');
        }
        queryParams.append('userDfp', userDFP.toString());
      }

      const endpoint = `${baseUrl}?${queryParams.toString()}`;

      const token = getAuthTokenForEnvironment(this.oAuthService);
      // X-Amministrazione-Id: codePA della PA selezionata, altrimenti stringa vuota
      const pa = this.userSessionService.getSelectedPaRiferimento();
      const paCode = pa?.codePA ?? '';

      fetchEventSource(endpoint, {
        method: 'GET',
        credentials: 'include',
        headers: {
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
          'X-Amministrazione-Id': paCode,
        },
        signal: this.abortController!.signal,
        openWhenHidden: true, // Mantieni connessione anche con tab in background
        onmessage: (event) => {
          try {
            // Ignora heartbeat SSE (eventi senza data o con data vuoto)
            if (!event.data) {
              return;
            }
            // L'evento SSE è un segnale che una nuova notifica è disponibile per questa PA.
            // Refresh silenzioso (senza spinner) dal DB per ottenere i record dell'utente corrente.
            this.refreshNotificationsSilent();
          } catch (error) {
            console.error('Errore gestione evento SSE:', error);
          }
        },
        onerror: (error) => {
          // fetchEventSource si riconnette automaticamente su errori di rete/5xx.
          // Il NetworkError è normale durante la riconnessione: non è un vero errore.
          if (error instanceof TypeError && error.message?.includes('NetworkError')) {
            console.info('SSE: riconnessione in corso...');
            return; // lascia che fetchEventSource riprovi automaticamente
          }

          console.error('Errore nella connessione SSE:', error);
          // Se è un errore HTTP (status code), interrompi i retry per evitare loop infiniti
          if (error instanceof Error && 'status' in error) {
            const status = (error as any).status as number;
            // Non ritentare su errori 4xx (client errors) - solo su 5xx o rete
            if (status >= 400 && status < 500) {
              throw error;
            }
          }
          // Per errori di rete o 5xx: fetchEventSource ritenta automaticamente
        },
      });
    });
  }

  private initNotificationsReader(): void {
    if (!this.notificationsToReadSubject$)
      this.notificationsToReadSubject$ = new Subject<{
        indexes: number[];
        fromDropdown?: boolean;
      }>();

    // Rimuovo debounce: invia immediatamente ogni notifica
    this.notificationsToReadSubject$.subscribe({
      next: (value) => this.markNotificationsAsRead(value),
    });
  }

  private async markNotificationsAsRead(value: {
    indexes: number[];
    fromDropdown?: boolean;
  }): Promise<void> {
    const notifications: INotification[] = this.notificationsList();
    const currentDate = new Date().toISOString(); // formato ISO completo per LocalDateTime backend

    // Invia ogni notifica singolarmente
    value.indexes.forEach((index) => {
      const notification = notifications[index];

      // Crea il singolo oggetto notifica con read: true
      const body: INotificationRead = {
        id: notification.id,
        message: notification.message,
        sender: notification.sender,
        //TODO: forse togliere isReady !
        ready: notification.ready,
        read: true, // Marca come letta
        creationDate: notification.creationDate,
        readDate: currentDate, // Data di lettura corrente
        type: notification.type,
        idModulo: notification.idModulo,
      };

      //console.log('📤 Invio singola notifica come letta:', body);
      this.markAsRead(body);
    });

    // Pulisce gli indici dopo l'invio
    this.notificationsToReadIndexes = [];
  }

  private async markAsRead(body: INotificationRead): Promise<void> {
    this.patchNotifiche(body).subscribe({
      next: () => {
        //console.log('✅ Notifica marcata come letta:', body.id);
        // Traccia la notifica per evitare che SSE sovrascriva il valore
        this.recentlyMarkedAsRead.add(body.id);

        // Aggiorna localmente la notifica come letta (stateless, senza refetch)
        this.notificationsList.update((currentList) =>
          currentList.map((n) => (n.id === body.id ? { ...n, read: true } : n))
        );

        // Rimuovi dal set dopo 5 secondi (tempo sufficiente per sync SSE)
        setTimeout(() => {
          this.recentlyMarkedAsRead.delete(body.id);
        }, 5000);
      },
      error: (err) => {
        console.error('❌ Errore marcatura notifica:', err);
        console.error('❌ Status:', err?.status);
        console.error('❌ Message:', err?.message);
        console.error('❌ Error body:', err?.error);
      },
    });
  }

  // Metodo pubblico per marcare una notifica come "da leggere" (unread)
  markNotificationAsUnread(index: number): void {
    const notifications: INotification[] = this.notificationsList();
    const notification = notifications[index];

    if (!notification) {
      console.error("❌ Notifica non trovata all'indice:", index);
      return;
    }

    const body: INotificationRead = {
      id: notification.id,
      message: notification.message,
      sender: notification.sender,
      ready: notification.ready,
      read: false, // Marca come da leggere
      creationDate: notification.creationDate,
      readDate: undefined, // Resetta la data di lettura
      type: notification.type,
      idModulo: notification.idModulo,
    };

    //console.log('📤 Invio singola notifica come da leggere:', body);
    this.markAsUnread(body);
  }

  private async markAsUnread(body: INotificationRead): Promise<void> {
    this.patchUnreadNotifiche(body).subscribe({
      next: () => {
        //console.log('✅ Notifica marcata come da leggere:', body.id);
        // Traccia la notifica per evitare che SSE sovrascriva il valore
        this.recentlyMarkedAsUnread.add(body.id);

        // Aggiorna localmente la notifica come da leggere (stateless, senza refetch)
        this.notificationsList.update((currentList) =>
          currentList.map((n) => (n.id === body.id ? { ...n, read: false } : n))
        );

        // Rimuovi dal set dopo 5 secondi (tempo sufficiente per sync SSE)
        setTimeout(() => {
          this.recentlyMarkedAsUnread.delete(body.id);
        }, 5000);
      },
      error: (err) => {
        console.error('❌ Errore marcatura notifica come da leggere:', err);
        console.error('❌ Status:', err?.status);
        console.error('❌ Message:', err?.message);
        console.error('❌ Error body:', err?.error);
      },
    });
  }

  public disconnect() {
    this.abortController?.abort();
    this.abortController = undefined;
    this.eventSourceObj?.close();
    this.userContextSubscription?.unsubscribe();
    this.userContextSubscription = undefined;
    this.notificationsToReadSubject$?.complete();
    this.eventSourceListenerSubscription?.unsubscribe();
    this.serviceIsReady = false;
  }
}
