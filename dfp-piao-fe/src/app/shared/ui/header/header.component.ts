import { Component, HostListener, inject, OnInit, OnDestroy, signal } from '@angular/core';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { NotificaService } from '../../services/notifica.service';
import { CommonModule } from '@angular/common';
import { LoginService } from '../../services/login.service';
import { NotificationDropdownComponent } from '../../components/notification-dropdown/notification-dropdown.component';
import { TranslateModule } from '@ngx-translate/core';
import { BaseComponent } from '../../components/base/base.component';
import { SvgComponent } from '../../components/svg/svg.component';
import { OAuthService } from 'angular-oauth2-oidc';
import { filter, take, takeUntil } from 'rxjs';
import { KEY_PA_ATTIVA, KEY_USER, MONUMENT_ICON } from '../../utils/constants';
import { ModalComponent } from '../../components/modal/modal.component';
import { ModalSelezionaPaComponent } from '../utente/modal-seleziona-pa/modal-seleziona-pa.component';
import { LabelValue } from '../../models/interfaces/label-value';
import { SessionStorageService } from '../../services/session-storage.service';
import { AuthService } from '../../services/auth.service';
import { RoleRoutingService } from '../../services/role-routing.service';

@Component({
  selector: 'piao-header',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    NotificationDropdownComponent,
    TranslateModule,
    SvgComponent,
    ModalComponent,
    ModalSelezionaPaComponent,
  ],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss',
})
export class HeaderComponent extends BaseComponent implements OnInit, OnDestroy {
  // Analogia React: invece di usare un context custom, qui inietti un service
  notificaService = inject(NotificaService);
  router = inject(Router);

  initialNameSurname: string = '';

  iconSelectPA: string = MONUMENT_ICON;

  openModalSelectPA = false;

  radioPaOptions: LabelValue[] = [];

  isMobileMenuOpen = signal<boolean>(false);

  private authService = inject(AuthService);
  private roleRoutingService = inject(RoleRoutingService);
  private oAuthService = inject(OAuthService);

  isDfpUser = false;
  homeRoute = '/area-privata-PA';
  organizationName = '';

  mobileMenuItems = this.buildMobileMenuItems();

  //router = inject(Router);

  headerLabels = {
    title: 'Portale PIAO',
    subTitle: 'Piano Integrato di Attività e Organizzazione',
    bannerTitle: 'Dipartimento della funzione pubblica',
  };

  handleLogout = () => {
    //Logout
    this.authService.logout();
  };

  // Stato del dropdown notifiche
  isDropdownOpen = signal<boolean>(false);

  // Stato del dropdown profilo
  isProfileDropdownOpen = signal<boolean>(false);

  async ngOnInit(): Promise<void> {
    // Evita di chiamare /tokenized/user prima del login: nella pagina /auth/login
    // il token non è ancora disponibile e si genererebbe un 401 inutile.
    if (this.isAuthenticatedRoute()) {
      this.getUserDetails();
      this.checkSelectPaFromIndex();
    } else {
      // Attende la prima navigazione verso una route autenticata e popola allora
      this.router.events
        .pipe(
          filter((e): e is NavigationEnd => e instanceof NavigationEnd),
          filter(() => this.isAuthenticatedRoute()),
          take(1),
          takeUntil(this.destroy$)
        )
        .subscribe(() => {
          this.getUserDetails();
          this.checkSelectPaFromIndex();
        });
    }
    // Inizializza il servizio notifiche
    await this.notificaService.initNotificationsService();
  }

  private isAuthenticatedRoute(): boolean {
    const url = this.router.url || '';
    return !url.startsWith('/auth/login') && this.oAuthService.hasValidAccessToken();
  }

  private checkSelectPaFromIndex(): void {
    const fromIndex = history.state?.fromIndex;
    if (!fromIndex) return;

    // Consuma il flag: lo rimuove da history.state per evitare che venga riletto
    // dopo un eventuale reload (es. cambio PA con window.location.href).
    // Edge/BFCache preserva history.state tra reload, Firefox no — quindi
    // normalizziamo il comportamento azzerandolo esplicitamente.
    const { fromIndex: _omit, ...restState } = history.state ?? {};
    history.replaceState(restState, '');

    this.accountService
      .getAccount()
      .pipe(take(1))
      .pipe(takeUntil(this.destroy$))
      .subscribe((user) => {
        if (user?.paRiferimento && user.paRiferimento.length > 1) {
          this.openModalSelectPA = true;
        }
      });
  }

  getUserDetails() {
    this.accountService
      .getAccount()
      .pipe(take(1))
      .pipe(takeUntil(this.destroy$))
      .subscribe((user) => {
        if (!user) {
          return;
        }

        const paRiferimento = user.paRiferimento?.find((pa) => pa.attiva);
        this.user = user;
        let name = this.user.nome ? this.user.nome.at(0) : '';
        let surname = this.user.cognome ? this.user.cognome.at(0) : '';
        this.initialNameSurname = `${name?.toLocaleUpperCase()}${surname?.toUpperCase()}`;
        this.isDfpUser = this.roleRoutingService.isDfpAuthority(user.typeAuthority);
        this.homeRoute = this.roleRoutingService.getHomeRouteByAuthority(user.typeAuthority);
        this.mobileMenuItems = this.buildMobileMenuItems();

        if (paRiferimento) {
          this.paRiferimento = paRiferimento;
          this.organizationName = paRiferimento.denominazionePA;
        } else {
          this.organizationName = 'Dipartimento della funzione pubblica';
        }

        this.populateRadioPaOptions();
      });
  }

  populateRadioPaOptions() {
    if (this.isDfpUser) {
      this.radioPaOptions = [];
      return;
    }

    if (this.user && this.user.paRiferimento) {
      this.radioPaOptions = this.user.paRiferimento
        .sort((a, b) => a.denominazionePA.localeCompare(b.denominazionePA))
        .map((pa) => ({
          label: pa.denominazionePA,
          value: pa.denominazionePA,
          additionalField: pa.attiva,
        }));
    } else {
      this.radioPaOptions = [];
    }
  }

  savePaData(): void {
    if (this.isDfpUser) {
      this.openModalSelectPA = false;
      return;
    }

    this.getUserContext$()
      .pipe(take(1))
      .pipe(takeUntil(this.destroy$))
      .subscribe(({ user }) => {
        const selectedPa = this.child?.formGroup.get('radioPaOptions')?.value;
        if (selectedPa && user.paRiferimento) {
          const currentActivePa = user.paRiferimento.find((pa) => pa.attiva);
          if (currentActivePa?.denominazionePA === selectedPa) {
            this.openModalSelectPA = false;
            return;
          }
          user.paRiferimento.forEach((pa) => {
            pa.attiva = pa.denominazionePA === selectedPa;
          });
          this.paRiferimento = user.paRiferimento.find((pa) => pa.attiva)!;
          this.openModalSelectPA = false;
          this.sessionStorageService.setItem(KEY_PA_ATTIVA, this.paRiferimento);
          this.sessionStorageService.setItem(KEY_USER, user);
          this.populateRadioPaOptions();
          // Full reload: prepend del base href poiché this.homeRoute è una route Angular interna
          window.location.href = '/area-riservata' + this.homeRoute;
        }
      });
  }

  private buildMobileMenuItems() {
    if (this.isDfpUser) {
      return [
        { label: 'Scrivania', route: this.homeRoute },
        { label: 'Profilo', route: '/profilo' },
        { label: 'Cruscotti di analisi', route: '/cruscotti-di-analisi' },
        { label: 'Documenti', route: '/documenti' },
        { label: 'Avvisi', route: '/avvisi' },
        { label: 'Gestionale', route: '/gestionale' },
      ];
    }

    return [
      { label: 'Scrivania', route: this.homeRoute },
      { label: 'Profilo', route: '/profilo' },
      { label: 'Servizi PIAO', route: '/servizi-piao' },
      { label: 'Validazione', route: '/validazione' },
      { label: 'Novita', route: '/novita' },
      { label: 'Gestionale', route: '/gestionale' },
      { label: 'Help Desk', route: '/help-desk' },
    ];
  }

  toggleDropdown(): void {
    this.isDropdownOpen.update((value) => !value);
  }

  closeDropdown(): void {
    this.isDropdownOpen.set(false);
  }

  toggleProfileDropdown(): void {
    this.isProfileDropdownOpen.update((value) => !value);
  }

  closeProfileDropdown(): void {
    this.isProfileDropdownOpen.set(false);
  }

  navigateTo(route: string): void {
    this.closeProfileDropdown();
    this.router.navigate([route]);
  }

  toggleMobileMenu(): void {
    this.isMobileMenuOpen.update((value) => !value);
  }

  closeMobileMenu(): void {
    this.isMobileMenuOpen.set(false);
  }

  mobileNavigateTo(route: string): void {
    this.closeMobileMenu();
    this.router.navigate([route]);
  }

  @HostListener('window:scroll')
  onWindowScroll(): void {
    this.closeDropdown();
    this.closeProfileDropdown();
  }
}
