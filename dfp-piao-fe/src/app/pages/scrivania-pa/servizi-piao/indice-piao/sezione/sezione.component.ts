import { Component, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { WorkflowComponent } from '../../../../../shared/components/workflow/workflow.component';
import { ActivatedRoute, Router } from '@angular/router';
import { SharedModule } from '../../../../../shared/module/shared/shared.module';
import { Sezione331Component } from './sezione3/sezione3.3.1/sezione3.3.1.component';
import { CardAlertComponent } from '../../../../../shared/ui/card-alert/card-alert.component';
import {
  DOCS_ICON,
  KEY_PA_ATTIVA,
  KEY_PIAO,
  PDF,
  SECTION_ID,
  SEZIONI_ACTIVE_ID,
  SHAPE_ICON,
  WARNING_ICON,
} from '../../../../../shared/utils/constants';
import { AzioniComponent } from '../../../../../shared/components/azioni/azioni.component';
import { Sezione1Component } from './sezione1/sezione1.component';
import { Sezione21Component } from './sezione2/sezione2.1/sezione2.1.component';
import { Sezione22Component } from './sezione2/sezione2.2/sezione2.2.component';
import { Sezione23Component } from './sezione2/sezione2.3/sezione2.3.component';
import { Sezione31Component } from './sezione3/sezione3.1/sezione3.1.component';
import { Sezione32Component } from './sezione3/sezione3.2/sezione3.2.component';
import { Sezione332Component } from './sezione3/sezione3.3.2/sezione3.3.2.component';
import { Sezione4Component } from './sezione4/sezione4.component';
import { SessionStorageService } from '../../../../../shared/services/session-storage.service';
import { PIAOService } from '../../../../../shared/services/piao.service';
import { StrutturaIndicePiaoDTO } from '../../../../../shared/models/classes/struttura-indice-piao-dto';
import {
  Subject,
  map,
  distinctUntilChanged,
  tap,
  shareReplay,
  of,
  combineLatest,
  Observable,
  switchMap,
} from 'rxjs';
import { ButtonComponent } from '../../../../../shared/components/button/button.component';
import { SvgComponent } from '../../../../../shared/components/svg/svg.component';
import { ModalComponent } from '../../../../../shared/components/modal/modal.component';
import {
  areAllValuesNull,
  collectNullPaths,
  hasRequiredErrors,
  getChangedFields,
  collectDeepDiff,
} from '../../../../../shared/utils/utils';
import { Sezione1Service } from '../../../../../shared/services/sezioni-1.service';
import { Sezione21Service } from '../../../../../shared/services/sezioni-21.service';
import { Sezione22Service } from '../../../../../shared/services/sezioni-22.service';
import { PIAODTO } from '../../../../../shared/models/classes/piao-dto';
import { FormArray, FormBuilder, FormGroup } from '@angular/forms';

import { SECTION_FIELDS_REQUIRED } from '../../../../../shared/utils/section-fields-required';
import { SectionStatusEnum } from '../../../../../shared/models/enums/section-status.enum';
import { ModalValidationComponent } from '../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-1/modal-validation/modal-validation.component';
import { BaseComponent } from '../../../../../shared/components/base/base.component';

import { ISezioneBase } from '../../../../../shared/models/interfaces/sezione-base.interface';
import { Sezione23Service } from '../../../../../shared/services/sezione23.service';
import { Sezione4Service } from '../../../../../shared/services/sezione4.service';
import { StakeholderService } from '../../../../../shared/services/stakeholder.service';
import { ApprovazionePubblicazioneComponent } from './approvazione-pubblicazione/approvazione-pubblicazione.component';
import { ModalPubblicazioneComponent } from '../../../../../shared/ui/servizi-piao/indice-piao/sezione/approvazione-pubblicazione/modal-pubblicazione/modal-pubblicazione.component';
import { Sezione331Service } from '../../../../../shared/services/sezione331.service';
import { Sezione332Service } from '../../../../../shared/services/sezione332.service';
import { Sezione31Service } from '../../../../../shared/services/sezione31.service';
import { Sezione32Service } from '../../../../../shared/services/sezione32.service';
import { IVerticalEllipsisActions } from '../../../../../shared/models/interfaces/vertical-ellipsis-actions';
import { Sezione32DTO } from '../../../../../shared/models/classes/sezione-32-dto';
import { TableStoricoSezioneComponent } from '../../../../../shared/ui/servizi-piao/table-storico-sezione/table-storico-sezione.component';
import { TipologiaOnline } from '../../../../../shared/models/enums/tipologia-online.enum';
import { ApprovazioneService } from '../../../../../shared/services/approvazione.service';
import { ApprovazioneDTO } from '../../../../../shared/models/classes/approvazione-dto';
import { PiaoStatusEnum } from '../../../../../shared/models/enums/piao-status.enum';
import { PiaoPeriodoCompilazioneDirective } from '../../../../../shared/directives/piao-periodo-compilazione.directive';
import { NotificaService } from 'src/app/shared/services/notifica.service';
import { SectionEnum } from 'src/app/shared/models/enums/section.enum';
import { AllegatoDTO } from 'src/app/shared/models/classes/allegato-dto';
import { takeUntil } from 'rxjs';

@Component({
  standalone: true,
  selector: 'piao-sezione',
  imports: [
    WorkflowComponent,
    SharedModule,
    Sezione331Component,
    CardAlertComponent,
    AzioniComponent,
    Sezione1Component,
    Sezione21Component,
    Sezione22Component,
    Sezione23Component,
    Sezione31Component,
    Sezione32Component,
    Sezione332Component,
    Sezione4Component,
    ButtonComponent,
    SvgComponent,
    ModalComponent,
    ModalValidationComponent,
    ApprovazionePubblicazioneComponent,
    ModalPubblicazioneComponent,
    TableStoricoSezioneComponent,
    PiaoPeriodoCompilazioneDirective,
  ],
  templateUrl: './sezione.component.html',
  styleUrl: './sezione.component.scss',
})
export class SezioneComponent extends BaseComponent implements OnInit, OnDestroy {
  router: Router = inject(Router);
  route: ActivatedRoute = inject(ActivatedRoute);
  piaoService: PIAOService = inject(PIAOService);
  fb: FormBuilder = inject(FormBuilder);
  sezione1Service: Sezione1Service = inject(Sezione1Service);
  sezione21Service: Sezione21Service = inject(Sezione21Service);
  sezione22Service: Sezione22Service = inject(Sezione22Service);
  sezione23Service: Sezione23Service = inject(Sezione23Service);
  sezione31Service: Sezione31Service = inject(Sezione31Service);
  sezione32Service: Sezione32Service = inject(Sezione32Service);
  sezione331Service: Sezione331Service = inject(Sezione331Service);
  sezione332Service: Sezione332Service = inject(Sezione332Service);
  sezione4Service: Sezione4Service = inject(Sezione4Service);
  stakeholderService: StakeholderService = inject(StakeholderService);
  approvazioneService: ApprovazioneService = inject(ApprovazioneService);
  notificaService: NotificaService = inject(NotificaService);

  // ViewChild per accedere alle sezioni
  @ViewChild('sezione1') sezione1Component?: Sezione1Component;
  @ViewChild('sezione21') sezione21Component?: Sezione21Component;
  @ViewChild('sezione22') sezione22Component?: Sezione22Component;
  @ViewChild('sezione23') sezione23Component?: Sezione23Component;
  @ViewChild('sezione31') sezione31Component?: Sezione31Component;
  @ViewChild('sezione32') sezione32Component?: Sezione32Component;
  @ViewChild('sezione331') sezione331Component?: Sezione331Component;
  @ViewChild('sezione332') sezione332Component?: Sezione332Component;
  @ViewChild('sezione4') sezione4Component?: Sezione4Component;
  @ViewChild('approvazionePubblicazione')
  approvazionePubblicazioneComponent?: ApprovazionePubblicazioneComponent;
  @ViewChild('tableStoricoSezione') tableStoricoSezioneChild?: TableStoricoSezioneComponent;

  // Variabile che contiene il valore del form quando viene creato
  sectionFormValue: any;

  activeSectionId!: string;
  title: string = '';
  subTitle: string = '';

  titleCardHeader: string = 'SCRIVANIA_PA.ALERT.TITLE';
  subTitleCardHeader: string = 'SCRIVANIA_PA.ALERT.SUB_TITLE';
  secondSubTitleCardHeader: string = 'SCRIVANIA_PA.ALERT.SECOND_SUB_TITLE';
  textHrefCardHeader: string = 'SCRIVANIA_PA.ALERT.TEXT_HREF';
  href: string = '/area-privata-PA/mancata-compilazione';
  buttonCaricaApprovazione: string =
    'APPROVAZIONE_E_PUBBLICAZIONE.CARICA_APPROVAZIONE.BUTTON_LABEL';

  goToPiao: string = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.GO_TO_PUBBLICATO';

  steps: StrutturaIndicePiaoDTO[] = [];

  private readonly subject$ = new Subject<void>();

  readonly forms: Record<string, FormGroup> = {};

  openModalNextStep: boolean = false;

  iconModalValidation: string = SHAPE_ICON;

  iconStyle: string = 'icon-modal';

  icon: string = WARNING_ICON;

  iconModalTableStorico: string = DOCS_ICON;

  piaoDTO!: PIAODTO;

  openModalValidation: boolean = false;

  isNextSection!: boolean;

  openModalPubblicazione: boolean = false;

  openModalStoricoSezione: boolean = false;

  testoSezioneMap: Record<string, string> = {};

  statoSezioneMap: Record<string, string> = {};

  selectedSectionId!: number; // id numerico
  selectedSectionEnum!: string; // codTipologiaFK

  isVisibleButtonPubblicazione: boolean = false;
  isPiaoStatoPubblicato: boolean = false;

  isDettaglio: boolean = false;
  isSemplificato: boolean = false;

  // Array azioni cacheato per evitare di restituire un nuovo riferimento ad ogni
  // change detection (causa di re-render del *ngFor nel menu di piao-azioni e
  // perdita del click sui menu item nelle sezioni con CD molto frequente, es. sezione 4 con Gantt)
  sectionActions: IVerticalEllipsisActions[] = [];
  private sectionActionsForId: string | null = null;

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);

    this.isSemplificato = this.piaoDTO.tipologiaOnline === TipologiaOnline.SEMPLIFICATO;

    // Imposto i titoli
    this.title = this.piaoDTO?.aggiornamento
      ? 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.T_AGGIORNA'
      : 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.T_REDIGI_COMPLETA';

    this.subTitle = this.piaoDTO?.aggiornamento
      ? 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ST_AGGIORNA'
      : 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ST_REDIGI_COMPLETA';

    this.loadStep();
    this.loadStakeholder();

    this.approvazioneService.onApprovazioneUpdated$
      .pipe(takeUntil(this.subject$))
      .subscribe((approvazione) => {
        if (approvazione) {
          this.isPiaoStatoPubblicato = approvazione.statoPiao === PiaoStatusEnum.PUBBLICATO;
        }
      });

    //this.updateIsDettaglio();
  }

  loadStakeholder(): void {
    const piaoDTO: PIAODTO = this.sessionStorageService.getItem(KEY_PIAO);
    if (piaoDTO?.id) {
      this.stakeholderService
        .getByPiao(piaoDTO.id)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (stakeholders) => {
            this.piaoDTO.stakeHolders = stakeholders;
            this.sessionStorageService.setItem(KEY_PIAO, this.piaoDTO);
          },
        });
    }
  }

  loadStep() {
    // Imposto subito l'ID sezione (snapshot), con fallback a '1'
    this.activeSectionId = this.route.snapshot.queryParamMap.get('idSezione') || '1';
    // Stream dei parametri query: reagisce ai cambi di 'idSezione'
    const idSezione$ = this.route.queryParamMap.pipe(
      map((params) => params.get('idSezione') ?? '1'),
      distinctUntilChanged(),
      tap((id) => {
        this.activeSectionId = id;
      })
    );

    // Stream dei steps (eseguito una sola volta; se piaoDTO.id non c'è, emette array vuoto)
    const steps$ = this.piaoDTO?.id
      ? this.piaoService.getStructureIndicePIAO(this.piaoDTO.id).pipe(
          map((value: StrutturaIndicePiaoDTO[]) => [...value]),
          // cache dell'ultimo valore per evitare ulteriori chiamate e ripubblicare ai nuovi subscriber
          shareReplay(1)
        )
      : of<StrutturaIndicePiaoDTO[]>([]);

    // Combino i due stream: aggiorno steps quando disponibile e continuo ad aggiornare activeSectionId
    combineLatest([idSezione$, steps$])
      .pipe(takeUntil(this.subject$))
      .subscribe({
        next: ([idSezione, steps]) => {
          this.activeSectionId = idSezione;
          let piaoSection = steps.find((section) => section.numeroSezione === '0');
          this.steps = steps.filter((section) => section !== piaoSection);
          this.piaoDTO.statoPiao = piaoSection?.statoSezione || this.piaoDTO.statoPiao;
          this.sessionStorageService.setItem(KEY_PIAO, this.piaoDTO);
          this.isVisibleButtonPubblicazione = this.steps
            .filter((step) => step.numeroSezione !== '5' && step.numeroSezione !== '0')
            .every((step) => {
              if (step.children && step.children.length > 0) {
                return step.children.every(
                  (child) => child.statoSezione === SectionStatusEnum.RICHIESTA_APPROVAZIONE
                );
              }
              return step.statoSezione === SectionStatusEnum.RICHIESTA_APPROVAZIONE;
            });
          this.buildTestoSezioneMap();
          this.buildStatoSezioneMap();
          this.updateIsDettaglio();
        },
        error: (err) => {
          console.error('Errore nel caricamento dell’indice PIAO:', err);
        },
      });
  }

  private updateIsDettaglio(): void {
    this.isDettaglio =
      this.activeSectionId === '5'
        ? this.resolveIsDettaglioApprovazione()
        : this.resolveIsDettaglio();

    if (this.isDettaglio) {
      this.title = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.T_DETTAGLIO';
      this.subTitle = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ST_DETTAGLIO';
    } else {
      this.title = this.piaoDTO?.aggiornamento
        ? 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.T_AGGIORNA'
        : 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.T_REDIGI_COMPLETA';
      this.subTitle = this.piaoDTO?.aggiornamento
        ? 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ST_AGGIORNA'
        : 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ST_REDIGI_COMPLETA';
    }
  }

  private resolveIsDettaglio(): boolean {
    return (
      //this.getDettaglioOverrideFromQuery(this.route.snapshot.queryParamMap.get('debugDettaglio')) ??
      [
        PiaoStatusEnum.IN_VALIDAZIONE.toString(),
        PiaoStatusEnum.VALIDATO.toString(),
        PiaoStatusEnum.PUBBLICATO.toString(),
        PiaoStatusEnum.APPROVATO.toString(),
        PiaoStatusEnum.RICHIESTA_APPROVAZIONE.toString(),
      ].includes(this.piaoDTO.statoPiao || '') ||
      [
        SectionStatusEnum.IN_VALIDAZIONE.toString(),
        SectionStatusEnum.VALIDATA.toString(),
        SectionStatusEnum.PUBBLICATO.toString(),
        SectionStatusEnum.APPROVATO.toString(),
        SectionStatusEnum.RICHIESTA_APPROVAZIONE.toString(),
      ].includes(this.statoSezioneMap[this.activeSectionId] || '') ||
      !this.hasFunzionalita('PIAO_CTA_DETAILS_EDITING') ||
      !this.isSezioneNonAssociata(this.activeSectionId)
    );
  }
  private resolveIsDettaglioApprovazione(): boolean {
    return (
      //this.getDettaglioOverrideFromQuery(this.route.snapshot.queryParamMap.get('debugDettaglio')) ??
      [PiaoStatusEnum.APPROVATO.toString(), PiaoStatusEnum.PUBBLICATO.toString()].includes(
        this.piaoDTO.statoPiao || ''
      ) ||
      [SectionStatusEnum.PUBBLICATO.toString(), SectionStatusEnum.APPROVATO.toString()].includes(
        this.statoSezioneMap[this.activeSectionId] || ''
      ) ||
      !this.hasFunzionalita('PIAO_CTA_DETAILS_EDITING') ||
      !this.isSezioneNonAssociata(this.activeSectionId)
    );
  }

  private buildStatoSezioneMap(): void {
    this.statoSezioneMap = {};
    for (const step of this.steps) {
      if (step.children && step.children.length > 0) {
        for (const child of step.children) {
          this.statoSezioneMap[child.numeroSezione] = child.statoSezione || '';
        }
      } else {
        this.statoSezioneMap[step.numeroSezione] = step.statoSezione || '';
      }
    }
  }

  private buildTestoSezioneMap(): void {
    const findTesto = (parentNumero: string, childNumero?: string): string => {
      if (!childNumero) {
        return this.steps.find((s) => s.numeroSezione === parentNumero)?.testo || '';
      }
      return (
        this.steps.find(
          (s) =>
            s.numeroSezione === parentNumero &&
            s.children?.find((c) => c.numeroSezione === childNumero)
        )?.testo || ''
      );
    };

    this.testoSezioneMap = {
      '1': '1 ' + findTesto('1'),
      '2.1': '2.1 ' + findTesto('2', '2.1'),
      '2.2': '2.2 ' + findTesto('2', '2.2'),
      '2.3': '2.3 ' + findTesto('2', '2.3'),
      '3.1': '3.1 ' + findTesto('3', '3.1'),
      '3.2': '3.2 ' + findTesto('3', '3.2'),
      '3.3.1': '3.3.1 ' + findTesto('3', '3.3.1'),
      '3.3.2': '3.3.2 ' + findTesto('3', '3.3.2'),
      '4': '4 ' + findTesto('4'),
      '5': '5 ' + findTesto('5'),
    };
  }

  handleOpenPage(id: string) {
    this.router
      .navigate([], {
        relativeTo: this.route,
        queryParams: { idSezione: id },
        queryParamsHandling: 'merge',
      })
      .then(() => {
        setTimeout(() => {
          window.scrollTo(0, 0);
          // Fallback per alcuni browser
          document.documentElement.scrollTop = 0;
          document.body.scrollTop = 0;
        }, 0);
      });
    this.updateIsDettaglio();
  }

  handleValidationSection() {
    this.handleSaveToDraftSection(false, '', false);
    this.openModalValidation = true;
  }

  handleValidation() {
    const activeSezione = this.getActiveSezione();

    if (activeSezione) {
      // Usa il metodo validate della sezione
      activeSezione
        .validate(this.testoSezioneMap[this.activeSectionId], 'sezioneInValidazione')
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (value: any) => {
            this.openModalValidation = false;
            this.postSaveSection(SectionStatusEnum.IN_VALIDAZIONE);
            window.scrollTo({ top: 0, left: 0, behavior: 'instant' });
          },
        });
    }
  }

  postSaveSection(
    statoSezione: string,
    forceRefresh: boolean = false,
    toastService: boolean = true
  ) {
    // Funzione helper per completare il post-save
    const finalizeSave = () => {
      this.sessionStorageService.setItem(KEY_PIAO, this.piaoDTO);
      this.loadStep();

      const activeSezione = this.getActiveSezione();
      if (activeSezione) {
        // Per sezioni refactorizzate, aggiorna lo stato nel form interno
        const form = activeSezione.getForm();
        if (form.controls['statoSezione']) {
          form.controls['statoSezione'].setValue(statoSezione);
        }
      } else {
        // Fallback per sezioni non refactorizzate
        this.activeForm.controls['statoSezione'].setValue(statoSezione);
      }

      if (toastService) {
        this.toastService.success('Richiesta inviata con successo!', { duration: 7000 });
      }
    };
    // Per i save normali, usa i dati già aggiornati dal buildSaveObservable
    finalizeSave();
  }

  handleSaveToDraftSection(
    stepper: boolean = false,
    statoSezione: string = '',
    toastService: boolean = true
  ): void {
    const activeSezione = this.getActiveSezione();

    if (activeSezione) {
      // Usa i metodi dell'interfaccia per sezioni refactorizzate
      statoSezione = activeSezione.getSectionStatus();
    } else {
      // Logica esistente per sezioni non refactorizzate
      const formIsNull = areAllValuesNull(this.activeForm);

      if (formIsNull) {
        statoSezione = SectionStatusEnum.DA_COMPILARE;
      } else {
        let requiredIsNotNull: boolean[] = [];
        const fieldNull = collectNullPaths(this.activeForm);
        for (let x of SECTION_FIELDS_REQUIRED[this.activeSectionId]) {
          requiredIsNotNull.push(fieldNull.includes(x));
        }
        if (requiredIsNotNull.includes(true)) {
          statoSezione = SectionStatusEnum.IN_COMPILAZIONE;
        } else {
          statoSezione = SectionStatusEnum.COMPILATA;
        }
      }
    }

    const save$ = this.buildSaveObservable(statoSezione);

    save$.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.postSaveSection(statoSezione, false, toastService);
        if (stepper) {
          this.openModalNextStep = false;
          this.handleNextPreviousStep(this.isNextSection, false);
        }
      },
    });
  }

  buildSaveObservable(statoSezione: string): Observable<any> {
    switch (this.activeSectionId) {
      case '1': {
        // Usa la nuova architettura per la sezione 1
        if (this.sezione1Component) {
          const sezione1ForSave = this.sezione1Component.prepareDataForSave();
          sezione1ForSave.statoSezione = statoSezione;
          sezione1ForSave.campiModificati = getChangedFields(
            this.activeForm?.value,
            this.sectionFormValue,
            ['id', 'idSezione1']
          );
          sezione1ForSave.testoSezione = this.testoSezioneMap['1'] || '';
          return this.sezione1Service.save(sezione1ForSave).pipe(
            switchMap((value: any) => {
              // Ricarica i dati freschi dal BE
              return this.sezione1Service.getSezione1ByIdPiao(this.piaoDTO.id!);
            }),
            tap((freshData: any) => {
              this.sezione1Component!.sezione1Data = freshData;
              this.sezione1Component?.createForm();
              this.sezione1Component?.reloadMatrice();
              this.sezione1Component?.reloadAttachment();
            })
          );
        }
        // Fallback per compatibilità
        return of({});
      }

      case '2.1': {
        if (this.sezione21Component) {
          const sezione21ForSave = this.sezione21Component.prepareDataForSave();
          sezione21ForSave.statoSezione = statoSezione;
          sezione21ForSave.campiModificati = getChangedFields(
            this.activeForm?.value,
            this.sectionFormValue,
            ['id', 'idSezione21', 'key']
          );
          sezione21ForSave.testoSezione = this.testoSezioneMap['2.1'] || '';

          return this.sezione21Service.save(sezione21ForSave).pipe(
            switchMap((value: any) => {
              // Ricarica i dati freschi dal BE
              return this.sezione21Service.getSezione21ByIdPiao(this.piaoDTO.id!);
            }),
            tap((freshData: any) => {
              this.sezione21Component!.sezione21Data = freshData;
              this.sezione21Component?.createForm();
              this.sezione21Component?.reloadAttachment();
            })
          );
        }
        // Fallback per compatibilità
        return of({});
      }
      case '2.2': {
        if (this.sezione22Component) {
          const sezione22ForSave = this.sezione22Component.prepareDataForSave();
          sezione22ForSave.statoSezione = statoSezione;
          sezione22ForSave.campiModificati = getChangedFields(
            this.activeForm?.value,
            this.sectionFormValue,
            ['id', 'idSezione22', 'key']
          );
          sezione22ForSave.testoSezione = this.testoSezioneMap['2.2'] || '';
          return this.sezione22Service.save(sezione22ForSave).pipe(
            switchMap((value: any) => {
              return this.sezione22Service.getSezione22ByIdPiao(this.piaoDTO.id!);
            }),
            tap((freshData: any) => {
              this.sezione22Component!.sezione22Data = freshData;
              this.sezione22Component?.createForm();
              this.sezione22Component?.reloadAttachment();
            })
          );
        }
        // Fallback per compatibilità
        return of({});
      }
      case '2.3': {
        if (this.sezione23Component) {
          const sezione23ForSave = this.sezione23Component.prepareDataForSave();
          sezione23ForSave.statoSezione = statoSezione;
          sezione23ForSave.campiModificati = getChangedFields(
            this.activeForm?.value,
            this.sectionFormValue
          );
          sezione23ForSave.testoSezione = this.testoSezioneMap['2.3'] || '';
          return this.sezione23Service.save(sezione23ForSave).pipe(
            switchMap((value: any) => {
              return this.sezione23Service.getSezione23ByIdPiao(this.piaoDTO.id!);
            }),
            tap((freshData: any) => {
              this.sezione23Component!.sezione23Data = freshData;
              this.sezione23Component?.createForm();
              this.sezione23Component?.reloadAttachment();
            })
          );
        }
        // Fallback per compatibilità
        return of({});
      }
      case '3.1': {
        if (this.sezione31Component) {
          const sezione31ForSave = this.sezione31Component.prepareDataForSave();
          sezione31ForSave.statoSezione = statoSezione;
          sezione31ForSave.campiModificati = getChangedFields(
            this.activeForm?.value,
            this.sectionFormValue,
            ['grafico', 'tabelleFunzionali', 'ampiezzaOrganizzative']
          );
          sezione31ForSave.testoSezione = this.testoSezioneMap['3.1'] || '';
          return this.sezione31Service.save(sezione31ForSave).pipe(
            switchMap((value: any) => {
              return this.sezione31Service.getSezione31ByIdPiao(this.piaoDTO.id!);
            }),
            tap((freshData: any) => {
              this.sezione31Component!.sezione31Data = freshData;
              this.sezione31Component?.createForm();
            })
          );
        }
        return of({});
      }
      case '3.2': {
        if (this.sezione32Component) {
          const sezione32ForSave = this.sezione32Component.prepareDataForSave();
          sezione32ForSave.statoSezione = statoSezione;
          sezione32ForSave.campiModificati = getChangedFields(
            this.activeForm?.value,
            this.sectionFormValue,
            ['tabelleFunzionali']
          );
          sezione32ForSave.testoSezione = this.testoSezioneMap['3.2'] || '';
          return this.sezione32Service.save(sezione32ForSave).pipe(
            switchMap(() => {
              return this.sezione32Service.getSezione32ByIdPiao(this.piaoDTO.id!);
            }),
            tap((freshData: Sezione32DTO | undefined) => {
              if (freshData) {
                this.sezione32Component!.sezione32Data = freshData;
                this.sezione32Component?.createForm();
              }
            })
          );
        }
        return of({});
      }
      case '3.3.1': {
        if (this.sezione331Component) {
          const sezione331ForSave = this.sezione331Component.prepareDataForSave();
          sezione331ForSave.statoSezione = statoSezione;
          sezione331ForSave.campiModificati = getChangedFields(
            this.activeForm?.value,
            this.sectionFormValue,
            [
              'valoreDotazioneOrganicaTable',
              'consistenzaDirigenzialeTable',
              'consistenzaNonDirigenzialeTable',
              'personaleDirigenzialeAssuzioniTable',
              'areeContrattualiAssuzioniTable',
              'riepilogoCessazioniTable',
              'rimodulazioneTableTable',
              'coperturaFabbisognoDirigTable',
              'coperturaFabbisognoContrTable',
              'prospettoPrevisionaleBaseAnno1Table',
              'prospettoPrevisionaleCessAnno1DiriTable',
              'prospettoPrevisionaleCessAnno1ContrTable',
              'prospettoPrevisionaleBaseAnno2Table',
              'prospettoPrevisionaleCessAnno2DiriTable',
              'prospettoPrevisionaleCessAnno2ContrTable',
              'tabelleFunzionali',
            ]
          );
          sezione331ForSave.testoSezione = this.testoSezioneMap['3.3.1'] || '';
          return this.sezione331Service.save(sezione331ForSave).pipe(
            switchMap((value: any) => {
              return this.sezione331Service.getSezione331ByIdPiao(this.piaoDTO.id!);
            }),
            tap((freshData: any) => {
              this.sezione331Component!.sezione331Data = freshData;
              this.sezione331Component?.createForm();
            })
          );
        }
        return of({});
      }
      case '3.3.2': {
        if (this.sezione332Component) {
          const sezione332ForSave = this.sezione332Component.prepareDataForSave();
          sezione332ForSave.statoSezione = statoSezione;
          sezione332ForSave.campiModificati = getChangedFields(
            this.activeForm?.value,
            this.sectionFormValue,
            [
              'id',
              'idSezione332',
              'obiettiviRisultatiFotografia',
              'obiettiviRisultati',
              'fotografiaFormazione',
              'attivitaFormative',
              'tabelleFunzionali',
            ]
          );
          sezione332ForSave.testoSezione = this.testoSezioneMap['3.3.2'] || '';
          return this.sezione332Service.save(sezione332ForSave).pipe(
            switchMap((value: any) => {
              return this.sezione332Service.getSezione332ByIdPiao(this.piaoDTO.id!);
            }),
            tap((freshData: any) => {
              this.sezione332Component!.sezione332Data = freshData;
              this.sezione332Component?.createForm();
            })
          );
        }
        return of({});
      }
      case '4': {
        if (this.sezione4Component) {
          const sezione4ForSave = this.sezione4Component.prepareDataForSave();
          sezione4ForSave.statoSezione = statoSezione;
          sezione4ForSave.campiModificati = getChangedFields(
            this.activeForm?.value,
            this.sectionFormValue,
            ['sottofaseMonitoraggio', 'monitoraggio21Obiettivi', 'monitoraggio22OrgObiettivi']
          );
          sezione4ForSave.testoSezione = this.testoSezioneMap['4'] || '';
          return this.sezione4Service.save(sezione4ForSave).pipe(
            switchMap((value: any) => {
              return this.sezione4Service.getSezione4ByIdPiao(this.piaoDTO.id!);
            }),
            tap((freshData: any) => {
              this.sezione4Component!.sezione4Data = freshData;
              this.sezione4Component?.createForm();
              this.sezione4Component?.reloadAttachment();
            })
          );
        }
        return of({});
      }

      default: {
        return of({});
      }
    }
  }

  handleNextPreviousStep(isNext: boolean, ctrlFormChangedOrIsSaveValidation: boolean = true): void {
    const formIsNull = areAllValuesNull(this.activeForm);

    if (!formIsNull && ctrlFormChangedOrIsSaveValidation) {
      const hasFormChanged = this.checkFormChanged();

      if (hasFormChanged) {
        this.isNextSection = isNext;
        this.openModalNextStep = true;
        return;
      }
    }

    let index =
      SECTION_ID.at(
        isNext
          ? SECTION_ID.indexOf(this.activeSectionId) + 1
          : SECTION_ID.indexOf(this.activeSectionId) - 1
      ) || '1';

    this.handleOpenPage(index);
    this.openModalNextStep = false;
  }

  /**
   * Ottiene la sezione attiva che implementa ISezioneBase
   */
  private getActiveSezione(): ISezioneBase | null {
    switch (this.activeSectionId) {
      case '1':
        return this.sezione1Component || null;
      // Aggiungi qui le altre sezioni quando saranno refactorizzate
      case '2.1':
        return this.sezione21Component || null;
      case '2.2':
        return this.sezione22Component || null;
      case '2.3':
        return this.sezione23Component || null;
      case '3.1':
        return this.sezione31Component || null;
      case '3.3.1':
        return this.sezione331Component || null;
      case '3.2':
        return this.sezione32Component || null;
      case '3.3.2':
        return this.sezione332Component || null;
      case '4':
        return this.sezione4Component || null;
      case '5':
        return this.approvazionePubblicazioneComponent || null;
      default:
        return null;
    }
  }

  /** Getter del form attivo */
  get activeForm(): FormGroup {
    // Per le sezioni refactorizzate, ottieni il form dal componente
    const activeSezione = this.getActiveSezione();
    if (activeSezione) {
      return activeSezione.getForm();
    }
    // Fallback per le sezioni non ancora refactorizzate
    return this.forms[this.activeSectionId];
  }

  getDisableButtonValidation(): boolean {
    if (!this.activeForm) {
      return true;
    }
    return (
      this.activeForm.controls['statoSezione']?.value !== SectionStatusEnum.COMPILATA.toString()
    );
  }

  getDisableButtonSave(): boolean {
    if (!this.activeForm) {
      return true;
    }

    return [
      SectionStatusEnum.IN_VALIDAZIONE.toString(),
      SectionStatusEnum.VALIDATA.toString(),
      SectionStatusEnum.PUBBLICATO.toString(),
      SectionStatusEnum.APPROVATO.toString(),
      SectionStatusEnum.RICHIESTA_APPROVAZIONE.toString(),
    ].includes(this.activeForm.controls['statoSezione']?.value);
  }

  hasPatternErrors(): boolean {
    if (!this.activeForm) {
      return false;
    }
    return this.checkPatternErrorsRecursively(this.activeForm, '');
  }

  /**
   * Controlla se il valore del form attivo è cambiato rispetto a quello salvato.
   * Esclude la proprietà 'codice' dal confronto e ordina gli array per id.
   */
  private checkFormChanged(): boolean {
    const currentFormValue = this.activeForm.value;

    const normalize = (obj: any): any => {
      if (obj == null) return obj;
      if (Array.isArray(obj)) {
        const sorted = obj.map((item: any) => normalize(item));
        sorted.sort((a: any, b: any) => {
          const idA = a?.id ?? '';
          const idB = b?.id ?? '';
          if (idA < idB) return -1;
          if (idA > idB) return 1;
          return JSON.stringify(a).localeCompare(JSON.stringify(b));
        });
        return sorted;
      }
      if (typeof obj === 'object') {
        const result: any = {};
        for (const key of Object.keys(obj)) {
          if (
            key === 'codice' ||
            key === 'statoSezione' ||
            key === 'valoreDotazioneOrganicaTable' ||
            key === 'consistenzaDirigenzialeTable' ||
            key === 'consistenzaNonDirigenzialeTable' ||
            key === 'personaleDirigenzialeAssuzioniTable' ||
            key === 'areeContrattualiAssuzioniTable' ||
            key === 'riepilogoCessazioniTable' ||
            key === 'coperturaFabbisognoDirigTable' ||
            key === 'coperturaFabbisognoContrTable' ||
            key === 'prospettoPrevisionaleBaseAnno1Table' ||
            key === 'prospettoPrevisionaleCessAnno1DiriTable' ||
            key === 'prospettoPrevisionaleCessAnno1ContrTable' ||
            key === 'prospettoPrevisionaleBaseAnno2Table' ||
            key === 'prospettoPrevisionaleCessAnno2DiriTable' ||
            key === 'prospettoPrevisionaleCessAnno2ContrTable' ||
            key === 'rimodulazioneTable'
          )
            continue;
          result[key] = normalize(obj[key]);
        }
        return result;
      }
      return obj;
    };

    const hasChanged =
      JSON.stringify(normalize(currentFormValue)) !==
      JSON.stringify(normalize(this.sectionFormValue));

    if (hasChanged) {
      const current = normalize(currentFormValue);
      const saved = normalize(this.sectionFormValue);
      this.logDeepDiff(current, saved, '');
    }

    return hasChanged;
  }

  /**
   * Restituisce i path dei campi modificati rispetto al valore salvato, separati da virgola.
   * Usa la stessa logica di normalizzazione di checkFormChanged.
   */

  private logDeepDiff(current: any, saved: any, path: string): void {
    if (current === saved) return;
    if (current == null || saved == null || typeof current !== typeof saved) {
      return;
    }
    if (Array.isArray(current) || Array.isArray(saved)) {
      const maxLen = Math.max(
        Array.isArray(current) ? current.length : 0,
        Array.isArray(saved) ? saved.length : 0
      );
      for (let i = 0; i < maxLen; i++) {
        this.logDeepDiff(current?.[i], saved?.[i], `${path}[${i}]`);
      }
      return;
    }
    if (typeof current === 'object') {
      const allKeys = new Set([...Object.keys(current || {}), ...Object.keys(saved || {})]);
      allKeys.forEach((key) => {
        this.logDeepDiff(current?.[key], saved?.[key], path ? `${path}.${key}` : key);
      });
      return;
    }
    if (current !== saved) {
    }
  }

  private checkPatternErrorsRecursively(control: any, path: string = ''): boolean {
    if (control instanceof FormGroup || control instanceof FormArray) {
      for (const key in control.controls) {
        const childControl = control.get(key);
        const childPath = path ? `${path}.${key}` : key;
        if (childControl?.errors?.['pattern']) {
          console.error(`[PATTERN ERROR] ${childPath}:`, childControl.errors['pattern']);
          return true;
        }
        if (this.checkPatternErrorsRecursively(childControl, childPath)) {
          return true;
        }
      }
    }
    return false;
  }

  getSectionStatus(form: FormGroup): string {
    //tutto il form null, DA_COMPILARE
    if (!this.hasFormValues(form)) {
      return SectionStatusEnum.DA_COMPILARE;
    }

    //se i required non sono stati compilati, IN_COMPILAZIONE
    if (hasRequiredErrors(form)) {
      return SectionStatusEnum.IN_COMPILAZIONE;
    }

    //form valido e con i campi compilati, COMPILATA
    return SectionStatusEnum.COMPILATA;
  }

  hasFormValues(form: FormGroup): boolean {
    return !areAllValuesNull(form);
  }

  // handlePubblica() {}

  handleConfermaPubblicazione() {
    if (this.child?.formGroup && this.approvazionePubblicazioneComponent) {
      const approvazioneData = {
        ...this.approvazionePubblicazioneComponent.prepareDataForSave(),
        ...this.child.formGroup.value,
      };
      this.approvazioneService
        .saveApprovazione(approvazioneData)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (value: any) => {
            this.loadStep();
            this.toastService.success('Approvazione salvata con successo');
          },
          error: (value: any) => {
            console.error('Errore in Approvazione.save');
          },
        });
    }
    this.openModalPubblicazione = false;
  }

  private getIdSezioneStorico(): number {
    switch (this.activeSectionId) {
      case '1':
        return this.piaoDTO.idSezione1!;
      case '2.1':
        return this.piaoDTO.idSezione21!;
      case '2.2':
        return this.piaoDTO.idSezione22!;
      case '2.3':
        return this.piaoDTO.idSezione23!;
      case '3.1':
        return this.piaoDTO.idSezione31!;
      case '3.2':
        return this.piaoDTO.idSezione32!;
      case '3.3.1':
        return this.piaoDTO.idSezione331!;
      case '3.3.2':
        return this.piaoDTO.idSezione332!;
      case '4':
        return this.piaoDTO.idSezione4!;
      default:
        throw new Error('Sezione non valida: ' + this.activeSectionId);
    }
  }

  handleOpenModalStoricoSezione() {
    this.selectedSectionId = this.getIdSezioneStorico();
    this.selectedSectionEnum = 'SEZIONE_' + this.activeSectionId.replace(/\./g, '');
    this.openModalStoricoSezione = true;
  }

  handleRecuperaInfoPiao(): void {
    const codPAFK = this.piaoDTO.codPAFK;
    if (!codPAFK) {
      this.toastService.error('Codice PA non disponibile');
      return;
    }

    const activeSezione = this.getActiveSezione();
    if (!activeSezione) {
      this.toastService.error('Recupero info non disponibile per questa sezione');
      return;
    }

    this.piaoService
      .getPiaoPrecedente(codPAFK)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (piaoPrecedente: PIAODTO | null) => {
          if (!piaoPrecedente || !piaoPrecedente.id) {
            this.toastService.warning('Nessun PIAO precedente trovato');
            return;
          }
          activeSezione
            .loadFromPreviousPiao(piaoPrecedente.id)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: (success) => {
                if (success) {
                  this.toastService.success('Dati recuperati dal PIAO precedente');
                } else {
                  let sectionText = this.testoSezioneMap[this.activeSectionId] || 'questa sezione';
                  let isPDF = piaoPrecedente.tipologia === PDF;
                  let messageForPDF = '';
                  if (isPDF) {
                    messageForPDF = ' poichè la tipologia era PDF';
                  }
                  this.toastService.warning(
                    `Nessun dato trovato per la '${sectionText}' dal PIAO precedente${messageForPDF}`
                  );
                }
              },
            });
        },
        error: (err) => {
          console.error('Errore nel recupero del PIAO precedente:', err);
          this.toastService.error('Errore nel recupero del PIAO precedente');
        },
      });
  }

  getActionsFor(): IVerticalEllipsisActions[] {
    // Ritorna lo stesso riferimento se la sezione attiva non è cambiata,
    // così Angular non distrugge/ricrea i <p> del menu ad ogni CD
    if (this.sectionActionsForId === this.activeSectionId && this.sectionActions.length > 0) {
      return this.sectionActions;
    }
    const actions: IVerticalEllipsisActions[] = [
      {
        label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ACTIONS.VIEW_CHANGE_HISTORY',
        callback: () => {
          this.handleOpenModalStoricoSezione();
        },
      },
    ];

    // L'azione "Recupera info PIAO" non è disponibile sulla sezione 5 (approvazione/pubblicazione)
    if (this.activeSectionId !== '5') {
      actions.push({
        label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ACTIONS.RECOVER_INFO_PIAO',
        callback: () => {
          //this.handleRecuperaInfoPiao();
          this.toastService.info('Funzionalità non disponibile al momento');
        },
      });
    }

    actions.push(
      {
        label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ACTIONS.DOWNLOAD_DRAFT_PDF',
        callback: () => {
          const pa = this.sessionStorageService.getItem(KEY_PA_ATTIVA);
          const codicePa = pa?.codePA;
          this.notificaService
            .generatePdf(this.piaoDTO.id || -1, SEZIONI_ACTIVE_ID[this.activeSectionId], codicePa)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: () => {
                // La generazione è stata avviata con successo, ora aspettiamo che il BE aggiorni lo stato tramite WebSocket
                this.toastService.success(
                  'Generazione del documento avviata. Una volta completata riceverai una notifica per poter scaricare il file.'
                );
              },
            });
        },
      },
      {
        label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ACTIONS.SECTION_GUIDE',
        callback: () => {},
      }
    );

    this.sectionActions = actions;
    this.sectionActionsForId = this.activeSectionId;
    return actions;
  }

  goToPiaoPubblicato(url: string) {
    if (url) {
      window.open(url, '_blank');
    } else {
      this.toastService.error('URL del PIAO pubblicato non disponibile');
    }
  }

  handleFormValueChanged(formValue: any): void {
    this.sectionFormValue = formValue;
  }

  override ngOnDestroy(): void {
    this.subject$.next();
    this.subject$.complete();
  }

  isSectionDisabled(sectionId: string): boolean {
    const section = this.steps.find((step) => step.numeroSezione === sectionId);
    return section?.disabled ?? false;
  }

  shouldShowNextButton(): boolean {
    // Non mostrare se siamo sulla sezione 5
    if (this.activeSectionId === '5') {
      return false;
    }
    // Non mostrare se siamo sulla sezione 4 e la 5 è disabilitata
    if (
      this.activeSectionId === '4' &&
      this.hasFunzionalita('PIAO_AGGIORNA_APPR_INPUT_DATA') &&
      this.isSectionDisabled('5')
    ) {
      return false;
    }
    return true;
  }
}
