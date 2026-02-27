import { Component } from '@angular/core';
import { CardComponent } from '../../../components/card/card.component';

@Component({
    selector: 'piao-card-header-novita-pa',
    imports: [CardComponent],
    templateUrl: './card-header-novita-pa.component.html',
    styleUrl: './card-header-novita-pa.component.scss'
})
export class CardHeaderNovitaPAComponent {
  titleCardHeader: string = 'NOVITA_PA.TITLE';
  subTitleCardHeader: string = 'NOVITA_PA.SUB_TITLE';
  piaoCardHeaderContainerClass: string = 'piao-card-header-novita-pa-container';
  piaoCardHeaderBodyClass: string = 'piao-card-header-novita-pa-body';
  piaoCardHeaderTitleClass: string = 'piao-card-header-novita-pa-title';
  piaoCardHeaderSubTitleClass: string = 'piao-card-header-novita-pa-subTitle';
}
