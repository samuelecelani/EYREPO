import { Component } from '@angular/core';
import { SharedModule } from '../../../module/shared/shared.module';

@Component({
  selector: 'piao-card-header-gestionale',
  imports: [SharedModule],
  templateUrl: './card-header-gestionale.component.html',
  styleUrl: './card-header-gestionale.component.scss',
})
export class CardHeaderGestionaleComponent {
  titleCardHeader: string = 'GESTIONALE.CARD_HEADER.TITLE';
  subTitleCardHeader: string = 'GESTIONALE.CARD_HEADER.SUB_TITLE';
}
