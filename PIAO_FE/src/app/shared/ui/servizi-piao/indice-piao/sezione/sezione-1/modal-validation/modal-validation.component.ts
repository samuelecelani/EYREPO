import { Component, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { ModalBodyComponent } from '../../../../../../components/modal/modal-body/modal-body.component';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { LabelValue } from '../../../../../../models/interfaces/label-value';

@Component({
  selector: 'piao-modal-validation',
  imports: [SharedModule, ReactiveFormsModule],
  templateUrl: './modal-validation.component.html',
  styleUrl: './modal-validation.component.scss',
})
export class ModalValidationComponent extends ModalBodyComponent implements OnInit {
  @Input() isValidazioneSezione!: boolean;
  @Input() activeSectionId!: string;

  title!: string;
  subTitle!: string;

  inputContent: LabelValue[] = [
    {
      label:
        'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.VALIDATION.MODAL_TWO_ACTIONS.RADIO_BUTTONS.CHOICE_1',
      value: 'RV',
      formControlName: 'choice',
    },
    /*
      {
        label: 'SCRIVANIA_PA.SERVIZI_PIAO.MODAL.REDIGI.RADIO_BUTTONS.CHOICE_2',
        value: PDF,
        formControlName: 'choice',
      },
      */
  ];

  ngOnInit(): void {
    this.formGroup = new FormGroup({
      choice: new FormControl<string | null>(null, Validators.required),
    });

    if (this.isValidazioneSezione) {
      this.title = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.VALIDATION.MODAL_TWO_ACTIONS.TITLE';
      this.subTitle =
        'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.VALIDATION.MODAL_TWO_ACTIONS.SUB_TITLE';
    }
  }
}
