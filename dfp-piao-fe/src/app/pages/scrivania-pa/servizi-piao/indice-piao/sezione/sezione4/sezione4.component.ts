import { ModalMilestoneComponent } from 'src/app/shared/ui/servizi-piao/indice-piao/sezione/sezione-4/monitoraggio-piao/milestone/modal-milestone/modal-milestone.component';
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
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable, catchError, map, of, takeUntil } from 'rxjs';
import { SharedModule } from '../../../../../../shared/module/shared/shared.module';
import { TextAreaComponent } from '../../../../../../shared/components/text-area/text-area.component';
import { AccordionComponent } from '../../../../../../shared/components/accordion/accordion.component';
import { AttachmentComponent } from '../../../../../../shared/ui/attachment/attachment.component';
import { CardAlertComponent } from '../../../../../../shared/ui/card-alert/card-alert.component';
import { DynamicTBComponent } from '../../../../../../shared/components/dynamic-tb/dynamic-tb.component';
import { SvgComponent } from '../../../../../../shared/components/svg/svg.component';
import { BaseComponent } from '../../../../../../shared/components/base/base.component';
import { ModalSottofaseComponent } from '../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-4/sotto-fase-monitoraggio/modal-sottofase/modal-sottofase.component';
import { ISezioneBase } from '../../../../../../shared/models/interfaces/sezione-base.interface';
import { PIAODTO } from '../../../../../../shared/models/classes/piao-dto';
import {
  Sezione4DTO,
  SottofaseMonitoraggioDTO,
} from '../../../../../../shared/models/classes/sezione-4-dto';
import { MilestoneDTO } from '../../../../../../shared/models/classes/milestone-dto';
import { UlterioriInfoDTO } from '../../../../../../shared/models/classes/ulteriori-info-dto';
import { DynamicTBConfig } from '../../../../../../shared/models/classes/config/dynamic-tb';
import {
  DATE_REGEX,
  INPUT_REGEX,
  KEY_PIAO,
  ONLY_NUMBERS_REGEX,
  SHAPE_ICON,
} from '../../../../../../shared/utils/constants';
import { SessionStorageService } from '../../../../../../shared/services/session-storage.service';
import { SectionStatusEnum } from '../../../../../../shared/models/enums/section-status.enum';
import { CodTipologiaAllegatoEnum } from '../../../../../../shared/models/enums/cod-tipologia-allegato.enum';
import { CodTipologiaSezioneEnum } from '../../../../../../shared/models/enums/cod-tipologia-sezione.enum';
import { SectionEnum } from '../../../../../../shared/models/enums/section.enum';
import {
  areAllValuesNull,
  createFormMongoAttivitaFromPiaoSession,
  createFormMongoFromPiaoSession,
  createFormMongoFromPiaoSession4Item,
  hasRequiredErrors,
  printFormErrors,
} from '../../../../../../shared/utils/utils';
import { Sezione4Service } from '../../../../../../shared/services/sezione4.service';
import { StoricoStatoSezioneService } from '../../../../../../shared/services/storico-stato-sezione.service';
import { ModalComponent } from '../../../../../../shared/components/modal/modal.component';
import { LabelValue } from '../../../../../../shared/models/interfaces/label-value';
import { ElencoSottoFaseMonitoraggioComponent } from '../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-4/sotto-fase-monitoraggio/elenco-sotto-fase-monitoraggio/elenco-sotto-fase-monitoraggio.component';
import { ElencoCategoriaObiettiviComponent } from '../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-4/categoria-obiettivi/elenco-categoria-obiettivi/elenco-categoria-obiettivi.component';
import { ElencoMilestoneComponent } from '../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-4/monitoraggio-piao/milestone/elenco-milestone/elenco-milestone.component';
import { AttoreDTO } from '../../../../../../shared/models/classes/attore-dto';
import { CategoriaObiettiviDTO } from '../../../../../../shared/models/classes/categoria-obiettivi-dto';
import { BaseMongoAttivitaDTO } from '../../../../../../shared/models/classes/base-mongo-attivita-dto';
import { CodTipologiaCategoriaEnum } from '../../../../../../shared/models/enums/cod-tipologia-categoria.enum';
import { GraficoGanttComponent } from '../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-4/grafico-gantt/grafico-gantt.component';
import { GanttTask } from '../../../../../../shared/models/interfaces/gant-task';

@Component({
  selector: 'piao-sezione-4',
  imports: [
    SharedModule,
    ReactiveFormsModule,
    TextAreaComponent,
    AccordionComponent,
    AttachmentComponent,
    CardAlertComponent,
    DynamicTBComponent,
    ModalMilestoneComponent,
    ModalComponent,
    ElencoSottoFaseMonitoraggioComponent,
    ElencoCategoriaObiettiviComponent,
    ElencoMilestoneComponent,
    GraficoGanttComponent,
  ],
  templateUrl: './sezione4.component.html',
  styleUrl: './sezione4.component.scss',
})
export class Sezione4Component extends BaseComponent implements OnInit, ISezioneBase {
  @Input() piaoDTO!: PIAODTO;
  @Input() sezione4Data?: Sezione4DTO;
  @Input() testoSezione!: string;
  @Input() isDettaglio: boolean = false;

  @Output() formValueChanged = new EventEmitter<any>();

  isFormReady = false;

  @ViewChild('attachmentAllegato') attachmentAllegatoComponent?: AttachmentComponent;
  @ViewChild('modalMilestoneChild') modalMilestoneChild?: ModalMilestoneComponent;

  private fb = inject(FormBuilder);
  private sezione4Service = inject(Sezione4Service);
  private storicoStatoSezioneService = inject(StoricoStatoSezioneService);

  form!: FormGroup;

  openModalMilestone = false;
  milestoneEdit?: MilestoneDTO;
  dropdownSottofaseMonitoraggio: LabelValue[] = [];

  icon: string = SHAPE_ICON;
  iconStyle: string = 'icon-modal';
  promemoriaDropdown!: LabelValue[];
  // Titolo principale
  title: string = 'SEZIONE_4.TITLE';
  subTitle: string = 'SEZIONE_4.SUB_TITLE';

  codTipologiaCategoria = CodTipologiaCategoriaEnum;

  // Attività di monitoraggio
  labelDescrStrumenti: string = 'SEZIONE_4.ATTIVITA_MONITORAGGIO.DESCR_STRUMENTI.LABEL';
  labelDescrModalitaRilevazione: string =
    'SEZIONE_4.ATTIVITA_MONITORAGGIO.DESCR_MODALITA_RILEVAZIONE.LABEL';

  // Card Alert
  titleCardAlert: string = 'SEZIONE_4.CARD_ALERT.TITLE';
  subTitleCardAlert: string = 'SEZIONE_4.CARD_ALERT.SUB_TITLE';

  // Introduzione
  labelIntro: string = 'SEZIONE_4.ATTIVITA_MONITORAGGIO.INTRO.LABEL';

  // Elenco sottofasi
  labelElencoSottofasi: string = 'SEZIONE_4.ELENCO_SOTTOFASI.LABEL';

  // Modal sottofase
  isModalSottofaseOpen: boolean = false;

  // Monitoraggio Sezione 2
  titleMonitoraggioSez2: string = 'SEZIONE_4.MONITORAGGIO_SEZ2.TITLE';
  descMonitoraggioSez2: string = 'SEZIONE_4.MONITORAGGIO_SEZ2.DESC';
  descMonitoraggioSez2Dettaglio: string = 'SEZIONE_4.MONITORAGGIO_SEZ2.DESC_DETTAGLIO';
  titleCardAlertSez2: string = 'SEZIONE_4.MONITORAGGIO_SEZ2.CARD_ALERT.TITLE';
  subTitleCardAlertSez2: string = 'SEZIONE_4.MONITORAGGIO_SEZ2.CARD_ALERT.SUB_TITLE';

  // Accordion Sezione 2
  titleAccordion21: string = 'SEZIONE_4.MONITORAGGIO_SEZ2.ACCORDION_21.TITLE';
  titleAccordion22Org: string = 'SEZIONE_4.MONITORAGGIO_SEZ2.ACCORDION_22_ORG.TITLE';
  titleAccordion22Ind: string = 'SEZIONE_4.MONITORAGGIO_SEZ2.ACCORDION_22_IND.TITLE';
  titleAccordion23: string = 'SEZIONE_4.MONITORAGGIO_SEZ2.ACCORDION_23.TITLE';

  // Monitoraggio Sezione 3
  titleMonitoraggioSez3: string = 'SEZIONE_4.MONITORAGGIO_SEZ3.TITLE';
  descMonitoraggioSez3: string = 'SEZIONE_4.MONITORAGGIO_SEZ3.DESC';
  descMonitoraggioSez3Dettaglio: string = 'SEZIONE_4.MONITORAGGIO_SEZ3.DESC_DETTAGLIO';

  // Accordion Sezione 3
  titleAccordion31: string = 'SEZIONE_4.MONITORAGGIO_SEZ3.ACCORDION_31.TITLE';
  titleAccordion32: string = 'SEZIONE_4.MONITORAGGIO_SEZ3.ACCORDION_32.TITLE';
  titleAccordion331: string = 'SEZIONE_4.MONITORAGGIO_SEZ3.ACCORDION_331.TITLE';
  titleAccordion332: string = 'SEZIONE_4.MONITORAGGIO_SEZ3.ACCORDION_332.TITLE';

  // Il monitoraggio del PIAO
  titleMonitoraggioPiao: string = 'SEZIONE_4.MONITORAGGIO_PIAO.TITLE';
  descMonitoraggioPiao: string = 'SEZIONE_4.MONITORAGGIO_PIAO.DESC';
  labelDescrMonitoraggio: string = 'SEZIONE_4.MONITORAGGIO_PIAO.DESCR_MONITORAGGIO.LABEL';

  // Documentazione a supporto
  titleDocumentazione: string = 'SEZIONE_4.DOCUMENTAZIONE.TITLE';

  // Ulteriori Info
  titleDynamicTBUlterioreInfo: string = 'SEZIONE_4.ULTERIORI_INFO.TITLE';
  subTitleDynamicTBUlterioreInfo: string = 'SEZIONE_4.ULTERIORI_INFO.SUB_TITLE';
  titleCardUlterioriInfo: string = 'SEZIONE_4.ULTERIORI_INFO.CARD_TITLE';
  labelBtnAddUlterioriInfo: string = 'SEZIONE_4.ULTERIORI_INFO.BTN_ADD';

  // Allegato
  titleAllegato: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ATTACHMENT.TITLE_ATTACHMENT';
  subTitleAllegato: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ATTACHMENT.SUB_TITLE_ATTACHMENT';

  // Labels per accordion content - Sezione 2
  labelIntro21: string = 'SEZIONE_4.ACCORDION_CONTENT.INTRO_21.LABEL';
  labelIntro22: string = 'SEZIONE_4.ACCORDION_CONTENT.INTRO_22.LABEL';
  labelDescr22: string = 'SEZIONE_4.ACCORDION_CONTENT.DESCR_22.LABEL';
  labelDescr23: string = 'SEZIONE_4.ACCORDION_CONTENT.DESCR_23.LABEL';

  // Labels per accordion content - Sezione 3
  labelDescr31: string = 'SEZIONE_4.ACCORDION_CONTENT.DESCR_31.LABEL';
  labelDescr32: string = 'SEZIONE_4.ACCORDION_CONTENT.DESCR_32.LABEL';
  labelDescr331: string = 'SEZIONE_4.ACCORDION_CONTENT.DESCR_331.LABEL';
  labelDescr332: string = 'SEZIONE_4.ACCORDION_CONTENT.DESCR_332.LABEL';
  labelElencoCategoriaObiettivi: string = 'SEZIONE_4.ACCORDION_CONTENT.ELENCO_CATEGORIA_OBIETTIVI';
  labelNonInseriteCategorieObiettivi: string = 'SEZIONE_4.ACCORDION_CONTENT.NON_INSERITE_CATEGORIE';
  labelInserisciCategoriaObiettivi: string =
    'SEZIONE_4.ACCORDION_CONTENT.INSERISCI_CATEGORIA_OBIETTIVI';

  // Configurazione Allegati e Sezione
  codTipologia: SectionEnum = SectionEnum.SEZIONE_4;
  codTipologiaAllegato: CodTipologiaAllegatoEnum = CodTipologiaAllegatoEnum.ALLEGATO_SEZIONE;
  codTipologiaAllegatoMonitoraggio: CodTipologiaAllegatoEnum =
    CodTipologiaAllegatoEnum.DOCUMENTAZIONE_MONITORAGGIO;
  sezioneSection: SectionEnum = SectionEnum.SEZIONE_4;

  // Configurazione Dynamic TB
  ulterioriInfoConfig!: DynamicTBConfig;

  // Accordion open state
  openAccordionSez2Index: number = 0;
  openAccordionSez3Index: number = 0;

  // Colonne tabella obiettivi
  obiettiviColumns = [
    { key: 'sottofaseRiferimento', label: 'SEZIONE_4.TABLE.SOTTOFASE_RIFERIMENTO' },
    { key: 'categoriaObiettivi', label: 'SEZIONE_4.TABLE.CATEGORIA_OBIETTIVI' },
    { key: 'attoriCoinvolti', label: 'SEZIONE_4.TABLE.ATTORI_COINVOLTI' },
    { key: 'attivita', label: 'SEZIONE_4.TABLE.ATTIVITA' },
    { key: 'dataInizio', label: 'SEZIONE_4.TABLE.DATA_INIZIO' },
    { key: 'dataFine', label: 'SEZIONE_4.TABLE.DATA_FINE' },
    { key: 'azioni', label: 'SEZIONE_4.TABLE.AZIONI' },
  ];

  minDate!: string;
  maxDate!: string;
  milestone: { date: string; name?: string }[] = [];

  private destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.initUlterioriInfoConfig();
    // Carica i dati aggiornati dal BE prima di creare il form
    this.loadSezione4Data();

    // Sottoscrizione agli aggiornamenti della sezione4
    this.sezione4Service.onSezione4Updated$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((sezione4) => {
        if (sezione4) {
          this.sezione4Data = sezione4;
          this.updateRealTimeSavedFormArrays(sezione4);
        }
      });
  }

  private updateRealTimeSavedFormArrays(sezione4: Sezione4DTO): void {
    // Aggiorna il FormArray delle sottofasi monitoraggio
    if (sezione4) {
      const sottofasiFormArray = this.fb.array(
        this.createSottofasiFormArray(sezione4.sottofaseMonitoraggio || [])
      );
      this.form.setControl('sottofaseMonitoraggio', sottofasiFormArray);

      // Aggiorna anche il dropdown delle sottofasi per le milestone
      this.populateDropdownSottofaseMonitoraggio();

      // Aggiorna il FormArray delle categorie obiettivi
      const categoriaObiettiviFormArraySez21 = this.fb.array(
        this.createCategoriaObiettiviFormArray(
          sezione4.categoriaObiettivi || [],
          CodTipologiaCategoriaEnum.SEZIONE_21
        )
      );
      // Aggiorna il FormArray delle categorie obiettivi
      const categoriaObiettiviFormArraySez22 = this.fb.array(
        this.createCategoriaObiettiviFormArray(
          sezione4.categoriaObiettivi || [],
          CodTipologiaCategoriaEnum.SEZIONE_22
        )
      );

      this.form.setControl('monitoraggio21Obiettivi', categoriaObiettiviFormArraySez21);
      this.form.setControl('monitoraggio22OrgObiettivi', categoriaObiettiviFormArraySez22);

      this.saveStoricoStatoSezione(sezione4);

      this.formValueChanged.emit(this.form.value);
    }
  }

  private loadSezione4Data(): void {
    if (this.piaoDTO?.id) {
      this.sezione4Service
        .getSezione4ByIdPiao(this.piaoDTO.id)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (data) => {
            if (data) {
              this.sezione4Data = data;
              this.piaoDTO.idSezione4 = data.id;
              this.sessionStorageService.setItem(KEY_PIAO, this.piaoDTO);
            }
            this.createForm();
            // Salva lo storico dello stato solo se non è già in uno degli stati validi
            if (!this.noReloadStato(this.sezione4Data?.statoSezione)) {
              this.saveStoricoStatoSezione(this.sezione4Data || new Sezione4DTO());
            }
          },
          error: () => {
            // In caso di errore, crea comunque il form vuoto
            this.createForm();
          },
        });
    } else {
      this.createForm();
    }
  }

  /**
   * Carica i dati della sezione 4 dal PIAO precedente senza modificare il piaoDTO in sessione.
   */
  loadFromPreviousPiao(previousPiaoId: number): Observable<boolean> {
    return this.sezione4Service.getSezione4ByIdPiao(previousPiaoId).pipe(
      map((data) => {
        let success = false;
        if (data) {
          // Non considerare lo statoSezione del PIAO precedente
          (data as any).statoSezione = undefined;
          this.sezione4Data = data;
          success = true;
        }
        this.createForm();
        return success;
      }),
      catchError((err) => {
        console.error('Errore nel caricamento dati sezione 4 dal PIAO precedente:', err);
        this.createForm();
        return of(false);
      })
    );
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

  prepareDataForSave(): Sezione4DTO {
    const formValue = this.form.getRawValue();
    const payload = {
      id: this.sezione4Data?.id, // Per update se esiste
      idPiao: this.piaoDTO?.id,
      statoSezione: formValue.statoSezione,
      descrStrumenti: formValue.descrStrumenti,
      descrModalitaRilevazione: formValue.descrModalitaRilevazione,
      intro: formValue.intro,
      descrMonitoraggio: formValue.descrMonitoraggio,
      // Monitoraggio Sezione 2
      intro21: formValue.intro21,
      intro22: formValue.intro22,
      descr22: formValue.descr22,
      descr23: formValue.descr23,
      // Monitoraggio Sezione 3
      descr31: formValue.descr31,
      descr32: formValue.descr32,
      descr331: formValue.descr331,
      descr332: formValue.descr332,
      // Sottofasi monitoraggio
      sottofaseMonitoraggio: formValue.sottofaseMonitoraggio,
      // Categorie obiettivi (unione dei due FormArray)
      categoriaObiettivi: [
        ...formValue.monitoraggio21Obiettivi,
        ...formValue.monitoraggio22OrgObiettivi,
      ],
      // Altri campi
      ulterioriInfo: formValue.ulterioriInfo,
      allegati: formValue.allegati,
    } as Sezione4DTO;

    // Console log del payload per debug

    return payload;
  }

  validate(testoSezione: string, campiModificati: string): Observable<any> {
    const sezione4Id = this.sezione4Data?.id || this.piaoDTO?.idSezione4 || -1;
    return this.sezione4Service.validation(sezione4Id, testoSezione, campiModificati);
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
    printFormErrors(this.form);

    if (hasRequiredErrors(this.form)) {
      return SectionStatusEnum.IN_COMPILAZIONE;
    }

    return SectionStatusEnum.COMPILATA;
  }

  createForm(): void {
    const sezione4 = this.sezione4Data || new Sezione4DTO();

    this.form = this.fb.group({
      id: this.fb.control<number | null>(sezione4.id || null, [
        Validators.maxLength(20),
        Validators.pattern(ONLY_NUMBERS_REGEX),
      ]),
      statoSezione: this.fb.control<string | null>(sezione4.statoSezione || null, [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      // Strumenti e modalità del monitoraggio
      descrStrumenti: this.fb.control<string | null>(sezione4.descrStrumenti || null, [
        Validators.required,
        Validators.maxLength(20000),
        Validators.pattern(INPUT_REGEX),
      ]),
      // Modalità di rilevazione della soddisfazione degli utenti
      descrModalitaRilevazione: this.fb.control<string | null>(
        sezione4.descrModalitaRilevazione || null,
        [Validators.required, Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      // Introduzione attività monitoraggio
      intro: this.fb.control<string | null>(sezione4.intro || null, [
        Validators.maxLength(20000),
        Validators.pattern(INPUT_REGEX),
      ]),
      // Il monitoraggio del PIAO
      descrMonitoraggio: this.fb.control<string | null>(sezione4.descrMonitoraggio || null, [
        Validators.maxLength(20000),
        Validators.pattern(INPUT_REGEX),
      ]),

      // === MONITORAGGIO SEZIONE 2 ===

      // 2.1 Valore Pubblico - Introduzione
      intro21: this.fb.control<string | null>(sezione4.intro21 || null, [
        Validators.maxLength(20000),
        Validators.pattern(INPUT_REGEX),
      ]),
      // 2.1 Valore Pubblico - Obiettivi
      monitoraggio21Obiettivi: this.fb.array(
        this.createCategoriaObiettiviFormArray(
          sezione4.categoriaObiettivi || [],
          CodTipologiaCategoriaEnum.SEZIONE_21
        )
      ),

      // 2.2 Performance - Introduzione
      intro22: this.fb.control<string | null>(sezione4.intro22 || null, [
        Validators.maxLength(20000),
        Validators.pattern(INPUT_REGEX),
      ]),
      // 2.2 Performance Organizzativa - Obiettivi
      monitoraggio22OrgObiettivi: this.fb.array(
        this.createCategoriaObiettiviFormArray(
          sezione4.categoriaObiettivi || [],
          CodTipologiaCategoriaEnum.SEZIONE_22
        )
      ),

      // 2.2 Performance Individuale - Descrizione
      descr22: this.fb.control<string | null>(sezione4.descr22 || null, [
        Validators.maxLength(20000),
        Validators.pattern(INPUT_REGEX),
      ]),

      // 2.3 Rischi corruttivi - Descrizione
      descr23: this.fb.control<string | null>(sezione4.descr23 || null, [
        Validators.maxLength(20000),
        Validators.pattern(INPUT_REGEX),
      ]),

      // === MONITORAGGIO SEZIONE 3 ===

      // 3.1 Organizzazione - Descrizione
      descr31: this.fb.control<string | null>(sezione4.descr31 || null, [
        Validators.maxLength(20000),
        Validators.pattern(INPUT_REGEX),
      ]),

      // 3.2 Lavoro agile - Descrizione
      descr32: this.fb.control<string | null>(sezione4.descr32 || null, [
        Validators.maxLength(20000),
        Validators.pattern(INPUT_REGEX),
      ]),

      // 3.3.1 Fabbisogno personale - Descrizione
      descr331: this.fb.control<string | null>(sezione4.descr331 || null, [
        Validators.maxLength(20000),
        Validators.pattern(INPUT_REGEX),
      ]),

      // 3.3.2 Formazione personale - Descrizione
      descr332: this.fb.control<string | null>(sezione4.descr332 || null, [
        Validators.maxLength(20000),
        Validators.pattern(INPUT_REGEX),
      ]),

      // Sottofasi monitoraggio
      sottofaseMonitoraggio: this.fb.array(
        this.createSottofasiFormArray(sezione4.sottofaseMonitoraggio || [])
      ),

      // Ulteriori info
      ulterioriInfo: createFormMongoFromPiaoSession<UlterioriInfoDTO>(
        this.fb,
        sezione4.ulterioriInfo || new UlterioriInfoDTO(),
        ['id', 'externalId', 'properties'],
        INPUT_REGEX,
        50,
        false
      ),

      allegati: this.fb.array<FormGroup>([
        this.fb.group({
          id: [
            sezione4.allegati?.[0]?.id || null,
            [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
          ],
          idEntitaFK: [
            sezione4.allegati?.[0]?.idEntitaFK || null,
            [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
          ],
          codDocumento: [
            sezione4.allegati?.[0]?.codDocumento || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          codTipologiaFK: [
            sezione4.allegati?.[0]?.codTipologiaFK || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          codTipologiaAllegato: [
            sezione4.allegati?.[0]?.codTipologiaAllegato || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          downloadUrl: [
            sezione4.allegati?.[0]?.downloadUrl || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          sizeAllegato: [
            sezione4.allegati?.[0]?.sizeAllegato || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          descrizione: [
            sezione4.allegati?.[0]?.descrizione || null,
            [Validators.maxLength(100), Validators.pattern(INPUT_REGEX)],
          ],
          createdByNameSurname: [
            sezione4.allegati?.[0]?.createdByNameSurname || null,
            [Validators.maxLength(255)],
          ],
          createdByRole: [
            sezione4.allegati?.[0]?.createdByRole || null,
            [Validators.maxLength(255)],
          ],
        }),
      ]),
    });

    // Emetti il valore del form dopo la creazione
    this.isFormReady = true;
    this.formValueChanged.emit(this.form.value);
  }

  private saveStoricoStatoSezione(sezione4: Sezione4DTO): void {
    const newStatus = this.getSectionStatus();
    if (newStatus !== sezione4.statoSezione) {
      this.storicoStatoSezioneService
        .save({
          idEntitaFK: sezione4.id || -1,
          codTipologiaFK: this.sezioneSection,
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

  private createCategoriaObiettiviFormArray(
    categorie: CategoriaObiettiviDTO[],
    tipologia: CodTipologiaCategoriaEnum
  ): FormGroup[] {
    if (!categorie || categorie.length === 0) {
      return [];
    }
    return categorie
      .filter((c) => c.codTipologiaFk === tipologia)
      .map((c) => this.createCategoriaObiettiviFormGroup(c));
  }

  private createCategoriaObiettiviFormGroup(categoria: CategoriaObiettiviDTO): FormGroup {
    return this.fb.group({
      id: [categoria.id || null],
      idSezione4: [categoria.idSezione4 || null],
      idSottofase: [categoria.idSottofase || null],
      idCategoriaObiettivi: [categoria.idCategoriaObiettivi || null],
      codTipologiaFk: [categoria.codTipologiaFk || null],
      attivita: createFormMongoAttivitaFromPiaoSession<BaseMongoAttivitaDTO>(
        this.fb,
        categoria.attivita || new BaseMongoAttivitaDTO(),
        ['id', 'externalId', 'propertyAttivita'],
        INPUT_REGEX,
        50,
        false
      ),
      attore: createFormMongoFromPiaoSession4Item<AttoreDTO>(
        this.fb,
        categoria.attore || new AttoreDTO(),
        ['id', 'externalId', 'externalIdFK', 'properties'],
        INPUT_REGEX,
        50,
        false
      ),
      ulterioriInfo: createFormMongoFromPiaoSession<UlterioriInfoDTO>(
        this.fb,
        categoria.ulterioriInfo || new UlterioriInfoDTO(),
        ['id', 'externalId', 'properties'],
        INPUT_REGEX,
        50,
        false
      ),
    });
  }

  private createSottofasiFormArray(sottofasi: SottofaseMonitoraggioDTO[]): FormGroup[] {
    if (!sottofasi || sottofasi.length === 0) {
      return [];
    }
    return sottofasi.map((sottofase) => this.createSottofaseFormGroup(sottofase));
  }

  private createSottofaseFormGroup(sottofase: SottofaseMonitoraggioDTO): FormGroup {
    return this.fb.group({
      id: [sottofase.id || null],
      idSezione4: [sottofase.idSezione4 || null],
      denominazione: [
        sottofase.denominazione || null,
        [Validators.maxLength(200), Validators.pattern(INPUT_REGEX)],
      ],
      descrizione: [
        sottofase.descrizione || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)],
      ],
      dataInizio: [sottofase.dataInizio || null],
      dataFine: [sottofase.dataFine || null],
      strumenti: [
        sottofase.strumenti || null,
        [Validators.maxLength(200), Validators.pattern(INPUT_REGEX)],
      ],
      fonteDato: [
        sottofase.fonteDato || null,
        [Validators.maxLength(200), Validators.pattern(INPUT_REGEX)],
      ],

      attore: createFormMongoFromPiaoSession4Item<AttoreDTO>(
        this.fb,
        sottofase.attore || new AttoreDTO(),
        ['id', 'externalId', 'externalIdFK', 'properties'],
        INPUT_REGEX,
        50,
        false
      ),

      milestone: this.fb.array(this.createMilestoneFormArray(sottofase.milestone || [])),
    });
  }

  private createMilestoneFormArray(milestones: MilestoneDTO[]): FormGroup[] {
    if (!milestones || milestones.length === 0) {
      return [];
    }
    return milestones.map((milestone) => this.createMilestoneFormGroup(milestone));
  }

  private createMilestoneFormGroup(milestone: MilestoneDTO): FormGroup {
    return this.fb.group({
      id: [milestone.id || null],
      idSottofaseMonitoraggio: [milestone.idSottofaseMonitoraggio || null, Validators.required],
      descrizione: [
        milestone.descrizione || null,
        [Validators.maxLength(50), Validators.required, Validators.pattern(INPUT_REGEX)],
      ],
      data: [milestone.data || null, [Validators.required, Validators.pattern(DATE_REGEX)]],
      isPromemoria: [milestone.isPromemoria || null],
      idPromemoria: [milestone.idPromemoria || null],
      dataPromemoria: [milestone.dataPromemoria || null, [Validators.pattern(DATE_REGEX)]],
    });
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

  // Getter per FormArray sottofasi
  get sottofaseMonitoraggioArray(): FormArray {
    return this.form.get('sottofaseMonitoraggio') as FormArray;
  }

  // Getter per FormArray obiettivi 2.1
  get monitoraggio21ObiettiviArray(): FormArray {
    return this.form.get('monitoraggio21Obiettivi') as FormArray;
  }

  // Getter per FormArray obiettivi 2.2 Org
  get monitoraggio22OrgObiettiviArray(): FormArray {
    return this.form.get('monitoraggio22OrgObiettivi') as FormArray;
  }

  // Getter per dropdown sottofasi con id valido
  get sottofaseMonitoraggioDropdown(): LabelValue[] {
    return this.sottofaseMonitoraggioArray.controls
      .filter((control) => control.get('id')?.value != null)
      .map((control) => ({
        label: control.get('denominazione')?.value || '',
        value: control.get('id')?.value,
        additionalField: {
          dataInizio: control.get('dataInizio')?.value,
          dataFine: control.get('dataFine')?.value,
        },
      }));
  }

  // Handlers per le azioni
  handleInserisciSottofase(): void {
    if (this.isDettaglio) return;

    this.isModalSottofaseOpen = true;
  }

  handleCloseModalSottofase(): void {
    this.isModalSottofaseOpen = false;
  }

  handleConfirmModalSottofase(sottofase: SottofaseMonitoraggioDTO): void {
    if (this.isDettaglio) return;

    this.sottofaseMonitoraggioArray.push(this.createSottofaseFormGroup(sottofase));
    this.isModalSottofaseOpen = false;
    this.toastService.success('Sottofase inserita con successo');
  }

  handleImpostaMilestone(): void {
    this.populateDropdownSottofaseMonitoraggio();
    this.milestoneEdit = undefined; // Reset per nuova milestone
    this.openModalMilestone = true;
  }

  handleCloseModalMilestone(): void {
    this.modalMilestoneChild?.resetForm();
    this.openModalMilestone = false;
    this.milestoneEdit = undefined;
  }

  handleConfirmModalMilestone(): void {
    if (this.isDettaglio) return;

    const modalBody = this.modalMilestoneChild;
    if (modalBody && modalBody.formGroup && modalBody.formGroup.valid) {
      const formValue = modalBody.formGroup.getRawValue();
      const milestone: MilestoneDTO = {
        id: formValue.id,
        idSottofaseMonitoraggio: formValue.idSottofaseMonitoraggio,
        descrizione: formValue.descrizione,
        data: formValue.data,
        isPromemoria: formValue.isPromemoria,
        idPromemoria: formValue.idPromemoria,
        dataPromemoria: formValue.dataPromemoria,
      };

      // Trova la sottofase corrispondente e aggiungi la milestone
      const sottofaseIndex = this.sottofaseMonitoraggioArray.controls.findIndex(
        (control) => control.get('id')?.value === milestone.idSottofaseMonitoraggio
      );

      if (sottofaseIndex !== -1) {
        const sottofaseControl = this.sottofaseMonitoraggioArray.at(sottofaseIndex);
        // La logica di salvataggio è gestita dal componente elenco-milestone tramite API
        this.toastService.success('Milestone impostata con successo');
        this.handleCloseModalMilestone();
      } else {
        this.toastService.error('Sottofase non trovata');
      }
    } else {
      if (modalBody?.formGroup) {
        modalBody.formGroup.markAllAsTouched();
      }
    }
  }

  private populateDropdownSottofaseMonitoraggio(): void {
    const sottofasiArray = this.sottofaseMonitoraggioArray;
    this.dropdownSottofaseMonitoraggio = sottofasiArray.controls.map((control, index) => {
      const denominazione = control.get('denominazione')?.value || `Sottofase ${index + 1}`;
      const id = control.get('id')?.value || index;
      return {
        label: denominazione,
        value: id,
      };
    });
  }

  // Handler per aggiungere sottofase
  handleInserisciSottofaseMonitoraggio(): void {
    if (this.isDettaglio) return;

    this.sottofaseMonitoraggioArray.push(
      this.createSottofaseFormGroup(new SottofaseMonitoraggioDTO())
    );
    this.toastService.info(
      'Funzionalità "Inserisci Sottofase" in sviluppo - Modale da implementare'
    );
  }

  // Handler per eliminazione sottofase
  handleEliminaSottofase(index: number): void {
    if (this.isDettaglio) return;

    this.sottofaseMonitoraggioArray.removeAt(index);
  }

  // Handler per inserimento categoria obiettivi
  handleInserisciCategoriaObiettivi(formArrayName: string): void {
    const formArray = this.form.get(formArrayName) as FormArray;
    formArray.push(this.createObiettivoFormGroup());
    this.toastService.info(
      'Funzionalità "Inserisci Categoria Obiettivi" in sviluppo - Modale da implementare'
    );
  }

  // Handler per eliminazione categoria obiettivi
  handleEliminaCategoriaObiettivi(formArrayName: string, index: number): void {
    if (this.isDettaglio) return;

    const formArray = this.form.get(formArrayName) as FormArray;
    formArray.removeAt(index);
  }

  // Crea FormGroup per un obiettivo
  private createObiettivoFormGroup(): FormGroup {
    return this.fb.group({
      id: [null],
      sottofaseRiferimento: [null, [Validators.maxLength(200), Validators.pattern(INPUT_REGEX)]],
      categoriaObiettivi: [null, [Validators.maxLength(200), Validators.pattern(INPUT_REGEX)]],
      attoriCoinvolti: [[]],
      attivita: [[]],
      dataInizio: [null],
      dataFine: [null],
    });
  }

  // Handler per il caricamento allegato
  handleAttachmentLoaded(): void {
    // Riemetti il valore del form quando gli allegati vengono caricati
    this.formValueChanged.emit(this.form.value);
  }

  // Handler per il reload degli allegati
  reloadAttachment(): void {
    this.attachmentAllegatoComponent?.getAllAttachment();
  }

  get tasks(): GanttTask[] {
    const tasks: GanttTask[] = [];

    const sottofasi = this.sottofaseMonitoraggioArray.value as SottofaseMonitoraggioDTO[];
    if (sottofasi && sottofasi.length > 0) {
      const dates = sottofasi
        .filter((s) => s.dataInizio && s.dataFine)
        .flatMap((s) => [s.dataInizio!, s.dataFine!]);

      if (dates.length > 0) {
        const minDate = dates.reduce((a, b) => (a < b ? a : b));
        const maxDate = dates.reduce((a, b) => (a > b ? a : b));

        this.minDate = minDate;
        this.maxDate = maxDate;

        tasks.push({
          name: 'Fase Monitoraggio',
          type: 'fase',
          start: minDate,
          end: maxDate,
        });

        let dataMilestone = sottofasi?.flatMap(
          (s) =>
            s.milestone
              ?.filter((m) => m.data)
              .map((m) => ({
                id: m.id,
                data: m.data,
                sottofaseName: s.denominazione,
              })) || []
        );

        // Deduplica milestone per data
        const uniqueDates = new Set<number>();
        this.milestone = dataMilestone
          .filter((m) => {
            if (m.id && !uniqueDates.has(m.id)) {
              uniqueDates.add(m.id);
              return true;
            }
            return false;
          })
          .map((m) => ({ date: m.data!.split('T')[0], name: m.sottofaseName }));

        sottofasi
          .filter((s) => s.dataInizio && s.dataFine)
          .forEach((s) => {
            tasks.push({
              name: s.denominazione || 'Sottofase',
              type: 'sottofase',
              start: s.dataInizio!,
              end: s.dataFine!,
            });
          });
      }
    }

    return tasks;
  }
}
