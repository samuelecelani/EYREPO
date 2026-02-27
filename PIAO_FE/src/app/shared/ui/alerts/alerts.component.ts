import { Component, Input } from '@angular/core';
import { BadgeComponent, TBadge } from '../../components/badge/badge.component';
import { SharedModule } from '../../module/shared/shared.module';

export interface IAlert {
  id: number | string;
  badgeText: string;
  badgeVariant: TBadge['variant'];
  title: string;
  description: string;
  date: string;
  visualizzato: boolean;
}

@Component({
    selector: 'piao-alerts',
    imports: [SharedModule, BadgeComponent],
    templateUrl: './alerts.component.html',
    styleUrl: './alerts.component.css'
})
//TODO: Refactor
export class AlertsComponent {
  @Input() badgeText: string = '';
  @Input() badgeVariant: TBadge['variant'] = 'primary';
  @Input() title: string = '';
  @Input() description: string = '';
  @Input() date: string = '';
  @Input() visualizzato: boolean = false;

  // @Input() alert: IAlert

  /**
   * Calcola la data relativa in formato "Oggi", "Ieri", "X giorni fa"
   */
  getRelativeDate(): string {
    if (!this.date) {
      return '';
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const createdDate = new Date(this.date);
    createdDate.setHours(0, 0, 0, 0);

    const diffTime = today.getTime() - createdDate.getTime();
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays === 0) {
      return 'Oggi';
    } else if (diffDays === 1) {
      return 'Ieri';
    } else {
      return `${diffDays} giorni fa`;
    }
  }
}
