import { Component } from '@angular/core';
import { SharedModule } from '../../../shared/module/shared/shared.module';
import { CardHeaderServiziPiaoComponent } from '../../../shared/ui/servizi-piao/card-header-servizi-piao/card-header-servizi-piao.component';
import { CardAlertComponent } from '../../../shared/ui/card-alert/card-alert.component';
import { WARNING_ICON } from '../../../shared/utils/constants';
import { CardActionServiziPiaoComponent } from '../../../shared/ui/servizi-piao/card-action-servizi-piao/card-action-servizi-piao.component';

@Component({
  selector: 'piao-servizi-piao',
  imports: [
    SharedModule,
    CardHeaderServiziPiaoComponent,
    CardAlertComponent,
    CardActionServiziPiaoComponent,
  ],
  templateUrl: './servizi-piao.component.html',
  styleUrl: './servizi-piao.component.scss',
})
export class ServiziPiaoComponent {
  /*CARD ALERT*/

  icon: string = WARNING_ICON;
  titleCardHeader: string = 'SCRIVANIA_PA.ALERT.TITLE';
  subTitleCardHeader: string = 'SCRIVANIA_PA.ALERT.SUB_TITLE';
  secondSubTitleCardHeader: string = 'SCRIVANIA_PA.ALERT.SECOND_SUB_TITLE';
  textHrefCardHeader: string = 'SCRIVANIA_PA.ALERT.TEXT_HREF';
  href: string = '/pages/area-privata-PA/mancata-compilazione';
}
