import { computed, inject, Injectable, Signal, signal, WritableSignal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { EMPTY, lastValueFrom, map, Observable, of, Subject, Subscription, switchMap } from 'rxjs';
import { IGetNotificheRequest } from '../models/interfaces/get-notifiche-request';
import { INotification } from '../models/interfaces/notification';
import { IAllNotificationsResponse } from '../models/interfaces/all-notifications-response';
import { Path } from '../utils/path';
import { INotificationRead } from '../models/interfaces/notification-read';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { GenericResponse } from '../models/interfaces/generic-response';
import { BaseComponent } from '../components/base/base.component';

@Injectable({
  providedIn: 'root',
})
export class NotificaService extends BaseComponent {
  constructor(private http: HttpClient) {
    super();
  }

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

  getNotifiche(request: IGetNotificheRequest): Observable<IAllNotificationsResponse> {
    return this.getUserContext$().pipe(
      switchMap((user) => {
        request.ruolo = user.ruoloUtente;
        request.codiceFiscale = user.cf;
        request.codicePa = user.paRiferimento?.codePA;

        return this.http
          .get<GenericResponse<IAllNotificationsResponse>>(Path.url('/notification/list'), {
            params: { ...request },
          })
          .pipe(map((res) => res.data));
      })
    );
  }

  patchNotifiche(request: INotificationRead): Observable<string> {
    return this.http.put(Path.url('/notification/readNotify'), request, {
      responseType: 'text',
    });
  }

  // Endpoint per marcare una notifica come "da leggere" (unread)
  patchUnreadNotifiche(request: INotificationRead): Observable<string> {
    return this.http.put(Path.url('/notification/unreadNotify'), request, {
      responseType: 'text',
    });
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

  private listenForNewNotifications(): void {
    // Chiudi eventuale connessione precedente
    this.abortController?.abort();
    this.abortController = new AbortController();

    this.getUserContext$().subscribe((user) => {
      const params: IGetNotificheRequest = {
        idModulo: 'PIAO',
        ruolo: user.ruoloUtente,
        codiceFiscale: user.cf,
        codicePa: user.paRiferimento?.codePA,
      };

      // Costruisce URL con parametri per EventSource
      const baseUrl = Path.url('/notification/subscribe');
      const queryParams = new URLSearchParams();

      if (params.idModulo) {
        queryParams.append('idModulo', params.idModulo);
        queryParams.append('ruolo', params.ruolo || '');
        queryParams.append('codiceFiscale', params.codiceFiscale || '');
        queryParams.append('codicePa', params.codicePa || '');
      }

      const endpoint = `${baseUrl}?${queryParams.toString()}`;

      fetchEventSource(endpoint, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'X-Fiscal-Code': 'TESTTEST',
        },
        signal: this.abortController!.signal,
        openWhenHidden: true, // Mantieni connessione anche con tab in background
        onmessage: (event) => {
          try {
            // Ignora heartbeat SSE (eventi senza data o con data vuoto)
            if (!event.data) {
              return;
            }
            const data = JSON.parse(event.data);
            // Helper per preservare lo stato read locale
            const preserveLocalReadState = (n: INotification): INotification => {
              if (this.recentlyMarkedAsRead.has(n.id)) {
                return { ...n, read: true };
              }
              if (this.recentlyMarkedAsUnread.has(n.id)) {
                return { ...n, read: false };
              }
              return n;
            };

            // Il backend invia singoli NotificationDTO via Flux<SSE<NotificationDTO>>
            if (data?.id && data?.message) {
              const notification = data as INotification;
              const finalNotification = preserveLocalReadState(notification);

              this.notificationsList.update((currentList) => {
                const exists = currentList.some((n) => n.id === finalNotification.id);
                if (exists) {
                  const updated = currentList.map((n) =>
                    n.id === finalNotification.id ? finalNotification : n
                  );
                  return this.sortByCreationDate(updated);
                } else {
                  const newList = this.sortByCreationDate([finalNotification, ...currentList]);
                  return newList.slice(0, this.maxNotificationNumber);
                }
              });
            } else {
              console.warn('Struttura risposta SSE non riconosciuta:', data);
            }
          } catch (error) {
            console.error('Errore parsing notifiche:', error);
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
    const currentDate = new Date().toISOString().split('T')[0]; // formato YYYY-MM-DD

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
      readDate: '', // Resetta la data di lettura
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
    this.notificationsToReadSubject$?.complete();
    this.eventSourceListenerSubscription?.unsubscribe();
    this.serviceIsReady = false;
  }
}
