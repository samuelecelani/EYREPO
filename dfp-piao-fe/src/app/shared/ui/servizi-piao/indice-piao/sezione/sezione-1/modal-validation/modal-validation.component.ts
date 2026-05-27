import { Component, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { ModalBodyComponent } from '../../../../../../components/modal/modal-body/modal-body.component';

@Component({
  selector: 'piao-modal-validation',
  imports: [SharedModule],
  templateUrl: './modal-validation.component.html',
  styleUrl: './modal-validation.component.scss',
})
export class ModalValidationComponent extends ModalBodyComponent implements OnInit {
  @Input() isValidazioneSezione!: boolean;
  @Input() activeSectionId!: string;
  @Input() denominazione!: string;

  title!: string;
  subTitle!: string;
  main: string = 'main';

  ngOnInit(): void {
    if (this.isValidazioneSezione) {
      this.title = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.VALIDATION.MODAL_TWO_ACTIONS.TITLE';
      this.subTitle =
        'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.VALIDATION.MODAL_TWO_ACTIONS.SUB_TITLE';
    }
  }
}
