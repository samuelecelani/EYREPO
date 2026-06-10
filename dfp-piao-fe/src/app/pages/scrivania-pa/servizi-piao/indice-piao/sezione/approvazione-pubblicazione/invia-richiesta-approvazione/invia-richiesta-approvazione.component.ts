import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { SharedModule } from '../../../../../../../shared/module/shared/shared.module';
import { TextBoxComponent } from '../../../../../../../shared/components/text-box/text-box.component';
import { TextAreaComponent } from '../../../../../../../shared/components/text-area/text-area.component';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonComponent } from '../../../../../../../shared/components/button/button.component';
import { BaseComponent } from '../../../../../../../shared/components/base/base.component';
import { AttachmentComponent } from '../../../../../../../shared/ui/attachment/attachment.component';
import { PIAODTO } from '../../../../../../../shared/models/classes/piao-dto';
import { CodTipologiaSezioneEnum } from '../../../../../../../shared/models/enums/cod-tipologia-sezione.enum';
import { CodTipologiaAllegatoEnum } from '../../../../../../../shared/models/enums/cod-tipologia-allegato.enum';
import { RichiestaApprovazioneService } from '../../../../../../../shared/services/richiesta-approvazione.service';
import { EMAIL_REGEX } from '../../../../../../../shared/utils/constants';
import {
  getChangedFields,
  piaoPDFGeneratoValidator,
} from '../../../../../../../shared/utils/utils';
import { SectionEnum } from '../../../../../../../shared/models/enums/section.enum';
import { TabellaGenerazionePdfComponent } from '../tabella-generazione-pdf/tabella-generazione-pdf.component';
import { AllegatoDTO } from '../../../../../../../shared/models/classes/allegato-dto';
import { PiaoStatusEnum } from '../../../../../../../shared/models/enums/piao-status.enum';
import { ApprovazioneService } from '../../../../../../../shared/services/approvazione.service';
import { takeUntil } from 'rxjs';

@Component({
  selector: 'piao-invia-richiesta-approvazione',
  imports: [
    SharedModule,
    ReactiveFormsModule,
    TextBoxComponent,
    TextAreaComponent,
    ButtonComponent,
    AttachmentComponent,
    TabellaGenerazionePdfComponent,
  ],
  templateUrl: './invia-richiesta-approvazione.component.html',
  styleUrl: './invia-richiesta-approvazione.component.scss',
})
export class InviaRichiestaApprovazioneComponent extends BaseComponent implements OnInit {
  @Input() piaoDTO!: PIAODTO;
  @Input() testoSezione!: string;
  @Input() isDettaglio: boolean = false;
  @Output() richiestaApprovazioneSaved = new EventEmitter<void>();

  title: string = 'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.TITLE';
  subtitle: string = 'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.SUB_TITLE';
  subtitleDettaglio: string =
    'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.SUB_TITLE_DETTAGLIO';
  emailLabel: string = 'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.EMAIL_LABEL';
  oggettoLabel: string =
    'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.EMAIL_OBJECT_LABEL';
  testoLabel: string = 'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.EMAIL_TEXT_LABEL';
  testoLabelDettaglio: string =
    'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.EMAIL_TEXT_LABEL_DETTAGLIO';
  buttonLabel: string = 'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.BUTTON_LABEL';
  attachment: string = 'APPROVAZIONE_E_PUBBLICAZIONE.ATTACHMENT.TITLE';
  attachmentDettaglio: string = 'APPROVAZIONE_E_PUBBLICAZIONE.ATTACHMENT.TITLE_DETTAGLIO';

  sezione: string = SectionEnum.SEZIONE_INVIA_APPROVAZIONE;
  codTipologia: string = SectionEnum.SEZIONE_INVIA_APPROVAZIONE;
  codTipologiaAllegato: string = CodTipologiaAllegatoEnum.RICHIESTA_APPROVAZIONE;

  fb: FormBuilder = inject(FormBuilder);
  form!: FormGroup;

  isFormReady: boolean = false;

  richiestaApprovazioneService: RichiestaApprovazioneService = inject(RichiestaApprovazioneService);
  approvazioneService: ApprovazioneService = inject(ApprovazioneService);

  formValueBackup: any = null;

  isVisibleButtonInvia: boolean = false;

  ngOnInit(): void {
    this.richiestaApprovazioneService.getRichiestaApprovazione(this.piaoDTO.id!).pipe(takeUntil(this.destroy$)).subscribe({
      next: (res: any) => {
        if (res) {
          this.createForm();
          this.form.patchValue(res);
          this.formValueBackup = structuredClone(this.form.value);
          this.isVisibleButtonInvia = res.statoPiao === PiaoStatusEnum.RICHIESTA_APPROVAZIONE;
        } else {
          this.createForm();
        }
        this.isFormReady = true;
      },
      error: (err) => {
        console.error('Errore nel recupero della richiesta di approvazione', err);
        this.createForm();
      },
    });
  }

  createForm() {
    this.form = this.fb.group({
      mail: this.fb.control<string | null>(null, [
        Validators.maxLength(50),
        Validators.pattern(EMAIL_REGEX),
        Validators.required,
      ]),
      idPiao: this.fb.control<number | null>(this.piaoDTO.id ?? null, [Validators.required]),
      oggetto: this.fb.control<string | null>(null, [
        Validators.maxLength(50),
        Validators.required,
      ]),
      testo: this.fb.control<string | null>(null, [
        Validators.maxLength(20000),
        Validators.required,
      ]),
      piaoPDF: this.fb.control<AllegatoDTO | null>(null, [piaoPDFGeneratoValidator]),
    });
  }

  handleInviaRichiestaValidazione() {
    // Blocca l'esecuzione se il form non è valido
    if (!this.form.valid) {
      return;
    }

    if (this.isVisibleButtonInvia) {
      return;
    }

    const obj = {
      ...this.form.value,
      testoSezione: this.testoSezione,
      campiModificati: getChangedFields(
        this.form.value,
        this.formValueBackup,
        ['id', 'idPiao'],
        'richiestaApprovazione' // campi da escludere dal confronto
      ),
    };
    this.richiestaApprovazioneService.saveRichiestaApprovazione(obj).pipe(takeUntil(this.destroy$)).subscribe({
      next: (res: any) => {
        this.toastService.success('Richiesta di approvazione salvata con successo');
        this.form.patchValue(res);
        this.formValueBackup = structuredClone(this.form.value);
        this.isVisibleButtonInvia = res.statoPiao === PiaoStatusEnum.RICHIESTA_APPROVAZIONE;
        this.richiestaApprovazioneSaved.emit();
      },
      error: (err) => {
        console.error('Errore nel salvataggio della richiesta di approvazione', err);
      },
    });
  }
}
