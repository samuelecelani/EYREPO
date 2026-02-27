import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  OnDestroy,
  Output,
  inject,
  ChangeDetectorRef,
  ViewChild,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormArray } from '@angular/forms';
import { BaseComponent } from '../../../../../../../shared/components/base/base.component';
import { ISezioneBase } from '../../../../../../../shared/models/interfaces/sezione-base.interface';
import { Observable, Subscription, catchError, forkJoin, of } from 'rxjs';
import { PIAODTO } from '../../../../../../../shared/models/classes/piao-dto';
import { Sezione22DTO } from '../../../../../../../shared/models/classes/sezione-22-dto';
import { Sezione22Service } from '../../../../../../../shared/services/sezioni-22.service';
import { INPUT_REGEX, KEY_PIAO, WARNING_ICON } from '../../../../../../../shared/utils/constants';
import { UlterioriInfoDTO } from '../../../../../../../shared/models/classes/ulteriori-info-dto';
import {
  createFormArrayObiettivoPerformanceFromPiaoSession,
  createFormArrayAdempimentoFromPiaoSession,
  createFormArrayFaseFromPiaoSession,
  createFormMongoFromPiaoSession,
  hasRequiredErrors,
  areAllValuesNull,
  minArrayLength,
  printFormErrors,
} from '../../../../../../../shared/utils/utils';
import { AllegatoDTO } from '../../../../../../../shared/models/classes/allegato-dto';
import { CodTipologiaAllegatoEnum } from '../../../../../../../shared/models/enums/cod-tipologia-allegato.enum';
import { CodTipologiaSezioneEnum } from '../../../../../../../shared/models/enums/cod-tipologia-sezione.enum';
import { SharedModule } from '../../../../../../../shared/module/shared/shared.module';
import { SwotElementListComponent } from '../../../../../../../shared/components/swot-element-list/swot-element-list.component';
import { TextAreaComponent } from '../../../../../../../shared/components/text-area/text-area.component';
import { TipologiaObbiettivo } from '../../../../../../../shared/models/enums/tipologia-obbiettivo.enum';
import { TipologiaAdempimento } from '../../../../../../../shared/models/enums/tipologia-adempimento.enum';
import { ObbiettiviPerformanceComponent } from '../../../../../../../shared/ui/obbiettivi-performance/obbiettivi-performance.component';
import { AdempimentiComponent } from '../../../../../../../shared/ui/adempimenti/adempimenti.component';
import { FasiComponent } from '../../../../../../../shared/ui/fasi/fasi.component';
import { ObbiettiviTrasversaliComponent } from '../../../../../../../shared/ui/obbiettivi-trasversali/obbiettivi-trasversali.component';
import { CardAlertComponent } from '../../../../../../../shared/ui/card-alert/card-alert.component';
import { DynamicTBComponent } from '../../../../../../../shared/components/dynamic-tb/dynamic-tb.component';
import { AttachmentComponent } from '../../../../../../../shared/ui/attachment/attachment.component';
import { DynamicTBConfig } from '../../../../../../../shared/models/classes/config/dynamic-tb';
import { SectionStatusEnum } from '../../../../../../../shared/models/enums/section-status.enum';
import { SECTION_FIELDS_REQUIRED } from '../../../../../../../shared/utils/section-fields-required';
import { SectionEnum } from '../../../../../../../shared/models/enums/section.enum';
import { ObiettiviPerformanceIndividualeComponent } from '../../../../../../../shared/ui/obiettivi-performance-individuale/obiettivi-performance-individuale.component';
import { CodTipologiaIndicatoreEnum } from '../../../../../../../shared/models/enums/cod-tipologia-indicatore.enum';
import { SessionStorageService } from '../../../../../../../shared/services/session-storage.service';
import { OVPDTO } from '../../../../../../../shared/models/classes/ovp-dto';
import { OvpService } from '../../../../../../../shared/services/ovp.service';
import { LabelValue } from '../../../../../../../shared/models/interfaces/label-value';

@Component({
  selector: 'piao-sezione-2-2',
  imports: [
    SharedModule,
    TextAreaComponent,
    DynamicTBComponent,
    AttachmentComponent,
    ReactiveFormsModule,
    CardAlertComponent,
    ObbiettiviPerformanceComponent,
    AdempimentiComponent,
    FasiComponent,
    ObbiettiviTrasversaliComponent,
    ObiettiviPerformanceIndividualeComponent,
  ],
  templateUrl: './sezione2.2.component.html',
  styleUrl: './sezione2.2.component.scss',
})
export class Sezione22Component extends BaseComponent implements OnInit, OnDestroy, ISezioneBase {
  form!: FormGroup;
  private fb = inject(FormBuilder);
  private sezione22Service = inject(Sezione22Service);
  private ovpService = inject(OvpService);
  private subscription = new Subscription();
  private sessionStorageService = inject(SessionStorageService);

  isFormReady = false;

  @Input() piaoDTO!: PIAODTO;
  @Input() sezione22Data?: Sezione22DTO;
  @Output() formValueChanged = new EventEmitter<any>();

  @ViewChild(AttachmentComponent) attachmentComponent?: AttachmentComponent;

  ovpList: OVPDTO[] = [];
  ovpOptions: LabelValue[] = [];
  private ovpMap: Map<number, OVPDTO> = new Map();

  iconAlert: string = WARNING_ICON;
  // Mappa degli obiettivi per tipologia
  obbiettiviPerformanceMap: Map<TipologiaObbiettivo, FormArray> = new Map();
  // FormArray degli obiettivi performance per il template
  obbiettiviPerformance?: FormArray;
  // FormArray degli obiettivi trasversali per il template
  obbiettiviAccessiDigitale?: FormArray;
  obbiettiviAccessiFisici?: FormArray;
  obbiettiviSemplificazione?: FormArray;
  obbiettiviPariOpportunita?: FormArray;
  obbiettiviPerformanceOrganizzativa?: FormArray;
  obbiettiviPerformanceIndividuale?: FormArray;
  adempimentiMap: Map<TipologiaAdempimento, FormArray> = new Map();
  // FormArray degli adempimenti per il template
  adempimentiInnovazioniAmm?: FormArray;
  adempimentiComportamentiUni?: FormArray;
  adempimentiObiettivi?: FormArray;
  adempimentiObiettiviInfra?: FormArray;
  adempimentiObiettiviPatrimoniali?: FormArray;

  // Espone l'enum al template
  TipologiaObbiettivo = TipologiaObbiettivo;
  TipologiaAdempimento = TipologiaAdempimento;
  TipologiaIndicatoreEnum = CodTipologiaIndicatoreEnum;

  // Gestione stato accordion adempimenti
  openAccordionIndex: number | null = null;

  // Titolo principale
  title: string = 'SEZIONE_22.TITLE';
  subTitleParteGenerale: string = 'SEZIONE_22.PARTE_GENERALE';
  titleParteFunzionale: string = 'SEZIONE_22.PARTE_FUNZIONALE';
  titleObbPerformanceAlert: string = 'SEZIONE_22.OBIETTIVI_PERFORMANCE.CARD_ALERT.TITLE';
  subObbPerformanceAlert: string = 'SEZIONE_22.OBIETTIVI_PERFORMANCE.CARD_ALERT.SUB_TITLE';
  // Ciclo performance
  subTitleCicloPerformance: string = 'SEZIONE_22.CICLO_PERFORMANCE';
  descriptionCicloPerformance: string = 'SEZIONE_22.DESCRIPTION_CICLO_PERFORMANCE';
  tooltipCicloPerformance: string = 'SEZIONE_22.TOOLTIP_CICLO_PERFORMANCE';

  // Obiettivi Performance PA
  titleObiettiviPerformancePA: string = 'SEZIONE_22.TITLE_OBIETTIVI_PERFORMANCE_PA';
  descriptionObiettiviPerformancePA: string = 'SEZIONE_22.DESCRIPTION_OBIETTIVI_PERFORMANCE_PA';
  tooltipObiettiviPerformancePA: string = 'SEZIONE_22.TOOLTIP_OBIETTIVI_PERFORMANCE_PA';

  // Adempimenti
  titleAdempimenti: string = 'SEZIONE_22.ADEMPIMENTI.TITLE_ADEMPIMENTI';
  subTitleAdempimenti: string = 'SEZIONE_22.ADEMPIMENTI.SUB_TITLE_ADEMPIMENTI';
  tooltipAdempimenti: string = 'SEZIONE_22.ADEMPIMENTI.TOOLTIP_ADEMPIMENTI';

  //Obbiettivi Trasversali
  titleObbiettiviTrasversali: string =
    'SEZIONE_22.OBBIETTIVI_TRASVERSALI.TITLE_OBIETTIVI_TRASVERSALI';
  subTitleObbiettiviTrasversali: string =
    'SEZIONE_22.OBBIETTIVI_TRASVERSALI.SUB_TITLE_OBIETTIVI_TRASVERSALI';
  tooltipObbiettiviTrasversali: string =
    'SEZIONE_22.OBBIETTIVI_TRASVERSALI.TOOLTIP_OBIETTIVI_TRASVERSALI';

  // Collegamento Performance
  titlePerformanceOrganizzativa: string = 'SEZIONE_22.PERFORMANCE_ORGANIZZATIVA_TITLE';
  descriptionPerformanceOrganizzativa: string = 'SEZIONE_22.PERFORMANCE_ORGANIZZATIVA_DESCRIPTION';
  labelPerformanceOrganizzativa: string = 'SEZIONE_22.PERFORMANCE_ORGANIZZATIVA_LABEL';
  titleCollegamentoPerformance: string = 'SEZIONE_22.TITLE_COLLEGAMENTO_PERFORMANCE';
  descriptionCollegamentoPerformance: string = 'SEZIONE_22.DESCRIPTION_COLLEGAMENTO_PERFORMANCE';
  tooltipCollegamentoPerformance: string = 'SEZIONE_22.TOOLTIP_COLLEGAMENTO_PERFORMANCE';

  // Performance Individuale
  titlePerformanceIndividuale: string = 'SEZIONE_22.TITLE_PERFORMANCE_INDIVIDUALE';
  descriptionPerformanceIndividuale: string = 'SEZIONE_22.DESCRIPTION_PERFORMANCE_INDIVIDUALE';
  tooltipPerformanceIndividuale: string = 'SEZIONE_22.TOOLTIP_PERFORMANCE_INDIVIDUALE';
  titleDynamicTBUlterioreInfo: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TB.ULTERIORE_INFO.TITLE';
  subTitleDynamicTBUlterioreInfo: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TB.ULTERIORE_INFO.SUB_TITLE';
  titleCardUlterioriInfo: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ULTERIORI_INFO.CARD_TITLE';
  labelBtnAddUlterioriInfo: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ULTERIORI_INFO.BTN_ADD';
  // Labels
  labelIntroduzione: string = 'SEZIONE_22.LABEL_INTRODUZIONE';
  labelDescrizione: string = 'SEZIONE_22.LABEL_DESCRIZIONE';

  //ALLEGATI
  allegati: AllegatoDTO[] = [];
  codTipologia: string = CodTipologiaSezioneEnum.SEZ2_2;
  codTipologiaAllegato: string = CodTipologiaAllegatoEnum.ALLEGATO_SEZIONE;

  sezioneSection: SectionEnum = SectionEnum.SEZIONE_2_2;

  //Ulteriori Info
  ulterioriInfoConfig!: DynamicTBConfig;

  ngOnInit(): void {
    this.initUlterioriInfoConfig();

    // Carica i dati aggiornati dal BE prima di creare il form
    this.loadSezione22Data();

    // Sottoscrizione agli aggiornamenti della sezione22
    this.subscription.add(
      this.sezione22Service.onSezione22Updated$.subscribe((sezione22) => {
        if (sezione22) {
          this.sezione22Data = sezione22;
          this.reloadForm(sezione22);
        }
      })
    );
  }

  private loadSezione22Data(): void {
    if (this.piaoDTO?.id) {
      const sezione22$ = this.sezione22Service
        .getSezione22ByIdPiao(this.piaoDTO.id)
        .pipe(catchError(() => of(null)));
      const ovp$ = this.ovpService.getAllOvpByIdPiao(this.piaoDTO.id).pipe(
        catchError((err) => {
          console.error('Errore nel caricamento degli OVP:', err);
          return of([] as OVPDTO[]);
        })
      );

      forkJoin([sezione22$, ovp$]).subscribe(([sezione22Data, ovpList]) => {
        // Gestione dati sezione 22
        if (sezione22Data) {
          this.sezione22Data = sezione22Data;
          this.piaoDTO.idSezione22 = sezione22Data.id;
          this.sessionStorageService.setItem(KEY_PIAO, this.piaoDTO);
        }

        // Gestione OVP
        this.ovpList = ovpList ? ovpList : [];

        this.createForm();
      });
    } else {
      this.createForm();
    }
  }
  createForm(): void {
    const sezione22 = this.sezione22Data || new Sezione22DTO();

    // Raggruppa gli obiettivi per tipologia
    this.createObbiettiviPerformanceMap(sezione22.obbiettiviPerformance || []);
    // Raggruppa gli adempimenti per tipologia
    this.createAdempimentiMap(sezione22.adempimenti || []);
    // Crea il FormArray per le fasi

    this.form = this.fb.group({
      idPiao: this.fb.control<number | null>(this.piaoDTO?.id || null),
      statoSezione: this.fb.control<string | null>(sezione22.statoSezione || null, [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),

      introPerformance: this.fb.control<string | null>(sezione22.introPerformance || null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
      ]),
      introObiettiviPerformance: this.fb.control<string | null>(
        sezione22.introObiettiviPerformance || null,
        [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]
      ),
      introAdempimenti: this.fb.control<string | null>(sezione22.introAdempimenti || null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
      ]),
      descriptionCollegamentoPerformance: this.fb.control<string | null>(
        sezione22.descriptionCollegamentoPerformance || null,
        [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]
      ),
      introPerformanceOrganizzativa: this.fb.control<string | null>(
        sezione22.introPerformanceOrganizzativa || null,
        [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]
      ),
      introPerformanceIndividuale: this.fb.control<string | null>(
        sezione22.introPerformanceIndividuale || null,
        [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]
      ),
      fasi:
        createFormArrayFaseFromPiaoSession(
          this.fb,
          sezione22.fase || [],
          INPUT_REGEX,
          this.piaoDTO?.id
        ) || this.fb.array([]),
      // Ulteriori Info
      ulterioriInfo: createFormMongoFromPiaoSession<UlterioriInfoDTO>(
        this.fb,
        sezione22.ulterioriInfo || new UlterioriInfoDTO(),
        ['id', 'externalId', 'properties'],
        INPUT_REGEX,
        50,
        false
      ),
      // Allegati
      allegati: this.fb.array<FormGroup>([
        this.fb.group({
          id: [
            sezione22.allegati?.[0]?.id || null,
            [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
          ],
          idEntitaFK: [
            sezione22.allegati?.[0]?.idEntitaFK || null,
            [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
          ],
          codDocumento: [
            sezione22.allegati?.[0]?.codDocumento || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          codTipologiaFK: [
            sezione22.allegati?.[0]?.codTipologiaFK || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          codTipologiaAllegato: [
            sezione22.allegati?.[0]?.codTipologiaAllegato || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          downloadUrl: [
            sezione22.allegati?.[0]?.downloadUrl || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          sizeAllegato: [
            sezione22.allegati?.[0]?.sizeAllegato || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          descrizione: [
            sezione22.allegati?.[0]?.descrizione || null,
            [Validators.maxLength(100), Validators.pattern(INPUT_REGEX)],
          ],
        }),
      ]),
      codTipologia: this.fb.control<string>(CodTipologiaSezioneEnum.SEZ2_2),
      codTipologiaAllegato: this.fb.control<string>(CodTipologiaAllegatoEnum.ALLEGATO_SEZIONE),
    });

    this.obbiettiviPerformanceMap.forEach((formArray, tipologia) => {
      if (
        tipologia === TipologiaObbiettivo.PERFORMANCE ||
        tipologia === TipologiaObbiettivo.PERFORMANCE_ORGANIZZATIVA ||
        tipologia === TipologiaObbiettivo.PERFORMANCE_INDIVIDUALE
      ) {
        this.form.addControl(`obbiettivi_${tipologia}`, formArray);
      } else {
        formArray.setValidators([minArrayLength(1)]);
        this.form.addControl(`obbiettivi_${tipologia}`, formArray);
        formArray.updateValueAndValidity();
      }
    });

    // Aggiungi i FormArray degli adempimenti al FormGroup principale
    this.adempimentiMap.forEach((formArray, tipologia) => {
      // Set validators on the FormArray itself
      formArray.setValidators([minArrayLength(1)]);
      this.form.addControl(`adempimenti_${tipologia}`, formArray);
      formArray.updateValueAndValidity();
    });

    // Aggiorna la validità dell'intero form
    this.form.updateValueAndValidity();

    // Emetti il valore del form dopo la creazione
    this.isFormReady = true;
    this.formValueChanged.emit(this.form.value);
  }

  reloadForm(sezione22?: Sezione22DTO): void {
    const data = sezione22 || new Sezione22DTO();

    // Aggiorna le fasi
    const fasiFormArray =
      createFormArrayFaseFromPiaoSession(this.fb, data.fase || [], INPUT_REGEX, this.piaoDTO?.id) ||
      this.fb.array([]);
    this.form.setControl('fasi', fasiFormArray);

    this.createAdempimentiMap(data.adempimenti || []);

    // Aggiorna i FormArray degli adempimenti
    this.adempimentiMap.forEach((formArray, tipologia) => {
      formArray.setValidators([minArrayLength(1)]);
      this.form.setControl(`adempimenti_${tipologia}`, formArray);
      formArray.updateValueAndValidity();
    });

    this.form.updateValueAndValidity();
    this.isFormReady = true;
    this.formValueChanged.emit(this.form.value);
  }

  /**
   * Crea una mappa di FormArray raggruppati per tipologia
   */
  private createObbiettiviPerformanceMap(obiettivi: any[]): void {
    // Raggruppa gli obiettivi per tipologia
    const grouped = new Map<TipologiaObbiettivo, any[]>();

    // Inizializza le tipologie principali
    grouped.set(TipologiaObbiettivo.PERFORMANCE, []);
    grouped.set(TipologiaObbiettivo.ACCESSI_DIGITALE, []);
    grouped.set(TipologiaObbiettivo.ACCESSI_FISICI, []);
    grouped.set(TipologiaObbiettivo.SEMPLIFICAZIONE, []);
    grouped.set(TipologiaObbiettivo.PARI_OPPORTUNITA, []);
    grouped.set(TipologiaObbiettivo.PERFORMANCE_ORGANIZZATIVA, []);
    grouped.set(TipologiaObbiettivo.PERFORMANCE_INDIVIDUALE, []);

    // Raggruppa gli obiettivi esistenti
    obiettivi.forEach((obiettivo) => {
      // Converte la stringa tipologia in enum value
      const tipologiaString = obiettivo.tipologia as string;
      const tipologia = TipologiaObbiettivo[tipologiaString as keyof typeof TipologiaObbiettivo];

      if (tipologia !== undefined && grouped.has(tipologia)) {
        grouped.get(tipologia)!.push(obiettivo);
      } else {
        console.warn(
          '[Sezione22Component] Tipologia non riconosciuta o mancante:',
          tipologiaString,
          'enum value:',
          tipologia
        );
      }
    });

    console.log(
      '[Sezione22Component] Grouped PERFORMANCE:',
      grouped.get(TipologiaObbiettivo.PERFORMANCE)
    );

    // Crea i FormArray per ogni tipologia
    grouped.forEach((obiettiviPerTipologia, tipologia) => {
      const formArray = createFormArrayObiettivoPerformanceFromPiaoSession(
        this.fb,
        obiettiviPerTipologia,
        INPUT_REGEX,
        this.piaoDTO.id || 0
      );
      this.obbiettiviPerformanceMap.set(tipologia, formArray!);
    });

    // Aggiorna la property per il template
    this.obbiettiviPerformance = this.obbiettiviPerformanceMap.get(TipologiaObbiettivo.PERFORMANCE);
    this.obbiettiviAccessiDigitale = this.obbiettiviPerformanceMap.get(
      TipologiaObbiettivo.ACCESSI_DIGITALE
    );
    this.obbiettiviAccessiFisici = this.obbiettiviPerformanceMap.get(
      TipologiaObbiettivo.ACCESSI_FISICI
    );
    this.obbiettiviSemplificazione = this.obbiettiviPerformanceMap.get(
      TipologiaObbiettivo.SEMPLIFICAZIONE
    );
    this.obbiettiviPariOpportunita = this.obbiettiviPerformanceMap.get(
      TipologiaObbiettivo.PARI_OPPORTUNITA
    );
    this.obbiettiviPerformanceOrganizzativa = this.obbiettiviPerformanceMap.get(
      TipologiaObbiettivo.PERFORMANCE_ORGANIZZATIVA
    );
    this.obbiettiviPerformanceIndividuale = this.obbiettiviPerformanceMap.get(
      TipologiaObbiettivo.PERFORMANCE_INDIVIDUALE
    );
  }

  /**
   * Crea e raggruppa gli adempimenti per tipologia
   */
  private createAdempimentiMap(adempimenti: any[]): void {
    // Raggruppa gli adempimenti per tipologia
    const grouped = new Map<TipologiaAdempimento, any[]>();

    // Inizializza le tipologie principali
    grouped.set(TipologiaAdempimento.INNOVAZIONI_AMM, []);
    grouped.set(TipologiaAdempimento.COMPORTAMENTI_UNI, []);
    grouped.set(TipologiaAdempimento.OBIETTIVI, []);
    grouped.set(TipologiaAdempimento.OBIETTIVI_INFRA, []);
    grouped.set(TipologiaAdempimento.OBIETTIVI_PATRIMONIALI, []);

    // Raggruppa gli adempimenti esistenti
    adempimenti.forEach((adempimento) => {
      // Converte la stringa tipologia in enum value
      const tipologiaString = adempimento.tipologia as string;
      const tipologia = TipologiaAdempimento[tipologiaString as keyof typeof TipologiaAdempimento];

      console.log(
        '[Sezione22Component] Adempimento tipologia string:',
        tipologiaString,
        'enum value:',
        tipologia,
        'adempimento:',
        adempimento
      );

      if (tipologia !== undefined && grouped.has(tipologia)) {
        grouped.get(tipologia)!.push(adempimento);
      } else {
        console.warn(
          '[Sezione22Component] Tipologia adempimento non riconosciuta:',
          tipologiaString,
          'enum value:',
          tipologia
        );
      }
    });

    // Crea i FormArray per ogni tipologia
    grouped.forEach((adempimentiPerTipologia, tipologia) => {
      const formArray = createFormArrayAdempimentoFromPiaoSession(
        this.fb,
        adempimentiPerTipologia,
        INPUT_REGEX,
        this.piaoDTO.id || 0
      );
      this.adempimentiMap.set(tipologia, formArray!);
    });

    // Aggiorna le property per il template
    this.adempimentiInnovazioniAmm = this.adempimentiMap.get(TipologiaAdempimento.INNOVAZIONI_AMM);
    this.adempimentiComportamentiUni = this.adempimentiMap.get(
      TipologiaAdempimento.COMPORTAMENTI_UNI
    );
    this.adempimentiObiettivi = this.adempimentiMap.get(TipologiaAdempimento.OBIETTIVI);
    this.adempimentiObiettiviInfra = this.adempimentiMap.get(TipologiaAdempimento.OBIETTIVI_INFRA);
    this.adempimentiObiettiviPatrimoniali = this.adempimentiMap.get(
      TipologiaAdempimento.OBIETTIVI_PATRIMONIALI
    );

    console.log('[Sezione22Component] adempimentiInnovazioniAmm:', this.adempimentiInnovazioniAmm);
    console.log(
      '[Sezione22Component] adempimentiInnovazioniAmm length:',
      this.adempimentiInnovazioniAmm?.length
    );
    console.log(
      '[Sezione22Component] adempimentiInnovazioniAmm value:',
      this.adempimentiInnovazioniAmm?.value
    );
    console.log('[Sezione22Component] adempimenti updated');
  }

  /**
   * Ottiene il FormArray degli obiettivi per una specifica tipologia
   */
  getObbiettiviByTipologia(tipologia: TipologiaObbiettivo): FormArray | undefined {
    return this.obbiettiviPerformanceMap.get(tipologia);
  }

  /**
   * Ottiene il FormArray degli adempimenti per una specifica tipologia
   */
  getAdempimentiByTipologia(tipologia: TipologiaAdempimento): FormArray | undefined {
    return this.adempimentiMap.get(tipologia);
  }

  getForm(): FormGroup {
    return this.form;
  }

  isFormValid(): boolean {
    return true;
  }

  hasFormValues(): boolean {
    return !areAllValuesNull(this.form);
  }

  resetForm(): void {
    this.form.reset();
    this.createForm();
  }

  prepareDataForSave(): Sezione22DTO {
    const sezione22 = this.sezione22Data || new Sezione22DTO();

    // Raccogli tutti gli obiettivi da tutte le mappe
    const allObiettivi: any[] = [];
    this.obbiettiviPerformanceMap.forEach((formArray) => {
      console.log(
        '[Sezione22Component] Preparing obiettivi for save, formArray value:',
        formArray.value
      );
      if (formArray && formArray.value) {
        const obiettivi = formArray.value.map((obiettivo: any, index: number) => {
          // Trova l'obiettivo originale per preservare gli ID
          const originalObiettivo = sezione22.obbiettiviPerformance?.find(
            (orig: any) => orig.id === obiettivo.id
          );

          // Trasforma l'array di ID stakeholder in array di ObiettivoStakeHolderDTO
          const stakeholdersIds = obiettivo.stakeholders || [];
          console.log('stake', stakeholdersIds);
          const stakeholdersTransformed = stakeholdersIds.map((selectedId: number) => {
            // Cerca se questo stakeholder esisteva già nell'obiettivo originale
            const existingStakeholder = originalObiettivo?.stakeholders?.find(
              (sh: any) => sh.stakeholder?.id === selectedId
            );

            // Se esiste, preserva l'ID dell'ObiettivoStakeHolderDTO, altrimenti crea nuovo
            return existingStakeholder
              ? { id: existingStakeholder.id, stakeholder: { id: selectedId } }
              : { stakeholder: { id: selectedId } };
          });

          return {
            ...obiettivo,
            stakeholders: stakeholdersTransformed,
          };
        });
        allObiettivi.push(...obiettivi);
      }
    });

    // Raccogli tutti gli adempimenti da tutte le mappe
    const allAdempimenti: any[] = [];
    this.adempimentiMap.forEach((formArray) => {
      if (formArray && formArray.value) {
        allAdempimenti.push(...formArray.value);
      }
    });

    return {
      ...sezione22,
      idPiao: this.form.controls['idPiao'].value || null,
      introPerformance: this.form.controls['introPerformance'].value || null,
      introObiettiviPerformance: this.form.controls['introObiettiviPerformance'].value || null,
      introAdempimenti: this.form.controls['introAdempimenti'].value || null,
      introPerformanceOrganizzativa:
        this.form.controls['introPerformanceOrganizzativa'].value || null,
      descriptionCollegamentoPerformance:
        this.form.controls['descriptionCollegamentoPerformance'].value || null,
      introPerformanceIndividuale: this.form.controls['introPerformanceIndividuale'].value || null,
      obbiettiviPerformance: allObiettivi,
      adempimenti: allAdempimenti,
      fase: this.form.get('fasi')?.value || [],
      ulterioriInfo: this.form.controls['ulterioriInfo'].value || null,
      allegati: this.form.controls['allegati'].value || null,
      statoSezione: this.getSectionStatus(),
    };
  }

  validate(): Observable<any> {
    const sezione22Id = this.sezione22Data?.id || this.piaoDTO?.idSezione22 || -1;
    return this.sezione22Service.validation(sezione22Id);
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

  private initUlterioriInfoConfig(): void {
    this.ulterioriInfoConfig = {
      labelTB: 'Nome campo',
      typeTB: 'text',
      idTB: 'value',
      controlTB: 'value',
      controlTBMongo: 'key',
      classTB: 'input',
      maxValidatorLengthTB: 50,
      descTooltipTB: 'Nome campo',
    };
  }

  private getOvpDropdownOptions(ovpList: OVPDTO[]): LabelValue[] {
    return ovpList.map((ovp) => ({
      label: ovp.denominazione || '',
      value: ovp.id || 0,
    }));
  }

  handleAttachmentLoaded(): void {
    // Riemetti il valore del form quando gli allegati vengono caricati
    this.formValueChanged.emit(this.form.value);
  }

  get stakeholderOptions(): LabelValue[] {
    if (!this.piaoDTO?.stakeHolders) {
      return [];
    }
    return this.piaoDTO.stakeHolders.map((stakeholder: any) => ({
      label: stakeholder.nomeStakeHolder || '',
      value: stakeholder.id || 0,
    }));
  }

  reloadAttachment(): void {
    this.attachmentComponent?.getAllAttachment();
  }

  override ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}
