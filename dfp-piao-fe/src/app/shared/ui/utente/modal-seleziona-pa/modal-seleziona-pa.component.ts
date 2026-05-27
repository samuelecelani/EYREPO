import { Component, inject, Input, OnInit } from '@angular/core';
import { ModalBodyComponent } from '../../../components/modal/modal-body/modal-body.component';
import { SharedModule } from '../../../module/shared/shared.module';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LabelValue } from '../../../models/interfaces/label-value';

@Component({
  selector: 'piao-modal-seleziona-pa',
  imports: [SharedModule, ReactiveFormsModule],
  templateUrl: './modal-seleziona-pa.component.html',
  styleUrl: './modal-seleziona-pa.component.scss',
})
export class ModalSelezionaPaComponent extends ModalBodyComponent implements OnInit {
  title: string = 'PROFILE.MODAL_SELECT_PA.TITLE';
  sub_title: string = 'PROFILE.MODAL_SELECT_PA.SUB_TITLE';
  radio: string = 'PROFILE.MODAL_SELECT_PA.RADIO';
  main: string = 'main';

  @Input() radioPaOptions!: LabelValue[];

  fb: FormBuilder = inject(FormBuilder);

  ngOnInit(): void {
    this.createForm();
  }

  createForm() {
    const defaultValue = this.radioPaOptions?.find((x) => x.additionalField)?.value ?? null;
    this.formGroup = this.fb.group({
      radioPaOptions: [defaultValue, Validators.required],
    });
    this.formGroup.updateValueAndValidity();
  }
}
