import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpContext, HttpHeaders } from '@angular/common/http';
import { EMPTY, map, Observable, of, shareReplay, switchMap, tap } from 'rxjs';
import { Path } from '../utils/path';
import { GenericResponse } from '../models/interfaces/generic-response';
import { BYPASS_APP_INTERCEPTORS } from '../interceptors/interceptor.tokens';
import { FunzionalitaDTO } from '../models/classes/funzionalita-dto';
import { UtenteDTO } from '../models/classes/utente-dto';
import { PARiferimentoDTO } from '../models/classes/pa-riferimento-dto';
import { SessionStorageService } from './session-storage.service';
import { KEY_PA_ATTIVA, KEY_USER } from '../utils/constants';

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private readonly sessionStorageService = inject(SessionStorageService);
  public funzionalitaList: string[] | null = null;

  // Observable condiviso per evitare chiamate HTTP duplicate in-flight
  private accountInFlight$: Observable<UtenteDTO | null> | null = null;
  private funzionalitaInFlight$: Observable<string[] | null> | null = null;

  constructor(private http: HttpClient) {}

  getAccount(): Observable<UtenteDTO | null> {
    if (this.sessionStorageService.getItem(KEY_USER) != null) {
      return of(this.sessionStorageService.getItem(KEY_USER)); // già in memoria
    }

    // Se c'è già una chiamata in corso, riutilizza lo stesso Observable
    if (!this.accountInFlight$) {
      this.accountInFlight$ = this.fetchUser().pipe(
        map((res) => res.data),
        tap((user) => {
          let paAttiva = user?.paRiferimento?.filter((x) => x.attiva);
          if (!paAttiva || paAttiva.length === 0) {
            if (user?.paRiferimento && user.paRiferimento.length > 0) {
              user.paRiferimento[0].attiva = true; // Se non c'è una PA attiva, attiva la prima (fallback per utenti legacy senza flag)
              paAttiva = [user.paRiferimento[0]];
            }
          }
          this.sessionStorageService.setItem(KEY_PA_ATTIVA, paAttiva ? paAttiva[0] : null);
          this.sessionStorageService.setItem(KEY_USER, user);
          this.accountInFlight$ = null; // Libera dopo il completamento
        }),
        shareReplay(1)
      );
    }

    return this.accountInFlight$;
  }

  getFunzionalita(role: string[]): Observable<string[] | null> {
    if (this.funzionalitaList != null) {
      return of(this.funzionalitaList); // già in memoria
    }

    // Se c'è già una chiamata in corso, riutilizza lo stesso Observable
    if (!this.funzionalitaInFlight$) {
      this.funzionalitaInFlight$ = this.fetchFunzionalita(role).pipe(
        map((res) => {
          const funzionalitaRes = res.data ?? [];
          return funzionalitaRes.map((f) => f.codiceFunzionalita);
        }),
        tap((funzionalita) => {
          this.funzionalitaList = funzionalita;
          this.funzionalitaInFlight$ = null; // Libera dopo il completamento
        }),
        shareReplay(1)
      );
    }

    return this.funzionalitaInFlight$;
  }

  private fetchUser(): Observable<GenericResponse<UtenteDTO | null>> {
    //bypass per l'errorInterceptor
    const context = new HttpContext().set(BYPASS_APP_INTERCEPTORS, true);

    return this.http.get<GenericResponse<UtenteDTO>>(Path.url('/tokenized/user'), {
      context: context,
      headers: new HttpHeaders({ 'id-spinner': 'none' }),
    });
  }

  private fetchFunzionalita(role: string[]): Observable<GenericResponse<FunzionalitaDTO[] | null>> {
    return this.http.post<GenericResponse<FunzionalitaDTO[]>>(
      Path.url('/funzionalita/by-ruolo'),
      role
    );
  }

  /**
   * Ritorna la PA attiva dell'utente corrente.
   * Emette la PARiferimentoDTO attiva oppure EMPTY se non presente.
   */
  getPaAttiva$(): Observable<PARiferimentoDTO> {
    return this.getAccount().pipe(
      switchMap((user) => {
        const paAttiva = user?.paRiferimento?.filter((x) => x.attiva);
        if (!user || (user.paRiferimento && user.paRiferimento.length === 0)) {
          return EMPTY;
        }
        return of(paAttiva ? paAttiva[0] : user.paRiferimento?.[0]);
      })
    );
  }

  /**
   * Ritorna le funzionalità dell'utente corrente in base ai ruoli della PA attiva.
   * Combina getAccount → PA attiva → ruoli → getFunzionalita in un unico stream.
   */
  getFunzionalitaUtente$(): Observable<string[]> {
    return this.getPaAttiva$().pipe(
      switchMap((paAttiva) => {
        const ruoliCode = paAttiva?.ruoli?.map((x) => x.codice) || [];
        return this.getFunzionalita(ruoliCode);
      }),
      map((funzionalita) => funzionalita || [])
    );
  }
}
