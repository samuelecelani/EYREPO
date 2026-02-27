import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { SharedModule } from '../../../shared/module/shared/shared.module';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin, Subject, switchMap, takeUntil } from 'rxjs';
import {
  DATE_REGEX,
  INPUT_REGEX,
  ONLY_NUMBERS_REGEX,
  SHAPE_ICON,
} from '../../../shared/utils/constants';
import { DropdownComponent } from '../../../shared/components/dropdown/dropdown.component';
import { TextAreaComponent } from '../../../shared/components/text-area/text-area.component';
import { TextBoxComponent } from '../../../shared/components/text-box/text-box.component';
import { DatePickerComponent } from '../../../shared/components/date-picker/date-picker.component';
import { LabelValue } from '../../../shared/models/interfaces/label-value';
import { MotivazioneDichiarazioneService } from '../../../shared/services/motivazione-dichiarazione.service';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { getTodayISO } from '../../../shared/utils/utils';
import { AttachmentComponent } from '../../../shared/ui/attachment/attachment.component';
import { CodTipologiaAllegatoEnum } from '../../../shared/models/enums/cod-tipologia-allegato.enum';
import { CodTipologiaSezioneEnum } from '../../../shared/models/enums/cod-tipologia-sezione.enum';
import { DichiarazioneScadenzaDTO } from '../../../shared/models/classes/dichiarazione-scadenza-dto';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { BaseComponent } from '../../../shared/components/base/base.component';
import { DichiarazioneScadenzaService } from '../../../shared/services/dichiarazione-scadenza.service';
import { ToastService } from '../../../shared/services/toast.service';

@Component({
  selector: 'piao-mancata-compilazione',
  imports: [
    SharedModule,
    ReactiveFormsModule,
    DatePickerComponent,
    TextBoxComponent,
    TextAreaComponent,
    DropdownComponent,
    ButtonComponent,
    AttachmentComponent,
    ModalComponent,
  ],
  templateUrl: './mancata-compilazione.component.html',
  styleUrl: './mancata-compilazione.component.scss',
})
export class MancataCompilazioneComponent extends BaseComponent implements OnInit, OnDestroy {
  private unsubscribe$ = new Subject<void>();
  title: string = 'MANCATA_COMPILAZIONE.TITLE';
  subTitle: string = 'MANCATA_COMPILAZIONE.SUB_TITLE';
  annoLabel: string = 'MANCATA_COMPILAZIONE.ANNO_LABEL';
  dataPubblicazioneLabel: string = 'MANCATA_COMPILAZIONE.DATA_PUBBLICAZIONE_LABEL';
  noteLabel: string = 'MANCATA_COMPILAZIONE.NOTE_LABEL';
  motivazioneLabel: string = 'MANCATA_COMPILAZIONE.MOTIVAZIONE_LABEL';
  descrizioneLabel: string = 'MANCATA_COMPILAZIONE.DESCRIZIONE_LABEL';
  responsabileLabel: string = 'MANCATA_COMPILAZIONE.RESPONSABILE_LABEL';
  titleModal: string = 'MANCATA_COMPILAZIONE.MODAL.TITLE';
  messageModal: string = 'MANCATA_COMPILAZIONE.MODAL.MESSAGE';
  subMessageModal: string = 'MANCATA_COMPILAZIONE.MODAL.SUB_MESSAGE';

  openModalDichiarazioneScadenza: boolean = false;
  iconModal: string = SHAPE_ICON;

  iconStyle: string = 'icon-modal';

  minDate: string = getTodayISO();

  fb: FormBuilder = inject(FormBuilder);
  motivazioneDichiarazioneService = inject(MotivazioneDichiarazioneService);
  dichiarazioneService = inject(DichiarazioneScadenzaService);
  toastService = inject(ToastService);
  form!: FormGroup;

  motivazioneOptions!: LabelValue[];

  idPiao!: number;

  codTipologia: string = CodTipologiaSezioneEnum.PIAO;
  codTipologiaAllegato: string = CodTipologiaAllegatoEnum.MANCATA_COMPILAZIONE;

  dichiarazioneScadenzaDTO!: DichiarazioneScadenzaDTO;

  isDateReadOnly: boolean = false;

  ngOnInit(): void {
    this.form = this.fb.group({
      anno: this.fb.control<string | null>(null, [
        Validators.maxLength(10),
        Validators.pattern(ONLY_NUMBERS_REGEX),
        Validators.required,
      ]),
      dataPubblicazione: this.fb.control<any | null>(null, [
        Validators.maxLength(50),
        Validators.pattern(DATE_REGEX),
        Validators.required,
      ]),
      note: this.fb.control<string | null>(null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
      ]),
      motivazione: this.fb.control<string | null>(null, [
        Validators.maxLength(50),
        Validators.pattern(INPUT_REGEX),
        Validators.required,
      ]),
      descrizione: this.fb.control<string | null>(null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
      ]),
      responsabile: this.fb.control<string | null>(null, [
        Validators.maxLength(100),
        Validators.pattern(INPUT_REGEX),
      ]),
      idPiao: this.fb.control<number | null>(null, [
        Validators.maxLength(10),
        Validators.pattern(INPUT_REGEX),
        Validators.required,
      ]),
    });

    this.getPaRiferimento$()
      .pipe(
        switchMap((pa) => {
          return forkJoin({
            motivazioni: this.motivazioneDichiarazioneService.getAllMotivazioniDichiarazione(),
            dichiarazione: this.dichiarazioneService.getExistingDichiarazioneScadenza(pa.codePA),
          });
        }),
        takeUntil(this.unsubscribe$)
      )
      .subscribe({
        next: ({ motivazioni, dichiarazione }) => {
          this.motivazioneOptions = motivazioni.map((motivazione) => ({
            label: motivazione.testo || '',
            value: motivazione.id?.toString() || '',
          }));

          if (dichiarazione) {
            this.dichiarazioneScadenzaDTO = dichiarazione;
            this.form.patchValue({
              anno: dichiarazione.annoRiferimento?.toString() ?? null,
              dataPubblicazione: dichiarazione.dataPubblicazione ?? null,
              note: dichiarazione.note ?? null,
              motivazione: dichiarazione.idMotivazioneDichiarazione?.toString() ?? null,
              descrizione: dichiarazione.descrizione ?? null,
              responsabile: dichiarazione.responsabile ?? null,
              idPiao: dichiarazione.idPiao ?? null,
            });
            this.idPiao = dichiarazione.idPiao ?? 0;
          }

          if (dichiarazione?.dataPubblicazione) {
            this.isDateReadOnly = true;
          }
        },
        error: (err) => {
          console.error('Errore nel recupero dei dati', err);
          this.motivazioneOptions = [];
        },
      });
  }

  override ngOnDestroy(): void {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
    super.ngOnDestroy();
  }

  getDisableButtonValidation(): boolean {
    return this.form.invalid;
  }

  buildDichiarazioneScadenzaDTO(): DichiarazioneScadenzaDTO {
    const formValue = this.form.value;
    const dto = new DichiarazioneScadenzaDTO();
    dto.annoRiferimento = Number(formValue.anno);
    dto.dataPubblicazione = new Date(formValue.dataPubblicazione);
    dto.note = formValue.note ?? '';
    dto.idMotivazioneDichiarazione = Number(formValue.motivazione);
    dto.descrizione = formValue.descrizione ?? '';
    dto.responsabile = formValue.responsabile ?? '';
    dto.idPiao = Number(this.idPiao);
    return dto;
  }

  handleValidationSection(): void {
    if (this.form.valid) {
      const dto = this.buildDichiarazioneScadenzaDTO();
      console.log('DichiarazioneScadenzaDTO:', dto);
      this.openModalDichiarazioneScadenza = false;
      this.dichiarazioneService.save(dto).subscribe({
        next: (response: DichiarazioneScadenzaDTO) => {
          this.form.patchValue({
            anno: response.annoRiferimento?.toString() ?? null,
            dataPubblicazione: response.dataPubblicazione ?? null,
            note: response.note ?? null,
            motivazione: response.idMotivazioneDichiarazione?.toString() ?? null,
            descrizione: response.descrizione ?? null,
            responsabile: response.responsabile ?? null,
            idPiao: response.idPiao ?? null,
          });
          if (response.dataPubblicazione) {
            this.isDateReadOnly = true;
          }
          this.toastService.success('Dichiarazione di scadenza salvata con successo');
        },
        error: (err) => {
          console.error('Errore nel salvataggio della DichiarazioneScadenza', err);
        },
      });
    } else {
      console.warn('Form non valido, non posso procedere con la validazione della sezione');
    }
  }
}
