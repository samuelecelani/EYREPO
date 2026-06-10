import { Component, Input } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';

export type TBadge = {
  variant: 'primary' | 'secondary' | 'success' | 'success-light' | 'expiry' | 'alert';
};

@Component({
    selector: 'piao-badge',
    imports: [SharedModule],
    templateUrl: './badge.component.html',
    styleUrl: './badge.component.css'
})
export class BadgeComponent {
  @Input() text: string = '';
  @Input() hasBorder: boolean = true;
  @Input() variant: TBadge['variant'] = 'primary';
}
