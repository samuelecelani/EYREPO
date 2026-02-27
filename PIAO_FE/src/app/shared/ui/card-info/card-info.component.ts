import { Component, EventEmitter, Input, Output } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { CardComponent } from '../../components/card/card.component';
import { ButtonComponent } from '../../components/button/button.component';
import { SvgComponent } from '../../components/svg/svg.component';

@Component({
  selector: 'piao-card-info',
  imports: [SharedModule, CardComponent, ButtonComponent, SvgComponent],
  templateUrl: './card-info.component.html',
  styleUrl: './card-info.component.scss',
})
export class CardInfoComponent {
  @Input() icon!: string;
  @Input() piaoCardHeaderContainerClass: string = 'piao-card-info-container';
  piaoCardHeaderBodyClass: string = 'piao-card-alert-body';
  @Input() piaoCardHeaderTitleClass: string = 'piao-card-alert-title';
  @Input() titleCardHeader: string = 'SCRIVANIA_PA.ALERT.TITLE';
  @Input() subTitleCardHeader!: string;
  @Input() showButton!: boolean;
  @Input() showIconButton!: boolean;
  @Input() titleButton!: string;
  @Input() hrefButton!: string;
  @Input() loadNewForm: boolean = false;
  @Input() disabledButton: boolean = false;
  @Input() titleLoadNewForm!: string;
  @Output() clicked: EventEmitter<void> = new EventEmitter<void>();
  fillColor: string = '#0066CC';

  handleClickButton() {
    if (this.hrefButton) {
      window.open(this.hrefButton, '_blank');
    }
  }

  handleLoadNewForm() {
    this.clicked.emit();
  }
}
