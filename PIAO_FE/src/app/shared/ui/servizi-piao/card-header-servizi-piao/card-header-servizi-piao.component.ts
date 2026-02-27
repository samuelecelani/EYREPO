import { Component } from '@angular/core';
import { CardComponent } from '../../../components/card/card.component';

@Component({
    selector: 'piao-card-header-servizi-piao',
    imports: [CardComponent],
    templateUrl: './card-header-servizi-piao.component.html',
    styleUrl: './card-header-servizi-piao.component.scss'
})
export class CardHeaderServiziPiaoComponent {
  titleCardHeader: string = 'SCRIVANIA_PA.SERVIZI_PIAO.TITLE';
  subTitleCardHeader: string = 'SCRIVANIA_PA.SERVIZI_PIAO.SUB_TITLE';
  piaoCardHeaderContainerClass: string = 'piao-card-header-servizi-piao-container';
  piaoCardHeaderBodyClass: string = 'piao-card-header-servizi-piao-body'
  piaoCardHeaderTitleClass: string = 'piao-card-header-servizi-piao-title'
  piaoCardHeaderSubTitleClass: string = 'piao-card-header-servizi-piao-subTitle'
}
