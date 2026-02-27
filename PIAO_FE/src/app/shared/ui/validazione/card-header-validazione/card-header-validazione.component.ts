import { Component } from '@angular/core';
import { CardComponent } from '../../../components/card/card.component';

@Component({
  selector: 'piao-card-header-validazione',
  imports: [CardComponent],
  templateUrl: './card-header-validazione.component.html',
  styleUrl: './card-header-validazione.component.scss',
})
export class CardHeaderValidazioneComponent {
  titleCardHeader: string = 'VALIDAZIONE.TITLE';
  subTitleCardHeader: string = 'VALIDAZIONE.SUB_TITLE';
  piaoCardHeaderContainerClass: string = 'piao-card-header-servizi-piao-container';
  piaoCardHeaderBodyClass: string = 'piao-card-header-servizi-piao-body';
  piaoCardHeaderTitleClass: string = 'piao-card-header-servizi-piao-title';
  piaoCardHeaderSubTitleClass: string = 'piao-card-header-servizi-piao-subTitle';
}
