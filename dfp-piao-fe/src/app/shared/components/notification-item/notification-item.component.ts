import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { INotification } from '../../models/interfaces/notification';
import { BadgeComponent } from '../badge/badge.component';
import { SvgComponent } from '../svg/svg.component';
import { TranslateService } from '@ngx-translate/core';
import { TranslateModule } from '@ngx-translate/core';
import { AttachmentService } from '../../services/attachment.service';
import { IMPOSSIBLE_PDF_DOWNLOAD } from '../../utils/constants';

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
  private attachmentService = inject(AttachmentService);

  impossiblePdfDownload = IMPOSSIBLE_PDF_DOWNLOAD;

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

  downloadFile(notification: INotification): void {
    if (!notification.downloadUrl) return;

    fetch(notification.downloadUrl, { method: 'GET' })
      .then(async (response) => {
        if (!response.ok) {
          throw new Error(`Download failed with status ${response.status}`);
        }

        const blob = await response.blob();
        const contentDisposition = response.headers.get('content-disposition');
        const nameFromHeader = this.extractFileNameFromContentDisposition(contentDisposition);
        const safeFileName =
          nameFromHeader ||
          this.extractFileNameFromUrl(notification.downloadUrl || '') ||
          'allegato';

        const objectUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = objectUrl;
        link.download = safeFileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(objectUrl);

        // Aggiorna lo stato UI per evitare doppio download
        this.notification.downloaded = true;

        // Se il file scaricato è una bozza generata dal worker, elimina definitivamente (hard-delete lato BE)
        // il record allegato. Il codDocumento corrisponde al filename (es. BOZZA_<sezione>_<idPiao>.pdf).
        const codDocumento =
          nameFromHeader || this.extractFileNameFromUrl(notification.downloadUrl || '');
        if (codDocumento?.startsWith('BOZZA_')) {
          this.attachmentService.deleteBozza(codDocumento).subscribe({
            next: () => {
              console.log('[NotificationItem] Bozza eliminata:', codDocumento);
              // Aggiorna in real-time la downloadUrl con il valore "impossibile"
              this.notification.downloadUrl = this.impossiblePdfDownload;
            },
            error: (err) => console.error('[NotificationItem] Errore delete bozza:', err),
          });
        }
      })
      .catch((error) => {
        console.error('Errore durante il download allegato:', error);
      });
  }

  private extractFileNameFromContentDisposition(contentDisposition: string | null): string | null {
    if (!contentDisposition) return null;
    const match = contentDisposition.match(/filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i);
    const rawName = match?.[1] || match?.[2];
    return rawName ? decodeURIComponent(rawName) : null;
  }

  private extractFileNameFromUrl(url: string): string | null {
    try {
      const parsedUrl = new URL(url);
      const lastSegment = parsedUrl.pathname.split('/').pop();
      return lastSegment ? decodeURIComponent(lastSegment) : null;
    } catch {
      return null;
    }
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
