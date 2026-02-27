import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { INotification } from '../../models/interfaces/notification';
import { BadgeComponent } from '../badge/badge.component';
import { SvgComponent } from '../svg/svg.component';
import { TranslateService } from '@ngx-translate/core';
import { TranslateModule } from '@ngx-translate/core';

export type NotificationLocation = 'dropdown-notification' | 'page-notification';

@Component({
  selector: 'piao-notification-item',
  standalone: true,
  imports: [CommonModule, BadgeComponent, SvgComponent, TranslateModule],
  templateUrl: './notification-item.component.html',
  styleUrl: './notification-item.component.scss',
})
export class NotificationItemComponent {
  private translate = inject(TranslateService);

  @Input() notification!: INotification;
  @Input() compact: boolean = false;
  @Input() showActionButton: boolean = false; // Per mostrare il pulsante sulla destra nella pagina notifiche
  @Input() location: NotificationLocation = 'page-notification'; // Indica dove viene usato il componente
  @Output() clicked = new EventEmitter<void>();
  @Output() toggleRead = new EventEmitter<void>(); // Nuovo evento per il toggle read/unread

  onItemClick(): void {
    this.clicked.emit();
  }

  onToggleRead(event: Event): void {
    event.stopPropagation();
    this.toggleRead.emit();
  }

  formatDate(date: Date | string): string {
    const now = new Date();
    const notificationDate = new Date(date);
    const diffMs = now.getTime() - notificationDate.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) {
      return this.translate.instant('NOTIFICATION.LABELS.NOW');
    } else if (diffMins < 60) {
      return `${diffMins} ${this.translate.instant('NOTIFICATION.LABELS.MINUTES_AGO')}`;
    } else if (diffHours < 24) {
      const timeUnit =
        diffHours === 1
          ? this.translate.instant('NOTIFICATION.LABELS.HOUR_AGO')
          : this.translate.instant('NOTIFICATION.LABELS.HOURS_AGO');
      return `${diffHours} ${timeUnit}`;
    } else if (diffDays < 7) {
      const timeUnit =
        diffDays === 1
          ? this.translate.instant('NOTIFICATION.LABELS.DAY_AGO')
          : this.translate.instant('NOTIFICATION.LABELS.DAYS_AGO');
      return `${diffDays} ${timeUnit}`;
    } else {
      return notificationDate.toLocaleDateString('it-IT', {
        day: 'numeric',
        month: 'short',
        year: notificationDate.getFullYear() !== now.getFullYear() ? 'numeric' : undefined,
      });
    }
  }
}
