import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../../module/shared/shared.module';
import { LabelValue } from '../../../models/interfaces/label-value';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { ModalBodyComponent } from '../../../components/modal/modal-body/modal-body.component';
import { DropdownComponent } from '../../../components/dropdown/dropdown.component';
import { ONLINE, PDF } from '../../../utils/constants';

@Component({
    selector: 'piao-modal-redigi-piao',
    imports: [SharedModule, DropdownComponent, ReactiveFormsModule],
    templateUrl: './modal-redigi-piao.component.html',
    styleUrl: './modal-redigi-piao.component.scss'
})
export class ModalRedigiPiaoComponent extends ModalBodyComponent implements OnInit {
  showChoiceNumberOfEmployees: boolean = false;
  titleDropdown: string = 'SCRIVANIA_PA.SERVIZI_PIAO.MODAL.REDIGI.DROPDOWN.TITLE';

  ngOnInit(): void {
    this.formGroup = new FormGroup({
      choice: new FormControl<string | null>(null, Validators.required),
      numberOfEmployess: new FormControl<string | null>(null),
    });
  }

  inputContent: LabelValue[] = [
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.MODAL.REDIGI.RADIO_BUTTONS.CHOICE_1',
      value: ONLINE,
      formControlName: 'choice',
    },
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.MODAL.REDIGI.RADIO_BUTTONS.CHOICE_2',
      value: PDF,
      formControlName: 'choice',
    },
  ];

  dropDown: LabelValue[] = [
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.MODAL.REDIGI.DROPDOWN.UP_50',
      value: 'plus50',
    },
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.MODAL.REDIGI.DROPDOWN.DOWN_50',
      value: 'min50',
    },
  ];

  handleChoice(value: string | boolean | undefined): void {
    const control = this.formGroup.controls['numberOfEmployess'];

    if (value === ONLINE) {
      control.addValidators([Validators.required]);
      this.showChoiceNumberOfEmployees = true;
    } else {
      control.clearValidators();
      control.reset();
      this.showChoiceNumberOfEmployees = false;
    }

    //aggiornamento del control e del form dopo aggiunta/rimozione validator
    control.updateValueAndValidity();
    this.formGroup.updateValueAndValidity();
  }
}
