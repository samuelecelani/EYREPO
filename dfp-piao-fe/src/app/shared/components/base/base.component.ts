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
import { CampiTecniciDTO } from '../../models/classes/campi-tecnici-dto';
import { SessionStorageService } from '../../services/session-storage.service';
import { SectionStatusEnum } from '../../models/enums/section-status.enum';
import { KEY_USER } from '../../utils/constants';
import { CodRuoloEnum } from '../../models/enums/cod-ruolo-enum';
import { ToastService } from '../../services/toast.service';

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
  protected destroy$ = new Subject<void>();
  user!: UtenteDTO | null;
  paRiferimento!: PARiferimentoDTO;
  readonly sessionStorageService = inject(SessionStorageService);
  readonly toastService = inject(ToastService);

  NO_RELOAD_STATES = new Set<string>([
    SectionStatusEnum.VALIDATA,
    SectionStatusEnum.RICHIESTA_APPROVAZIONE,
    SectionStatusEnum.PUBBLICATO,
    SectionStatusEnum.APPROVATO,
    SectionStatusEnum.IN_VALIDAZIONE,
  ]);

  noReloadStato(stato?: string): boolean {
    if (!stato) {
      return true;
    }

    return this.NO_RELOAD_STATES.has(stato);
  }
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
    if (this.sessionStorageService.getItem(KEY_USER) != null) {
      this.user = this.sessionStorageService.getItem(KEY_USER);
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
   * Carica i privilegi dell'utente dal ruolo attivo della PA attiva (una sola volta)
   */
  protected loadFunzionalita(): void {
    if (this.funzionalitaLoaded) {
      return;
    }

    this.accountService
      .getPaAttiva$()
      .pipe(
        tap((pa) => {
          const ruoloAttivo = pa?.ruoli?.find((r) => r.ruoloAttivo);
          const privileges = ruoloAttivo?.privileges ?? [];
          this.funzionalita$.next(privileges);
          this.funzionalitaSet = new Set(privileges);
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
  hasFunzionalita(codFunzionalita: string | string[]): boolean {
    // Se non ancora caricate e non in caricamento, avvia il caricamento
    if (!this.funzionalitaLoaded && !this.funzionalitaLoading) {
      this.funzionalitaLoading = true;
      this.loadFunzionalita();
      // Se loadFunzionalita ha completato sincronamente (dati utente in sessionStorage),
      // funzionalitaLoaded è ora true e proseguiamo con il controllo dei codici
    }

    // Se ancora in caricamento asincrono (dati non in sessionStorage), ritorna false temporaneamente
    if (!this.funzionalitaLoaded) {
      return false;
    }

    const codici = Array.isArray(codFunzionalita) ? codFunzionalita : [codFunzionalita];
    return codici.some((cod) => this.funzionalitaSet.has(cod));
  }

  /**

 * Verifica se l'utente corrente è un REDATTORE e la sezione indicata

 * NON è tra le sue sezioni associate.

 * In tal caso la sezione deve essere mostrata in sola lettura (dettaglio).

 * @param activeSectionId L'id della sezione attiva (es. '1', '2.1', '3.3.1')

 * @returns true se la sezione è bloccata per il REDATTORE, false altrimenti

 */

  isSezioneNonAssociata(activeSectionId: string): boolean {
    const ctx = this.getUserContext();

    if (!ctx) {
      return false;
    }

    const sectionEnum = 'SEZIONE_' + activeSectionId.replace(/\./g, '');

    const nonAssociata = ctx.ruoloAttivo.sezioneAssociata?.includes(sectionEnum);

    if (!nonAssociata) {
      this.toastService.warning(
        'Non puoi modificare questa sezione perché non è associata al tuo profilo.'
      );
    }

    return nonAssociata;
  }

  /**
   * Costruisce un oggetto CampiTecniciDTO con i dati dell'utente corrente.
   * @returns CampiTecniciDTO popolato oppure un oggetto con solo validity=true se il contesto utente non è disponibile
   */
  buildCampiTecnici(): CampiTecniciDTO {
    const campiTecnici = new CampiTecniciDTO();
    campiTecnici.validity = true;

    const ctx = this.getUserContext();
    if (ctx) {
      campiTecnici.createdBy = ctx.cf;
      campiTecnici.createdByRole = ctx.ruoloUtente;
      campiTecnici.createdByNameSurname = `${ctx.user.nome ?? ''} ${ctx.user.cognome ?? ''}`.trim();
    }

    return campiTecnici;
  }

  get disabledButtonConfirm(): boolean {
    if (!this.child?.formGroup) {
      return true;
    }

    return this.child.formGroup.invalid;
  }

  ngOnDestroy(): void {
    //emette il valore e pulisce il subject
    this.destroy$.next();
    this.destroy$.complete();
  }
}
