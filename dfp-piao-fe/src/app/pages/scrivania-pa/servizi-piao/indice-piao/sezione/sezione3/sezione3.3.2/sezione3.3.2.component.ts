import {
  Component,
  DestroyRef,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SharedModule } from '../../../../../../../shared/module/shared/shared.module';
import { BaseComponent } from '../../../../../../../shared/components/base/base.component';
import { ISezioneBase } from '../../../../../../../shared/models/interfaces/sezione-base.interface';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { catchError, map, Observable, of } from 'rxjs';
import { TextAreaComponent } from '../../../../../../../shared/components/text-area/text-area.component';
import { Sezione332DTO } from '../../../../../../../shared/models/classes/sezione-332-dto';
import { PIAODTO } from '../../../../../../../shared/models/classes/piao-dto';
import { Sezione332Service } from '../../../../../../../shared/services/sezione332.service';
import { SessionStorageService } from '../../../../../../../shared/services/session-storage.service';
import { StoricoStatoSezioneService } from '../../../../../../../shared/services/storico-stato-sezione.service';
import {
  areAllValuesNull,
  createFormArrayAttivitaFormativeFromPiaoSession,
  createFormArrayObiettiviRisultatiFotografiaFromPiaoSession,
  createFormArrayTabelleFunzionaliFromPiaoSession,
  hasRequiredErrors,
  printFormErrors,
} from '../../../../../../../shared/utils/utils';
import { SectionStatusEnum } from '../../../../../../../shared/models/enums/section-status.enum';
import { INPUT_REGEX, KEY_PIAO, SHAPE_ICON } from '../../../../../../../shared/utils/constants';
import { AccordionComponent } from '../../../../../../../shared/components/accordion/accordion.component';
import { SectionEnum } from '../../../../../../../shared/models/enums/section.enum';
import { TabellaFunzionaleComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/tabella-funzionale/tabella-funzionale.component';
import { ElencoFotografiaObiettiviComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-3.3.2/elenco-fotografia-obiettivi/elenco-fotografia-obiettivi.component';
import { CodTipologiaFotoObiettivoEnum } from '../../../../../../../shared/models/enums/cod-tipologia-foto-obi.enum';
import { ElencoAttivitaFormativaComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-3.3.2/elenco-attivita-formativa/elenco-attivita-formativa.component';
import { WorkflowComponent } from '../../../../../../../shared/components/workflow/workflow.component';

@Component({
  selector: 'piao-sezione-3-3-2',
  imports: [
    SharedModule,
    AccordionComponent,
    TextAreaComponent,
    TabellaFunzionaleComponent,
    ElencoFotografiaObiettiviComponent,
    ElencoAttivitaFormativaComponent,
  ],
  templateUrl: './sezione3.3.2.component.html',
  styleUrl: './sezione3.3.2.component.scss',
})
export class Sezione332Component extends BaseComponent implements ISezioneBase, OnInit {
  @Input() piaoDTO!: PIAODTO;
  @Input() sezione332Data?: Sezione332DTO;
  @Input() testoSezione!: string;
  @Input() isDettaglio: boolean = false;

  @Output() formValueChanged = new EventEmitter<any>();

  icon: string = SHAPE_ICON;
  iconStyle: string = 'icon-modal';

  title: string = 'SEZIONE-3.3.2.TITLE';
  subTitle: string = 'SEZIONE-3.3.2.PARTE_GENERALE';

  /*ACCORDION 1*/
  contestoNormativoLabel: string = 'SEZIONE-3.3.2.ACCORDION.1.CONTESTO_NORMATIVO_LABEL';
  contestoNormativoLabelDettaglio: string =
    'SEZIONE-3.3.2.ACCORDION.1.CONTESTO_NORMATIVO_LABEL_DETTAGLIO';

  /*ACCORDION 2*/
  descrizioneQualitativaLabel: string = 'SEZIONE-3.3.2.ACCORDION.2.DESCRIZIONE_QUALITATIVA_LABEL';
  rappresentazioneStatoArteTitle: string =
    'SEZIONE-3.3.2.ACCORDION.2.RAPPRESENTAZIONE_STATO_ARTE_TITLE';
  fotografiaFormazioneTitle: string = 'SEZIONE-3.3.2.ACCORDION.2.FOTOGRAFIA_FORMAZIONE_TITLE';
  fotografiaFormazioneTitleDettaglio: string =
    'SEZIONE-3.3.2.ACCORDION.2.FOTOGRAFIA_FORMAZIONE_TITLE_DETTAGLIO';
  attivitaFormativaTitle: string = 'SEZIONE-3.3.2.ACCORDION.2.ATTIVITA_FORMATIVA_TITLE';

  /*ACCORDION 3*/
  prioritaStrategicaLabel: string = 'SEZIONE-3.3.2.ACCORDION.3.PRIORITA_STRATEGICHE_TITLE';
  descrizioneStrategiaLabel: string = 'SEZIONE-3.3.2.ACCORDION.3.DESCRIZIONE_STRATEGIA_LABEL';
  obiettiviRisultatiTitle: string = 'SEZIONE-3.3.2.ACCORDION.3.OBIETTIVI_RISULTATI_TITLE';
  obiettiviRisultatiTitleDettaglio: string =
    'SEZIONE-3.3.2.ACCORDION.3.OBIETTIVI_RISULTATI_TITLE_DETTAGLIO';
  risorseDisponibiliTitle: string = 'SEZIONE-3.3.2.ACCORDION.3.RISORSE_DISPONIBILI_TITLE';
  descrizioneRisorseLabel: string = 'SEZIONE-3.3.2.ACCORDION.3.DESCRIZIONE_RISORSE_LABEL';
  incentiviTitle: string = 'SEZIONE-3.3.2.ACCORDION.3.INCENTIVI_TITLE';
  descrizioneIncentiviLabel: string = 'SEZIONE-3.3.2.ACCORDION.3.DESCRIZIONE_INCENTIVI_LABEL';

  /*TAB Funzionale*/
  funzionaleTitle: string = 'SEZIONE-3.3.2.PARTE_FUNZIONALE';
  tabFunzionaleTitle: string = 'SEZIONE-3.3.2.TABELLA_FUNZIONALE.LABEL';
  tabFunzionaleTitleDettaglio: string = 'SEZIONE-3.3.2.TABELLA_FUNZIONALE.LABEL_DETTAGLIO';

  codTipologiaFK: string = SectionEnum.SEZIONE_3_3_2;

  private sezione332Service = inject(Sezione332Service);
  private storicoStatoSezioneService = inject(StoricoStatoSezioneService);

  private fb = inject(FormBuilder);

  form!: FormGroup;

  isFormReady: boolean = false;

  private destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.loadSezione332Data();

    // Sottoscrizione agli aggiornamenti della sezione332
    this.sezione332Service.onSezione332Updated$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((sezione332) => {
        if (sezione332) {
          this.sezione332Data = sezione332;
          this.updateRealTimeSavedFormArrays(sezione332);
        }
      });
  }

  handleCloseModalFotografia(): void {}

  private loadSezione332Data(): void {
    if (this.piaoDTO?.id) {
      this.sezione332Service.getSezione332ByIdPiao(this.piaoDTO.id).subscribe({
        next: (data) => {
          if (data) {
            this.sezione332Data = data;
            this.piaoDTO.idSezione332 = data.id;
            this.sessionStorageService.setItem(KEY_PIAO, this.piaoDTO);
          }
          this.createForm();
          // Salva lo storico dello stato solo se non è già in uno degli stati validi
          if (!this.noReloadStato(this.sezione332Data?.statoSezione)) {
            this.saveStoricoStatoSezione(this.sezione332Data || new Sezione332DTO());
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
   * Carica i dati della sezione 3.3.2 dal PIAO precedente senza modificare il piaoDTO in sessione.
   */
  loadFromPreviousPiao(previousPiaoId: number): Observable<boolean> {
    return this.sezione332Service.getSezione332ByIdPiao(previousPiaoId).pipe(
      map((data) => {
        let success = false;
        if (data) {
          this.sezione332Data = data;
          success = true;
        }
        this.createForm();
        return success;
      }),
      catchError((err) => {
        console.error('Errore nel caricamento dati sezione 3.3.2 dal PIAO precedente:', err);
        this.createForm();
        return of(false);
      })
    );
  }

  updateRealTimeSavedFormArrays(sezione332: Sezione332DTO): void {
    // Aggiorna i campi del form con i nuovi dati della sezione332
    const tabellaFunzionaleArray = this.form.get('tabelleFunzionali') as FormArray;
    if (tabellaFunzionaleArray) {
      const newTabellaFunzionaleArray = createFormArrayTabelleFunzionaliFromPiaoSession(
        this.fb,
        sezione332.tabelleFunzionali || [],
        sezione332.id || this.piaoDTO?.idSezione332
      );
      tabellaFunzionaleArray.clear();
      newTabellaFunzionaleArray.controls.forEach((control) => tabellaFunzionaleArray.push(control));
    }

    const obiettiviArray = this.form.get('obiettiviRisultatiFotografia') as FormArray;
    if (obiettiviArray) {
      const newObiettiviArray = createFormArrayObiettiviRisultatiFotografiaFromPiaoSession(
        this.fb,
        sezione332.obiettiviRisultatiFotografia || [],
        sezione332.id || this.piaoDTO?.idSezione332
      );
      obiettiviArray.clear();
      newObiettiviArray.controls.forEach((control) => obiettiviArray.push(control));
    }

    const fotografiaArray = this.form.get('fotografiaFormazione') as FormArray;
    if (fotografiaArray) {
      const allItems = sezione332.obiettiviRisultatiFotografia || [];
      const filtered = allItems.filter(
        (i) => i.codTipologiaFK === CodTipologiaFotoObiettivoEnum.FOTOGRAFIA_FORMAZIONE
      );
      const newArray = createFormArrayObiettiviRisultatiFotografiaFromPiaoSession(
        this.fb,
        filtered,
        sezione332.id || this.piaoDTO?.idSezione332
      );
      fotografiaArray.clear();
      newArray.controls.forEach((control) => fotografiaArray.push(control));
    }

    const obiettiviRisultatiArray = this.form.get('obiettiviRisultati') as FormArray;
    if (obiettiviRisultatiArray) {
      const allItems = sezione332.obiettiviRisultatiFotografia || [];
      const filtered = allItems.filter(
        (i) => i.codTipologiaFK === CodTipologiaFotoObiettivoEnum.OBIETTIVI_RISULTATI
      );
      const newArray = createFormArrayObiettiviRisultatiFotografiaFromPiaoSession(
        this.fb,
        filtered,
        sezione332.id || this.piaoDTO?.idSezione332
      );
      obiettiviRisultatiArray.clear();
      newArray.controls.forEach((control) => obiettiviRisultatiArray.push(control));
    }

    this.saveStoricoStatoSezione(sezione332);
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
  prepareDataForSave(): Sezione332DTO {
    const formValue = this.form.getRawValue();
    const sezione332 = this.sezione332Data || new Sezione332DTO();
    const sezione332Id = this.sezione332Data?.id || this.piaoDTO?.idSezione332 || -1;

    return {
      ...sezione332,
      id: sezione332Id,
      idPiao: this.piaoDTO?.id || null,
      contestoNormativo: formValue.contestoNormativo || null,
      descrizioneQualitativa: formValue.descrizioneQualitativa || null,
      descrizioneStrategia: formValue.descrizioneStrategia || null,
      descrizioneRisorse: formValue.descrizioneRisorse || null,
      descrizioneIncentivi: formValue.descrizioneIncentivi || null,
      obiettiviRisultatiFotografia: [
        ...(formValue.fotografiaFormazione || []),
        ...(formValue.obiettiviRisultati || []),
      ],
      attivitaFormative: this.sezione332Data?.attivitaFormative || null,
      tabelleFunzionali: formValue.tabelleFunzionali || null,
      statoSezione: this.getSectionStatus(),
    } as Sezione332DTO;
  }

  validate(testoSezione: string, campiModificati: string): Observable<any> {
    const sezione332Id = this.sezione332Data?.id || this.piaoDTO?.idSezione332 || -1;
    return this.sezione332Service.validation(sezione332Id, testoSezione, campiModificati);
  }
  resetForm(): void {
    this.form.reset();
  }

  getSectionStatus(): string {
    //tutto il form null, DA_COMPILARE
    if (!this.hasFormValues()) {
      return SectionStatusEnum.DA_COMPILARE;
    }

    // Stampa tutti gli errori required e minLength del form
    console.log('=== VALIDATION ERRORS ===');
    printFormErrors(this.form);
    console.log('=========================');

    //se i required non sono stati compilati, IN_COMPILAZIONE
    if (hasRequiredErrors(this.form)) {
      return SectionStatusEnum.IN_COMPILAZIONE;
    }

    //form valido e con i campi compilati, COMPILATA
    return SectionStatusEnum.COMPILATA;
  }
  createForm(): void {
    this.form = this.fb.group({
      id: this.fb.control<number | null>(this.piaoDTO.idSezione332 || null),
      idPiao: this.fb.control<number | null>(this.piaoDTO.id || null),
      statoSezione: this.fb.control<string | null>(this.sezione332Data?.statoSezione || null, [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      contestoNormativo: this.fb.control<string | null>(
        this.sezione332Data?.contestoNormativo || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      descrizioneQualitativa: this.fb.control<string | null>(
        this.sezione332Data?.descrizioneQualitativa || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      descrizioneStrategia: this.fb.control<string | null>(
        this.sezione332Data?.descrizioneStrategia || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      descrizioneRisorse: this.fb.control<string | null>(
        this.sezione332Data?.descrizioneRisorse || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      descrizioneIncentivi: this.fb.control<string | null>(
        this.sezione332Data?.descrizioneIncentivi || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),

      attivitaFormative: createFormArrayAttivitaFormativeFromPiaoSession(
        this.fb,
        this.sezione332Data?.attivitaFormative || [],
        this.sezione332Data?.id || this.piaoDTO?.idSezione332
      ),
      fotografiaFormazione: createFormArrayObiettiviRisultatiFotografiaFromPiaoSession(
        this.fb,
        (this.sezione332Data?.obiettiviRisultatiFotografia || []).filter(
          (i) => i.codTipologiaFK === CodTipologiaFotoObiettivoEnum.FOTOGRAFIA_FORMAZIONE
        ),
        this.sezione332Data?.id || this.piaoDTO?.idSezione332
      ),
      obiettiviRisultati: createFormArrayObiettiviRisultatiFotografiaFromPiaoSession(
        this.fb,
        (this.sezione332Data?.obiettiviRisultatiFotografia || []).filter(
          (i) => i.codTipologiaFK === CodTipologiaFotoObiettivoEnum.OBIETTIVI_RISULTATI
        ),
        this.sezione332Data?.id || this.piaoDTO?.idSezione332
      ),

      // Tabelle funzionali
      tabelleFunzionali: createFormArrayTabelleFunzionaliFromPiaoSession(
        this.fb,
        this.sezione332Data?.tabelleFunzionali || [],
        this.sezione332Data?.id || this.piaoDTO?.idSezione332
      ),
    });

    // Emetti il valore del form dopo la creazione
    this.isFormReady = true;
    this.formValueChanged.emit(this.form.value);
  }

  private saveStoricoStatoSezione(sezione332: Sezione332DTO): void {
    const newStatus = this.getSectionStatus();
    if (newStatus !== sezione332.statoSezione) {
      this.storicoStatoSezioneService
        .save({
          idEntitaFK: sezione332.id || -1,
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
}
