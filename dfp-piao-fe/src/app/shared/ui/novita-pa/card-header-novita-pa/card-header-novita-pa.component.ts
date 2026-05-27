import { Component } from '@angular/core';
import { SharedModule } from '../../../module/shared/shared.module';

@Component({
  selector: 'piao-card-header-novita-pa',
  imports: [SharedModule],
  templateUrl: './card-header-novita-pa.component.html',
  styleUrl: './card-header-novita-pa.component.scss',
})
export class CardHeaderNovitaPAComponent {
  titleCardHeader: string = 'NOVITA_PA.TITLE';
  subTitleCardHeader: string = 'NOVITA_PA.SUB_TITLE';
}
