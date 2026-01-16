import { Component } from '@angular/core';
import { SharedModule } from '../../../../../../../shared/module/shared/shared.module';
import { CardInfoComponent } from '../../../../../../../shared/ui/card-info/card-info.component';
import { WARNING_ICON } from '../../../../../../../shared/utils/constants';
import { AccordionComponent } from '../../../../../../../shared/components/accordion/accordion.component';
import { BodyTableMinervaSezioneComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-3.1.1/body-table-minerva-sezione/body-table-minerva-sezione.component';

@Component({
    selector: 'piao-sezione-3-3-1',
    imports: [SharedModule, CardInfoComponent, AccordionComponent, BodyTableMinervaSezioneComponent],
    templateUrl: './sezione3.3.1.component.html',
    styleUrl: './sezione3.3.1.component.scss'
})
export class Sezione331Component {
  title: string = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE-3.3.1.TITLE';
  subTitle: string = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE-3.3.1.SUB_TITLE';

  /*CARD-INFO*/
  icon: string = WARNING_ICON;
  subTitleCardInfo: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE-3.3.1.CARD_INFO_MINERVA.SUB_TITLE';
  showButtonMinerva: boolean = true;
  showIconButton: boolean = true;
  hrefButtonMinerva: string = 'https://www.google.com';
  titleButtonMinerva: string = 'BUTTONS.GO_TO_MINERVA';

  /*TAB MINERVA */
  titleTab1: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE-3.3.1.ACCORDION.1.TAB_1';
  titleTab2: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE-3.3.1.ACCORDION.1.TAB_2';
}
