import { Component } from '@angular/core';
import { SharedModule } from '../../../module/shared/shared.module';

@Component({
  selector: 'piao-card-header-validazione',
  imports: [SharedModule],
  templateUrl: './card-header-validazione.component.html',
  styleUrl: './card-header-validazione.component.scss',
})
export class CardHeaderValidazioneComponent {
  titleCardHeader: string = 'VALIDAZIONE.TITLE';
  subTitleCardHeader: string = 'VALIDAZIONE.SUB_TITLE';
}
