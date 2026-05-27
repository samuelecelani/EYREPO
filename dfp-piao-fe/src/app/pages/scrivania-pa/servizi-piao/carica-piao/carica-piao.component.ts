import {
  Component,
  inject,
  OnDestroy,
  OnInit,
  ChangeDetectorRef,
  ChangeDetectionStrategy,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subject, BehaviorSubject } from 'rxjs';
import { forkJoin, takeUntil } from 'rxjs';
import { SharedModule } from '../../../../shared/module/shared/shared.module';
import { DatePickerComponent } from '../../../../shared/components/date-picker/date-picker.component';
import { TextBoxComponent } from '../../../../shared/components/text-box/text-box.component';
import { DropdownComponent } from '../../../../shared/components/dropdown/dropdown.component';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { ModalComponent } from '../../../../shared/components/modal/modal.component';
import { BaseComponent } from '../../../../shared/components/base/base.component';
import { CaricaPiaoDTO } from '../../../../shared/models/classes/carica-piao-dto';
import { LabelValue } from '../../../../shared/models/interfaces/label-value';
import { getTodayISO, minArrayLength } from '../../../../shared/utils/utils';
import {
  DATE_REGEX,
  INPUT_REGEX,
  KEY_PIAO,
  ONLINE,
  ORDINARIO,
  PDF,
  SEMPLIFICATO,
  SHAPE_ICON,
  URL_REGEX,
} from '../../../../shared/utils/constants';
import { CodTipologiaAllegatoEnum } from '../../../../shared/models/enums/cod-tipologia-allegato.enum';
import { SectionEnum } from '../../../../shared/models/enums/section.enum';
import { SessionStorageService } from '../../../../shared/services/session-storage.service';
import { PIAODTO } from '../../../../shared/models/classes/piao-dto';
import { AttachmentComponent } from '../../../../shared/ui/attachment/attachment.component';
import { PIAOService } from '../../../../shared/services/piao.service';
import { AutoritaApprovatoreDTO } from '../../../../shared/models/classes/autorita-approvatore-dto';
import { AllegatoDTO } from '../../../../shared/models/classes/allegato-dto';
import { PiaoStatusEnum } from '../../../../shared/models/enums/piao-status.enum';
import { CardInfoComponent } from '../../../../shared/ui/card-info/card-info.component';

@Component({
  selector: 'piao-carica-piao',
  imports: [
    SharedModule,
    ReactiveFormsModule,
    DatePickerComponent,
    TextBoxComponent,
    DropdownComponent,
    ButtonComponent,
    ModalComponent,
    AttachmentComponent,
    CardInfoComponent,
  ],
  templateUrl: './carica-piao.component.html',
  styleUrl: './carica-piao.component.scss',
})
export class CaricaPiaoComponent extends BaseComponent implements OnInit, OnDestroy {
  private unsubscribe$ = new Subject<void>();
  private cdr: ChangeDetectorRef = inject(ChangeDetectorRef);
  private piaoService: PIAOService = inject(PIAOService);
  //* isUpdateMode è true quando c'è già un PIAO caricato e si vuole aggiornarlo, è false quando invece si vuole caricare un nuovo PIAO
  private isUpdateMode$ = new BehaviorSubject<boolean>(false);

  // Flag impostato dalla rotta di revisione tramite router state
  isRevisione: boolean = false;

  constructor() {
    super();
    // Leggo lo state direttamente da `history.state` (API nativa del browser,
    // valorizzata da Angular Router quando si naviga con `state`). Funziona
    // in modo affidabile in Edge (legacy e Chromium) ed evita le API
    // deprecate di `Router.getCurrentNavigation()`.
    const navState = typeof history !== 'undefined' ? history.state : null;
    this.isRevisione = !!navState?.['isRevisione'];
  }

  // Labels
  title: string = 'PIAO_PDF.TITLE';
  subTitle: string = 'PIAO_PDF.SUB_TITLE';
  dataApprovazioneLabel: string = 'PIAO_PDF.DATA_APPROVAZIONE_LABEL';
  dataApprovazioneLabelTooltip: string = 'PIAO_PDF.DATA_APPROVAZIONE_LABEL_TOOLTIP';
  autoritaLabel: string = 'PIAO_PDF.AUTORITA_LABEL';
  estremiAttoLabel: string = 'PIAO_PDF.ESTREMI_ATTO_LABEL';
  triennioLabel: string = 'PIAO_PDF.TRIENNIO_LABEL';
  numDipendentiLabel: string = 'PIAO_PDF.NUM_DIPENDENTI_LABEL';
  urlLabel: string = 'PIAO_PDF.URL_LABEL';

  form!: FormGroup;
  fb: FormBuilder = inject(FormBuilder);

  minDate: string = getTodayISO();
  iconModal: string = SHAPE_ICON;
  iconStyle: string = 'icon-modal';

  caricaPiaoDTO!: CaricaPiaoDTO;
  openModal: boolean = false;

  piaoDTO?: PIAODTO;
  idPiao?: number;

  codTipologia: SectionEnum = SectionEnum.PIAO;

  codTipologiaAllegatoApprovazione: CodTipologiaAllegatoEnum =
    CodTipologiaAllegatoEnum.APPROVAZIONE_PUBBLICAZIONE;
  codTipologiaAllegatoPiao: CodTipologiaAllegatoEnum = CodTipologiaAllegatoEnum.PIAO;
  codTipologiaAllegatoExtra: CodTipologiaAllegatoEnum =
    CodTipologiaAllegatoEnum.ULTERIORI_ALLEGATI_PIAO;

  autoritaOptions: LabelValue[] = [];

  triennioOptions: LabelValue[] = [];

  numDipendentiOptions: LabelValue[] = [
    { label: 'SCRIVANIA_PA.SERVIZI_PIAO.MODAL.REDIGI.DROPDOWN.UP_50', value: ORDINARIO },
    { label: 'SCRIVANIA_PA.SERVIZI_PIAO.MODAL.REDIGI.DROPDOWN.DOWN_50', value: SEMPLIFICATO },
  ];

  isDettaglio: boolean = false;

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
    if (this.piaoDTO) {
      this.isUpdateMode = this.piaoDTO.aggiornamento ?? false;
      this.idPiao = this.piaoDTO?.id;

      this.isDettaglio =
        !this.hasFunzionalita('PIAO_CTA_DETAILS_EDITING') ||
        this.piaoDTO.statoPiao === PiaoStatusEnum.PUBBLICATO;
    }
    this.initializeForm();
    this.loadDropdownOptions();
  }

  get isUpdateMode(): boolean {
    return this.isUpdateMode$.value;
  }

  set isUpdateMode(value: boolean) {
    this.isUpdateMode$.next(value);
    this.cdr.markForCheck();
  }

  get piaoPageLabels(): Record<string, string> {
    const labels = {
      title: this.isRevisione
        ? 'PIAO_PDF.REVISIONE.TITLE'
        : this.isUpdateMode
          ? 'PIAO_PDF.AGGIORNA.TITLE'
          : 'PIAO_PDF.CARICA.TITLE',
      subtitle: this.isRevisione
        ? 'PIAO_PDF.REVISIONE.SUB_TITLE'
        : this.isUpdateMode
          ? 'PIAO_PDF.AGGIORNA.SUB_TITLE'
          : 'PIAO_PDF.CARICA.SUB_TITLE',
    };
    return labels;
  }

  get piaoAttachmentLabels(): Record<string, string> {
    return {
      title: this.isRevisione
        ? 'PIAO_PDF.ATTACHMENTS.PIAO_PDF.REVISIONE.TITLE'
        : this.isUpdateMode
          ? 'PIAO_PDF.ATTACHMENTS.PIAO_PDF.AGGIORNA.TITLE'
          : 'PIAO_PDF.ATTACHMENTS.PIAO_PDF.CARICA.TITLE',
      load: this.isRevisione
        ? 'PIAO_PDF.ATTACHMENTS.PIAO_PDF.REVISIONE.LOAD'
        : this.isUpdateMode
          ? 'PIAO_PDF.ATTACHMENTS.PIAO_PDF.AGGIORNA.LOAD'
          : 'PIAO_PDF.ATTACHMENTS.PIAO_PDF.CARICA.LOAD',
    };
  }

  get approvazioneAttachmentLabels(): Record<string, string> {
    return {
      title: this.isRevisione
        ? 'PIAO_PDF.ATTACHMENTS.APPROVAL.REVISIONE.TITLE'
        : this.isUpdateMode
          ? 'PIAO_PDF.ATTACHMENTS.PIAO_PDF.CARICA.TITLE'
          : 'PIAO_PDF.ATTACHMENTS.PIAO_PDF.CARICA.TITLE',
      subTitle: this.isRevisione
        ? 'PIAO_PDF.ATTACHMENTS.APPROVAL.REVISIONE.SUB_TITLE'
        : this.isUpdateMode
          ? 'PIAO_PDF.ATTACHMENTS.APPROVAL.SUB_TITLE'
          : 'PIAO_PDF.ATTACHMENTS.APPROVAL.SUB_TITLE',
      load: this.isRevisione
        ? 'PIAO_PDF.ATTACHMENTS.APPROVAL.REVISIONE.LOAD'
        : this.isUpdateMode
          ? 'PIAO_PDF.ATTACHMENTS.PIAO_PDF.CARICA.LOAD'
          : 'PIAO_PDF.ATTACHMENTS.PIAO_PDF.CARICA.LOAD',
    };
  }

  get extraAttachmentLabels(): Record<string, string> {
    return {
      title: this.isRevisione
        ? 'PIAO_PDF.ATTACHMENTS.EXTRA.REVISIONE.TITLE'
        : this.isUpdateMode
          ? 'PIAO_PDF.ATTACHMENTS.EXTRA.TITLE'
          : 'PIAO_PDF.ATTACHMENTS.EXTRA.TITLE',
      load: this.isRevisione
        ? 'PIAO_PDF.ATTACHMENTS.EXTRA.REVISIONE.LOAD'
        : this.isUpdateMode
          ? 'PIAO_PDF.ATTACHMENTS.EXTRA.LOAD'
          : 'PIAO_PDF.ATTACHMENTS.EXTRA.LOAD',
    };
  }

  get submitButtonLabel(): string {
    return this.isUpdateMode ? 'PIAO_PDF.AGGIORNA.SUBMIT' : 'PIAO_PDF.CARICA.SUBMIT';
  }

  get modalLabels(): Record<string, string> {
    return {
      title: this.isUpdateMode ? 'PIAO_PDF.MODAL.AGGIORNA.TITLE' : 'PIAO_PDF.MODAL.CARICA.TITLE',
      message: this.isUpdateMode
        ? 'PIAO_PDF.MODAL.AGGIORNA.MESSAGE'
        : 'PIAO_PDF.MODAL.CARICA.MESSAGE',
    };
  }

  private loadDropdownOptions(): void {
    forkJoin({
      trienni: this.piaoService.getTrienniRiferimento(),
      autorita: this.piaoService.getAutoritaApprovatore(),
    })
      .pipe(takeUntil(this.unsubscribe$))
      .subscribe({
        next: ({ trienni, autorita }) => {
          this.triennioOptions = trienni.map((triennio) => ({
            label: triennio,
            value: triennio,
          }));
          this.autoritaOptions = autorita.map((item: AutoritaApprovatoreDTO) => ({
            label: item.testo ?? item.codice,
            value: item.id,
          }));
          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error('Errore nel caricamento dei dropdown PIAO PDF', err);
          this.triennioOptions = [];
          this.autoritaOptions = [];
          this.cdr.markForCheck();
        },
      });
  }

  private initializeForm(): void {
    this.form = this.fb.group({
      dataApprovazione: [
        this.piaoDTO?.dataApprovazione ?? null,
        [Validators.required, Validators.pattern(DATE_REGEX)],
      ],
      autorità: [this.piaoDTO?.idAutoritaApprovatore ?? null, Validators.required],
      estremiAtto: [
        this.piaoDTO?.estremiAttoApprovazione ?? null,
        [Validators.required, Validators.pattern(INPUT_REGEX), Validators.maxLength(200)],
      ],
      triennio: [this.piaoDTO?.triennioRiferimento ?? null, Validators.required],
      numDipendenti: [this.piaoDTO?.tipologiaOnline ?? null, Validators.required],
      url: [
        this.piaoDTO?.url ?? null,
        [Validators.required, Validators.pattern(URL_REGEX), Validators.maxLength(500)],
      ],
      allegatiApprovazione: this.fb.array(
        [this.createAllegatoFormGroup()],
        [minArrayLength(1), Validators.required]
      ),
      allegatiPiao: this.fb.array(
        [this.createAllegatoFormGroup()],
        [minArrayLength(1), Validators.required]
      ),
    });
  }

  handleSubmit(): void {
    if (this.isDraftSaveDisabled) {
      return;
    }

    const piao: PIAODTO = {
      ...(this.piaoDTO || {}),
      id: Number(this.piaoDTO?.id),
      denominazionePA: this.piaoDTO?.denominazionePA ?? '',
      dataApprovazione: this.form.controls['dataApprovazione']?.value,
      idAutoritaApprovatore: this.form.controls['autorità']?.value,
      estremiAttoApprovazione: this.form.controls['estremiAtto']?.value,
      triennioRiferimento: this.form.controls['triennio']?.value,
      tipologiaOnline: this.form.controls['numDipendenti']?.value,
      url: this.form.controls['url']?.value,
      stakeHolders: this.piaoDTO?.stakeHolders || [],
    };

    if (piao && piao.id) {
      console.log('Aggiornamento PIAO esistente:', piao);
      this.piaoService.saveBozzaPiaoPDF(piao).subscribe({
        next: () => {
          this.sessionStorageService.setItem(KEY_PIAO, piao);
          this.toastService.success('Piao PDF salvato con successo!');
        },
        error: (err) => {
          console.error('Errore nel salvataggio del PIAO PDF', err);
        },
      });
      return;
    }
    console.log('Non sono entrato nel save: ', piao);
  }

  private createAllegatoFormGroup(allegato?: AllegatoDTO): FormGroup {
    return this.fb.group<Record<keyof AllegatoDTO, any>>({
      // Identificativi / tipologia
      id: this.fb.control(allegato?.id ?? null),
      idEntitaFK: this.fb.control(allegato?.idEntitaFK ?? null),
      codDocumento: this.fb.control(allegato?.codDocumento ?? null, Validators.required),
      codDocumentoFE: this.fb.control(allegato?.codDocumentoFE ?? null),
      codTipologiaFK: this.fb.control(allegato?.codTipologiaFK ?? null),
      codTipologiaAllegato: this.fb.control(allegato?.codTipologiaAllegato ?? null),

      // Descrizione e meta
      descrizione: this.fb.control(allegato?.descrizione ?? null),
      downloadUrl: this.fb.control(allegato?.downloadUrl ?? null),
      sizeAllegato: this.fb.control(allegato?.sizeAllegato ?? null),
      type: this.fb.control(allegato?.type ?? null),
      isDoc: this.fb.control(allegato?.isDoc ?? null),
      base64: this.fb.control(allegato?.base64 ?? null),
      status: this.fb.control(allegato?.status ?? null),
      statusAllegato: this.fb.control(allegato?.statusAllegato ?? null),

      // Campi tecnici (ereditati da CampiTecniciDTO)
      validity: this.fb.control(allegato?.validity ?? null),
      createdBy: this.fb.control(allegato?.createdBy ?? null),
      createdTs: this.fb.control(allegato?.createdTs ?? null),
      updatedBy: this.fb.control(allegato?.updatedBy ?? null),
      updatedTs: this.fb.control(allegato?.updatedTs ?? null),
      createdByRole: this.fb.control(allegato?.createdByRole ?? null),
      updatedByRole: this.fb.control(allegato?.updatedByRole ?? null),
      createdByNameSurname: this.fb.control(allegato?.createdByNameSurname ?? null),
      updatedByNameSurname: this.fb.control(allegato?.updatedByNameSurname ?? null),
    });
  }

  getDisableButtonValidation(): boolean {
    return this.form?.invalid || !this.form;
  }

  /**
   * Per il salvataggio in bozza considera solo i campi principali,
   * ignorando lo stato dei FormArray degli allegati.
   */
  get isDraftSaveDisabled(): boolean {
    if (!this.form) {
      return true;
    }
    const draftControls = [
      'dataApprovazione',
      'autorità',
      'estremiAtto',
      'triennio',
      'numDipendenti',
      'url',
    ];
    return draftControls.some((name) => this.form.controls[name]?.invalid);
  }

  handlePublish(): void {
    if (this.form.invalid) {
      // Show error toast - using standard notification
      console.warn('Form is invalid');
      return;
    }

    this.openModal = true;
  }

  confirmPublish(): void {
    if (this.form.valid) {
      const piao: PIAODTO = {
        ...(this.piaoDTO || {}),
        id: Number(this.piaoDTO?.id),
        denominazionePA: this.piaoDTO?.denominazionePA ?? '',
        dataApprovazione: this.form.controls['dataApprovazione']?.value,
        idAutoritaApprovatore: this.form.controls['autorità']?.value,
        estremiAttoApprovazione: this.form.controls['estremiAtto']?.value,
        triennioRiferimento: this.form.controls['triennio']?.value,
        tipologiaOnline: this.form.controls['numDipendenti']?.value,
        url: this.form.controls['url']?.value,
        stakeHolders: this.piaoDTO?.stakeHolders || [],
      };

      if (piao && piao.id) {
        console.log('Aggiornamento PIAO esistente:', piao);
        this.piaoService.pubblicaPiaoPDF(piao).subscribe({
          next: () => {
            piao.statoPiao = PiaoStatusEnum.PUBBLICATO;
            this.isDettaglio = true;
            this.sessionStorageService.setItem(KEY_PIAO, piao);
            this.openModal = false;
            this.toastService.success('Piao PDF pubblicato con successo!');
          },
          error: (err) => {
            console.error('Errore nella pubblicazione del PIAO PDF', err);
          },
        });
        return;
      }

      this.openModal = false;
    }
    console.log('Form non valido, non posso pubblicare:', this.form);
  }

  override ngOnDestroy(): void {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
  }
}
