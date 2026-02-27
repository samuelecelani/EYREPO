import { Component, inject, Input, OnDestroy, ViewChild } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { AccountService } from '../../services/account.service';
import {
  BehaviorSubject,
  catchError,
  EMPTY,
  Observable,
  of,
  shareReplay,
  Subject,
  switchMap,
  takeUntil,
  tap,
} from 'rxjs';
import { UtenteDTO } from '../../models/classes/utente-dto';
import { ModalBodyComponent } from '../modal/modal-body/modal-body.component';
import { PARiferimentoDTO } from '../../models/classes/pa-riferimento-dto';
import { LoginService } from '../../services/login.service';
import { RuoloUtenteDTO } from '../../models/classes/ruolo-utente-dto';

@Component({
  selector: 'piao-base',
  imports: [SharedModule],
  templateUrl: './base.component.html',
  styleUrl: './base.component.scss',
})
export class BaseComponent implements OnDestroy {
  @ViewChild('child') child!: ModalBodyComponent;
  isVisible: boolean = false;
  accountService: AccountService = inject(AccountService);
  loginService: LoginService = inject(LoginService);
  private destroy$ = new Subject<void>();
  user!: UtenteDTO | null;
  paRiferimento!: PARiferimentoDTO;

  /**
   * Ritorna il contesto utente (user, codice fiscale, ruolo attivo).
   * Se manca uno dei dati necessari ritorna null.
   */
  getUserContext(): {
    user: UtenteDTO;
    cf: string;
    ruoloUtente: string;
    ruoloAttivo: RuoloUtenteDTO;
    paRiferimento: PARiferimentoDTO;
  } | null {
    const user = this.getUser();
    if (!user) {
      return null;
    }
    const paRiferimento = this.getPaRiferimento();
    if (!paRiferimento) {
      return null;
    }
    const ruoloAttivo = paRiferimento.ruoli.find((r) => r.ruoloAttivo);
    const ruoloUtente = ruoloAttivo?.codice;
    if (!ruoloUtente) {
      return null;
    }
    return { user, cf: user.fiscalCode ?? '', ruoloUtente, ruoloAttivo, paRiferimento };
  }

  /**
   * Versione reattiva di getUserContext.
   * Ritorna un Observable che emette il contesto utente una volta disponibile,
   * oppure EMPTY se mancano dati necessari.
   */
  getUserContext$(): Observable<{
    user: UtenteDTO;
    cf: string;
    ruoloUtente: string;
    ruoloAttivo: RuoloUtenteDTO;
    paRiferimento: PARiferimentoDTO;
  }> {
    return this.accountService.getAccount().pipe(
      switchMap((user) => {
        if (!user) {
          return EMPTY;
        }
        const paAttiva = user.paRiferimento?.filter((x) => x.attiva);
        if (!paAttiva || paAttiva.length === 0) {
          return EMPTY;
        }
        this.user = user;
        this.paRiferimento = paAttiva[0];
        const ruoloAttivo = this.paRiferimento.ruoli.find((r) => r.ruoloAttivo);
        const ruoloUtente = ruoloAttivo?.codice;
        if (!ruoloUtente || !ruoloAttivo) {
          return EMPTY;
        }
        return of({
          user,
          cf: user.fiscalCode ?? '',
          ruoloUtente,
          ruoloAttivo,
          paRiferimento: this.paRiferimento,
        });
      })
    );
  }

  /**
   * Ritorna la PA di riferimento attiva.
   * Se non è ancora stata caricata, esegue la chiamata account e la recupera.
   */
  getPaRiferimento(): PARiferimentoDTO | null {
    if (this.paRiferimento) {
      return this.paRiferimento;
    }

    this.accountService
      .getAccount()
      .pipe(
        switchMap((user) => {
          const paAttiva = user?.paRiferimento?.filter((x) => x.attiva);
          if (!paAttiva || paAttiva.length === 0) {
            return EMPTY;
          }
          this.user = user;
          this.paRiferimento = paAttiva[0];
          return of(this.paRiferimento);
        }),
        takeUntil(this.destroy$)
      )
      .subscribe();

    return this.paRiferimento ?? null;
  }

  /**
   * Ritorna l'utente corrente.
   * Se non è ancora stato caricato, esegue la chiamata account e lo recupera.
   */
  getUser(): UtenteDTO | null {
    if (this.user) {
      return this.user;
    }

    this.accountService
      .getAccount()
      .pipe(
        switchMap((user) => {
          if (!user) {
            return EMPTY;
          }
          this.user = user;
          return of(this.user);
        }),
        takeUntil(this.destroy$)
      )
      .subscribe();

    return this.user ?? null;
  }

  /**
   * Versione reattiva di getPaRiferimento.
   * Ritorna un Observable<PARiferimentoDTO> che emette la PA attiva.
   * Se già caricata, emette subito; altrimenti esegue la chiamata account.
   */
  getPaRiferimento$(): Observable<PARiferimentoDTO> {
    if (this.paRiferimento) {
      return of(this.paRiferimento);
    }
    return this.accountService.getAccount().pipe(
      switchMap((user) => {
        const paAttiva = user?.paRiferimento?.filter((x) => x.attiva);
        if (!paAttiva || paAttiva.length === 0) {
          return EMPTY;
        }
        this.user = user;
        this.paRiferimento = paAttiva[0];
        return of(this.paRiferimento);
      })
    );
  }

  // BehaviorSubject per gestire le funzionalità in modo reattivo
  protected funzionalita$ = new BehaviorSubject<string[]>([]);
  // Set per lookup O(1) invece di O(n)
  private funzionalitaSet = new Set<string>();
  private funzionalitaLoaded = false;
  private funzionalitaLoading = false;
  private visibilityObservable$?: Observable<string[] | null>;

  /**
   * Carica le funzionalità dell'utente una sola volta
   * NOTA: Non chiamare direttamente, viene gestito automaticamente da hasFunzionalita()
   */
  protected loadFunzionalita(): void {
    // Double-check pattern per sicurezza
    if (this.funzionalitaLoaded) {
      return;
    }

    this.accountService
      .getAccount()
      .pipe(
        switchMap((user) => {
          const paAttiva = user?.paRiferimento.filter((x) => x.attiva);

          if (!user?.paRiferimento || !paAttiva) {
            return of([]);
          }

          this.user = user;
          this.paRiferimento = paAttiva[0];
          const ruoliCode = this.paRiferimento?.ruoli?.map((x) => x.codice) || [];

          return this.accountService.getFunzionalita(ruoliCode);
        }),
        tap((funzionalita) => {
          const funzArray = funzionalita || [];
          this.funzionalita$.next(funzArray);
          this.funzionalitaSet = new Set(funzArray); // Set per lookup O(1)
          this.funzionalitaLoaded = true;
          this.funzionalitaLoading = false;
        }),
        catchError(() => {
          this.funzionalita$.next([]);
          this.funzionalitaSet = new Set();
          this.funzionalitaLoaded = true;
          this.funzionalitaLoading = false;
          return of([]);
        }),
        takeUntil(this.destroy$)
      )
      .subscribe();
  }

  getVisibility(codFunzionalita: string): Observable<string[] | null> {
    // Se già esiste un Observable condiviso in corso, riutilizzalo
    if (this.visibilityObservable$) {
      return this.visibilityObservable$;
    }

    // Crea un nuovo Observable condiviso
    this.visibilityObservable$ = this.accountService.getAccount().pipe(
      switchMap((user) => {
        const paAttiva = user?.paRiferimento.filter((x) => x.attiva);

        if (!user?.paRiferimento || !paAttiva) {
          return EMPTY;
        }

        this.user = user;
        this.paRiferimento = paAttiva[0];
        const ruoliCode = this.paRiferimento?.ruoli?.map((x) => x.codice) || [];

        return this.accountService.getFunzionalita(ruoliCode);
      }),
      tap((funzionalita) => {
        if (funzionalita && funzionalita.length > 0) {
          this.funzionalita$.next(funzionalita);
          this.funzionalitaSet = new Set(funzionalita);
          this.funzionalitaLoaded = true;
          this.funzionalitaLoading = false;
          this.isVisible = funzionalita.includes(codFunzionalita);
        }
      }),
      catchError(() => {
        return of([]);
      }),
      takeUntil(this.destroy$),
      shareReplay({ bufferSize: 1, refCount: true }) // Condivide il risultato tra più subscriber
    );

    return this.visibilityObservable$;
  }

  /**
   * Verifica se una funzionalità è presente per l'utente corrente
   * Metodo sincrono utilizzabile direttamente nell'HTML
   * Carica automaticamente le funzionalità se non ancora caricate
   * Utilizza Set per lookup O(1) - prestazioni ottimali
   * @param codFunzionalita Il codice della funzionalità da verificare
   * @returns true se la funzionalità è presente, false altrimenti
   */
  hasFunzionalita(codFunzionalita: string): boolean {
    // Se non ancora caricate e non in caricamento, avvia il caricamento
    if (!this.funzionalitaLoaded && !this.funzionalitaLoading) {
      this.funzionalitaLoading = true; // Imposta PRIMA di chiamare loadFunzionalita
      this.loadFunzionalita();
      return false; // Prima chiamata restituisce false, poi Angular aggiornerà automaticamente
    }

    // Se in caricamento, ritorna false temporaneamente
    if (this.funzionalitaLoading) {
      return false;
    }

    // Set.has() è O(1) invece di Array.includes() che è O(n)
    return this.funzionalitaSet.has(codFunzionalita);
  }

  get disabledButtonConfirm(): boolean {
    if (!this.child?.formGroup) {
      return false;
    }

    return this.child.formGroup.invalid;
  }

  ngOnDestroy(): void {
    //emette il valore e pulisce il subject
    this.destroy$.next();
    this.destroy$.complete();
  }
}
