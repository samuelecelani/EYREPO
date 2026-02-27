import { Component, inject, OnInit } from '@angular/core';
import { SharedModule } from '../../../../../../../shared/module/shared/shared.module';
import { TextBoxComponent } from '../../../../../../../shared/components/text-box/text-box.component';
import { TextAreaComponent } from '../../../../../../../shared/components/text-area/text-area.component';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonComponent } from '../../../../../../../shared/components/button/button.component';
import { BaseComponent } from '../../../../../../../shared/components/base/base.component';

@Component({
  selector: 'piao-invia-richiesta-approvazione',
  imports: [
    SharedModule,
    ReactiveFormsModule,
    TextBoxComponent,
    TextAreaComponent,
    ButtonComponent,
  ],
  templateUrl: './invia-richiesta-approvazione.component.html',
  styleUrl: './invia-richiesta-approvazione.component.scss',
})
export class InviaRichiestaApprovazioneComponent extends BaseComponent implements OnInit {
  title: string = 'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.TITLE';
  subtitle: string = 'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.SUB_TITLE';
  emailLabel: string = 'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.EMAIL_LABEL';
  oggettoLabel: string =
    'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.EMAIL_OBJECT_LABEL';
  testoLabel: string = 'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.EMAIL_TEXT_LABEL';
  buttonLabel: string = 'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.BUTTON_LABEL';

  fb: FormBuilder = inject(FormBuilder);
  form!: FormGroup;

  ngOnInit(): void {
    this.createForm();
  }

  createForm() {
    this.form = this.fb.group({
      email: this.fb.control<string | null>(null, [
        Validators.maxLength(50),
        Validators.email,
        Validators.required,
      ]),
      oggetto: this.fb.control<string | null>(null, [
        Validators.maxLength(50),
        Validators.required,
      ]),
      testo: this.fb.control<string | null>(null, [
        Validators.maxLength(2000),
        Validators.required,
      ]),
    });
  }

  handleInviaRichiestaValidazione() {
    console.log('Method not implemented.');
  }
}
