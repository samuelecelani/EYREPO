import { Component, inject } from '@angular/core';
import { SharedModule } from '../../../shared/module/shared/shared.module';
import { CardHeaderServiziPiaoComponent } from '../../../shared/ui/servizi-piao/card-header-servizi-piao/card-header-servizi-piao.component';
import { CardAlertComponent } from '../../../shared/ui/card-alert/card-alert.component';
import { WARNING_ICON } from '../../../shared/utils/constants';
import { CardActionServiziPiaoComponent } from '../../../shared/ui/servizi-piao/card-action-servizi-piao/card-action-servizi-piao.component';
import { PiaoPeriodoCompilazioneDirective } from '../../../shared/directives/piao-periodo-compilazione.directive';
import { AssetService } from '../../../shared/services/asset.service';

@Component({
  selector: 'piao-servizi-piao',
  imports: [
    SharedModule,
    CardHeaderServiziPiaoComponent,
    CardAlertComponent,
    CardActionServiziPiaoComponent,
    PiaoPeriodoCompilazioneDirective,
  ],
  templateUrl: './servizi-piao.component.html',
  styleUrl: './servizi-piao.component.scss',
})
export class ServiziPiaoComponent {
  protected readonly asset = inject(AssetService);

  /*CARD ALERT*/

  icon: string = WARNING_ICON;
  titleCardHeader: string = 'SCRIVANIA_PA.ALERT.TITLE';
  subTitleCardHeader: string = 'SCRIVANIA_PA.ALERT.SUB_TITLE';
  secondSubTitleCardHeader: string = 'SCRIVANIA_PA.ALERT.SECOND_SUB_TITLE';
  textHrefCardHeader: string = 'SCRIVANIA_PA.ALERT.TEXT_HREF';
  href: string = '/area-privata-PA/mancata-compilazione';
}
