import { Component, DestroyRef, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { catchError, forkJoin, map, Observable, of, switchMap } from 'rxjs';
import { SharedModule } from '../../../../../../../shared/module/shared/shared.module';
import { TextAreaComponent } from '../../../../../../../shared/components/text-area/text-area.component';
import { AccordionComponent } from '../../../../../../../shared/components/accordion/accordion.component';
import { AttachmentComponent } from '../../../../../../../shared/ui/attachment/attachment.component';
import { BaseComponent } from '../../../../../../../shared/components/base/base.component';
import { ISezioneBase } from '../../../../../../../shared/models/interfaces/sezione-base.interface';
import { PIAODTO } from '../../../../../../../shared/models/classes/piao-dto';
import { Sezione31DTO } from '../../../../../../../shared/models/classes/sezione-31-dto';
import { INPUT_REGEX, KEY_PIAO } from '../../../../../../../shared/utils/constants';
import { SectionStatusEnum } from '../../../../../../../shared/models/enums/section-status.enum';
import { CodTipologiaAllegatoEnum } from '../../../../../../../shared/models/enums/cod-tipologia-allegato.enum';
import { CodTipologiaSezioneEnum } from '../../../../../../../shared/models/enums/cod-tipologia-sezione.enum';
import {
  areAllValuesNull,
  createFormArrayAmpiezzaOrganizzativaFromPiaoSession,
  createFormArrayTabelleFunzionaliFromPiaoSession,
  hasRequiredErrors,
  minArrayLength,
  printFormErrors,
} from '../../../../../../../shared/utils/utils';
import { Sezione31Service } from '../../../../../../../shared/services/sezione31.service';
import { GraficoSezione31DTO } from '../../../../../../../shared/models/classes/grafico-sezione-31-dto';
import { AmpiezzaOrganizzativaComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-3.1/ampiezza-organizzativa/ampiezza-organizzativa.component';
import { SessionStorageService } from '../../../../../../../shared/services/session-storage.service';
import { TabellaFunzionaleComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/tabella-funzionale/tabella-funzionale.component';
import { SectionEnum } from '../../../../../../../shared/models/enums/section.enum';
import { GraficoOrganicoAmministrazioneComponent } from '../../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-3.1/grafico-organico-amministrazione/grafico-organico-amministrazione.component';
import { AzioniComponent } from '../../../../../../../shared/components/azioni/azioni.component';
import { IVerticalEllipsisActions } from '../../../../../../../shared/models/interfaces/vertical-ellipsis-actions';
import { StoricoStatoSezioneService } from '../../../../../../../shared/services/storico-stato-sezione.service';

@Component({
  selector: 'piao-sezione-3-1',
  imports: [
    SharedModule,
    ReactiveFormsModule,
    TextAreaComponent,
    AccordionComponent,
    AttachmentComponent,
    AmpiezzaOrganizzativaComponent,
    TabellaFunzionaleComponent,
    //GraficoOrganicoAmministrazioneComponent,
    //AzioniComponent,
  ],
  templateUrl: './sezione3.1.component.html',
  styleUrl: './sezione3.1.component.scss',
})
export class Sezione31Component extends BaseComponent implements OnInit, ISezioneBase {
  @Input() piaoDTO!: PIAODTO;
  @Input() sezione31Data?: Sezione31DTO;
  @Input() testoSezione!: string;
  @Input() isDettaglio: boolean = false;

  @Output() formValueChanged = new EventEmitter<any>();

  isFormReady = false;

  private fb = inject(FormBuilder);
  private sezione31Service = inject(Sezione31Service);
  private destroyRef = inject(DestroyRef);

  form!: FormGroup;
  openAccordionIndex: number = 1;
  graficoData: GraficoSezione31DTO[] = [];

  // Titolo principale
  title: string = 'SEZIONE_31.TITLE';

  // Parte Generale
  subTitleParteGenerale: string = 'SEZIONE_31.PARTE_GENERALE';

  // Accordion 1 - Fotografia dell'organizzazione
  titleAccordion1: string = 'SEZIONE_31.ACCORDION_1.TITLE';

  // Struttura organizzativa
  subtitleStrutturaOrganizzativa: string = 'SEZIONE_31.ACCORDION_1.STRUTTURA_ORG.SUBTITLE';
  labelStrutturaOrganizzativa: string = 'SEZIONE_31.ACCORDION_1.STRUTTURA_ORG.LABEL';

  // Organigramma
  labelOrganigramma: string = 'SEZIONE_31.ACCORDION_1.ORGANIGRAMMA.LABEL';
  labelOrganigrammaDettaglio: string = 'SEZIONE_31.ACCORDION_1.ORGANIGRAMMA.LABEL_DETTAGLIO';
  labelImageNotFound: string = 'SEZIONE.ATTACHMENT.ORGANIGRAMMA.IMAGE_NOT_FOUND';

  descOrganigramma: string = 'SEZIONE_31.ACCORDION_1.ORGANIGRAMMA.DESC';

  // Ampiezza organizzativa
  labelAmpiezzaOrganizzativa: string = 'SEZIONE_31.ACCORDION_1.AMPIEZZA_ORG.LABEL';

  // Ulteriori dettagli
  labelUlterioriDettagli: string = 'SEZIONE_31.ACCORDION_1.ULTERIORI_DETTAGLI.LABEL';

  // Incarichi dirigenziali
  subtitleIncarichiDirigenziali: string = 'SEZIONE_31.ACCORDION_1.INCARICHI_DIR.SUBTITLE';
  labelIncarichiDirigenziali: string = 'SEZIONE_31.ACCORDION_1.INCARICHI_DIR.LABEL';
  labelIncarichiDirigenzialiDettaglio: string =
    'SEZIONE_31.ACCORDION_1.INCARICHI_DIR.LABEL_DETTAGLIO';

  // Profili professionali
  subtitleProfiliProfessionali: string = 'SEZIONE_31.ACCORDION_1.PROFILI_PROF.SUBTITLE';
  labelProfiliProfessionali: string = 'SEZIONE_31.ACCORDION_1.PROFILI_PROF.LABEL';
  labelProfiliProfessionaliDettaglio: string =
    'SEZIONE_31.ACCORDION_1.PROFILI_PROF.LABEL_DETTAGLIO';

  // Rappresentazione grafica
  labelRappresentazioneGrafica: string = 'SEZIONE_31.ACCORDION_1.RAPP_GRAFICA.LABEL';

  // Accordion 2 - Linee strategiche
  titleAccordion2: string = 'SEZIONE_31.ACCORDION_2.TITLE';
  labelLineeStrategiche: string = 'SEZIONE_31.ACCORDION_2.LINEE_STRATEGICHE.LABEL';
  descLineeStrategiche: string = 'SEZIONE_31.ACCORDION_2.LINEE_STRATEGICHE.DESC';

  // Parte Funzionale
  subTitleParteFunzionale: string = 'SEZIONE_31.PARTE_FUNZIONALE';
  labelTabellaFunzionale: string = 'SEZIONE_31.TABELLA_FUNZIONALE.LABEL';

  // Configurazione Attachment immagine organigramma
  sezione: SectionEnum = SectionEnum.SEZIONE_3_1;
  codTipologia: SectionEnum = SectionEnum.SEZIONE_3_1;
  codTipologiaAllegatoImage1: CodTipologiaAllegatoEnum =
    CodTipologiaAllegatoEnum.IMMAGINE_SEZIONE_31;

  codTipologiaFK: string = SectionEnum.SEZIONE_3_1;

  storicoStatoSezioneService = inject(StoricoStatoSezioneService);

  ngOnInit(): void {
    this.loadSezione31Data();

    this.sezione31Service.onSezione31Updated$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((sezione31) => {
        if (sezione31) {
          this.sezione31Data = sezione31;
          this.updateRealTimeSavedFormArrays(sezione31);
        }
      });
  }

  private updateRealTimeSavedFormArrays(sezione31: Sezione31DTO): void {
    // Aggiorna il FormArray ampiezzaOrganizzativa con i dati restituiti dal salvataggio in tempo reale
    const ampiezzaArray = this.form.get('ampiezzaOrganizzative') as FormArray;
    if (ampiezzaArray) {
      const newArray = createFormArrayAmpiezzaOrganizzativaFromPiaoSession(
        this.fb,
        sezione31.ampiezzaOrganizzative || [],
        sezione31.id || this.piaoDTO?.idSezione31
      );
      ampiezzaArray.clear();
      newArray.controls.forEach((control) => ampiezzaArray.push(control));
    }

    const tabellaFunzionaleArray = this.form.get('tabelleFunzionali') as FormArray;
    if (tabellaFunzionaleArray) {
      const newTabellaFunzionaleArray = createFormArrayTabelleFunzionaliFromPiaoSession(
        this.fb,
        sezione31.tabelleFunzionali || [],
        sezione31.id || this.piaoDTO?.idSezione31
      );
      tabellaFunzionaleArray.clear();
      newTabellaFunzionaleArray.controls.forEach((control) => tabellaFunzionaleArray.push(control));
    }

    const newStatus = this.getSectionStatus();
    console.log('nuovoStato', newStatus, 'vecchioStato', sezione31.statoSezione);
    if (newStatus !== sezione31.statoSezione) {
      this.storicoStatoSezioneService
        .save({
          idEntitaFK: sezione31.id || -1,
          codTipologiaFK: this.sezione,
          testo: newStatus || '',
        })
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.form.get('statoSezione')?.setValue(newStatus);
            this.sezione31Data = { ...this.sezione31Data, statoSezione: newStatus } as Sezione31DTO;
            console.log('Storico stato sezione salvato con successo');
          },
          error: () => {
            console.log('Errore nel salvare lo storico stato sezione');
          },
        });
    }
  }

  private loadSezione31Data(): void {
    const idPiao = this.piaoDTO?.id;

    if (!idPiao) {
      this.createForm();
      return;
    }

    this.sezione31Service
      .getSezione31ByIdPiao(idPiao)
      .pipe(
        takeUntilDestroyed(this.destroyRef),

        switchMap((sezione31) => {
          if (sezione31) {
            this.sezione31Data = sezione31;
            this.piaoDTO.idSezione31 = sezione31.id;
            this.sessionStorageService.setItem(KEY_PIAO, this.piaoDTO);
          }

          //  Se il flag graficoMinerva è true → scarico il grafico
          if (sezione31?.graficoMinerva) {
            return this.sezione31Service.getGraficoSezione31(idPiao);
          }

          //  Se il flag è false → non scarico nulla
          return of([]);
        })
      )
      .subscribe({
        next: (grafico) => {
          this.graficoData = grafico || [];
          this.createForm();

          console.log(
            'loadSezione31Data',
            this.sezione31Data?.statoSezione,
            'graficoMinerva:',
            this.sezione31Data?.graficoMinerva
          );
        },
        error: () => {
          this.createForm();
        },
      });
  }

  /**
   * Carica i dati della sezione 3.1 dal PIAO precedente senza modificare il piaoDTO in sessione.
   */
  loadFromPreviousPiao(previousPiaoId: number): Observable<boolean> {
    let success = false;
    return this.sezione31Service.getSezione31ByIdPiao(previousPiaoId).pipe(
      takeUntilDestroyed(this.destroyRef),
      switchMap((sezione31) => {
        if (sezione31) {
          this.sezione31Data = sezione31;
          success = true;
        }
        if (sezione31?.graficoMinerva) {
          return this.sezione31Service.getGraficoSezione31(previousPiaoId);
        }
        return of([]);
      }),
      map((grafico) => {
        this.graficoData = grafico || [];
        this.createForm();
        return success;
      }),
      catchError((err) => {
        console.error('Errore nel caricamento dati sezione 3.1 dal PIAO precedente:', err);
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

  prepareDataForSave(): Sezione31DTO {
    const formValue = this.form.getRawValue();
    const sezione31 = this.sezione31Data || new Sezione31DTO();
    const sezione31Id = this.sezione31Data?.id || this.piaoDTO?.idSezione31 || -1;
    console.log('save', formValue, formValue.grafico);

    return {
      ...sezione31,
      id: sezione31Id,
      idPiao: this.piaoDTO?.id || null,
      strutturaOrganizzativaAP: formValue.strutturaOrganizzativaAP || null,
      ampiezzaOrganica: formValue.ampiezzaOrganica || null,
      incarichiDirigenziali: formValue.incarichiDirigenziali || null,
      profiliProfessionali: formValue.profiliProfessionali || null,
      lineeOrganizzazione: formValue.lineeOrganizzazione || null,
      ampiezzaOrganizzative: formValue.ampiezzaOrganizzative || null,
      tabelleFunzionali: formValue.tabelleFunzionali || null,
      allegati: formValue.allegati || null,
      statoSezione: this.getSectionStatus(),
      graficoMinerva: formValue.grafico ?? false,
    } as Sezione31DTO;
  }

  validate(testoSezione: string, campiModificati: string): Observable<any> {
    const sezione31Id = this.sezione31Data?.id || this.piaoDTO?.idSezione31 || -1;
    return this.sezione31Service.validation(sezione31Id, testoSezione, campiModificati);
  }

  resetForm(): void {
    this.form.reset();
    this.createForm();
  }

  getSectionStatus(): string {
    // Tutto il form null, DA_COMPILARE
    if (!this.hasFormValues()) {
      return SectionStatusEnum.DA_COMPILARE;
    }

    // Stampa tutti gli errori required e minLength del form
    console.log('=== VALIDATION ERRORS ===');
    printFormErrors(this.form);
    console.log('=========================');

    // Se i required non sono stati compilati, IN_COMPILAZIONE
    if (hasRequiredErrors(this.form)) {
      return SectionStatusEnum.IN_COMPILAZIONE;
    }

    // Form valido e con i campi compilati, COMPILATA
    return SectionStatusEnum.COMPILATA;
  }

  createForm(): void {
    const sezione31 = this.sezione31Data || new Sezione31DTO();

    this.form = this.fb.group({
      id: this.fb.control<number | null>(this.piaoDTO.idSezione331 || null),
      idPiao: this.fb.control<number | null>(this.piaoDTO.id || null),
      statoSezione: this.fb.control<string | null>(sezione31.statoSezione || null, [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      // Struttura organizzativa al 31.12
      strutturaOrganizzativaAP: this.fb.control<string | null>(
        sezione31.strutturaOrganizzativaAP || null,
        [Validators.required, Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      // Ampiezza organica
      ampiezzaOrganica: this.fb.control<string | null>(sezione31.ampiezzaOrganica || null, [
        Validators.maxLength(20000),
        Validators.pattern(INPUT_REGEX),
      ]),
      // Incarichi dirigenziali e simili
      incarichiDirigenziali: this.fb.control<string | null>(
        sezione31.incarichiDirigenziali || null,
        [Validators.required, Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      // Rappresentazione dei profili professionali
      profiliProfessionali: this.fb.control<string | null>(sezione31.profiliProfessionali || null, [
        Validators.required,
        Validators.maxLength(20000),
        Validators.pattern(INPUT_REGEX),
      ]),
      // Linee dell'organizzazione
      lineeOrganizzazione: this.fb.control<string | null>(sezione31.lineeOrganizzazione || null, [
        Validators.maxLength(20000),
        Validators.pattern(INPUT_REGEX),
      ]),

      // Ampiezza organizzativa
      ampiezzaOrganizzative: createFormArrayAmpiezzaOrganizzativaFromPiaoSession(
        this.fb,
        sezione31.ampiezzaOrganizzative || [],
        this.sezione31Data?.id || this.piaoDTO?.idSezione31
      ),

      // Tabelle funzionali
      tabelleFunzionali: createFormArrayTabelleFunzionaliFromPiaoSession(
        this.fb,
        sezione31.tabelleFunzionali || [],
        this.sezione31Data?.id || this.piaoDTO?.idSezione31
      ),

      grafico: this.fb.control<boolean>(sezione31.graficoMinerva ?? false),

      allegati: this.fb.array<FormGroup>(
        [
          this.fb.group({
            id: [
              sezione31.allegati?.[0]?.id || null,
              [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
            ],
            idEntitaFK: [
              sezione31.allegati?.[0]?.idEntitaFK || null,
              [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
            ],
            codDocumento: [
              sezione31.allegati?.[0]?.codDocumento || null,
              [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
            ],
            codTipologiaFK: [
              sezione31.allegati?.[0]?.codTipologiaFK || null,
              [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
            ],
            codTipologiaAllegato: [
              sezione31.allegati?.[0]?.codTipologiaAllegato || null,
              [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
            ],
            downloadUrl: [
              sezione31.allegati?.[0]?.downloadUrl || null,
              [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
            ],
            sizeAllegato: [
              sezione31.allegati?.[0]?.sizeAllegato || null,
              [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
            ],
            descrizione: [
              sezione31.allegati?.[0]?.descrizione || null,
              [Validators.maxLength(100), Validators.pattern(INPUT_REGEX)],
            ],
            createdByNameSurname: [
              sezione31.allegati?.[0]?.createdByNameSurname || null,
              [Validators.maxLength(255)],
            ],
            createdByRole: [
              sezione31.allegati?.[0]?.createdByRole || null,
              [Validators.maxLength(255)],
            ],
            base64: [sezione31.allegati?.[0]?.base64 || null, Validators.required],
          }),
        ],
        [minArrayLength(1), Validators.required]
      ),
    });

    // Emetti il valore del form dopo la creazione
    this.isFormReady = true;
    this.formValueChanged.emit(this.form.value);
  }

  handleAttachmentLoaded(): void {
    // Riemetti il valore del form quando gli allegati vengono caricati
    console.log('Attachment caricato, ricalcolo stato sezione', this.sezione31Data?.statoSezione);
    this.handleReloadStatusSection();
    this.formValueChanged.emit(this.form.value);
  }

  handleReloadStatusSection(): void {
    const newStatus = this.getSectionStatus();
    console.log('nuovoStato', newStatus, 'vecchioStato', this.sezione31Data?.statoSezione);
    if (!this.noReloadStato(this.sezione31Data?.statoSezione)) {
      if (newStatus !== this.sezione31Data?.statoSezione) {
        this.storicoStatoSezioneService
          .save({
            idEntitaFK: this.sezione31Data?.id || -1,
            codTipologiaFK: this.sezione,
            testo: newStatus || '',
          })
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: () => {
              this.form.get('statoSezione')?.setValue(newStatus);
              this.sezione31Data = {
                ...this.sezione31Data,
                statoSezione: newStatus,
              } as Sezione31DTO;
              console.log('Storico stato sezione salvato con successo');
            },
            error: () => {
              console.log('Errore nel salvare lo storico stato sezione');
            },
          });
      }
    }
  }

  handleEliminaGrafico(): void {
    this.graficoData = [];
    this.form.get('grafico')?.setValue(false);
    if (this.sezione31Data) {
      this.sezione31Data.graficoMinerva = false;
    }
    this.formValueChanged.emit(this.form.value);
    this.toastService.success('Grafico eliminato con successo');
  }

  handleOpenModMinerva() {
    window.open('https://www.google.com', '_blank');
  }

  getAzioniForGrafico(): IVerticalEllipsisActions[] {
    const azioni: IVerticalEllipsisActions[] = [
      {
        label: 'Modifica su Minerva',
        icon: 'Upload',
        callback: () => this.handleOpenModMinerva(),
      },
    ];

    // Mostra il pulsante "Elimina" solo se il grafico esiste
    const graficoValue = this.form.get('grafico')?.value;

    if (graficoValue || (this.graficoData && this.graficoData.length > 0)) {
      azioni.push({
        label: 'Elimina',
        callback: () => this.handleEliminaGrafico(),
      });
    }

    return azioni;
  }
}
