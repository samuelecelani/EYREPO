import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  EventEmitter,
  inject,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SharedModule } from '../../../../../../../shared/module/shared/shared.module';
import { CardInfoComponent } from '../../../../../../../shared/ui/card-info/card-info.component';
import { INPUT_REGEX, KEY_PIAO, WARNING_ICON } from '../../../../../../../shared/utils/constants';
import { AccordionComponent } from '../../../../../../../shared/components/accordion/accordion.component';
import { BodyTableMinervaSezioneComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-3.1.1/body-table-minerva-sezione/body-table-minerva-sezione.component';
import { ISezioneBase } from '../../../../../../../shared/models/interfaces/sezione-base.interface';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  Observable,
  catchError,
  debounceTime,
  map,
  merge,
  of,
  switchMap,
  takeUntil,
  tap,
} from 'rxjs';
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
  changeDetection: ChangeDetectionStrategy.OnPush,
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
  private cdr = inject(ChangeDetectorRef);

  tabelleMinerva: Map<string, any>[] = [];
  reportType: string | null = null;

  codTipologiaFK: string = SectionEnum.SEZIONE_3_3_1;

  /**
   * Tabelle PAC (15) — mapping 1:1 con `nameTablePAC` lato backend.
   * Scommentare il control per renderlo `required` quando il reportType è PAC.
   * NB: `rimodulazioneTable` (DOTAZIONE_ORGANICA_RIMODULAZIONE) è PAC-only ed è
   * gestita a parte da `updateRimodulazioneTableValidator` perché conditional.
   */
  private readonly pacRequiredTableControls: string[] = [
    'valoreDotazioneOrganicaTable', // VALORE_FINANZIARIO_DOTAZIONE_ORGANICA
    'consistenzaDirigenzialeTable', // CONSISTENZA_PERSONALE_DIRIGENZIALE
    'consistenzaNonDirigenzialeTable', // CONSISTENZA_PERSONALE_NON_DIRIGENZIALE
    // 'personaleDirigenzialeAssuzioniTable', // RIEPILOGO_ASSUNZIONI_DIRIGENZIALE
    // 'areeContrattualiAssuzioniTable', // RIEPILOGO_ASSUNZIONI_AREE_CONTRATTUALI
    'riepilogoCessazioniTable', // RIEPILOGO_CESSAZIONI
    // 'rimodulazioneTable', // DOTAZIONE_ORGANICA_RIMODULAZIONE -> conditional, vedi updateRimodulazioneTableValidator
    'coperturaFabbisognoDirigTable', // COPERTURA_FABBISOGNO_ANNO_CORRENTE_DIRIGENZIALE
    'coperturaFabbisognoContrTable', // COPERTURA_FABBISOGNO_ANNO_CORRENTE_AREE_CONTRATTUALI
    'prospettoPrevisionaleBaseAnno1Table', // CESSAZIONI_ANNO_CORRENTE
    'prospettoPrevisionaleCessAnno1DiriTable', // COPERTURA_FABBISOGNO_ANNO1_DIRIGENZIALE
    'prospettoPrevisionaleCessAnno1ContrTable', // COPERTURA_FABBISOGNO_ANNO1_AREE_CONTRATTUALI
    'prospettoPrevisionaleBaseAnno2Table', // CESSAZIONI_SERVIZIO
    'prospettoPrevisionaleCessAnno2DiriTable', // COPERTURA_FABBISOGNO_ANNO2_DIRIGENZIALE
    'prospettoPrevisionaleCessAnno2ContrTable', // COPERTURA_FABBISOGNO_ANNO2_AREE_CONTRATTUALI
  ];

  /**
   * Tabelle PAL (21) — mapping 1:1 con `nameTablePAL` lato backend.
   * Include DOTAZIONE_ORGANICA_RIMODULAZIONE (conditional, vedi `updateRimodulazioneTableValidator`)
   * e le tabelle aggiuntive CONSISTENZA_TEMPO_DET + RENDICONTI + SPESE_T2 +
   * VERIFICA_SOGLIA + LIMITE_SPESA che non esistono in PAC.
   */
  private readonly palRequiredTableControls: string[] = [
    'valoreDotazioneOrganicaTable', // VALORE_FINANZIARIO_DOTAZIONE_ORGANICA
    'consistenzaDirigenzialeTable', // CONSISTENZA_PERSONALE_DIRIGENZIALE
    'consistenzaNonDirigenzialeTable', // CONSISTENZA_PERSONALE_NON_DIRIGENZIALE
    'consistenzaDirigenzialeTempoDetTable', // CONSISTENZA_PERSONALE_DIRIGENZIALE_TEMPO_DETERMINATO
    'consistenzaNonDirigenzialeTempoDetTable', // CONSISTENZA_PERSONALE_NON_DIRIGENZIALE_TEMPO_DETERMINATO
    // 'personaleDirigenzialeAssuzioniTable', // RIEPILOGO_ASSUNZIONI_DIRIGENZIALE
    // 'areeContrattualiAssuzioniTable', // RIEPILOGO_ASSUNZIONI_AREE_CONTRATTUALI
    'riepilogoCessazioniTable', // RIEPILOGO_CESSAZIONI
    // 'rimodulazioneTable', // DOTAZIONE_ORGANICA_RIMODULAZIONE -> conditional, vedi updateRimodulazioneTableValidator
    // 'rendicontiEntrateUltimi3AnniTable', // RENDICONTI_ENTRATE_ULTIMI_3_ANNI
    'spesePersonaleAnnoT2Table', // SPESE_PERSONALE_ANNO_T2
    'verificaValoreSogliaTable', // VERIFICA_VALORE_SOGLIA
    // 'limiteSpesaPersonale20112013Table', // LIMITE_SPESA_PERSONALE_2011_2013
    // 'limiteSpesaPersonaleTable', // LIMITE_SPESA_PERSONALE
    'coperturaFabbisognoDirigTable', // COPERTURA_FABBISOGNO_ANNO_CORRENTE_DIRIGENZIALE
    'coperturaFabbisognoContrTable', // COPERTURA_FABBISOGNO_ANNO_CORRENTE_AREE_CONTRATTUALI
    'prospettoPrevisionaleBaseAnno1Table', // CESSAZIONI_ANNO_CORRENTE
    'prospettoPrevisionaleCessAnno1DiriTable', // COPERTURA_FABBISOGNO_ANNO1_DIRIGENZIALE
    'prospettoPrevisionaleCessAnno1ContrTable', // COPERTURA_FABBISOGNO_ANNO1_AREE_CONTRATTUALI
    'prospettoPrevisionaleBaseAnno2Table', // CESSAZIONI_SERVIZIO
    'prospettoPrevisionaleCessAnno2DiriTable', // COPERTURA_FABBISOGNO_ANNO2_DIRIGENZIALE
    'prospettoPrevisionaleCessAnno2ContrTable', // COPERTURA_FABBISOGNO_ANNO2_AREE_CONTRATTUALI
  ];

  /** Tutti i possibili control tabella (PAC ∪ PAL): usato per il reset dei validator. */
  private readonly allKnownTableControls: string[] = Array.from(
    new Set([
      // PAC
      'valoreDotazioneOrganicaTable',
      'consistenzaDirigenzialeTable',
      'consistenzaNonDirigenzialeTable',
      'personaleDirigenzialeAssuzioniTable',
      'areeContrattualiAssuzioniTable',
      'riepilogoCessazioniTable',
      'rimodulazioneTable',
      'coperturaFabbisognoDirigTable',
      'coperturaFabbisognoContrTable',
      'prospettoPrevisionaleBaseAnno1Table',
      'prospettoPrevisionaleCessAnno1DiriTable',
      'prospettoPrevisionaleCessAnno1ContrTable',
      'prospettoPrevisionaleBaseAnno2Table',
      'prospettoPrevisionaleCessAnno2DiriTable',
      'prospettoPrevisionaleCessAnno2ContrTable',
      // PAL extra
      'consistenzaDirigenzialeTempoDetTable',
      'consistenzaNonDirigenzialeTempoDetTable',
      'rendicontiEntrateUltimi3AnniTable',
      'spesePersonaleAnnoT2Table',
      'verificaValoreSogliaTable',
      'limiteSpesaPersonale20112013Table',
      'limiteSpesaPersonaleTable',
    ])
  );

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
  titleTabTempDeterDir: string = 'SEZIONE-3.3.1.ACCORDION.1.TAB_TEMP_DETER_DIR';
  titleTabTempDeterNonDir: string = 'SEZIONE-3.3.1.ACCORDION.1.TAB_TEMP_DETER_NON_DIR';
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

  prospettoCalcoloAssunzionaliLabel: string =
    'SEZIONE-3.3.1.ACCORDION.3.PROSPETTO_CALCOLO_ASSUNZIONALI_LABEL';
  titleTab16: string = 'SEZIONE-3.3.1.ACCORDION.3.TAB_16';
  titleTab17: string = 'SEZIONE-3.3.1.ACCORDION.3.TAB_17';
  titleTab18: string = 'SEZIONE-3.3.1.ACCORDION.3.TAB_18';
  limiteSpesaPersonaleLabel: string = 'SEZIONE-3.3.1.ACCORDION.3.LIMITE_SPESA_PERSONALE_LABEL';
  titleTab19: string = 'SEZIONE-3.3.1.ACCORDION.3.TAB_19';
  titleTab20: string = 'SEZIONE-3.3.1.ACCORDION.3.TAB_20';

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

  ngOnInit(): void {
    this.loadSezione331Data();

    this.sezione331Service.onSezione331Updated$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((sezione331) => {
        if (sezione331) {
          this.sezione331Data = sezione331;
          this.updateRealTimeSavedFormArrays(sezione331);
          this.cdr.markForCheck();
        }
      });
  }

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
          },
          error: () => {},
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

    //se i required non sono stati compilati, IN_COMPILAZIONE
    if (hasRequiredErrors(this.form)) {
      // DEBUG: elenca in console quali control hanno errori required/minlength che bloccano lo stato COMPILATA
      const missing = this.findRequiredErrorPaths(this.form);
      console.warn('[Sezione3.3.1] Required mancanti -> IN_COMPILAZIONE:', missing);
      return SectionStatusEnum.IN_COMPILAZIONE;
    }

    //form valido e con i campi compilati, COMPILATA
    return SectionStatusEnum.COMPILATA;
  }

  /**
   * DEBUG ONLY. Restituisce l'elenco dei path dei control che hanno errori
   * `required`, `minlength` o `minArrayLength` (le stesse condizioni valutate
   * da `hasRequiredErrors`). Utile per capire perché la sezione resta
   * `IN_COMPILAZIONE` invece di passare a `COMPILATA`.
   */
  private findRequiredErrorPaths(
    control: any,
    path: string = ''
  ): Array<{ path: string; errors: string[] }> {
    const result: Array<{ path: string; errors: string[] }> = [];
    if (!control) return result;

    const isContainer = control.controls && typeof control.controls === 'object';

    const ownErrors: string[] = [];
    if (control.errors?.['required']) ownErrors.push('required');
    if (control.errors?.['minlength']) ownErrors.push('minlength');
    if (control.errors?.['minArrayLength']) ownErrors.push('minArrayLength');
    if (ownErrors.length) {
      result.push({ path: path || '(root)', errors: ownErrors });
    }

    if (isContainer) {
      const children = control.controls;
      if (Array.isArray(children)) {
        children.forEach((child: any, idx: number) => {
          result.push(...this.findRequiredErrorPaths(child, `${path}[${idx}]`));
        });
      } else {
        Object.keys(children).forEach((key) => {
          const child = children[key];
          const childPath = path ? `${path}.${key}` : key;
          result.push(...this.findRequiredErrorPaths(child, childPath));
        });
      }
    }

    return result;
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
      valoreDotazioneOrganicaTable: this.fb.control<boolean | null>(null),
      consistenzaDirigenzialeTable: this.fb.control<boolean | null>(null),
      consistenzaNonDirigenzialeTable: this.fb.control<boolean | null>(null),
      // Nuovi control specifici per PAL
      consistenzaDirigenzialeTempoDetTable: this.fb.control<boolean | null>(null),
      consistenzaNonDirigenzialeTempoDetTable: this.fb.control<boolean | null>(null),
      descrizioneQualitativa: this.fb.control<string | null>(
        this.sezione331Data?.descrizioneQualitativa || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      personaleDirigenzialeAssuzioniTable: this.fb.control<boolean | null>(null),
      areeContrattualiAssuzioniTable: this.fb.control<boolean | null>(null),
      riepilogoCessazioniTable: this.fb.control<boolean | null>(null),
      // Nuovi control specifici per PAL
      rendicontiEntrateUltimi3AnniTable: this.fb.control<boolean | null>(null),
      spesePersonaleAnnoT2Table: this.fb.control<boolean | null>(null),
      verificaValoreSogliaTable: this.fb.control<boolean | null>(null),
      limiteSpesaPersonale20112013Table: this.fb.control<boolean | null>(null),
      limiteSpesaPersonaleTable: this.fb.control<boolean | null>(null),
      strategiaProgrammazione: this.fb.control<string | null>(
        this.sezione331Data?.strategiaProgrammazione || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      obiettivoTrasformazione: this.fb.control<string | null>(
        this.sezione331Data?.obiettivoTrasformazione || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      rimodulazione: this.fb.control<boolean | null>(this.sezione331Data?.rimodulazione ?? null, [
        Validators.required,
      ]),
      rimodulazioneTable: this.fb.control<boolean | null>(null),
      strategiaCopertura: this.fb.control<string | null>(
        this.sezione331Data?.strategiaCopertura || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      coperturaFabbisognoDirigTable: this.fb.control<boolean | null>(null),
      coperturaFabbisognoContrTable: this.fb.control<boolean | null>(null),
      descrizioneStrategia: this.fb.control<string | null>(
        this.sezione331Data?.descrizioneStrategia || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      stimaEvoluzione: this.fb.control<string | null>(
        this.sezione331Data?.stimaEvoluzione || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      prospettoPrevisionaleBaseAnno1Table: this.fb.control<boolean | null>(null),
      prospettoPrevisionaleCessAnno1DiriTable: this.fb.control<boolean | null>(null),
      prospettoPrevisionaleCessAnno1ContrTable: this.fb.control<boolean | null>(null),
      prospettoPrevisionaleBaseAnno2Table: this.fb.control<boolean | null>(null),
      prospettoPrevisionaleCessAnno2DiriTable: this.fb.control<boolean | null>(null),
      prospettoPrevisionaleCessAnno2ContrTable: this.fb.control<boolean | null>(null),
      // Tabelle funzionali
      tabelleFunzionali: createFormArrayTabelleFunzionaliFromPiaoSession(
        this.fb,
        this.sezione331Data?.tabelleFunzionali || [],
        this.sezione331Data?.id || this.piaoDTO?.idSezione331
      ),
    });

    // Applica i validatori required sui control delle tabelle in base al reportType ricevuto da Minerva
    this.applyTableValidatorsByReportType();

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
    this.cdr.markForCheck();
  }

  private watchAllTableControls(): void {
    const tableControlNames = [
      'valoreDotazioneOrganicaTable',
      'consistenzaDirigenzialeTable',
      'consistenzaNonDirigenzialeTable',
      'consistenzaDirigenzialeTempoDetTable',
      'consistenzaNonDirigenzialeTempoDetTable',
      'personaleDirigenzialeAssuzioniTable',
      'areeContrattualiAssuzioniTable',
      'riepilogoCessazioniTable',
      'rendicontiEntrateUltimi3AnniTable',
      'spesePersonaleAnnoT2Table',
      'verificaValoreSogliaTable',
      'limiteSpesaPersonale20112013Table',
      'limiteSpesaPersonaleTable',
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

    // Reagisce ad ogni cambio (anche parziale) e ricalcola lo stato sezione con debounce.
    // Sostituisce il precedente combineLatest + take(1) che richiedeva l'emissione da TUTTI i
    // control prima di scattare (e quindi spesso non si attivava mai).
    const tableControls$ = tableControlNames
      .map((name) => this.form.get(name)?.valueChanges)
      .filter((obs$): obs$ is NonNullable<typeof obs$> => !!obs$);

    merge(...tableControls$)
      .pipe(debounceTime(300), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        const newStatus = this.getSectionStatus();
        const currentStatus = this.sezione331Data?.statoSezione;
        if (newStatus === currentStatus) return;

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
              this.form.get('statoSezione')?.setValue(newStatus, { emitEvent: false });
              this.sezione331Data = {
                ...this.sezione331Data,
                statoSezione: newStatus,
              } as Sezione331DTO;
              this.cdr.markForCheck();
            },
            error: () => {},
          });
      });
  }

  private updateRimodulazioneTableValidator(rimodulazioneValue: boolean | null): void {
    const rimodulazioneTable = this.form.get('rimodulazioneTable');
    if (!rimodulazioneTable) return;

    // DOTAZIONE_ORGANICA_RIMODULAZIONE è presente sia in PAC che in PAL:
    // è required SOLO se il radio rimodulazione === true, indipendentemente dal reportType.
    if (rimodulazioneValue === true) {
      rimodulazioneTable.setValidators([Validators.required]);
    } else {
      rimodulazioneTable.clearValidators();
    }
    rimodulazioneTable.updateValueAndValidity({ emitEvent: false });
  }

  /**
   * Applica i validatori required sui control delle tabelle in base al `reportType`
   * ricevuto dalla response di Minerva (PAC, PAL o UNI).
   *
   * Le liste `pacRequiredTableControls` e `palRequiredTableControls` rispecchiano
   * 1:1 le liste lato backend (`nameTablePAC` / `nameTablePAL`). NON si fa unione:
   * - PAL contiene in più le tabelle CONSISTENZA_TEMPO_DET, RENDICONTI, SPESE_T2,
   *   VERIFICA_SOGLIA, LIMITE_SPESA (non presenti in PAC)
   * - `rimodulazioneTable` (DOTAZIONE_ORGANICA_RIMODULAZIONE) è presente in entrambi
   *   ed è gestita a parte da `updateRimodulazioneTableValidator` (conditional sul radio)
   */
  private applyTableValidatorsByReportType(): void {
    const requiredControls =
      this.reportType === 'PAL' ? this.palRequiredTableControls : this.pacRequiredTableControls;

    this.allKnownTableControls.forEach((name) => {
      const ctrl = this.form.get(name);
      if (!ctrl) return;
      if (requiredControls.includes(name)) {
        ctrl.setValidators([Validators.required]);
      } else {
        ctrl.clearValidators();
      }
      ctrl.updateValueAndValidity({ emitEvent: false });
    });
  }

  private loadSezione331Data(): void {
    if (this.piaoDTO?.id) {
      this.getPaRiferimento$()
        .pipe(
          switchMap((pa) =>
            this.sezione331Service.getSezione331ByIdPiao(this.piaoDTO.id!).pipe(
              tap((sezione331) => {
                if (sezione331) {
                  this.piaoDTO.idSezione331 = sezione331.id;
                }
              }),
              switchMap((sezione331) =>
                this.minervaService
                  .getTabella(
                    pa.codePA,
                    String(new Date().getFullYear()),
                    sezione331?.id || -1,
                    isStatoMinervaAttivo(sezione331?.statoSezione)
                  )
                  .pipe(
                    catchError(() => of(null)),
                    map((tabelleMinerva) => ({ sezione331, tabelleMinerva }))
                  )
              )
            )
          )
        )
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: ({ sezione331, tabelleMinerva }) => {
            if (sezione331) {
              this.sezione331Data = sezione331;
              this.piaoDTO.idSezione331 = sezione331.id;
              this.sessionStorageService.setItem(KEY_PIAO, this.piaoDTO);
            }
            if (tabelleMinerva) {
              this.tabelleMinerva = tabelleMinerva;
              this.reportType = this.extractReportType(tabelleMinerva);
            }
            this.createForm();
            this.cdr.markForCheck();
          },
          error: () => {
            this.createForm();
            this.cdr.markForCheck();
          },
        });
    } else {
      this.createForm();
    }
  }

  /**
   * Estrae il reportType (PAC | PAL | UNI) dalla response di Minerva.
   * Il campo è presente sulle entry della collezione tabelle, restituiamo il primo trovato.
   */
  private extractReportType(tabelleMinerva: any): string | null {
    if (!tabelleMinerva) return null;
    if (Array.isArray(tabelleMinerva)) {
      const entry = tabelleMinerva.find((t) => t && (t as any)['reportType']);
      return entry ? ((entry as any)['reportType'] as string) : null;
    }
    return (tabelleMinerva as any)?.['reportType'] ?? null;
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
          // Non considerare lo statoSezione del PIAO precedente
          (sezione331 as any).statoSezione = undefined;
          this.sezione331Data = sezione331;
          success = true;
        }
        if (tabelleMinerva) {
          this.tabelleMinerva = tabelleMinerva;
          this.reportType = this.extractReportType(tabelleMinerva);
        }
        this.createForm();
        this.cdr.markForCheck();
        return success;
      }),
      catchError((err) => {
        console.error('Errore nel caricamento dati sezione 3.3.1 dal PIAO precedente:', err);
        this.createForm();
        this.cdr.markForCheck();
        return of(false);
      })
    );
  }
}
