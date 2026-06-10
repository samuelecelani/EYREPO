import { Component } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { getValue } from '../../../../../../config/loader-config';

@Component({
  selector: 'piao-modal-not-found-data-minerva',
  imports: [SharedModule],
  templateUrl: './modal-not-found-data-minerva.component.html',
  styleUrl: './modal-not-found-data-minerva.component.scss',
})
export class ModalNotFoundDataMinervaComponent {
  link: string = getValue('publicUrlMinerva') ?? '';
}
