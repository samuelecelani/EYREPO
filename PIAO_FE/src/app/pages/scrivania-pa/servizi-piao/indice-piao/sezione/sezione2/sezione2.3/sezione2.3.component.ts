import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import { TextAreaComponent } from '../../../../../../../shared/components/text-area/text-area.component';
import { SharedModule } from '../../../../../../../shared/module/shared/shared.module';
import { ISezioneBase } from '../../../../../../../shared/models/interfaces/sezione-base.interface';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin, Observable, of, Subscription, switchMap } from 'rxjs';
import { PIAODTO } from '../../../../../../../shared/models/classes/piao-dto';
import { Sezione23DTO } from '../../../../../../../shared/models/classes/sezione-23-dto';
import {
  areAllValuesNull,
  createFormArrayAttivitaSensibileFromPiaoSession,
  createFormArrayFromPiaoSession,
  createFormArrayGenericIndicatoreFromPiaoSession,
  createFormArrayMonitoraggioPrevenzioneFromPiaoSession,
  createFormArrayObiettivoPrevenzioneCorruzioneTrasparenzaFromPiaoSession,
  createFormMongoFromPiaoSession,
  hasRequiredErrors,
  minArrayLength,
  printFormErrors,
} from '../../../../../../../shared/utils/utils';
import { ObiettivoPrevenzioneDTO } from '../../../../../../../shared/models/classes/obiettivo-prevenzione-dto';
import { SectionStatusEnum } from '../../../../../../../shared/models/enums/section-status.enum';
import { Sezione23Service } from '../../../../../../../shared/services/sezione23.service';
import { INPUT_REGEX, KEY_PIAO, WARNING_ICON } from '../../../../../../../shared/utils/constants';
import { ObbligoLeggeService } from '../../../../../../../shared/services/obbligo-legge.service';
import { ObbligoLeggeDTO } from '../../../../../../../shared/models/classes/obbligo-legge-dto';
import { UlterioriInfoDTO } from '../../../../../../../shared/models/classes/ulteriori-info-dto';
import { BaseComponent } from '../../../../../../../shared/components/base/base.component';
import { AttachmentComponent } from '../../../../../../../shared/ui/attachment/attachment.component';
import { DynamicTBComponent } from '../../../../../../../shared/components/dynamic-tb/dynamic-tb.component';
import { DynamicTBConfig } from '../../../../../../../shared/models/classes/config/dynamic-tb';
import { CodTipologiaAllegatoEnum } from '../../../../../../../shared/models/enums/cod-tipologia-allegato.enum';
import { CodTipologiaSezioneEnum } from '../../../../../../../shared/models/enums/cod-tipologia-sezione.enum';
import { SectionEnum } from '../../../../../../../shared/models/enums/section.enum';
import { CardAlertComponent } from '../../../../../../../shared/ui/card-alert/card-alert.component';
import { ModalComponent } from '../../../../../../../shared/components/modal/modal.component';
import { ValutazioneRischioComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-2.3/valutazione-rischio/valutazione-rischio.component';
import { GestioneRischioComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-2.3/gestione-rischio/gestione-rischio.component';
import { ElencoAttivitaSensibileComponent } from 'src/app/shared/ui/servizi-piao/indice-piao/sezione/sezione-2.3/attivita-sensibile/elenco-attivita-sensibile.component';
import { ElencoAdempimentoNormativoComponent } from 'src/app/shared/ui/servizi-piao/indice-piao/sezione/sezione-2.3/adempimenti-normativi/elenco-adempimento-normativo/elenco-adempimento-normativo.component';
import { ElencoMonitoraggioMisurePrevenzioneComponent } from 'src/app/shared/ui/servizi-piao/indice-piao/sezione/sezione-2.3/monitoraggio-misure-prevenzione/elenco-monitoraggio-misure-prevenzione/elenco-monitoraggio-misure-prevenzione.component';
import { ObiettiviGeneraliComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-2.3/obiettivi-generali/obiettivi-generali.component';
import { MisureGeneraliComponent } from '../../../../../../../shared/ui/misure-generali/misure-generali.component';
import { AdempimentoNormativoDTO } from '../../../../../../../shared/models/classes/adempimento-normativo-dto';
import { AttivitaSensibileDTO } from '../../../../../../../shared/models/classes/attivita-sensibile-dto';
import { FattoreDTO } from '../../../../../../../shared/models/classes/fattore-dto';
import { ModalDatiPubblicazioneComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-2.3/dati-pubblicazione/modal-dati-pubblicazione/modal-dati-pubblicazione.component';
import { ValorePubblicoAnticorruzioneComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-2.3/valore-pubblico-anticorruzione/valore-pubblico-anticorruzione.component';
import { SessionStorageService } from '../../../../../../../shared/services/session-storage.service';
import { DenominazioneObbligoLeggeComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-2.3/denominazione-obbligo-legge/denominazione-obbligo-legge.component';

@Component({
  selector: 'piao-sezione-2-3',
  imports: [
    SharedModule,
    TextAreaComponent,
    AttachmentComponent,
    DynamicTBComponent,
    CardAlertComponent,
    ReactiveFormsModule,
    ValutazioneRischioComponent,
    ElencoAttivitaSensibileComponent,
    ElencoAdempimentoNormativoComponent,
    ObiettiviGeneraliComponent,
    MisureGeneraliComponent,
    ElencoMonitoraggioMisurePrevenzioneComponent,
    ModalComponent,
    ModalDatiPubblicazioneComponent,
    GestioneRischioComponent,
    ValorePubblicoAnticorruzioneComponent,
    DenominazioneObbligoLeggeComponent,
  ],
  templateUrl: './sezione2.3.component.html',
  styleUrl: './sezione2.3.component.scss',
})
export class Sezione23Component extends BaseComponent implements OnInit, OnDestroy, ISezioneBase {
  @Input() piaoDTO!: PIAODTO;
  @Input() sezione23Data?: Sezione23DTO;
  @Output() formValueChanged = new EventEmitter<any>();

  isFormReady = false;

  sezione23Service: Sezione23Service = inject(Sezione23Service);
  private sessionStorageService = inject(SessionStorageService);

  @ViewChild(AttachmentComponent) attachmentComponent?: AttachmentComponent;
  @ViewChild(ElencoAttivitaSensibileComponent)
  elencoAttivitaSensibileComponent?: ElencoAttivitaSensibileComponent;

  private obbligoLeggeService = inject(ObbligoLeggeService);
  private fb = inject(FormBuilder);
  private subscription = new Subscription();

  form!: FormGroup;

  iconAlert: string = WARNING_ICON;

  // Titolo principale
  title: string = 'SEZIONE_23.TITLE';
  // Parte Generale
  subTitleParteGenerale: string = 'SEZIONE_23.PARTE_GENERALE';
  // Title Attuazione
  titleAttuazione: string = 'SEZIONE_23.ATTUAZIONE_TITLE';
  // SubTitle Attuazione
  subTitleAttuazione: string = 'SEZIONE_23.ATTUAZIONE_SUB_TITLE';
  // Attuazione label
  attuazioneLabel: string = 'SEZIONE_23.ATTUAZIONE_LABEL';
  //Parte Funzionale
  subTitleParteFunzionale: string = 'SEZIONE_23.PARTE_FUNZIONALE';

  //Analisi del contesto
  titleAnalisiContesto: string = 'SEZIONE_23.ANALISI_CONTESTO_TITLE';
  subTitleAnalisiContesto: string = 'SEZIONE_23.ANALISI_CONTESTO_SUB_TITLE';
  titleAnalisiContestoAlert: string = 'SEZIONE_23.CARD_ALERT_ANALISI_TITLE';
  subAnalisiContestoAlert: string = 'SEZIONE_23.CARD_ALERT_ANALISI_SUB_TITLE';
  labelImpattiContestoEsterno: string = 'SEZIONE_23.IMPATTO_CONTESTO_ESTERNO_LABEL';
  labelImpattiContestoInterno: string = 'SEZIONE_23.IMPATTO_CONTESTO_INTERNO_LABEL';

  //Prevenzione della corruzione e trasparenza
  titlePrevenzioneCorruzione: string = 'SEZIONE_23.PREVENZIONE_CORRUZIONE_TITLE';
  subTitlePrevenzioneCorruzione: string = 'SEZIONE_23.PREVENZIONE_CORRUZIONE_SUB_TITLE';
  titleModalitaGestione: string = 'SEZIONE_23.MODALITA_GESTIONE_TITLE';
  labelDescSistemaGestione: string = 'SEZIONE_23.DESC_SISTEMA_GESTIONE_LABEL';
  labelDescModalitaIdentificazione: string = 'SEZIONE_23.DESC_MODALITA_INDENTIFICAZIONE_LABEL';
  labelDescModalitaAnalisi: string = 'SEZIONE_23.DESC_MODALITA_ANALISI_LABEL';
  labelDescModalitaMisurazione: string = 'SEZIONE_23.DESC_MODALITA_MISURAZIONE_LABEL';
  labelDescModalitaTrattamento: string = 'SEZIONE_23.DESC_MODALITA_TRATTAMENTO_LABEL';
  labelDescModalitaMonitoraggio: string = 'SEZIONE_23.DESC_MODALITA_MONITORAGGIO_LABEL';

  //Obiettivi Generali di Prevenzione della Corruzione e trasparenza
  titleObiettiviGeneraliCorruzioneTrasparenza: string =
    'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.OBIETTIVI_PREVENZIONE_CORRUZIONE_TITLE';
  labelIntroObiettiviPrevenzione: string =
    'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.INTRO_DEFINIZIONE_OBIETTIVI_LABEL';

  //Misure Generali di Prevenzione del rischio corruttivo
  titleMisurePrevenzione: string =
    'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.MISURE_GENERALI_PREVENZIONE_TITLE';
  labelIntroMisurePrevenzione: string =
    'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.INTRO_DEFINIZIONE_MISURE_LABEL';

  //TRASPARENZA
  titleTrasparenza: string = 'SEZIONE_23.TRASPARENZA.TITLE_TRASPARENZA';
  subTitleTrasparenza: string = 'SEZIONE_23.TRASPARENZA.SUB_TITLE_TRASPARENZA';
  labelIntroTrasparenza: string = 'SEZIONE_23.TRASPARENZA.DESC_TRASPARENZA_LABEL';

  //Valore pubblico e anticorruzione
  titleValorePubblicoAnticorruzione: string = 'SEZIONE_23.ANTICORRUZIONE.TITLE_ANTICORRUZIONE';
  subTitleValorePubblicoAnticorruzione: string =
    'SEZIONE_23.ANTICORRUZIONE.SUB_TITLE_ANTICORRUZIONE';
  subTitleValorePubblicoAnticorruzioneIntro: string =
    'SEZIONE_23.ANTICORRUZIONE.SUB_TITLE_OBIETTIVI_ANTICORRUZIONE';
  labelDescIniziativeAnticorruzione: string = 'SEZIONE_23.ANTICORRUZIONE.DESC_ANTICORRUZIONE_LABEL';

  //Attività sensibili
  titleAttivitaSensibili: string = 'SEZIONE_23.ATTIVITA_SENSIBILI.TITLE_ATTIVITA_SENSIBILI';
  subTitleAttivitaSensibili: string = 'SEZIONE_23.ATTIVITA_SENSIBILI.SUB_TITLE_ATTIVITA_SENSIBILI';
  labelIntroAttivitaSensibili: string =
    'SEZIONE_23.ATTIVITA_SENSIBILI.DESC_ATTIVITA_SENSIBILI_LABEL';
  subTitleCardAlertAttivitaSensibili: string = 'SEZIONE_23.ATTIVITA_SENSIBILI.CARD_ALERT_SUB_TITLE';

  //Valutazione del rischio corruttivo
  titleValutazioneRischio: string = 'SEZIONE_23.VALUTAZIONE_RISCHIO.TITLE_VALUTAZIONE_RISCHIO';
  subTitleValutazioneRischio: string =
    'SEZIONE_23.VALUTAZIONE_RISCHIO.SUB_TITLE_VALUTAZIONE_RISCHIO';
  labelIntroValutazioneRischio: string =
    'SEZIONE_23.VALUTAZIONE_RISCHIO.DESC_VALUTAZIONE_RISCHIO_LABEL';

  //Gestione del rischio corruttivo
  titleGestioneRischio: string = 'SEZIONE_23.GESTIONE_RISCHIO.TITLE_GESTIONE_RISCHIO';
  subTitleGestioneRischio: string = 'SEZIONE_23.GESTIONE_RISCHIO.SUB_TITLE_GESTIONE_RISCHIO';
  labelIntroGestioneRischio: string = 'SEZIONE_23.GESTIONE_RISCHIO.DESC_GESTIONE_RISCHIO_LABEL';

  //Monitoraggio
  titleMonitoraggio: string = 'SEZIONE_23.MONITORAGGIO_MISURE.TITLE_MONITORAGGIO_MISURE';
  subTitleMonitoraggio: string = 'SEZIONE_23.MONITORAGGIO_MISURE.SUB_TITLE_MONITORAGGIO_MISURE';
  labelIntroMonitoraggio: string = 'SEZIONE_23.MONITORAGGIO_MISURE.DESC_MONITORAGGIO_MISURE_LABEL';

  /*Ulteriore Info*/
  titleDynamicTBUlterioreInfo: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TB.ULTERIORE_INFO.TITLE';
  subTitleDynamicTBUlterioreInfo: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TB.ULTERIORE_INFO.SUB_TITLE';
  subTitleUlterioriInfo: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ULTERIORI_INFO.TITLE';
  descriptionUlterioriInfo: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ULTERIORI_INFO.DESCRIPTION';
  labelBtnAddNuovoCampo: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ULTERIORI_INFO.BTN_ADD_CAMPO';
  titleCardUlterioriInfo: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ULTERIORI_INFO.CARD_TITLE';
  labelBtnAddUlterioriInfo: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ULTERIORI_INFO.BTN_ADD';

  // Allegato
  subTitleAllegato: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ALLEGATO.TITLE';
  descriptionAllegato: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ALLEGATO.DESCRIPTION';
  labelBtnCaricaAllegato: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ALLEGATO.BTN_UPLOAD';
  labelTADescrizioneAllegato: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ALLEGATO.LABEL_DESCRIZIONE';
  titleDettaglioAllegato: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ALLEGATO.DETTAGLIO_TITLE';

  ulterioriInfoConfig!: DynamicTBConfig;
  codTipologia: string = CodTipologiaSezioneEnum.SEZ2_3;
  codTipologiaAllegato: string = CodTipologiaAllegatoEnum.ALLEGATO_SEZIONE;

  sezioneSection: SectionEnum = SectionEnum.SEZIONE_2_3;
  openModalDatiPubblicazione: boolean = false;

  ngOnInit(): void {
    this.initUlterioriInfoConfig();

    // Carica i dati aggiornati dal BE prima di creare il form
    this.loadSezione23Data();

    // Sottoscrizione agli aggiornamenti della sezione23
    this.subscription.add(
      this.sezione23Service.onSezione23Updated$.subscribe((sezione23) => {
        if (sezione23) {
          this.sezione23Data = sezione23;
          this.updateRealTimeSavedFormArrays(sezione23);
        }
      })
    );
  }

  private loadSezione23Data(): void {
    if (this.piaoDTO?.id) {
      this.sezione23Service.getSezione23ByIdPiao(this.piaoDTO.id).subscribe({
        next: (data) => {
          if (data) {
            this.sezione23Data = data;
            this.piaoDTO.idSezione23 = data.id;
            this.sessionStorageService.setItem(KEY_PIAO, this.piaoDTO);
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

  getForm(): FormGroup {
    return this.form;
  }

  isFormValid(): boolean {
    return this.form.valid;
  }

  hasFormValues(): boolean {
    return !areAllValuesNull(this.form);
  }

  prepareDataForSave(): Sezione23DTO {
    const formValue = this.form.value;
    const sezione23Id = this.sezione23Data?.id || this.piaoDTO?.idSezione23 || -1;

    const sezione23: Sezione23DTO = {
      id: sezione23Id || 0,
      idPiao: this.piaoDTO.id || 0,
      statoSezione: this.getSectionStatus(),
      introAdempimentiNormativi: formValue.introAdempimentiNormativi,
      impattoContestoExt: formValue.impattoContestoExt,
      impattoContestoInt: formValue.impattoContestoInt,
      descrGestioneRischio: formValue.descrGestioneRischio,
      descrIdentificazioneRischio: formValue.descrIdentificazioneRischio,
      descrAnalisiRischio: formValue.descrAnalisiRischio,
      descrMisurazioneRischio: formValue.descrMisurazioneRischio,
      descrTrattamentoRischio: formValue.descrTrattamentoRischio,
      descrMonitoraggioRischio: formValue.descrMonitoraggioRischio,
      introObiettivoPrevenzione: formValue.introObiettivoPrevenzione,
      introMisurePrevenzione: formValue.introMisurePrevenzione,
      descrTrasparenza: formValue.descrTrasparenza,
      introValorePubblico: formValue.introValorePubblico,
      introAttivitaSensibili: formValue.introAttivitaSensibili,
      introValutazioneRischio: formValue.introValutazioneRischio,
      introGestioneRischio: formValue.introGestioneRischio,
      introMonitoraggio: formValue.introMonitoraggio,
      obiettivoPrevenzioneCorruzioneTrasparenza:
        formValue.obiettivoPrevenzioneCorruzioneTrasparenza,
      attivitaSensibile: this.buildAttivitaSensibileForSave(this.sezione23Data),
      modalitaMonitoraggio: formValue.modalitaMonitoraggio,
      adempimentiNormativi: formValue.adempimentiNormativi,
      obiettivoPrevenzione: formValue.obiettivoPrevenzione,
      misuraPrevenzione: this.buildMisuraPrevenzioneForSave(this.sezione23Data),
      obblighiLegge: formValue.obblighiLegge,
      ulterioriInfo: formValue.ulterioriInfo,
      allegati: formValue.allegati,
    };

    console.log('Dati preparati per il salvataggio:', sezione23);

    return sezione23;
  }

  validate(): Observable<any> {
    const sezione23Id = this.sezione23Data?.id || this.piaoDTO?.idSezione23 || -1;
    return this.sezione23Service.validation(sezione23Id);
  }

  /**
   * Aggiorna solo i FormArray gestiti dai componenti con salvataggio puntuale (real-time),
   * senza toccare i campi di testo e gli altri controlli non salvati puntualmente.
   * In questo modo le modifiche non ancora salvate dall'utente non vengono perse.
   */
  private updateRealTimeSavedFormArrays(sezione23: Sezione23DTO): void {
    // Attività sensibili
    this.form.setControl(
      'attivitaSensibile',
      createFormArrayAttivitaSensibileFromPiaoSession(
        this.fb,
        sezione23.attivitaSensibile || [],
        INPUT_REGEX
      )
    );

    this.elencoAttivitaSensibileComponent?.ngOnInit();

    this.form.setControl(
      'valutazioneRischio',
      this.buildValutazioneRischioFromAttivitaSensibile(sezione23.attivitaSensibile || [])
    );

    this.form.setControl(
      'gestioneRischio',
      this.buildGestioneRischioFromAttivitaSensibile(sezione23.attivitaSensibile || [])
    );

    // Adempimenti normativi
    this.form.setControl(
      'adempimentiNormativi',
      createFormArrayFromPiaoSession<AdempimentoNormativoDTO>(
        this.fb,
        sezione23.adempimentiNormativi || [],
        sezione23.id,
        ['id', 'idSezione23', 'normativa', 'azione'],
        500,
        500,
        INPUT_REGEX,
        false
      )
    );

    // Modalità monitoraggio
    this.form.setControl(
      'monitoraggioPrevenzione',
      createFormArrayMonitoraggioPrevenzioneFromPiaoSession(
        this.fb,
        (sezione23.attivitaSensibile || []).flatMap((attivita) =>
          (attivita.eventoRischio || []).flatMap((evento) =>
            (evento.misure || []).flatMap((misura) => misura.monitoraggioPrevenzione || [])
          )
        )
      )
    );

    // Emetti il valore aggiornato del form
    this.formValueChanged.emit(this.form.value);
  }

  resetForm(): void {
    this.form.reset();
    this.createForm();
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
    const sezione23 = this.sezione23Data || new Sezione23DTO();

    // Debug: log delle proprietà obblighi per verificare il nome corretto
    console.log('[Sezione23] createForm - sezione23:', sezione23);

    this.form = this.fb.group({
      idPiao: this.fb.control<number | null>(this.piaoDTO.id || null),
      statoSezione: this.fb.control<string | null>(sezione23.statoSezione || null, [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      introAdempimentiNormativi: this.fb.control<string | null>(
        sezione23.introAdempimentiNormativi || null,
        [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]
      ),
      impattoContestoExt: this.fb.control<string | null>(sezione23.impattoContestoExt || null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
      ]),
      impattoContestoInt: this.fb.control<string | null>(sezione23.impattoContestoInt || null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
      ]),
      descrGestioneRischio: this.fb.control<string | null>(sezione23.descrGestioneRischio || null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
        Validators.required,
      ]),
      descrIdentificazioneRischio: this.fb.control<string | null>(
        sezione23.descrIdentificazioneRischio || null,
        [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX), Validators.required]
      ),
      descrAnalisiRischio: this.fb.control<string | null>(sezione23.descrAnalisiRischio || null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
        Validators.required,
      ]),
      descrMisurazioneRischio: this.fb.control<string | null>(
        sezione23.descrMisurazioneRischio || null,
        [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX), Validators.required]
      ),
      descrTrattamentoRischio: this.fb.control<string | null>(
        sezione23.descrTrattamentoRischio || null,
        [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX), Validators.required]
      ),
      descrMonitoraggioRischio: this.fb.control<string | null>(
        sezione23.descrMonitoraggioRischio || null,
        [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX), Validators.required]
      ),
      introObiettivoPrevenzione: this.fb.control<string | null>(
        sezione23.introObiettivoPrevenzione || null,
        [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]
      ),
      introMisurePrevenzione: this.fb.control<string | null>(
        sezione23.introMisurePrevenzione || null,
        [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]
      ),
      descrTrasparenza: this.fb.control<string | null>(sezione23.descrTrasparenza || null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
      ]),
      introValorePubblico: this.fb.control<string | null>(sezione23.introValorePubblico || null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
      ]),
      introAttivitaSensibili: this.fb.control<string | null>(
        sezione23.introAttivitaSensibili || null,
        [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]
      ),
      introValutazioneRischio: this.fb.control<string | null>(
        sezione23.introValutazioneRischio || null,
        [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]
      ),
      introGestioneRischio: this.fb.control<string | null>(sezione23.introGestioneRischio || null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
      ]),
      introMonitoraggio: this.fb.control<string | null>(sezione23.introMonitoraggio || null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
      ]),
      valutazioneRischio: this.buildValutazioneRischioFromAttivitaSensibile(
        sezione23.attivitaSensibile || []
      ),
      gestioneRischio: this.buildGestioneRischioFromAttivitaSensibile(
        sezione23.attivitaSensibile || []
      ),
      obiettivoPrevenzioneCorruzioneTrasparenza:
        createFormArrayObiettivoPrevenzioneCorruzioneTrasparenzaFromPiaoSession(
          this.fb,
          sezione23.obiettivoPrevenzioneCorruzioneTrasparenza || [],
          INPUT_REGEX
        ),
      attivitaSensibile: createFormArrayAttivitaSensibileFromPiaoSession(
        this.fb,
        sezione23.attivitaSensibile || [],
        INPUT_REGEX
      ),
      monitoraggioPrevenzione: createFormArrayMonitoraggioPrevenzioneFromPiaoSession(
        this.fb,
        (sezione23.attivitaSensibile || []).flatMap((attivita) =>
          (attivita.eventoRischio || []).flatMap((evento) =>
            (evento.misure || []).flatMap((misura) => misura.monitoraggioPrevenzione || [])
          )
        )
      ),
      adempimentiNormativi: createFormArrayFromPiaoSession<AdempimentoNormativoDTO>(
        this.fb,
        sezione23.adempimentiNormativi || [],
        sezione23.id,
        ['id', 'idSezione23', 'normativa', 'azione'],
        500,
        500,
        INPUT_REGEX,
        false
      ),
      ulterioriInfo: createFormMongoFromPiaoSession<UlterioriInfoDTO>(
        this.fb,
        sezione23.ulterioriInfo || new UlterioriInfoDTO(),
        ['id', 'externalId', 'properties'],
        INPUT_REGEX,
        50,
        false
      ),
      obiettivoPrevenzione: this.createObiettiviFormArray(sezione23.obiettivoPrevenzione || []),
      misuraPrevenzione: this.createMisureGeneraliFormArray(sezione23.misuraPrevenzione || []),
      obblighiLegge: this.createObbligoLeggeFormArray(sezione23.obblighiLegge || []),
      allegati: this.fb.array<FormGroup>([
        this.fb.group({
          id: [
            sezione23.allegati?.[0]?.id || null,
            [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
          ],
          idEntitaFK: [
            sezione23.allegati?.[0]?.idEntitaFK || null,
            [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
          ],
          codDocumento: [
            sezione23.allegati?.[0]?.codDocumento || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          codTipologiaFK: [
            sezione23.allegati?.[0]?.codTipologiaFK || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          codTipologiaAllegato: [
            sezione23.allegati?.[0]?.codTipologiaAllegato || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          downloadUrl: [
            sezione23.allegati?.[0]?.downloadUrl || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          sizeAllegato: [
            sezione23.allegati?.[0]?.sizeAllegato || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          descrizione: [
            sezione23.allegati?.[0]?.descrizione || null,
            [Validators.maxLength(100), Validators.pattern(INPUT_REGEX)],
          ],
        }),
      ]),
    });

    console.log('Form creato SEZIONE23 con i seguenti valori:', this.form.value);

    this.elencoAttivitaSensibileComponent?.ngOnInit();

    // Emetti il valore del form dopo la creazione
    this.isFormReady = true;
    this.formValueChanged.emit(this.form.value);
  }

  /**
   * Ricostruisce l'array attivitaSensibile per il salvataggio, fondendo i dati
   * provenienti dai tre FormArray separati:
   * - attivitaSensibile (dati base + eventoRischio iniziale)
   * - valutazioneRischio (campi di valutazione degli eventi rischiosi)
   * - gestioneRischio (misure di prevenzione per ogni evento)
   */
  private buildAttivitaSensibileForSave(sezione23Data: any): any[] {
    const formValue = this.form.value;
    const attivitaList: any[] = formValue.attivitaSensibile || [];
    const valutazioneList: any[] = formValue.valutazioneRischio || [];
    const gestioneList: any[] = formValue.gestioneRischio || [];

    console.log('Costruzione attivitaSensibile per il salvataggio', gestioneList);

    // 1) Map idEventoRischioso → misure[] da gestioneRischio
    const misureByEventoId = new Map<number, any[]>();

    // Costruisce una mappa evento → misure originali da sezione23Data per preservare gli ID
    const originalMisureByEventoId = new Map<number, any[]>();
    for (const attivita of sezione23Data?.attivitaSensibile || []) {
      for (const evento of attivita.eventoRischio || []) {
        if (evento.id != null && evento.misure?.length) {
          originalMisureByEventoId.set(evento.id, evento.misure);
        }
      }
    }

    // Prende tutti i monitoraggi dal FormArray 'monitoraggioPrevenzione' del form
    const allMonitoraggi: any[] = formValue.monitoraggioPrevenzione || [];

    for (const gestione of gestioneList) {
      const idEvento = gestione.idEventoRischioso;
      if (idEvento == null) continue;

      const originalMisure = originalMisureByEventoId.get(idEvento) || [];

      const misure = (gestione.misuraPrevenzione || []).map((m: any) => {
        // Trova la misura originale per preservare gli ID degli stakeholder
        const originalMisura = originalMisure.find((orig: any) => orig.id === m.id);

        // Trasforma l'array di ID stakeholder preservando gli ID esistenti
        const stakeholderIds = m.stakeholder || [];
        const stakeholderTransformed = stakeholderIds.map((selectedId: number) => {
          const existingStakeholder = originalMisura?.stakeholder?.find(
            (sh: any) => sh.stakeholder?.id === selectedId
          );
          return existingStakeholder
            ? { id: existingStakeholder.id, stakeholder: { id: selectedId } }
            : { stakeholder: { id: selectedId } };
        });

        // Filtra i monitoraggi dal form che appartengono a questa misura
        const monitoraggiPerMisura = allMonitoraggi.filter(
          (mon: any) => mon.idMisuraPrevenzioneEventoRischio === m.id
        );

        return {
          id: m.id,
          codice: m.codice,
          denominazione: m.denominazione,
          descrizione: m.descrizione,
          responsabile: m.responsabile,
          idEventoRischio: idEvento,
          idObiettivoPrevenzioneCorruzioneTrasparenza:
            m.idObiettivoPrevenzioneCorruzioneTrasparenza,
          indicatori: (m.indicatori || []).map((ind: any) => ({
            id: ind.id,
            indicatore: ind.indicatore,
          })),
          stakeholder: stakeholderTransformed,
          monitoraggioPrevenzione: monitoraggiPerMisura.map((mon: any) => ({
            id: mon.id,
            idSezione23: mon.idSezione23,
            idMisuraPrevenzioneEventoRischio: mon.idMisuraPrevenzioneEventoRischio,
            tipologia: mon.tipologia,
            descrizione: mon.descrizione,
            responsabile: mon.responsabile,
            tempistiche: mon.tempistiche,
          })),
        };
      });
      misureByEventoId.set(idEvento, misure);
    }

    // 2) Map idAttivitaSensibile → eventoRischio[] da valutazioneRischio + misure
    const eventiByAttivitaId = new Map<number, any[]>();
    for (const valutazione of valutazioneList) {
      const idAttivita = valutazione.idAttivitaSensibile;
      if (idAttivita == null) continue;

      const eventi = (valutazione.eventiRischiosi || []).map((e: any) => ({
        id: e.id,
        idAttivitaSensibile: e.idAttivitaSensibile,
        denominazione: e.denominazione,
        probabilita: e.probabilita,
        impatto: e.impatto,
        controlli: e.controlli,
        valutazione: e.valutazione,
        idLivelloRischio: e.idLivelloRischio,
        motivazione: e.motivazione,
        fattore: e.fattore,
        ulterioriInfo: e.ulterioriInfo,
        misure: misureByEventoId.get(e.id) || [],
      }));
      eventiByAttivitaId.set(idAttivita, eventi);
    }

    // 3) Fonde tutto in attivitaSensibile
    return attivitaList.map((attivita: any) => ({
      ...attivita,
      eventoRischio: eventiByAttivitaId.get(attivita.id) || attivita.eventoRischio || [],
    }));
  }

  /**
   * Costruisce l'array misuraPrevenzione per il salvataggio,
   * mappando stakeHolders da array di ID a MisuraGeneraleStakeholderDTO[]
   * e indicatori a MisuraGeneraleIndicatoreDTO[].
   */
  private buildMisuraPrevenzioneForSave(sezione23Data: any): any[] {
    const formValue = this.form.value;
    const misure = formValue.misuraPrevenzione || [];
    const originalMisure = sezione23Data?.misuraPrevenzione || [];

    return misure.map((m: any) => {
      // Trova la misura originale per preservare gli ID degli stakeholder
      const originalMisura = originalMisure.find((orig: any) => orig.id === m.id);

      // Trasforma l'array di ID stakeholder in array di MisuraGeneraleStakeholderDTO
      const stakeholderIds = m.stakeholder || [];
      const stakeholderTransformed = stakeholderIds.map((selectedId: number) => {
        // Cerca se questo stakeholder esisteva già nella misura originale
        const existingStakeholder = originalMisura?.stakeholder?.find(
          (sh: any) => sh.stakeholder?.id === selectedId
        );

        // Se esiste, preserva l'ID del MisuraGeneraleStakeholderDTO, altrimenti crea nuovo
        return existingStakeholder
          ? { id: existingStakeholder.id, stakeholder: { id: selectedId } }
          : { stakeholder: { id: selectedId } };
      });

      return {
        id: m.id,
        idSezione23: m.idSezione23,
        idObiettivoPrevenzione: m.idObiettivoPrevenzione,
        codice: m.codice,
        denominazione: m.denominazione,
        descrizione: m.descrizione,
        responsabileMisura: m.responsabileMisura,
        indicatori: (m.indicatori || []).map((ind: any) => ({
          id: ind.id,
          indicatore: ind.indicatore,
        })),
        stakeholder: stakeholderTransformed,
      };
    });
  }

  /**
   * Costruisce il FormArray 'valutazioneRischio' estraendo gli eventi rischiosi
   * dalle attività sensibili. Ogni attività sensibile con eventoRischio diventa
   * un gruppo con id, idAttivitaSensibile e il FormArray eventiRischiosi.
   */
  private buildValutazioneRischioFromAttivitaSensibile(
    attivitaSensibileList: AttivitaSensibileDTO[]
  ): FormArray<FormGroup> {
    if (!Array.isArray(attivitaSensibileList) || attivitaSensibileList.length === 0) {
      return this.fb.array<FormGroup>([]);
    }

    const groups = attivitaSensibileList
      .filter((a) => a.id != null && (a.eventoRischio?.length ?? 0) > 0)
      .map((attivita) =>
        this.fb.group({
          idAttivitaSensibile: [attivita.id, Validators.required],
          eventiRischiosi: this.fb.array<FormGroup>(
            (attivita.eventoRischio || []).map((evento) =>
              this.fb.group({
                id: [evento.id],
                idAttivitaSensibile: [evento.idAttivitaSensibile],
                denominazione: [evento.denominazione],
                probabilita: [evento.probabilita],
                controlli: [evento.controlli],
                impatto: [evento.impatto],
                valutazione: [evento.valutazione],
                idLivelloRischio: [evento.idLivelloRischio],
                motivazione: [evento.motivazione],
                fattore: createFormMongoFromPiaoSession<FattoreDTO>(
                  this.fb,
                  evento.fattore || new FattoreDTO(),
                  ['id', 'externalId', 'properties'],
                  INPUT_REGEX,
                  50,
                  false
                ),
                ulterioriInfo: createFormMongoFromPiaoSession<UlterioriInfoDTO>(
                  this.fb,
                  evento.ulterioriInfo || new UlterioriInfoDTO(),
                  ['id', 'externalId', 'properties'],
                  INPUT_REGEX,
                  50,
                  false
                ),
              })
            ),
            [minArrayLength(1)]
          ),
        })
      );

    return this.fb.array<FormGroup>(groups);
  }

  /**
   * Costruisce il FormArray 'gestioneRischio' estraendo le misure di prevenzione
   * dagli eventi rischiosi delle attività sensibili. Ogni evento rischioso con misure
   * diventa un gruppo con id, idEventoRischioso e il FormArray misuraPrevenzione.
   */
  private buildGestioneRischioFromAttivitaSensibile(
    attivitaSensibileList: AttivitaSensibileDTO[]
  ): FormArray<FormGroup> {
    if (!Array.isArray(attivitaSensibileList) || attivitaSensibileList.length === 0) {
      return this.fb.array<FormGroup>([]);
    }

    const groups: FormGroup[] = [];

    attivitaSensibileList.forEach((attivita) => {
      (attivita.eventoRischio || []).forEach((evento) => {
        if (evento.id == null || !evento.misure?.length) return;

        groups.push(
          this.fb.group({
            idEventoRischioso: [evento.id, [Validators.required]],
            misuraPrevenzione: this.fb.array<FormGroup>(
              (evento.misure || []).map((misura) =>
                this.fb.group({
                  id: [misura.id],
                  idObiettivoPrevenzioneCorruzioneTrasparenza: [
                    misura.idObiettivoPrevenzioneCorruzioneTrasparenza,
                    [Validators.required],
                  ],
                  codice: [misura.codice, [Validators.required]],
                  denominazione: [
                    misura.denominazione,
                    [Validators.required, Validators.maxLength(250)],
                  ],
                  descrizione: [misura.descrizione, [Validators.maxLength(100)]],
                  responsabile: [misura.responsabile, [Validators.maxLength(100)]],
                  stakeholder: [
                    misura.stakeholder?.map((s) => s.stakeholder?.id).filter((id) => id != null) ||
                      [],
                    [],
                  ],
                  indicatori: (() => {
                    const indicatoriMapped = (misura.indicatori || []).map((ind) => ({
                      id: ind.id,
                      indicatore: ind.indicatore,
                    }));
                    const formArray = createFormArrayGenericIndicatoreFromPiaoSession(
                      this.fb,
                      indicatoriMapped,
                      ['id', 'indicatore'],
                      INPUT_REGEX
                    );
                    formArray?.setValidators(Validators.required);
                    return formArray;
                  })(),
                  monitoraggioPrevenzione: createFormArrayMonitoraggioPrevenzioneFromPiaoSession(
                    this.fb,
                    misura.monitoraggioPrevenzione || [],
                    INPUT_REGEX
                  ),
                })
              ),
              [minArrayLength(1)]
            ),
          })
        );
      });
    });

    return this.fb.array<FormGroup>(groups);
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

  /**
   * Crea il FormArray per le misure generali di prevenzione
   */
  private createMisureGeneraliFormArray(misure: any[]): FormArray {
    const formArray = this.fb.array<FormGroup>([]);
    misure.forEach((misura) => {
      const misuraGroup = this.fb.group({
        id: [misura.id],
        idSezione23: [misura.idSezione23],
        idObiettivoPrevenzione: [misura.idObiettivoPrevenzione],
        codice: [
          misura.codice,
          [Validators.maxLength(50), Validators.pattern(INPUT_REGEX), Validators.required],
        ],
        denominazione: [
          misura.denominazione,
          [Validators.maxLength(250), Validators.pattern(INPUT_REGEX), Validators.required],
        ],
        descrizione: [
          misura.descrizione,
          [Validators.maxLength(100), Validators.pattern(INPUT_REGEX)],
        ],
        responsabileMisura: [
          misura.denominazione,
          [Validators.maxLength(250), Validators.pattern(INPUT_REGEX), Validators.required],
        ],
        stakeholder: [
          misura.stakeholder?.map((s: any) => s.stakeholder?.id).filter((id: any) => id != null) ||
            [],
        ],
        indicatori: this.fb.array(
          (misura.indicatori || []).map((ind: any) =>
            this.fb.group({
              id: [ind.id],
              indicatore: [ind.indicatore],
            })
          ),
          [minArrayLength(1)]
        ),
      });
      formArray.push(misuraGroup);
    });
    return formArray;
  }

  /**
   * Crea il FormArray per gli obiettivi di prevenzione
   */
  private createObiettiviFormArray(obiettivi: ObiettivoPrevenzioneDTO[]): FormArray {
    const formArray = this.fb.array<FormGroup>([]);
    obiettivi.forEach((obiettivo) => {
      const obiettivoGroup = this.fb.group({
        id: [obiettivo.id],
        idSezione23: [obiettivo.idSezione23],
        codice: [
          obiettivo.codice,
          [Validators.maxLength(50), Validators.pattern(INPUT_REGEX), Validators.required],
        ],
        denominazione: [
          obiettivo.denominazione,
          [Validators.maxLength(250), Validators.pattern(INPUT_REGEX), Validators.required],
        ],
        descrizione: [
          obiettivo.descrizione,
          [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)],
        ],
        indicatori: this.fb.array(
          (obiettivo.indicatori || []).map((ind) =>
            this.fb.group({
              id: [ind.id],
              indicatore: [ind.indicatore],
            })
          )
        ),
      });
      formArray.push(obiettivoGroup);
    });
    return formArray;
  }

  /**
   * Getter per il FormArray degli obiettivi di prevenzione
   */
  get obiettivoPrevenzioneControls(): FormArray {
    return this.form.get('obiettivoPrevenzione') as FormArray;
  }

  /**
   * Getter per il FormArray delle misure generali
   */
  get misureGeneraliControls(): FormArray {
    return this.form.get('misureGenerali') as FormArray;
  }

  /**
   * Getter per il FormArray degli obblighi di legge
   */
  get obblighiLeggeControls(): FormArray {
    return this.form.get('obblighiLegge') as FormArray;
  }

  /**
   * Crea il FormArray per gli obblighi di legge
   */
  private createObbligoLeggeFormArray(obblighi: any[]): FormArray {
    const formArray = this.fb.array<FormGroup>([]);
    obblighi.forEach((obbligo) => {
      const obbligoGroup = this.fb.group({
        id: [obbligo.id],
        idSezione23: [obbligo.idSezione23],
        denominazione: [
          obbligo.denominazione,
          [Validators.required, Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
        ],
        descrizione: [
          obbligo.descrizione,
          [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)],
        ],
        datiPubblicati: (() => {
          const arr = this.createDatiPubblicatiFormArray(obbligo.datiPubblicati || []);
          arr.setValidators(minArrayLength(1));
          return arr;
        })(),
      });
      formArray.push(obbligoGroup);
    });
    return formArray;
  }

  /**
   * Crea il FormArray per i dati pubblicati
   */
  private createDatiPubblicatiFormArray(dati: any[]): FormArray {
    const formArray = this.fb.array<FormGroup>([]);
    dati.forEach((dato) => {
      const datoGroup = this.fb.group({
        id: [dato.id],
        idObbligoLegge: [dato.idObbligoLegge],
        denominazione: [
          dato.denominazione,
          [Validators.required, Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
        ],
        tipologia: [dato.tipologia, [Validators.required]],
        responsabile: [
          dato.responsabile,
          [Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
        ],
        terminiScadenza: [
          dato.terminiScadenza,
          [Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
        ],
        modalitaMonitoraggio: [
          dato.modalitaMonitoraggio,
          [Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
        ],
        motivazioneImpossibilita: [
          dato.motivazioneImpossibilita,
          [Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
        ],
        ulterioriInfo: createFormMongoFromPiaoSession<UlterioriInfoDTO>(
          this.fb,
          dato.ulterioriInfo || new UlterioriInfoDTO(),
          ['id', 'externalId', 'properties'],
          INPUT_REGEX,
          50,
          false
        ),
      });
      formArray.push(datoGroup);
    });
    return formArray;
  }

  /**
   * Getter per l'ID della sezione 23
   */
  get idSezione23(): number {
    return this.sezione23Data?.id || this.piaoDTO?.idSezione23 || 0;
  }

  /**
   * Apre la modale per i dati di pubblicazione (test)
   */
  openDatiPubblicazioneModal(): void {
    this.openModalDatiPubblicazione = true;
  }

  /**
   * Chiude la modale per i dati di pubblicazione
   */
  closeDatiPubblicazioneModal(): void {
    this.openModalDatiPubblicazione = false;
  }

  /**
   * Conferma e salva i dati della modale
   */
  confirmDatiPubblicazioneModal(): void {
    // Implementa la logica di salvataggio qui se necessario
    console.log('Confermato salvataggio dati pubblicazione');
    this.closeDatiPubblicazioneModal();
  }

  handleAttachmentLoaded(): void {
    // Riemetti il valore del form quando gli allegati vengono caricati
    this.formValueChanged.emit(this.form.value);
  }

  reloadAttachment(): void {
    this.attachmentComponent?.getAllAttachment();
  }

  override ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}
