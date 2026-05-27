import { Component, DestroyRef, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { catchError, map, Observable, of } from 'rxjs';
import { AccordionComponent } from '../../../../../../../shared/components/accordion/accordion.component';
import { BaseComponent } from '../../../../../../../shared/components/base/base.component';
import { ISezioneBase } from '../../../../../../../shared/models/interfaces/sezione-base.interface';
import { PIAODTO } from '../../../../../../../shared/models/classes/piao-dto';
import { SectionStatusEnum } from '../../../../../../../shared/models/enums/section-status.enum';
import { SectionEnum } from '../../../../../../../shared/models/enums/section.enum';
import { INPUT_REGEX, KEY_PIAO } from '../../../../../../../shared/utils/constants';
import {
  areAllValuesNull,
  createFormArrayTabelleFunzionaliFromPiaoSession,
  getSectionStatus,
  hasRequiredErrors,
  printFormErrors,
} from '../../../../../../../shared/utils/utils';
import { SharedModule } from '../../../../../../../shared/module/shared/shared.module';
import { TabellaFunzionaleComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/tabella-funzionale/tabella-funzionale.component';
import { Sezione32DTO } from '../../../../../../../shared/models/classes/sezione-32-dto';
import { TextAreaComponent } from '../../../../../../../shared/components/text-area/text-area.component';
import { CardInfoComponent } from '../../../../../../../shared/ui/card-info/card-info.component';
import { SessionStorageService } from '../../../../../../../shared/services/session-storage.service';
import { Sezione32Service } from '../../../../../../../shared/services/sezione32.service';
import { StoricoStatoSezioneService } from '../../../../../../../shared/services/storico-stato-sezione.service';

@Component({
  selector: 'piao-sezione-3-2',
  imports: [
    SharedModule,
    AccordionComponent,
    TextAreaComponent,
    CardInfoComponent,
    TabellaFunzionaleComponent,
  ],
  templateUrl: './sezione3.2.component.html',
  styleUrl: './sezione3.2.component.scss',
})
export class Sezione32Component extends BaseComponent implements ISezioneBase, OnInit {
  @Input() piaoDTO!: PIAODTO;
  @Input() sezione32Data?: Sezione32DTO;
  @Input() testoSezione!: string;
  @Output() formValueChanged = new EventEmitter<any>();
  @Input() isDettaglio: boolean = false;
  private sezione32Service = inject(Sezione32Service);
  private destroyRef = inject(DestroyRef);
  private storicoStatoSezioneService = inject(StoricoStatoSezioneService);

  private fb = inject(FormBuilder);

  form!: FormGroup;
  isFormReady: boolean = false;

  parteGeneraleTitle: string = 'SEZIONE_32.PARTE_GENERALE';
  parteFunzionaleTitle: string = 'SEZIONE_32.PARTE_FUNZIONALE';
  tabellaParteFunzionaleTitle: string = 'SEZIONE_32.TABELLA_FUNZIONALE.LABEL';
  tabellaParteFunzionaleTitleDettaglio: string = 'SEZIONE_32.TABELLA_FUNZIONALE.LABEL_DETTAGLIO';

  codTipologiaFK: string = SectionEnum.SEZIONE_3_2;

  titleAccordion1: string = 'SEZIONE_32.ACCORDION_1.TITLE';
  titleAccordion2: string = 'SEZIONE_32.ACCORDION_2.TITLE';
  titleAccordion3: string = 'SEZIONE_32.ACCORDION_3.TITLE';

  descrizioneContestoRiferimentoLabel: string = 'SEZIONE_32.ACCORDION_1.LABEL';
  descrizioneContestoRiferimentoLabelDettaglio: string = 'SEZIONE_32.ACCORDION_1.LABEL_DETTAGLIO';

  descrizioneObiettiviLavoroAgileLabel: string = 'SEZIONE_32.ACCORDION_2.LABEL';
  descrizioneObiettiviLavoroAgileLabelDettaglio: string = 'SEZIONE_32.ACCORDION_2.LABEL_DETTAGLIO';

  infoTooltipContesto: string = 'SEZIONE_32.ACCORDION_1.TOOLTIP';
  infoTooltipObiettivi: string = 'SEZIONE_32.ACCORDION_2.TOOLTIP';

  descrizioneStatoAttuazioneLabel: string =
    'SEZIONE_32.ACCORDION_3.DESCRIZIONE_STATO_ATTUAZIONE_LABEL';
  descrizioneStatoAttuazioneLabelDettaglio: string =
    'SEZIONE_32.ACCORDION_3.DESCRIZIONE_STATO_ATTUAZIONE_LABEL_DETTAGLIO';
  descrizioneFattoriAbilitantiLabel: string =
    'SEZIONE_32.ACCORDION_3.DESCRIZIONE_FATTORI_ABILITANTI_LABEL';
  descrizioneFattoriAbilitantiLabelDettaglio: string =
    'SEZIONE_32.ACCORDION_3.DESCRIZIONE_FATTORI_ABILITANTI_LABEL_DETTAGLIO';
  descrizionePersonaleAgileTitle: string = 'SEZIONE_32.ACCORDION_3.PERSONALE_AGILE_TITLE';
  descrizionePersonaleAgileLabel: string = 'SEZIONE_32.ACCORDION_3.PERSONALE_AGILE_LABEL';
  descrizioneGiornateLavorateTitle: string = 'SEZIONE_32.ACCORDION_3.GIORNATE_LAVORATE_TITLE';
  descrizioneGiornateLavorateLabel: string = 'SEZIONE_32.ACCORDION_3.GIORNATE_LAVORATE_LABEL';
  descrizioneGiornateLavorateLabelDettaglio: string =
    'SEZIONE_32.ACCORDION_3.GIORNATE_LAVORATE_LABEL_DETTAGLIO';

  descrizioneLivelloSoddisfazioneTitle: string =
    'SEZIONE_32.ACCORDION_3.LIVELLO_SODDISFAZIONE_TITLE';
  descrizioneLivelloSoddisfazioneLabel: string =
    'SEZIONE_32.ACCORDION_3.LIVELLO_SODDISFAZIONE_LABEL';
  descrizioneLivelloSoddisfazioneLabelDettaglio: string =
    'SEZIONE_32.ACCORDION_3.LIVELLO_SODDISFAZIONE_LABEL_DETTAGLIO';
  descrizioneContributiTitle: string = 'SEZIONE_32.ACCORDION_3.CONTRIBUTI_TITLE';
  descrizioneContributiLabel: string = 'SEZIONE_32.ACCORDION_3.CONTRIBUTI_LABEL';
  descrizioneContributiLabelDettaglio: string = 'SEZIONE_32.ACCORDION_3.CONTRIBUTI_LABEL_DETTAGLIO';

  descrizioneImpattiTitle: string = 'SEZIONE_32.ACCORDION_3.IMPATTI_TITLE';
  descrizioneImpattiLabel: string = 'SEZIONE_32.ACCORDION_3.IMPATTI_LABEL';

  infoTooltipFattoriAbilitanti: string = 'SEZIONE_32.ACCORDION_3.FATTORI_ABILITANTI_TOOLTIP';
  infoTooltipContributi: string = 'SEZIONE_32.ACCORDION_3.CONTRIBUTI_TOOLTIP';

  cardInfoTitle: string = 'SEZIONE_32.ACCORDION_3.CARD_INFO_TITLE';
  cardInfoSubTitle: string = 'SEZIONE_32.ACCORDION_3.CARD_INFO_SUBTITLE';
  cachedStatoSezione: string = SectionStatusEnum.DA_COMPILARE;

  ngOnInit(): void {
    this.loadSezione32Data();

    this.sezione32Service.onSezione32Updated$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((sezione32) => {
        if (sezione32) {
          this.sezione32Data = sezione32;
          this.updateRealTimeSavedFormArrays(sezione32);
        }
      });
  }

  updateRealTimeSavedFormArrays(sezione32: Sezione32DTO): void {
    // Aggiorna i form array con i dati restituiti dal salvataggio in tempo reale

    const tabellaFunzionaleArray = this.form.get('tabelleFunzionali') as FormArray;
    if (tabellaFunzionaleArray) {
      const newTabellaFunzionaleArray = createFormArrayTabelleFunzionaliFromPiaoSession(
        this.fb,
        sezione32.tabelleFunzionali || [],
        sezione32.id || this.piaoDTO?.idSezione32
      );
      tabellaFunzionaleArray.clear();
      newTabellaFunzionaleArray.controls.forEach((control) => tabellaFunzionaleArray.push(control));
    }

    this.saveStoricoStatoSezione(sezione32);
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

  prepareDataForSave(): Sezione32DTO {
    const formValue = this.form.getRawValue();
    const sezione32 = this.sezione32Data || new Sezione32DTO();
    const sezione32Id = this.sezione32Data?.id || this.piaoDTO?.idSezione32 || -1;

    return {
      ...sezione32,
      id: sezione32Id,
      idPiao: this.piaoDTO.id || undefined,
      descrizioneContestoRiferimento: formValue.descrizioneContestoRiferimento || null,
      descrizioneObiettiviLavoroAgile: formValue.descrizioneObiettiviLavoroAgile || null,
      descrizioneStatoAttuazione: formValue.descrizioneStatoAttuazione || null,
      descrizioneFattoriAbilitanti: formValue.descrizioneFattoriAbilitanti || null,
      descrizionePersonaleAgile: formValue.descrizionePersonaleAgile || null,
      descrizioneGiornateLavorate: formValue.descrizioneGiornateLavorate || null,
      descrizioneLivelloSoddisfazione: formValue.descrizioneLivelloSoddisfazione || null,
      descrizioneContributi: formValue.descrizioneContributi || null,
      descrizioneImpatti: formValue.descrizioneImpatti || null,
      tabelleFunzionali: formValue.tabelleFunzionali || null,
      statoSezione: this.getSectionStatus(),
      testoSezione: this.testoSezione,
    };
  }

  validate(testoSezione: string, campiModificati: string): Observable<any> {
    const sezione32Id = this.sezione32Data?.id || this.piaoDTO?.idSezione32 || -1;
    return this.sezione32Service.validation(sezione32Id, testoSezione, campiModificati);
  }

  resetForm(): void {
    this.form.reset();
    this.createForm();
  }

  getSectionStatus(): string {
    if (!this.hasFormValues()) {
      return SectionStatusEnum.DA_COMPILARE;
    }

    // Stampa tutti gli errori required e minLength del form
    console.log('=== VALIDATION ERRORS ===');
    printFormErrors(this.form);
    console.log('=========================');

    if (hasRequiredErrors(this.form)) {
      return SectionStatusEnum.IN_COMPILAZIONE;
    }

    return SectionStatusEnum.COMPILATA;
  }

  createForm(): void {
    this.form = this.fb.group({
      id: this.fb.control<number | null>(this.piaoDTO.idSezione32 || null),
      idPiao: this.fb.control<number | null>(this.piaoDTO.id || null),
      statoSezione: this.fb.control<string | null>(this.sezione32Data?.statoSezione || null, [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      descrizioneContestoRiferimento: this.fb.control<string | null>(
        this.sezione32Data?.descrizioneContestoRiferimento || null,
        [Validators.required, Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      descrizioneObiettiviLavoroAgile: this.fb.control<string | null>(
        this.sezione32Data?.descrizioneObiettiviLavoroAgile || null,
        [Validators.required, Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      descrizioneStatoAttuazione: this.fb.control<string | null>(
        this.sezione32Data?.descrizioneStatoAttuazione || null,
        [Validators.required, Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      descrizioneFattoriAbilitanti: this.fb.control<string | null>(
        this.sezione32Data?.descrizioneFattoriAbilitanti || null,
        [Validators.required, Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      descrizionePersonaleAgile: this.fb.control<string | null>(
        this.sezione32Data?.descrizionePersonaleAgile || null,
        [Validators.required, Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      descrizioneGiornateLavorate: this.fb.control<string | null>(
        this.sezione32Data?.descrizioneGiornateLavorate || null,
        [Validators.required, Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      descrizioneLivelloSoddisfazione: this.fb.control<string | null>(
        this.sezione32Data?.descrizioneLivelloSoddisfazione || null,
        [Validators.required, Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      descrizioneContributi: this.fb.control<string | null>(
        this.sezione32Data?.descrizioneContributi || null,
        [Validators.required, Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      descrizioneImpatti: this.fb.control<string | null>(
        this.sezione32Data?.descrizioneImpatti || null,
        [Validators.required, Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      tabelleFunzionali: createFormArrayTabelleFunzionaliFromPiaoSession(
        this.fb,
        this.sezione32Data?.tabelleFunzionali || [],
        this.sezione32Data?.id || this.piaoDTO?.idSezione32
      ),
    });

    this.isFormReady = true;

    this.formValueChanged.emit(this.form.value);
  }

  private saveStoricoStatoSezione(sezione32: Sezione32DTO): void {
    const newStatus = this.getSectionStatus();
    if (newStatus !== sezione32.statoSezione) {
      this.storicoStatoSezioneService
        .save({
          idEntitaFK: sezione32.id || -1,
          codTipologiaFK: this.codTipologiaFK,
          testo: newStatus || '',
        })
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.form.get('statoSezione')?.setValue(newStatus);
            console.log('Storico stato sezione salvato con successo');
          },
          error: () => {
            console.log('Errore nel salvare lo storico stato sezione');
          },
        });
    }
  }

  private loadSezione32Data(): void {
    if (this.piaoDTO?.id) {
      this.sezione32Service.getSezione32ByIdPiao(this.piaoDTO.id).subscribe({
        next: (data) => {
          if (data) {
            this.sezione32Data = data;
            this.piaoDTO.idSezione32 = data.id;
            this.sessionStorageService.setItem(KEY_PIAO, this.piaoDTO);
          }
          this.createForm();
          // Salva lo storico dello stato solo se non è già in uno degli stati validi
          if (!this.noReloadStato(this.sezione32Data?.statoSezione)) {
            this.saveStoricoStatoSezione(this.sezione32Data || new Sezione32DTO());
          }
        },
        error: () => {
          this.createForm();
        },
      });
    } else {
      this.createForm();
    }
  }

  /**
   * Carica i dati della sezione 3.2 dal PIAO precedente senza modificare il piaoDTO in sessione.
   */
  loadFromPreviousPiao(previousPiaoId: number): Observable<boolean> {
    return this.sezione32Service.getSezione32ByIdPiao(previousPiaoId).pipe(
      map((data) => {
        let success = false;
        if (data) {
          this.sezione32Data = data;
          success = true;
        }
        this.createForm();
        return success;
      }),
      catchError((err) => {
        console.error('Errore nel caricamento dati sezione 3.2 dal PIAO precedente:', err);
        this.createForm();
        return of(false);
      })
    );
  }
}
