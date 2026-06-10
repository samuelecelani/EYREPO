import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { OAuthService } from 'angular-oauth2-oidc';
import { CookieService } from 'ngx-cookie-service';
import { getConfig, getValue } from '../config/loader-config';
import { buildAuthConfig } from '../config/auth.config';
import { Globals } from '../../globals';
import { PublicUserAPIService } from '../api-clients/utenti/services';
import { InlineResponse200 } from '../api-clients/utenti/models';
import { UtenteDTO } from '../models/classes/utente-dto';
import { UserSessionService } from './user-session-service.service';
import { UsersService } from './users.service';
import { AccountService } from './account.service';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly oAuthService: OAuthService = inject(OAuthService);
  private readonly cookieService: CookieService = inject(CookieService);

  private readonly accountService = inject(AccountService);
  private readonly globals: Globals = inject(Globals);
  private readonly router: Router = inject(Router);
  private readonly userSessionService = inject(UserSessionService);

  /** Stato interno per auto-refresh */
  private refreshTimer: any = null;
  private discoveryLoaded = false;
  private autoRefreshStarted = false;

  /** ammId da ripristinare dopo re-login OIDC (portato tramite OIDC state) */
  private _preferredPaCode: string | null = null;
  /** Evita doppie chiamate API concorrenti in restoreSession */
  private _restoring = false;

  setUserAndPermissions(user: UtenteDTO) {
    // this.userService.setUser(user);
    // if (user && (user as any).privileges) {
    //   this.rolesService.flushRolesAndPermissions();
    //   this.rolesService.addRoleWithPermissions(user.roleType, Object.values(user?.privileges as any));
    // }
    // if (user.storedFileUuidPhoto) {
    //   this.loadUserPhoto(user);
    // }
  }

  private async initAuth(): Promise<void> {
    if (this.discoveryLoaded) return;

    const config = buildAuthConfig(getConfig());
    this.oAuthService.configure(config);

    // Carico la discovery UNA sola volta per sessione
    await this.oAuthService.loadDiscoveryDocument();
    this.discoveryLoaded = true;
  }

  private scheduleNextRefresh(tokenExpiration: number, addRandomDelay: boolean = false): void {
    if (!this.oAuthService.hasValidAccessToken()) return;

    const waitTime = this.calculateTokenRefreshTimeout(addRandomDelay);

    localStorage.setItem('next_token_update', waitTime.toString());

    // pulisco eventuale timer precedente e programmo il prossimo
    this.cancelRefresh();
    this.refreshTimer = setTimeout(() => this.performRefresh(tokenExpiration), waitTime);
  }

  private calculateTokenRefreshTimeout(addRandomDelay: boolean) {
    const now = Date.now();
    const exp = this.oAuthService.getAccessTokenExpiration(); // ms epoch
    const lifetime = Math.max(exp - now, 0);

    // Fattore di anticipo: es. 0.8 -> pianifica a 80% della durata
    const factor = Number(getValue('openidTimeoutFactor')) || 0.8;
    let delay = 0;
    if (addRandomDelay) {
      const min = 5000; // 5 secondi
      const max = 20000; // 20 secondi
      delay = Math.floor(Math.random() * (max - min + 1)) + min;
    }
    const waitTime = Math.max(Math.floor(lifetime * factor), 5_000 + delay); // minimo 5s per sicurezza
    return waitTime;
  }

  private async performRefresh(tokenExpiration: number): Promise<void> {
    try {
      const currentTokenExp = this.oAuthService.getAccessTokenExpiration();
      if (currentTokenExp !== tokenExpiration) {
        // Token refreshed from other, rescheduling;
        this.scheduleNextRefresh(currentTokenExp);
        return;
      }

      await this.initAuth();

      await this.oAuthService.refreshToken().then(() => {
        this.scheduleNextRefresh(this.oAuthService.getAccessTokenExpiration());
      });
    } catch (err) {
      // Forziamo un nuovo login: portiamo paCode e route corrente nello state OIDC
      // così al ritorno possiamo ripristinare la selezione senza usare storage
      const paCode = this.userSessionService.getSelectedPaRiferimento()?.codePA;
      const customState = JSON.stringify({
        route: this.router.url,
        ammId: paCode ?? null,
      });
      const config = buildAuthConfig(getConfig());
      // Disabilitiamo preserveRequestedRoute perché gestiamo noi il redirect nel custom state
      this.oAuthService.configure({ ...config, preserveRequestedRoute: false });
      this.oAuthService.initLoginFlow(customState);
    }
  }

  public startAutoRefresh(): void {
    if (this.autoRefreshStarted) return;
    this.autoRefreshStarted = true;

    // Ogni volta che la lib emette un nuovo token, ripianifico
    this.oAuthService.events.subscribe((e) => {
      if (e.type === 'token_received') {
        this.scheduleNextRefresh(this.oAuthService.getAccessTokenExpiration());
      }
    });

    // Se all’avvio ho già un token valido, pianifico subito
    if (this.oAuthService.hasValidAccessToken()) {
      this.scheduleNextRefresh(this.oAuthService.getAccessTokenExpiration());
    }
  }

  public cancelRefresh(): void {
    if (this.refreshTimer) {
      clearTimeout(this.refreshTimer);
      this.refreshTimer = null;
    }
  }

  async logout() {
    await this.initAuth(); // garantisco configure+discovery
    await this.performOAuthLogout();
  }

  private async performOAuthLogout(): Promise<void> {
    try {
      const revokeEndpoint = this.oAuthService.revocationEndpoint;
      const clientId = getValue('openidClientId');
      const refreshToken = this.oAuthService.getRefreshToken();

      // Revoca refresh token per invalidare le sessioni lato Keycloak
      if (refreshToken && revokeEndpoint) {
        await fetch(revokeEndpoint, {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: new URLSearchParams({
            client_id: clientId,
            token: refreshToken,
            token_type_hint: 'refresh_token',
          }),
          credentials: 'omit',
        });
      }

      // Pulisci lo stato locale PRIMA del redirect
      this.deleteAllLocalAuthData();

      // Invalida la sessione SSO su Keycloak in background (senza redirect del browser).
      // Usiamo fetch con credentials per inviare il cookie di sessione Keycloak.
      const idToken = this.oAuthService.getIdToken();
      const logoutUrl = this.oAuthService.logoutUrl;

      if (logoutUrl) {
        const params = new URLSearchParams();
        if (idToken) {
          params.set('id_token_hint', idToken);
        }
        try {
          await fetch(`${logoutUrl}?${params.toString()}`, {
            method: 'GET',
            credentials: 'include',
            mode: 'no-cors',
          });
        } catch {
          // Ignora errori di rete/CORS: la sessione potrebbe essere già invalidata
        }
      }

      // Naviga direttamente con il router Angular
      this.router.navigate(['/auth/login']);
    } catch (error) {
      // Fallback: pulisce locale e ridirige
      this.deleteAllLocalAuthData();
      this.router.navigate(['/auth/login']);
    }
  }

  deleteAllLocalAuthData() {
    this.userSessionService.clear();
    localStorage.clear();
    sessionStorage.clear();
    this.deleteAllCookies();
    this.cancelRefresh();
  }

  deleteAllCookies(): void {
    this.cookieService.deleteAll();
  }

  check() {
    // if (!this.userService.isUserSet()) {
    //   this.userService.loadUserFromLocalStorage();
    //   // Carica anche l'immagine del profilo se l'utente è presente
    //   const user = this.userService.getUser();
    //   if (user) {
    //     this.loadUserPhoto(user);
    //   }
    // }
    //
    // if (
    //   this.jwtService.isTokenExpired(
    //     localStorage.getItem(
    //       this.appConfigService.getValue("LOCAL_STORAGE_TOKEN"),
    //     ) || "",
    //   )
    // ) {
    //   localStorage.clear();
    //   this.userService.clearUser();
    //   return false;
    // }
    //
    // if (!this.autoRefreshStarted) this.startAutoRefresh();
    // return this.userService.isUserSet();
  }

  async loginIDP(redirect: string = '') {
    await this.initAuth();
    this.oAuthService.initLoginFlow();
  }

  public afterLoginWithValidToken(redirect: string, preferredAmmId?: string): void {
    this._preferredPaCode = preferredAmmId ?? null;
    this.startAutoRefresh();
    if (redirect) {
      this.router.navigateByUrl(redirect);
      this.restoreSession();
      return;
    }

    // Nessun redirect richiesto: decidi la destinazione in base all'authority dell'utente
    this.accountService.getAccount().subscribe({
      next: (res: any) => {
        if (res?.typeAuthority === 'DFP') {
          this.router.navigate(['/area-privata-DFP']);
        } else if (res?.typeAuthority === 'PA' || res?.typeAuthority === 'PA_CAPOFILA') {
          this.router.navigate(['/area-privata-PA'], { state: { fromIndex: true } });
        } else {
          this.router.navigate(['/index']);
        }
      },
      error: (err: any) => {
        console.error(err);
        this.router.navigate(['/index']);
      },
    });
    this.restoreSession();
  }

  /** Carica l'utente dall'API se la PA non è ancora in memoria.
   *  Chiamato sia dopo il login che al refresh (da MainLayoutComponent).
   *  Usa _preferredPaCode (ricavato dallo state OIDC) per ripristinare la selezione senza chiedere. */
  public restoreSession(): void {
    if (this.userSessionService.hasSelectedPaRiferimento()) return;
    if (!this.oAuthService.hasValidAccessToken()) return;
    if (this._restoring) return;

    this._restoring = true;
    const preferredPaCode = this._preferredPaCode;

    this.accountService.getAccount().subscribe({
      next: (res: any) => {
        if (res?.status?.id === this.globals.HTTP_NOT_ERROR_STATUS_ID && res.data) {
          this.userSessionService.setUser(res.data, preferredPaCode ?? undefined);
        }
      },
      error: () => {
        /* l'utente può riprovare da "Seleziona Amministrazione" nell'header */
      },
      complete: () => {
        this._restoring = false;
      },
    });
  }

  // public downloadUserPhoto(user: UserRequest) {
  //   if (user?.storedFileUuidPhoto) {
  //     this.customStoredFileControllerService.generatePublicLink(user?.storedFileUuidPhoto).subscribe(resLink => {
  //       if (resLink.status.id === this.globals.HTTP_NOT_ERROR_STATUS_ID) {
  //         this.customStoredFileControllerService.downloadFromPublicLink(resLink.data).subscribe({
  //           next: (res: any) => {
  //             if (res && res.body) {
  //               const blob = res.body;
  //               this.userService.savePhotoToSessionStorage(blob);
  //             }
  //           },
  //           error: (err) => {
  //            //Errore caricamento immagine;
  //           },
  //         });
  //       }
  //     });
  //   }
  // }

  // public loadUserPhoto(user: UserRequest) {
  //   const photo = this.userService.loadPhotoFromSessionStorage()
  //   if(!photo) {
  //     this.downloadUserPhoto(user);
  //   }
  //
  // }
}
