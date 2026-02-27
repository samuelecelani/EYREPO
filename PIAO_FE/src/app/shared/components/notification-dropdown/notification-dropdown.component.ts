import { Component, inject, Output, EventEmitter, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { NotificaService } from '../../services/notifica.service';
import { NotificationItemComponent } from '../notification-item/notification-item.component';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'piao-notification-dropdown',
  standalone: true,
  imports: [CommonModule, NotificationItemComponent, TranslateModule],
  templateUrl: './notification-dropdown.component.html',
  styleUrl: './notification-dropdown.component.scss',
})
export class NotificationDropdownComponent {
  notificaService = inject(NotificaService);
  router = inject(Router);

  @Output() close = new EventEmitter<void>();

  // Filtra solo le prime 4 notifiche da mostrare nel dropdown
  displayedNotifications = computed(() => this.notificaService.notificationsList().slice(0, 4));

  // Calcola il totale delle notifiche dall'API
  totalNotifications = computed(() => {
    const totalFromService = this.notificaService.totalNotificationsCount();
    // Se il conteggio totale non è disponibile, usa la lunghezza della lista
    return totalFromService > 0
      ? totalFromService
      : this.notificaService.notificationsList().length;
  });

  onCloseClick(): void {
    this.close.emit();
  }

  onNotificationClick(index: number): void {
    // Marca la notifica come letta
    this.notificaService.notifyMouseLeave(index, true);
  }

  onViewAllClick(): void {
    // Naviga alla pagina notifiche
    this.router.navigate(['/pages/notifiche']);
    this.close.emit();
  }
}
