import { Component } from '@angular/core';
import { SharedModule } from '../../../module/shared/shared.module';

@Component({
  selector: 'piao-card-header-servizi-piao',
  imports: [SharedModule],
  templateUrl: './card-header-servizi-piao.component.html',
  styleUrl: './card-header-servizi-piao.component.scss',
})
export class CardHeaderServiziPiaoComponent {
  titleCardHeader: string = 'SCRIVANIA_PA.SERVIZI_PIAO.TITLE';
  subTitleCardHeader: string = 'SCRIVANIA_PA.SERVIZI_PIAO.SUB_TITLE';
}
