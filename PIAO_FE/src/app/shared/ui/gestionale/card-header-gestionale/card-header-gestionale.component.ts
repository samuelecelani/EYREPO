import { Component, OnDestroy, OnInit } from '@angular/core';
import { CardComponent } from '../../../components/card/card.component';
import { SharedModule } from '../../../module/shared/shared.module';

@Component({
  selector: 'piao-card-header-gestionale',
  imports: [CardComponent, SharedModule],
  templateUrl: './card-header-gestionale.component.html',
  styleUrl: './card-header-gestionale.component.scss',
})
export class CardHeaderGestionaleComponent {
  titleCardHeader: string = 'GESTIONALE.CARD_HEADER.TITLE';
  subTitleCardHeader: string = 'GESTIONALE.CARD_HEADER.SUB_TITLE';
  piaoCardHeaderContainerClass: string = 'piao-card-header-servizi-piao-container';
  piaoCardHeaderBodyClass: string = 'piao-card-header-servizi-piao-body';
  piaoCardHeaderTitleClass: string = 'piao-card-header-servizi-piao-title';
  piaoCardHeaderSubTitleClass: string = 'piao-card-header-servizi-piao-subTitle';
}
