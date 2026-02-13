import { Component, EventEmitter, Input, Output } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { CardConfig } from '../../models/classes/config/card-config';
import { SvgComponent } from '../svg/svg.component';

@Component({
    selector: 'piao-card',
    imports: [SharedModule, SvgComponent],
    templateUrl: './card.component.html',
    styleUrl: './card.component.scss'
})
export class CardComponent {
  @Input() title!: string;
  @Input() subTitle!: string;
  @Input() secondSubTitle!: string;
  @Input() textHref!: string;
  @Input() href!: string;
  @Input() piaoCardContainer!: string;
  @Input() icon!: string;
  @Input() piaoCardBody!: string;
  @Input() piaoCardTitle!: string;
  @Input() piaoCardSubTitle!: string;
  @Input() disabled = false;
  @Input() isClickable = false;
  @Input() backgroundImg!: string;
  @Input() fillColorIcon!: string;
  @Output() cardClick = new EventEmitter<void>();
  currentYear = new Date().getFullYear();

  onCardClick() {
    if (!this.disabled) {
      this.cardClick.emit();
    }
  }
}
