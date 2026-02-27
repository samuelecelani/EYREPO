import { Component, inject, OnInit } from '@angular/core';
import { SharedModule } from '../../shared/module/shared/shared.module';
import { CardHeaderScrivaniaPAComponent } from '../../shared/ui/scrivania-pa/card-header-scrivania-pa/card-header-scrivania-pa.component';
import { NovitaScrivaniaPAComponent } from '../../shared/ui/scrivania-pa/novita-scrivania-pa/novita-scrivania-pa.component';
import { AvvisiScrivaniaPAComponent } from '../../shared/ui/scrivania-pa/avvisi-scrivania-pa/avvisi-scrivania-pa.component';
import { IndicePiaoScrivaniaPAComponent } from '../../shared/ui/scrivania-pa/indice-piao-scrivania-pa/indice-piao-scrivania-pa.component';
import { CardAlertComponent } from '../../shared/ui/card-alert/card-alert.component';
import { KEY_PIAO, WARNING_ICON } from '../../shared/utils/constants';
import { SessionStorageService } from '../../shared/services/session-storage.service';

@Component({
  selector: 'piao-scrivania-pa',
  imports: [
    SharedModule,
    CardHeaderScrivaniaPAComponent,
    NovitaScrivaniaPAComponent,
    AvvisiScrivaniaPAComponent,
    IndicePiaoScrivaniaPAComponent,
    CardAlertComponent,
  ],
  templateUrl: './scrivania-pa.component.html',
  styleUrls: ['./scrivania-pa.component.scss'],
})
export class ScrivaniaPAComponent implements OnInit {
  sessionStorageService: SessionStorageService = inject(SessionStorageService);

  ngOnInit(): void {
    this.sessionStorageService.removeItem(KEY_PIAO);
  }

  /*CARD ALERT*/
  icon: string = WARNING_ICON;
  titleCardHeader: string = 'SCRIVANIA_PA.ALERT.TITLE';
  subTitleCardHeader: string = 'SCRIVANIA_PA.ALERT.SUB_TITLE';
  secondSubTitleCardHeader: string = 'SCRIVANIA_PA.ALERT.SECOND_SUB_TITLE';
  textHrefCardHeader: string = 'SCRIVANIA_PA.ALERT.TEXT_HREF';
  href: string = '/pages/area-privata-PA/mancata-compilazione';
}
