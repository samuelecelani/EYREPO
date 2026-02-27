import { Component, inject, OnInit } from '@angular/core';
import { ModalBodyComponent } from '../../../../../../components/modal/modal-body/modal-body.component';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { SharedModule } from '../../../../../../module/shared/shared.module';

@Component({
  selector: 'piao-modal-pubblicazione',
  imports: [SharedModule, ReactiveFormsModule],
  templateUrl: './modal-pubblicazione.component.html',
  styleUrl: './modal-pubblicazione.component.scss',
})
export class ModalPubblicazioneComponent extends ModalBodyComponent implements OnInit {
  title: string = 'APPROVAZIONE_E_PUBBLICAZIONE.MODALE_PUBBLICAZIONE.TITLE';
  subTitle: string = 'APPROVAZIONE_E_PUBBLICAZIONE.MODALE_PUBBLICAZIONE.SUB_TITLE';
  checkbox: string = 'APPROVAZIONE_E_PUBBLICAZIONE.MODALE_PUBBLICAZIONE.CHECKBOX';

  fb: FormBuilder = inject(FormBuilder);

  ngOnInit(): void {
    this.createForm();
  }

  createForm() {
    this.formGroup = this.fb.group({
      checkbox: [false, Validators.requiredTrue],
    });
  }
}
