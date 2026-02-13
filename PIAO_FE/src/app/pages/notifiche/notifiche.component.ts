import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { SharedModule } from '../../shared/module/shared/shared.module';
import { NotificaService } from '../../shared/services/notifica.service';
import { NotificationItemComponent } from '../../shared/components/notification-item/notification-item.component';
import { SvgComponent } from '../../shared/components/svg/svg.component';
import { INotification } from '../../shared/models/interfaces/notification';

@Component({
  selector: 'piao-notifiche',
  standalone: true,
  imports: [CommonModule, SharedModule, NotificationItemComponent, SvgComponent],
  templateUrl: './notifiche.component.html',
  styleUrl: './notifiche.component.scss',
})
export class NotificheComponent implements OnInit {
  notificaService = inject(NotificaService);
  router = inject(Router);

  // Tab attivo: 'all' o 'unread'
  activeTab = signal<'all' | 'unread'>('all');

  // Paginazione
  currentPage = signal<number>(1);
  //TODO: parametrizzare su db con json
  itemsPerPage = 10;

  // Totale notifiche
  totalNotifications = computed(() => this.notificaService.notificationsList().length);

  // Lista notifiche filtrata in base al tab
  filteredNotifications = computed(() => {
    const notifications = this.notificaService.notificationsList();
    if (this.activeTab() === 'unread') {
      return notifications.filter((n) => !n.read);
    }
    return notifications;
  });

  // Notifiche paginate
  paginatedNotifications = computed(() => {
    const filtered = this.filteredNotifications();
    const start = (this.currentPage() - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return filtered.slice(start, end);
  });

  // Totale pagine
  totalPages = computed(() => {
    return Math.ceil(this.filteredNotifications().length / this.itemsPerPage);
  });

  ngOnInit(): void {
    if (!this.notificaService.serviceIsReady) {
      this.notificaService.initNotificationsService();
    }
  }

  setActiveTab(tab: 'all' | 'unread'): void {
    this.activeTab.set(tab);
    this.currentPage.set(1); // Reset alla prima pagina quando si cambia tab
  }

  onBackClick(): void {
    // Torna alla pagina precedente o alla scrivania
    this.router.navigate(['/pages/scrivania-PA']);
  }

  onNotificationClick(notification: INotification): void {
    // Gestisce il click sulla notifica (es. navigazione al dettaglio)
    console.log('Notifica cliccata:', notification);
    // TODO: implementare navigazione al dettaglio notifica se necessario
  }

  onToggleNotificationRead(notification: INotification): void {
    // Toggle stato read/unread
    const index = this.notificaService
      .notificationsList()
      .findIndex((n) => n.id === notification.id);
    if (index !== -1) {
      if (!notification.read) {
        // Se la notifica è non letta, la marca come letta
        this.notificaService.notifyMouseLeave(index, false);
      } else {
        // Se la notifica è già letta, la marca come "da leggere"
        this.notificaService.markNotificationAsUnread(index);
      }
    }
  }

  markAllAsRead(): void {
    // Marca tutte le notifiche non lette come lette
    const unreadNotifications = this.notificaService
      .notificationsList()
      .map((n, idx) => ({ notification: n, index: idx }))
      .filter((item) => !item.notification.read);

    unreadNotifications.forEach((item) => {
      this.notificaService.notifyMouseLeave(item.index, false);
    });
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages()) {
      this.currentPage.set(page);
      // Scroll to top della lista
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  getPageNumbers(): number[] {
    const total = this.totalPages();
    const current = this.currentPage();
    const pages: number[] = [];

    if (total <= 7) {
      // Se ci sono 7 o meno pagine, mostra tutte
      for (let i = 1; i <= total; i++) {
        pages.push(i);
      }
    } else {
      // Altrimenti mostra un range intelligente
      if (current <= 3) {
        // Siamo all'inizio
        pages.push(1, 2, 3, 4, 5);
      } else if (current >= total - 2) {
        // Siamo alla fine
        pages.push(total - 4, total - 3, total - 2, total - 1, total);
      } else {
        // Siamo nel mezzo
        pages.push(current - 2, current - 1, current, current + 1, current + 2);
      }
    }

    return pages;
  }
}
