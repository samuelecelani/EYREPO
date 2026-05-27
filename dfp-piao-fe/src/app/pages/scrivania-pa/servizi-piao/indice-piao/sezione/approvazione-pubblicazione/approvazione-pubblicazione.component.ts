import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { InviaRichiestaApprovazioneComponent } from './invia-richiesta-approvazione/invia-richiesta-approvazione.component';
import { BaseComponent } from '../../../../../../shared/components/base/base.component';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SharedModule } from '../../../../../../shared/module/shared/shared.module';
import { TextBoxComponent } from '../../../../../../shared/components/text-box/text-box.component';
import { DatePickerComponent } from '../../../../../../shared/components/date-picker/date-picker.component';
import { DATE_REGEX, INPUT_REGEX, URL_REGEX } from '../../../../../../shared/utils/constants';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';
import { ISezioneBase } from '../../../../../../shared/models/interfaces/sezione-base.interface';
import { Observable, of, Subscription, takeUntil } from 'rxjs';
import { areAllValuesNull, minArrayLength } from '../../../../../../shared/utils/utils';
import { PIAODTO } from '../../../../../../shared/models/classes/piao-dto';
import { AttachmentComponent } from '../../../../../../shared/ui/attachment/attachment.component';
import { CodTipologiaAllegatoEnum } from '../../../../../../shared/models/enums/cod-tipologia-allegato.enum';
import { CodTipologiaSezioneEnum } from '../../../../../../shared/models/enums/cod-tipologia-sezione.enum';
import { ApprovazioneService } from '../../../../../../shared/services/approvazione.service';
import { ApprovazioneDTO } from '../../../../../../shared/models/classes/approvazione-dto';
import { SectionEnum } from '../../../../../../shared/models/enums/section.enum';

@Component({
  selector: 'piao-approvazione-pubblicazione',
  imports: [
    SharedModule,
    ReactiveFormsModule,
    InviaRichiestaApprovazioneComponent,
    DatePickerComponent,
    TextBoxComponent,
    AttachmentComponent,
  ],
  templateUrl: './approvazione-pubblicazione.component.html',
  styleUrl: './approvazione-pubblicazione.component.scss',
})
export class ApprovazionePubblicazioneComponent
  extends BaseComponent
  implements OnInit, ISezioneBase
{
  @Input() piaoDTO!: PIAODTO;
  @Output() formValueChanged = new EventEmitter<any>();
  @Output() richiestaApprovazioneSaved = new EventEmitter<void>();
  @Input() testoSezione!: string;
  @Input() approvazioneData!: ApprovazioneDTO;
  @Input() isDettaglio: boolean = false;

  isFormReady = false;

  title: string = 'APPROVAZIONE_E_PUBBLICAZIONE.CARICA_APPROVAZIONE.TITLE';
  titleDettaglio: string = 'APPROVAZIONE_E_PUBBLICAZIONE.CARICA_APPROVAZIONE.TITLE_DETTAGLIO';
  subtitle: string = 'APPROVAZIONE_E_PUBBLICAZIONE.CARICA_APPROVAZIONE.SUB_TITLE';
  subtitleDettaglio: string =
    'APPROVAZIONE_E_PUBBLICAZIONE.CARICA_APPROVAZIONE.SUB_TITLE_DETTAGLIO';
  data: string = 'APPROVAZIONE_E_PUBBLICAZIONE.CARICA_APPROVAZIONE.DATA_APPROVAZIONE_LABEL';
  dataDettaglio: string =
    'APPROVAZIONE_E_PUBBLICAZIONE.CARICA_APPROVAZIONE.DATA_APPROVAZIONE_LABEL_DETTAGLIO';
  url: string = 'APPROVAZIONE_E_PUBBLICAZIONE.CARICA_APPROVAZIONE.URL_PIAO_LABEL';

  today = new Date().toISOString().split('T')[0];

  fb: FormBuilder = inject(FormBuilder);
  form!: FormGroup;

  sezione: string = SectionEnum.SEZIONE_APPROVAZIONE_PUBBLICAZIONE;
  codTipologia: string = SectionEnum.SEZIONE_APPROVAZIONE_PUBBLICAZIONE;
  codTipologiaAllegato: string = CodTipologiaAllegatoEnum.APPROVAZIONE_PUBBLICAZIONE;

  approvazioneService: ApprovazioneService = inject(ApprovazioneService);

  formValueBackup: any = null;

  ngOnInit(): void {
    this.loadApprovazione();

    this.approvazioneService.onApprovazioneUpdated$
      .pipe(takeUntil(this.destroy$))
      .subscribe((approvazione) => {
        if (approvazione) {
          this.approvazioneData = approvazione;
          this.reloadForm(approvazione);
        }
      });
  }

  private loadApprovazione() {
    this.approvazioneService.getApprovazione(this.piaoDTO.id!).subscribe({
      next: (res: any) => {
        if (res) {
          this.createForm();
          this.form.patchValue({
            data: res.data ?? null,
            url: res.url ?? null,
          });
          this.formValueBackup = structuredClone(this.form.value);
        } else {
          this.createForm();
        }
        this.isFormReady = true;
      },
      error: (err) => {
        console.error("Errore nel recupero dell'approvazione", err);
        this.createForm();
        this.isFormReady = true;
      },
    });
  }

  /**
   * Non applicabile alla sezione di approvazione/pubblicazione.
   */
  loadFromPreviousPiao(_previousPiaoId: number): Observable<boolean> {
    // no-op: la sezione di approvazione non supporta il recupero dal PIAO precedente
    return of(false);
  }

  reloadForm(approvazione: ApprovazioneDTO) {
    if (this.form) {
      this.form.patchValue({
        data: approvazione.data ?? null,
        url: approvazione.url ?? null,
      });
      this.formValueBackup = structuredClone(this.form.value);
    }
  }

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
    const formValue = this.form.value;
    return {
      idPiao: this.piaoDTO.id,
      data: formValue.data,
      url: formValue.url,
    } as ApprovazioneDTO;
  }
  validate(): Observable<any> {
    return of([]);
  }

  resetForm(): void {
    this.form.reset();
  }

  getSectionStatus(): string {
    return '';
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
      allegati: this.fb.control<any[]>([], [minArrayLength(1), Validators.required]),
    });
    this.isFormReady = true;
  }

  override ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
