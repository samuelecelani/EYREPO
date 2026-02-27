import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  OnDestroy,
  Output,
  inject,
  ViewChild,
} from '@angular/core';
import { FormArray, FormGroup, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable, Subscription } from 'rxjs';
import { SharedModule } from '../../../../../../../shared/module/shared/shared.module';
import { TextAreaComponent } from '../../../../../../../shared/components/text-area/text-area.component';
import { DynamicTBComponent } from '../../../../../../../shared/components/dynamic-tb/dynamic-tb.component';
import { SwotElementListComponent } from '../../../../../../../shared/components/swot-element-list/swot-element-list.component';
import { DynamicTBConfig } from '../../../../../../../shared/models/classes/config/dynamic-tb';
import { DynamicTBTAConfig } from '../../../../../../../shared/models/classes/config/dynamic-tbta-config';
import { SvgComponent } from '../../../../../../../shared/components/svg/svg.component';
import { BaseComponent } from '../../../../../../../shared/components/base/base.component';
import {
  INPUT_REGEX,
  WARNING_ICON,
  ONLY_NUMBERS_REGEX,
  KEY_PIAO,
} from '../../../../../../../shared/utils/constants';
import { AllegatoDTO } from '../../../../../../../shared/models/classes/allegato-dto';
import { OvpComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-2.1/ovp/ovp.component';
import { ISezioneBase } from '../../../../../../../shared/models/interfaces/sezione-base.interface';
import { PIAODTO } from '../../../../../../../shared/models/classes/piao-dto';
import { Sezione21DTO } from '../../../../../../../shared/models/classes/sezione-21-dto';
import { Sezione21Service } from '../../../../../../../shared/services/sezioni-21.service';
import { SectionStatusEnum } from '../../../../../../../shared/models/enums/section-status.enum';
import { SECTION_FIELDS_REQUIRED } from '../../../../../../../shared/utils/section-fields-required';
import {
  areAllValuesNull,
  getSectionStatus,
  createFormArrayOVPFromPiaoSession,
  cleanSingleMongoDTO,
  createFormMongoFromPiaoSession,
  createFormArrayWithNControlsFromPiaoSession,
  filterNonNullFields,
  filterAtLeastOneNonNullField,
  hasRequiredErrors,
  printFormErrors,
} from '../../../../../../../shared/utils/utils';
import { UlterioriInfoDTO } from '../../../../../../../shared/models/classes/ulteriori-info-dto';
import { CodTipologiaSezioneEnum } from '../../../../../../../shared/models/enums/cod-tipologia-sezione.enum';
import { CodTipologiaAllegatoEnum } from '../../../../../../../shared/models/enums/cod-tipologia-allegato.enum';
import { AttachmentComponent } from '../../../../../../../shared/ui/attachment/attachment.component';
import { FondiEuropeiDTO } from '../../../../../../../shared/models/classes/fondi-europei-dto';
import { ProceduraDTO } from '../../../../../../../shared/models/classes/procedura-dto';
import { RisorseFinanziarieComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-2.1/risorse-finanziarie/risorse-finanziarie.component';
import { FondiEuropeiComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-2.1/fondi-europei/fondi-europei.component';
import { ProcedureComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-2.1/procedure/procedure.component';
import { OVPDTO } from '../../../../../../../shared/models/classes/ovp-dto';
import { SocialDTO } from '../../../../../../../shared/models/classes/social-dto';
import { SectionEnum } from '../../../../../../../shared/models/enums/section.enum';
import { SessionStorageService } from '../../../../../../../shared/services/session-storage.service';
import { StakeholderService } from '../../../../../../../shared/services/stakeholder.service';

@Component({
  selector: 'piao-sezione-2-1',
  imports: [
    SharedModule,
    TextAreaComponent,
    SwotElementListComponent,
    DynamicTBComponent,
    OvpComponent,
    ReactiveFormsModule,
    AttachmentComponent,
    RisorseFinanziarieComponent,
    FondiEuropeiComponent,
    ProcedureComponent,
  ],
  templateUrl: './sezione2.1.component.html',
  styleUrl: './sezione2.1.component.scss',
})
export class Sezione21Component extends BaseComponent implements OnInit, OnDestroy, ISezioneBase {
  private fb = inject(FormBuilder);
  private sezione21Service = inject(Sezione21Service);
  private subscription = new Subscription();
  private sessionStorageService = inject(SessionStorageService);
  private stakeholderService = inject(StakeholderService);

  isFormReady = false;

  @Input() piaoDTO!: PIAODTO;
  @Input() sezione21Data?: Sezione21DTO;
  @Output() formValueChanged = new EventEmitter<any>();
  @ViewChild('attachment') attachmentComponent?: AttachmentComponent;

  form!: FormGroup;

  // Titolo principale
  title: string = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.TITLE';

  // Parte Generale
  subTitleParteGenerale: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.PARTE_GENERALE';

  // Analisi contesto
  subTitleAnalisiContesto: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ANALISI_CONTESTO.TITLE';
  descriptionAnalisiContesto: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ANALISI_CONTESTO.DESCRIPTION';
  labelTAContestoInterno: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ANALISI_CONTESTO.LABEL_INTERNO';
  labelTAContestoEsterno: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ANALISI_CONTESTO.LABEL_ESTERNO';

  // Analisi SWOT
  subTitleAnalisiSwot: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ANALISI_SWOT.TITLE';
  descriptionAnalisiSwot: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ANALISI_SWOT.DESCRIPTION';

  // SWOT Labels
  labelSwotPuntiForza: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ANALISI_SWOT.PUNTI_FORZA.LABEL';
  labelSwotPuntiDebolezza: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ANALISI_SWOT.PUNTI_DEBOLEZZA.LABEL';
  labelSwotOpportunita: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ANALISI_SWOT.OPPORTUNITA.LABEL';
  labelSwotMinacce: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ANALISI_SWOT.MINACCE.LABEL';

  // Concetto generale di valore pubblico
  subTitleValorePubblico: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.VALORE_PUBBLICO.TITLE';
  labelTADescrizioneValorePubblico: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.VALORE_PUBBLICO.LABEL';
  descriptionUploadImmagini: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.VALORE_PUBBLICO.UPLOAD_DESCRIPTION';
  labelBtnCaricaImmagine: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.VALORE_PUBBLICO.BTN_UPLOAD';

  // Parte Funzionale
  subTitleParteFunzionale: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.PARTE_FUNZIONALE';

  // Obiettivi di valore pubblico
  subTitleObiettiviValorePubblico: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.TITLE';
  descriptionObiettiviValorePubblico: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.DESCRIPTION';
  titleAlertObiettiviVP: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.ALERT_TITLE';
  subTitleAlertObiettiviVP: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.ALERT_SUBTITLE';
  titleCardObiettivoVP: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.CARD_TITLE';
  labelBtnAddObiettivoVP: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.BTN_ADD';

  // Obiettivi trasversali
  subTitleObiettiviTrasversali: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_TRASVERSALI.TITLE';
  descriptionObiettiviTrasversali: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_TRASVERSALI.DESCRIPTION';

  subTitleAccessibilitaDigitale: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_TRASVERSALI.ACCESSIBILITA_DIGITALE';
  labelTADescrizioneAccessiDigitale: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_TRASVERSALI.LABEL_DESCRIZIONE';

  subTitleAccessibilitaFisica: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_TRASVERSALI.ACCESSIBILITA_FISICA';
  labelTADescrizioneAccessiFisica: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_TRASVERSALI.LABEL_DESCRIZIONE';

  subTitleSemplificazione: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_TRASVERSALI.SEMPLIFICAZIONE';
  labelTADescrizioneSemplificazione: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_TRASVERSALI.LABEL_DESCRIZIONE';

  subTitlePariOpportunita: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_TRASVERSALI.PARI_OPPORTUNITA';
  labelTADescrizionePariOpportunita: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_TRASVERSALI.LABEL_DESCRIZIONE';

  // Ulteriori informazioni
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

  // Tabella allegati headers
  thNomeDocumento: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ALLEGATO.TABLE.NOME_DOC';
  thDataCaricamento: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ALLEGATO.TABLE.DATA';
  thProfiloUtente: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ALLEGATO.TABLE.PROFILO';
  thDimensione: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ALLEGATO.TABLE.DIMENSIONE';
  thAzioni: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.ALLEGATO.TABLE.AZIONI';

  labelBtnScarica: string = 'BUTTONS.DOWNLOAD';
  labelBtnElimina: string = 'BUTTONS.DELETE';

  // Icons
  iconAlert: string = WARNING_ICON;

  // Configs
  fondiEuropeiConfig!: DynamicTBTAConfig;
  ulterioriInfoConfig!: DynamicTBConfig;

  // Allegati
  codTipologia: string = CodTipologiaSezioneEnum.SEZ2_1;
  codTipologiaAllegato: string = CodTipologiaAllegatoEnum.ALLEGATO_SEZIONE;
  codTipologiaAllegatoImage1: string = CodTipologiaAllegatoEnum.IMMAGINE_SEZIONE_21_1;
  codTipologiaAllegatoImage2: string = CodTipologiaAllegatoEnum.IMMAGINE_SEZIONE_21_2;

  sezioneSection: SectionEnum = SectionEnum.SEZIONE_2_1;

  ngOnInit(): void {
    this.initUlterioriInfoConfig();
    console.log(this.ulterioriInfoConfig.labelTB);

    // Carica i dati dal BE se esiste un PIAO, poi crea il form
    this.loadSezione21Data();

    // Sottoscrizione agli aggiornamenti della sezione21
    this.subscription.add(
      this.sezione21Service.onSezione21Updated$.subscribe((sezione21) => {
        if (sezione21) {
          this.sezione21Data = sezione21;
          this.reloadForm(sezione21);
        }
      })
    );
  }

  //not used, but could be useful in case of partial updates of the form
  loadStakeholder(): void {
    const piaoDTO: PIAODTO = this.sessionStorageService.getItem(KEY_PIAO);
    if (piaoDTO?.id) {
      this.stakeholderService.getByPiao(piaoDTO.id).subscribe({
        next: (stakeholders) => {
          this.piaoDTO.stakeHolders = stakeholders;
          this.sessionStorageService.setItem(KEY_PIAO, this.piaoDTO);
        },
      });
    }
  }

  private loadSezione21Data(): void {
    if (this.piaoDTO?.id) {
      this.sezione21Service.getSezione21ByIdPiao(this.piaoDTO.id).subscribe({
        next: (data) => {
          if (data) {
            this.sezione21Data = data;
            this.piaoDTO.idSezione21 = data.id;
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

  createForm(): void {
    const sezione21 = this.sezione21Data || new Sezione21DTO();

    this.form = this.fb.group({
      id: this.fb.control<number | null>(sezione21.id || null),
      idPiao: this.fb.control<number | null>(this.piaoDTO.id || null),
      statoSezione: this.fb.control<string | null>(sezione21.statoSezione || null, [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      contestoInt: this.fb.control<string | null>(sezione21.contestoInt || null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
      ]),
      contestoExt: this.fb.control<string | null>(sezione21.contestoExt || null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
      ]),
      swotPuntiForza: this.fb.group({
        id: [sezione21.swotPuntiForza?.id || null],
        externalId: [sezione21.swotPuntiForza?.externalId || sezione21.id || null],
        properties: this.populateSwotProperties(
          sezione21.swotPuntiForza?.properties || [],
          'puntiForza'
        ),
      }),
      swotPuntiDebolezza: this.fb.group({
        id: [sezione21.swotPuntiDebolezza?.id || null],
        externalId: [sezione21.swotPuntiDebolezza?.externalId || sezione21.id || null],
        properties: this.populateSwotProperties(
          sezione21.swotPuntiDebolezza?.properties || [],
          'puntiDebolezza'
        ),
      }),
      swotOpportunita: this.fb.group({
        id: [sezione21.swotOpportunita?.id || null],
        externalId: [sezione21.swotOpportunita?.externalId || sezione21.id || null],
        properties: this.populateSwotProperties(
          sezione21.swotOpportunita?.properties || [],
          'opportunita'
        ),
      }),
      swotMinacce: this.fb.group({
        id: [sezione21.swotMinacce?.id || null],
        externalId: [sezione21.swotMinacce?.externalId || sezione21.id || null],
        properties: this.populateSwotProperties(sezione21.swotMinacce?.properties || [], 'minacce'),
      }),
      descrizioneValorePubblico: this.fb.control<string | null>(
        sezione21.descrizioneValorePubblico || null,
        [Validators.maxLength(1500), Validators.pattern(INPUT_REGEX), Validators.required]
      ),
      fondiEuropei: createFormArrayWithNControlsFromPiaoSession<FondiEuropeiDTO>(
        this.fb,
        sezione21.fondiEuropei || [],
        ['id', 'progettoFinanziato', 'descrizione', 'fondiStanziati'],
        [ONLY_NUMBERS_REGEX, INPUT_REGEX, INPUT_REGEX, ONLY_NUMBERS_REGEX],
        [20, 250, 1000, 50],
        false
      ),
      procedure: createFormArrayWithNControlsFromPiaoSession<ProceduraDTO>(
        this.fb,
        sezione21.procedure || [],
        [
          'id',
          'denominazione',
          'descrizione',
          'unitaMisura',
          'misurazione',
          'target',
          'uffResponsabile',
        ],
        [
          ONLY_NUMBERS_REGEX,
          INPUT_REGEX,
          INPUT_REGEX,
          INPUT_REGEX,
          INPUT_REGEX,
          INPUT_REGEX,
          INPUT_REGEX,
        ],
        [20, 500, 500, 50, 50, 50, 50],
        false,
        [false, true, false, false, false, true, true]
      ),
      descrizioneAccessiDigitale: this.fb.control<string | null>(
        sezione21.descrizioneAccessiDigitale || null,
        [Validators.maxLength(1000), Validators.pattern(INPUT_REGEX)]
      ),
      descrizioneAccessiFisica: this.fb.control<string | null>(
        sezione21.descrizioneAccessiFisica || null,
        [Validators.maxLength(1000), Validators.pattern(INPUT_REGEX)]
      ),
      descrizioneSemplificazione: this.fb.control<string | null>(
        sezione21.descrizioneSemplificazione || null,
        [Validators.maxLength(1000), Validators.pattern(INPUT_REGEX)]
      ),
      descrizionePariOpportunita: this.fb.control<string | null>(
        sezione21.descrizionePariOpportunita || null,
        [Validators.maxLength(1000), Validators.pattern(INPUT_REGEX)]
      ),
      introRisorseFinanziarie: this.fb.control<string | null>(
        sezione21.introRisorseFinanziarie || null,
        [Validators.maxLength(1000), Validators.pattern(INPUT_REGEX)]
      ),
      introFondiEuropei: this.fb.control<string | null>(sezione21.introFondiEuropei || null, [
        Validators.maxLength(1000),
        Validators.pattern(INPUT_REGEX),
      ]),
      introProcedure: this.fb.control<string | null>(sezione21.introProcedure || null, [
        Validators.maxLength(1000),
        Validators.pattern(INPUT_REGEX),
      ]),
      ulterioriInfo: createFormMongoFromPiaoSession<UlterioriInfoDTO>(
        this.fb,
        sezione21.ulterioriInfo || new UlterioriInfoDTO(),
        ['id', 'externalId', 'properties'],
        INPUT_REGEX,
        50,
        false
      ),
      ovp: createFormArrayOVPFromPiaoSession(this.fb, sezione21.ovp || [], INPUT_REGEX, {
        idSezione1: this.piaoDTO.idSezione1 || -1,
        idPiao: this.piaoDTO.id || -1,
      }),
      risorseFinanziarie: (() => {
        const formArray = this.extractRisorseFromOVP(sezione21.ovp || []);
        formArray?.setValidators(Validators.required);
        return formArray;
      })(),
      allegati: this.fb.array<FormGroup>([
        this.fb.group({
          id: [
            sezione21.allegati?.[0]?.id || null,
            [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
          ],
          idEntitaFK: [
            sezione21.allegati?.[0]?.idEntitaFK || null,
            [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
          ],
          codDocumento: [
            sezione21.allegati?.[0]?.codDocumento || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          codTipologiaFK: [
            sezione21.allegati?.[0]?.codTipologiaFK || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          codTipologiaAllegato: [
            sezione21.allegati?.[0]?.codTipologiaAllegato || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          downloadUrl: [
            sezione21.allegati?.[0]?.downloadUrl || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          sizeAllegato: [
            sezione21.allegati?.[0]?.sizeAllegato || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          descrizione: [
            sezione21.allegati?.[0]?.descrizione || null,
            [Validators.maxLength(100), Validators.pattern(INPUT_REGEX)],
          ],
        }),
      ]),
    });
    console.log('Form created:', this.form, this.form.value);

    this.isFormReady = true;
    this.formValueChanged.emit(this.form.value);
  }

  reloadForm(sezione21: Sezione21DTO): void {
    // Aggiorna solo i FormArray salvati in real-time senza ricreare tutto il form
    this.form.setControl(
      'ovp',
      createFormArrayOVPFromPiaoSession(this.fb, sezione21.ovp || [], INPUT_REGEX, {
        idSezione1: this.piaoDTO.idSezione1 || -1,
        idPiao: this.piaoDTO.id || -1,
      })
    );

    const risorseFormArray = this.extractRisorseFromOVP(sezione21.ovp || []);
    risorseFormArray?.setValidators(Validators.required);
    this.form.setControl('risorseFinanziarie', risorseFormArray);

    this.formValueChanged.emit(this.form.value);
  }

  private populateSwotProperties(properties: any[], swotType?: string): FormArray<FormGroup> {
    const formArray = this.fb.array<FormGroup>([]);

    if (properties && properties.length > 0) {
      properties.forEach((prop, index) => {
        // Se la key esiste e segue il pattern corretto, la mantiene, altrimenti la genera
        let key = prop.key;
        if (!key || !key.match(/^(puntiForza|puntiDebolezza|opportunita|minacce)\d+$/)) {
          // Genera la key automaticamente basandosi sul tipo
          if (swotType) {
            key = `${swotType}${index + 1}`;
          } else {
            // Fallback: usa il pattern generico
            key = `swot${index + 1}`;
          }
        }

        formArray.push(
          this.fb.group({
            key: [key, [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)]],
            value: [
              prop.value || null,
              [Validators.maxLength(100), Validators.pattern(INPUT_REGEX)],
            ],
          })
        );
      });
    }

    // Se non ci sono properties, aggiungi almeno un elemento vuoto con key generata
    if (formArray.length === 0) {
      const defaultKey = swotType ? `${swotType}1` : 'swot1';
      formArray.push(
        this.fb.group({
          key: [defaultKey, [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)]],
          value: [null, [Validators.maxLength(100), Validators.pattern(INPUT_REGEX)]],
        })
      );
    }

    return formArray;
  }

  private extractRisorseFromOVP(ovpList: OVPDTO[]): FormArray<FormGroup> {
    const risorseArray = this.fb.array<FormGroup>([]);

    ovpList.forEach((ovp) => {
      if (ovp.risorseFinanziarie && ovp.risorseFinanziarie.length > 0) {
        ovp.risorseFinanziarie.forEach((risorsa) => {
          risorseArray.push(
            this.fb.group({
              id: [risorsa.id, [Validators.maxLength(20), Validators.pattern(ONLY_NUMBERS_REGEX)]],
              idOvp: [
                risorsa.idOvp || ovp.id,
                [Validators.maxLength(20), Validators.pattern(ONLY_NUMBERS_REGEX)],
              ],
              iniziativa: [
                risorsa.iniziativa,
                [Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
              ],
              descrizione: [
                risorsa.descrizione,
                [Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
              ],
              dotazioneFinanziaria: [
                risorsa.dotazioneFinanziaria,
                [Validators.maxLength(20), Validators.pattern(ONLY_NUMBERS_REGEX)],
              ],
              fonteFinanziamento: [
                risorsa.fonteFinanziamento,
                [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
              ],
            })
          );
        });
      }
    });

    return risorseArray;
  }

  // Implementazione interfaccia ISezioneBase
  getForm(): FormGroup {
    return this.form;
  }

  isFormValid(): boolean {
    return this.form.valid;
  }

  hasFormValues(): boolean {
    return !areAllValuesNull(this.form);
  }

  prepareDataForSave(): Sezione21DTO {
    const sezione21 = this.sezione21Data || new Sezione21DTO();

    return {
      ...sezione21,
      idPiao: this.form.controls['idPiao'].value || null,
      contestoInt: this.form.controls['contestoInt'].value || null,
      contestoExt: this.form.controls['contestoExt'].value || null,
      swotPuntiForza: this.form.controls['swotPuntiForza'].value || null,
      swotPuntiDebolezza: this.form.controls['swotPuntiDebolezza'].value || null,
      swotOpportunita: this.form.controls['swotOpportunita'].value || null,
      swotMinacce: this.form.controls['swotMinacce'].value || null,
      descrizioneValorePubblico: this.form.controls['descrizioneValorePubblico'].value || null,
      descrizioneAccessiDigitale: this.form.controls['descrizioneAccessiDigitale'].value || null,
      descrizioneAccessiFisica: this.form.controls['descrizioneAccessiFisica'].value || null,
      descrizioneSemplificazione: this.form.controls['descrizioneSemplificazione'].value || null,
      descrizionePariOpportunita: this.form.controls['descrizionePariOpportunita'].value || null,
      procedure: filterAtLeastOneNonNullField<ProceduraDTO>(this.form.controls['procedure'].value, [
        'denominazione',
        'descrizione',
        'unitaMisura',
        'misurazione',
        'target',
        'uffResponsabile',
      ]),
      fondiEuropei: filterAtLeastOneNonNullField<FondiEuropeiDTO>(
        this.form.controls['fondiEuropei'].value,
        ['progettoFinanziato', 'descrizione', 'fondiStanziati']
      ),
      introRisorseFinanziarie: this.form.controls['introRisorseFinanziarie'].value || null,
      introFondiEuropei: this.form.controls['introFondiEuropei'].value || null,
      introProcedure: this.form.controls['introProcedure'].value || null,
      ovp: this.redistributeRisorseToOVP(),
      ulterioriInfo: cleanSingleMongoDTO<UlterioriInfoDTO>(
        this.form.controls['ulterioriInfo'].value
      ),
      allegati: this.form.controls['allegati'].value || null,
      statoSezione: this.getSectionStatus(),
    };
  }

  validate(): Observable<any> {
    const sezione21Id = this.sezione21Data?.id || this.piaoDTO?.idSezione21 || -1;
    return this.sezione21Service.validation(sezione21Id);
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

  private redistributeRisorseToOVP(): OVPDTO[] {
    const ovpList: OVPDTO[] = this.form.controls['ovp'].value || [];

    console.log('Original OVP List:', ovpList);

    console.log('Form Risorse Finanziarie:', this.form.controls['risorseFinanziarie'].value);

    const risorseFlat: any[] = filterAtLeastOneNonNullField(
      this.form.controls['risorseFinanziarie'].value || [],
      ['idOvp', 'iniziativa', 'descrizione', 'dotazioneFinanziaria', 'fonteFinanziamento']
    );

    console.log('Flat Risorse Finanziarie:', risorseFlat);

    // Reset delle risorse in tutti gli OVP
    ovpList.forEach((ovp) => {
      ovp.sezione21Id = this.form?.get('id')?.value; // Associa l'id della sezione 2.1 all'OVP
      ovp.risorseFinanziarie = [];
    });

    // Redistribuisci le risorse agli OVP corretti in base a idOvp
    risorseFlat.forEach((risorsa) => {
      // Scarta se idOvp è null o undefined

      if (risorsa.idOvp === null || risorsa.idOvp === undefined) {
        return;
      }

      const targetOvp =
        typeof risorsa.idOvp === 'number'
          ? ovpList.find((ovp) => ovp.id == risorsa.idOvp)
          : ovpList.find((ovp) => ovp.codice == risorsa.idOvp);

      if (targetOvp) {
        if (!targetOvp.risorseFinanziarie) {
          targetOvp.risorseFinanziarie = [];
        }
        risorsa.idOvp = targetOvp.id; // Assicura che l'idOvp sia l'ID numerico corretto
        targetOvp.risorseFinanziarie.push(risorsa);
      }
      // Se non trova l'OVP corrispondente, la risorsa viene scartata
    });

    console.log('Redistributed OVP List:', ovpList);

    return ovpList;
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

  // Getter per form arrays
  get swotPuntiForzaProperties(): FormArray {
    return this.form.get('swotPuntiForza.properties') as FormArray;
  }

  get swotPuntiDebolezzaProperties(): FormArray {
    return this.form.get('swotPuntiDebolezza.properties') as FormArray;
  }

  get swotOpportunitaProperties(): FormArray {
    return this.form.get('swotOpportunita.properties') as FormArray;
  }

  get swotMinacceProperties(): FormArray {
    return this.form.get('swotMinacce.properties') as FormArray;
  }

  get ulterioriInfoProperties(): FormArray {
    return this.form.get('ulterioriInfo.properties') as FormArray;
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
