import { Component, inject, OnInit, OnDestroy, signal } from '@angular/core';
import { Router } from '@angular/router';
import { NotificaService } from '../../services/notifica.service';
import { CommonModule } from '@angular/common';
import { LoginService } from '../../services/login.service';
import { NotificationDropdownComponent } from '../../components/notification-dropdown/notification-dropdown.component';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'piao-header',
  standalone: true,
  imports: [CommonModule, NotificationDropdownComponent, TranslateModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent implements OnInit, OnDestroy {
  // Analogia React: invece di usare un context custom, qui inietti un service
  notificaService = inject(NotificaService);
  router = inject(Router);
  loginService = inject(LoginService);

  //router = inject(Router);

  headerLabels = {
    title: 'Portale PIAO',
    subTitle: 'Piano Integrato di Attività e Organizzazione',
    bannerTitle: 'Dipartimento della funzione pubblica',
  };

  handleLogout = () => {
    this.notificaService.disconnect();
    this.loginService.logout();
    this.loginService.login();
  };

  // Stato del dropdown
  isDropdownOpen = signal<boolean>(false);

  async ngOnInit(): Promise<void> {
    // Inizializza il servizio notifiche
    await this.notificaService.initNotificationsService();
  }

  ngOnDestroy(): void {
    // Non disconnettere il servizio SSE qui: il servizio è root-scoped e deve
    // restare attivo durante tutta la sessione. La disconnessione avviene solo al logout.
  }

  toggleDropdown(): void {
    this.isDropdownOpen.update((value) => !value);
  }

  closeDropdown(): void {
    this.isDropdownOpen.set(false);
  }
}
