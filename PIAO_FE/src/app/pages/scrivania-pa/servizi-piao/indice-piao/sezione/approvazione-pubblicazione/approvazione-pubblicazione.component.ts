import { Component, inject, Input, OnInit } from '@angular/core';
import { InviaRichiestaApprovazioneComponent } from './invia-richiesta-approvazione/invia-richiesta-approvazione.component';
import { BaseComponent } from '../../../../../../shared/components/base/base.component';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SharedModule } from '../../../../../../shared/module/shared/shared.module';
import { TextBoxComponent } from '../../../../../../shared/components/text-box/text-box.component';
import { DatePickerComponent } from '../../../../../../shared/components/date-picker/date-picker.component';
import { DATE_REGEX, URL_REGEX } from '../../../../../../shared/utils/constants';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';
import { ISezioneBase } from '../../../../../../shared/models/interfaces/sezione-base.interface';
import { Observable, of } from 'rxjs';
import { areAllValuesNull } from '../../../../../../shared/utils/utils';
import { PIAODTO } from '../../../../../../shared/models/classes/piao-dto';

@Component({
  selector: 'piao-approvazione-pubblicazione',
  imports: [
    SharedModule,
    ReactiveFormsModule,
    InviaRichiestaApprovazioneComponent,
    DatePickerComponent,
    TextBoxComponent,
  ],
  templateUrl: './approvazione-pubblicazione.component.html',
  styleUrl: './approvazione-pubblicazione.component.scss',
})
export class ApprovazionePubblicazioneComponent
  extends BaseComponent
  implements OnInit, ISezioneBase
{
  @Input() piaoDTO!: PIAODTO;
  isFormReady = false;

  title: string = 'APPROVAZIONE_E_PUBBLICAZIONE.CARICA_APPROVAZIONE.TITLE';
  subtitle: string = 'APPROVAZIONE_E_PUBBLICAZIONE.CARICA_APPROVAZIONE.SUB_TITLE';
  data: string = 'APPROVAZIONE_E_PUBBLICAZIONE.CARICA_APPROVAZIONE.DATA_APPROVAZIONE_LABEL';
  url: string = 'APPROVAZIONE_E_PUBBLICAZIONE.CARICA_APPROVAZIONE.URL_PIAO_LABEL';

  today = new Date().toISOString().split('T')[0];

  fb: FormBuilder = inject(FormBuilder);
  form!: FormGroup;

  getForm(): FormGroup {
    return this.form;
  }
  isFormValid(): boolean {
    return this.form.valid;
  }
  hasFormValues(): boolean {
    return !areAllValuesNull(this.form);
  }
  prepareDataForSave() {
    console.log('In prepare data');
  }
  validate(): Observable<any> {
    console.log('In prepare data');
    return of([]);
  }
  resetForm(): void {
    this.form.reset();
  }
  getSectionStatus(): string {
    return '';
  }

  ngOnInit(): void {
    this.createForm();
  }

  createForm() {
    this.form = this.fb.group({
      data: this.fb.control<Date | null>(null, [
        Validators.required,
        Validators.pattern(DATE_REGEX),
      ]),
      url: this.fb.control<string | null>(null, [
        Validators.required,
        Validators.pattern(URL_REGEX),
        Validators.maxLength(100),
      ]),
    });
  }
}
