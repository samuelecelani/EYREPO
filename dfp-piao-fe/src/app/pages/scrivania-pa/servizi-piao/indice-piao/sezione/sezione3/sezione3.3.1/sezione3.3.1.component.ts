import { Component, DestroyRef, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SharedModule } from '../../../../../../../shared/module/shared/shared.module';
import { CardInfoComponent } from '../../../../../../../shared/ui/card-info/card-info.component';
import { INPUT_REGEX, KEY_PIAO, WARNING_ICON } from '../../../../../../../shared/utils/constants';
import { AccordionComponent } from '../../../../../../../shared/components/accordion/accordion.component';
import { BodyTableMinervaSezioneComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-3.1.1/body-table-minerva-sezione/body-table-minerva-sezione.component';
import { ISezioneBase } from '../../../../../../../shared/models/interfaces/sezione-base.interface';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, combineLatest, map, Observable, of, switchMap, take } from 'rxjs';
import { PIAODTO } from '../../../../../../../shared/models/classes/piao-dto';
import { Sezione331DTO } from '../../../../../../../shared/models/classes/sezione-331-dto';
import { Sezione331Service } from '../../../../../../../shared/services/sezione331.service';
import { SessionStorageService } from '../../../../../../../shared/services/session-storage.service';
import { StoricoStatoSezioneService } from '../../../../../../../shared/services/storico-stato-sezione.service';
import {
  areAllValuesNull,
  createFormArrayTabelleFunzionaliFromPiaoSession,
  hasRequiredErrors,
  isStatoMinervaAttivo,
  printFormErrors,
} from '../../../../../../../shared/utils/utils';
import { SectionStatusEnum } from '../../../../../../../shared/models/enums/section-status.enum';
import { TextAreaComponent } from '../../../../../../../shared/components/text-area/text-area.component';
import { LabelValue } from '../../../../../../../shared/models/interfaces/label-value';
import { BaseComponent } from '../../../../../../../shared/components/base/base.component';
import { TabellaFunzionaleComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/tabella-funzionale/tabella-funzionale.component';
import { SectionEnum } from '../../../../../../../shared/models/enums/section.enum';
import { TipologiaTabellaSezione331 } from '../../../../../../../shared/models/enums/tipologia-tabella-sezione3-3-1.enum';
import { MinervaService } from '../../../../../../../shared/services/minerva.service';
import { getValue } from '../../../../../../../shared/config/loader-config';

@Component({
  selector: 'piao-sezione-3-3-1',
  imports: [
    SharedModule,
    CardInfoComponent,
    AccordionComponent,
    BodyTableMinervaSezioneComponent,
    TextAreaComponent,
    CardInfoComponent,
    TabellaFunzionaleComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './sezione3.3.1.component.html',
  styleUrl: './sezione3.3.1.component.scss',
})
export class Sezione331Component extends BaseComponent implements ISezioneBase, OnInit {
  @Input() piaoDTO!: PIAODTO;
  @Input() sezione331Data?: Sezione331DTO;
  @Input() testoSezione!: string;
  @Input() isDettaglio: boolean = false;

  @Output() formValueChanged = new EventEmitter<any>();

  private sezione331Service = inject(Sezione331Service);
  private minervaService = inject(MinervaService);
  private destroyRef = inject(DestroyRef);
  private storicoStatoSezioneService = inject(StoricoStatoSezioneService);

  tabelleMinerva: Map<string, any>[] = [];

  codTipologiaFK: string = SectionEnum.SEZIONE_3_3_1;

  ngOnInit(): void {
    this.loadSezione331Data();

    this.sezione331Service.onSezione331Updated$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((sezione331) => {
        if (sezione331) {
          this.sezione331Data = sezione331;
          this.updateRealTimeSavedFormArrays(sezione331);
        }
      });
  }

  isFormReady: boolean = false;
  title: string = 'SEZIONE-3.3.1.TITLE';
  subTitle: string = 'SEZIONE-3.3.1.SUB_TITLE';
  contestoLabel: string = 'SEZIONE-3.3.1.CONTESTO_LABEL';
  contestoLabelDettaglio: string = 'SEZIONE-3.3.1.CONTESTO_LABEL_DETTAGLIO';

  /*CARD-INFO*/
  icon: string = WARNING_ICON;
  subTitleCardInfo: string = 'SEZIONE-3.3.1.CARD_INFO_MINERVA.SUB_TITLE';
  showButtonMinerva: boolean = true;
  showIconButton: boolean = true;
  hrefButtonMinerva: string = getValue('publicUrlMinerva') ?? '';
  titleButtonMinerva: string = 'BUTTONS.GO_TO_MINERVA';

  /*ACCORDION 1 */
  titleTab1: string = 'SEZIONE-3.3.1.ACCORDION.1.TAB_1';
  titleTab2: string = 'SEZIONE-3.3.1.ACCORDION.1.TAB_2';
  titleTab3: string = 'SEZIONE-3.3.1.ACCORDION.1.TAB_3';
  qualitativaLabel: string = 'SEZIONE-3.3.1.ACCORDION.1.QUALITATIVA_LABEL';
  riepilogoLabel: string = 'SEZIONE-3.3.1.ACCORDION.1.RIEPILOGO_LABEL';
  titleTab4: string = 'SEZIONE-3.3.1.ACCORDION.1.TAB_4';
  titleTab5: string = 'SEZIONE-3.3.1.ACCORDION.1.TAB_5';
  titleTab6: string = 'SEZIONE-3.3.1.ACCORDION.1.TAB_6';

  /*ACCORDION 2 */
  strategieProgrammazioneLabel: string = 'SEZIONE-3.3.1.ACCORDION.2.STRATEGIE_PROGRAMMAZIONE_LABEL';
  obiettivoTrasformazioneTitle: string = 'SEZIONE-3.3.1.ACCORDION.2.OBIETTIVO_TRASFORMAZIONE_TITLE';
  obiettivoTrasformazioneLabel: string = 'SEZIONE-3.3.1.ACCORDION.2.OBIETTIVO_TRASFORMAZIONE_LABEL';
  obiettivoTrasformazioneLabelDettaglio: string =
    'SEZIONE-3.3.1.ACCORDION.2.OBIETTIVO_TRASFORMAZIONE_LABEL_DETTAGLIO';

  rimodulazioneLabel: string = 'SEZIONE-3.3.1.ACCORDION.2.RIMODULAZIONE_LABEL';
  rimodulazioneLabelDettaglio: string = 'SEZIONE-3.3.1.ACCORDION.2.RIMODULAZIONE_LABEL_DETTAGLIO';

  titleTab7: string = 'SEZIONE-3.3.1.ACCORDION.2.TAB_7';
  strategiaCoperturaTitle: string = 'SEZIONE-3.3.1.ACCORDION.2.STRATEGIA_COPERTURA_TITLE';
  cardInfoTitle: string = 'SEZIONE-3.3.1.ACCORDION.2.CARD_INFO_TITLE';
  cardInfoSubTitle: string = 'SEZIONE-3.3.1.ACCORDION.2.CARD_INFO_SUBTITLE';
  strategiaCoperturaLabel: string = 'SEZIONE-3.3.1.ACCORDION.2.STRATEGIA_COPERTURA_LABEL';
  strategiaCoperturaLabelDettaglio: string =
    'SEZIONE-3.3.1.ACCORDION.2.STRATEGIA_COPERTURA_LABEL_DETTAGLIO';

  descrizioneStrategiaLabel: string = 'SEZIONE-3.3.1.ACCORDION.2.DESCRIZIONE_STRATEGIA_LABEL';
  coperturaFabbisognoTitle: string = 'SEZIONE-3.3.1.ACCORDION.2.COPERTURA_FABBISOGNO_TITLE';
  titleTab8: string = 'SEZIONE-3.3.1.ACCORDION.2.TAB_8';
  titleTab9: string = 'SEZIONE-3.3.1.ACCORDION.2.TAB_9';

  /*ACCORDION 3*/
  stimaEvoluzioneLabel: string = 'SEZIONE-3.3.1.ACCORDION.3.STIMA_EVOLUZIONE_LABEL';
  stimaEvoluzioneLabelDettaglio: string =
    'SEZIONE-3.3.1.ACCORDION.3.STIMA_EVOLUZIONE_LABEL_DETTAGLIO';

  titleTab10: string = 'SEZIONE-3.3.1.ACCORDION.3.TAB_10';
  prospettoPrevisionaleCessAnno1Title: string =
    'SEZIONE-3.3.1.ACCORDION.3.PROSPETTO_PREV_CESS_ANNO1_TITLE';
  titleTab11: string = 'SEZIONE-3.3.1.ACCORDION.3.TAB_11';
  titleTab12: string = 'SEZIONE-3.3.1.ACCORDION.3.TAB_12';
  titleTab13: string = 'SEZIONE-3.3.1.ACCORDION.3.TAB_13';
  prospettoPrevisionaleCessAnno2Title: string =
    'SEZIONE-3.3.1.ACCORDION.3.PROSPETTO_PREV_CESS_ANNO2_TITLE';
  titleTab14: string = 'SEZIONE-3.3.1.ACCORDION.3.TAB_14';
  titleTab15: string = 'SEZIONE-3.3.1.ACCORDION.3.TAB_15';

  /*Parte Funzionale*/
  funzionaleTitle: string = 'SEZIONE-3.3.1.PARTE_FUNZIONALE_TITLE';
  tabFunzionaleTitle: string = 'SEZIONE-3.3.1.TABELLA_FUNZIONALE.LABEL';
  tabFunzionaleTitleDettaglio: string = 'SEZIONE-3.3.1.TABELLA_FUNZIONALE.LABEL_DETTAGLIO';

  TipologiaTabellaSezione331 = TipologiaTabellaSezione331;

  inputContent: LabelValue[] = [
    {
      label: 'SEZIONE-3.3.1.ACCORDION.2.CHOICE1_LABEL',
      value: true,
      formControlName: 'rimodulazione',
    },
    {
      label: 'SEZIONE-3.3.1.ACCORDION.2.CHOICE2_LABEL',
      value: false,
      formControlName: 'rimodulazione',
    },
  ];

  private fb = inject(FormBuilder);

  form!: FormGroup;

  updateRealTimeSavedFormArrays(sezione331: Sezione331DTO): void {
    // Aggiorna i form array con i dati restituiti dal salvataggio in tempo reale

    const tabellaFunzionaleArray = this.form.get('tabelleFunzionali') as FormArray;
    if (tabellaFunzionaleArray) {
      const newTabellaFunzionaleArray = createFormArrayTabelleFunzionaliFromPiaoSession(
        this.fb,
        sezione331.tabelleFunzionali || [],
        sezione331.id || this.piaoDTO?.idSezione331
      );
      tabellaFunzionaleArray.clear();
      newTabellaFunzionaleArray.controls.forEach((control) => tabellaFunzionaleArray.push(control));
    }

    const newStatus = this.getSectionStatus();
    if (newStatus !== sezione331.statoSezione) {
      this.storicoStatoSezioneService
        .save({
          idEntitaFK: sezione331.id || -1,
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

  getForm(): FormGroup {
    return this.form;
  }
  isFormValid(): boolean {
    return this.form.valid;
  }
  hasFormValues(): boolean {
    return !areAllValuesNull(this.form);
  }
  prepareDataForSave(): Sezione331DTO {
    const formValue = this.form.getRawValue();
    const sezione331 = this.sezione331Data || new Sezione331DTO();
    const sezione331Id = this.sezione331Data?.id || this.piaoDTO?.idSezione331 || -1;

    return {
      ...sezione331,
      id: sezione331Id,
      idPiao: this.piaoDTO?.id || null,
      contesto: formValue.contesto || null,
      descrizioneQualitativa: formValue.descrizioneQualitativa || null,
      strategiaProgrammazione: formValue.strategiaProgrammazione || null,
      obiettivoTrasformazione: formValue.obiettivoTrasformazione || null,
      rimodulazione: formValue.rimodulazione ?? null,
      strategiaCopertura: formValue.strategiaCopertura || null,
      descrizioneStrategia: formValue.descrizioneStrategia || null,
      stimaEvoluzione: formValue.stimaEvoluzione || null,
      tabelleFunzionali: formValue.tabelleFunzionali || null,
      statoSezione: this.getSectionStatus(),
    } as Sezione331DTO;
  }
  validate(testoSezione: string, campiModificati: string): Observable<any> {
    const sezione331Id = this.sezione331Data?.id || this.piaoDTO?.idSezione331 || -1;
    return this.sezione331Service.validation(sezione331Id, testoSezione, campiModificati);
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
      id: this.fb.control<number | null>(this.piaoDTO.idSezione331 || null),
      idPiao: this.fb.control<number | null>(this.piaoDTO.id || null),
      statoSezione: this.fb.control<string | null>(this.sezione331Data?.statoSezione || null, [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      contesto: this.fb.control<string | null>(this.sezione331Data?.contesto || null, [
        Validators.maxLength(20000),
        Validators.pattern(INPUT_REGEX),
      ]),
      valoreDotazioneOrganicaTable: this.fb.control<boolean | null>(null, [Validators.required]),
      consistenzaDirigenzialeTable: this.fb.control<boolean | null>(null, [Validators.required]),
      consistenzaNonDirigenzialeTable: this.fb.control<boolean | null>(null, [Validators.required]),
      descrizioneQualitativa: this.fb.control<string | null>(
        this.sezione331Data?.descrizioneQualitativa || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      personaleDirigenzialeAssuzioniTable: this.fb.control<boolean | null>(null, [
        Validators.required,
      ]),
      areeContrattualiAssuzioniTable: this.fb.control<boolean | null>(
        null
        //[Validators.required]
      ),
      riepilogoCessazioniTable: this.fb.control<boolean | null>(null, [Validators.required]),
      strategiaProgrammazione: this.fb.control<string | null>(
        this.sezione331Data?.strategiaProgrammazione || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX), Validators.required]
      ),
      obiettivoTrasformazione: this.fb.control<string | null>(
        this.sezione331Data?.obiettivoTrasformazione || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX), Validators.required]
      ),
      rimodulazione: this.fb.control<boolean | null>(this.sezione331Data?.rimodulazione ?? null, [
        Validators.required,
      ]),
      rimodulazioneTable: this.fb.control<boolean | null>(null),
      strategiaCopertura: this.fb.control<string | null>(
        this.sezione331Data?.strategiaCopertura || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX), Validators.required]
      ),
      coperturaFabbisognoDirigTable: this.fb.control<boolean | null>(null, [Validators.required]),
      coperturaFabbisognoContrTable: this.fb.control<boolean | null>(
        null
        //[Validators.required]
      ),
      descrizioneStrategia: this.fb.control<string | null>(
        this.sezione331Data?.descrizioneStrategia || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX), Validators.required]
      ),
      stimaEvoluzione: this.fb.control<string | null>(
        this.sezione331Data?.stimaEvoluzione || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      prospettoPrevisionaleBaseAnno1Table: this.fb.control<boolean | null>(null, [
        Validators.required,
      ]),
      prospettoPrevisionaleCessAnno1DiriTable: this.fb.control<boolean | null>(null, [
        Validators.required,
      ]),
      prospettoPrevisionaleCessAnno1ContrTable: this.fb.control<boolean | null>(null, [
        //Validators.required
      ]),
      prospettoPrevisionaleBaseAnno2Table: this.fb.control<boolean | null>(null, [
        Validators.required,
      ]),
      prospettoPrevisionaleCessAnno2DiriTable: this.fb.control<boolean | null>(null, [
        Validators.required,
      ]),
      prospettoPrevisionaleCessAnno2ContrTable: this.fb.control<boolean | null>(null, [
        //Validators.required,
      ]),
      // Tabelle funzionali
      tabelleFunzionali: createFormArrayTabelleFunzionaliFromPiaoSession(
        this.fb,
        this.sezione331Data?.tabelleFunzionali || [],
        this.sezione331Data?.id || this.piaoDTO?.idSezione331
      ),
    });

    // Validatore condizionale: rimodulazioneTable è required solo se rimodulazione === true
    this.updateRimodulazioneTableValidator(this.form.controls['rimodulazione'].value);
    this.form.controls['rimodulazione'].valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        this.updateRimodulazioneTableValidator(value);
      });

    // Quando tutti i table controls hanno un valore, verifica lo stato sezione
    this.watchAllTableControls();

    // Emetti il valore del form dopo la creazione
    this.isFormReady = true;
    this.formValueChanged.emit(this.form.value);
  }

  private watchAllTableControls(): void {
    const tableControlNames = [
      'valoreDotazioneOrganicaTable',
      'consistenzaDirigenzialeTable',
      'consistenzaNonDirigenzialeTable',
      'personaleDirigenzialeAssuzioniTable',
      'areeContrattualiAssuzioniTable',
      'riepilogoCessazioniTable',
      'coperturaFabbisognoDirigTable',
      'coperturaFabbisognoContrTable',
      'prospettoPrevisionaleBaseAnno1Table',
      'prospettoPrevisionaleCessAnno1DiriTable',
      'prospettoPrevisionaleCessAnno1ContrTable',
      'prospettoPrevisionaleBaseAnno2Table',
      'prospettoPrevisionaleCessAnno2DiriTable',
      'prospettoPrevisionaleCessAnno2ContrTable',
      'rimodulazioneTable',
    ];

    const tableControls$ = tableControlNames.map((name) => this.form.get(name)!.valueChanges);

    combineLatest(tableControls$)
      .pipe(take(1), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        const newStatus = this.getSectionStatus();
        const currentStatus = this.sezione331Data?.statoSezione;
        console.log(
          'Ricalcolo stato sezione dopo cambiamento tabelle - nuovoStato:',
          newStatus,
          'statoSezioneData:',
          currentStatus
        );
        if (newStatus !== currentStatus) {
          const sezione331Id = this.sezione331Data?.id || this.piaoDTO?.idSezione331 || -1;
          this.storicoStatoSezioneService
            .save({
              idEntitaFK: sezione331Id,
              codTipologiaFK: this.codTipologiaFK,
              testo: newStatus || '',
            })
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe({
              next: () => {
                this.form.get('statoSezione')?.setValue(newStatus);
                this.sezione331Data = {
                  ...this.sezione331Data,
                  statoSezione: newStatus,
                } as Sezione331DTO;
                console.log('Storico stato sezione salvato con successo (all tables loaded)');
              },
              error: () => {
                console.log('Errore nel salvare lo storico stato sezione');
              },
            });
        }
      });
  }

  private updateRimodulazioneTableValidator(rimodulazioneValue: boolean | null): void {
    const rimodulazioneTable = this.form.get('rimodulazioneTable');
    if (rimodulazioneValue === true) {
      rimodulazioneTable?.setValidators([Validators.required]);
    } else {
      rimodulazioneTable?.clearValidators();
    }
    rimodulazioneTable?.updateValueAndValidity();
  }

  private loadSezione331Data(): void {
    if (this.piaoDTO?.id) {
      this.getPaRiferimento$()
        .pipe(
          switchMap((pa) =>
            this.sezione331Service
              .getSezione331ByIdPiao(this.piaoDTO.id!)
              .pipe(
                switchMap((sezione331) =>
                  this.minervaService
                    .getTabella(
                      pa.codePA,
                      String(new Date().getFullYear()),
                      sezione331?.id || -1,
                      isStatoMinervaAttivo(sezione331?.statoSezione)
                    )
                    .pipe(map((tabelleMinerva) => ({ sezione331, tabelleMinerva })))
                )
              )
          )
        )
        .subscribe({
          next: ({ sezione331, tabelleMinerva }) => {
            if (sezione331) {
              this.sezione331Data = sezione331;
              this.piaoDTO.idSezione331 = sezione331.id;
              this.sessionStorageService.setItem(KEY_PIAO, this.piaoDTO);
            }
            if (tabelleMinerva) {
              this.tabelleMinerva = tabelleMinerva;
            }
            this.createForm();
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
   * Carica i dati della sezione 3.3.1 dal PIAO precedente senza modificare il piaoDTO in sessione.
   */
  loadFromPreviousPiao(previousPiaoId: number): Observable<boolean> {
    return this.getPaRiferimento$().pipe(
      switchMap((pa) =>
        this.sezione331Service
          .getSezione331ByIdPiao(previousPiaoId)
          .pipe(
            switchMap((sezione331) =>
              this.minervaService
                .getTabella(
                  pa.codePA,
                  String(new Date().getFullYear()),
                  sezione331?.id || -1,
                  isStatoMinervaAttivo(sezione331?.statoSezione)
                )
                .pipe(map((tabelleMinerva) => ({ sezione331, tabelleMinerva })))
            )
          )
      ),
      map(({ sezione331, tabelleMinerva }) => {
        let success = false;
        if (sezione331) {
          this.sezione331Data = sezione331;
          success = true;
        }
        if (tabelleMinerva) {
          this.tabelleMinerva = tabelleMinerva;
        }
        this.createForm();
        return success;
      }),
      catchError((err) => {
        console.error('Errore nel caricamento dati sezione 3.3.1 dal PIAO precedente:', err);
        this.createForm();
        return of(false);
      })
    );
  }
}
