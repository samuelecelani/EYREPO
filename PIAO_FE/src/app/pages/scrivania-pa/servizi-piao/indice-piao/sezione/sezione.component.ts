import { Component, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { WorkflowComponent } from '../../../../../shared/components/workflow/workflow.component';
import { ActivatedRoute, Router } from '@angular/router';
import { SharedModule } from '../../../../../shared/module/shared/shared.module';
import { Sezione331Component } from './sezione3/sezione3.3.1/sezione3.3.1.component';
import { CardAlertComponent } from '../../../../../shared/ui/card-alert/card-alert.component';
import {
  KEY_PIAO,
  SECTION_ID,
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
  takeUntil,
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
import { ToastService } from '../../../../../shared/services/toast.service';

import { ISezioneBase } from '../../../../../shared/models/interfaces/sezione-base.interface';
import { Sezione23Service } from '../../../../../shared/services/sezione23.service';
import { Sezione4Service } from '../../../../../shared/services/sezione4.service';
import { StakeholderService } from '../../../../../shared/services/stakeholder.service';
import { ApprovazionePubblicazioneComponent } from './approvazione-pubblicazione/approvazione-pubblicazione.component';
import { ModalPubblicazioneComponent } from '../../../../../shared/ui/servizi-piao/indice-piao/sezione/approvazione-pubblicazione/modal-pubblicazione/modal-pubblicazione.component';

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
  ],
  templateUrl: './sezione.component.html',
  styleUrl: './sezione.component.scss',
})
export class SezioneComponent extends BaseComponent implements OnInit, OnDestroy {
  router: Router = inject(Router);
  route: ActivatedRoute = inject(ActivatedRoute);
  sessionStorageService: SessionStorageService = inject(SessionStorageService);
  piaoService: PIAOService = inject(PIAOService);
  fb: FormBuilder = inject(FormBuilder);
  sezione1Service: Sezione1Service = inject(Sezione1Service);
  sezione21Service: Sezione21Service = inject(Sezione21Service);
  sezione22Service: Sezione22Service = inject(Sezione22Service);
  sezione23Service: Sezione23Service = inject(Sezione23Service);
  sezione4Service: Sezione4Service = inject(Sezione4Service);
  toastService: ToastService = inject(ToastService);
  stakeholderService: StakeholderService = inject(StakeholderService);

  // ViewChild per accedere alle sezioni
  @ViewChild('sezione1') sezione1Component?: Sezione1Component;
  @ViewChild('sezione21') sezione21Component?: Sezione21Component;
  @ViewChild('sezione22') sezione22Component?: Sezione22Component;
  @ViewChild('sezione23') sezione23Component?: Sezione23Component;
  @ViewChild('sezione4') sezione4Component?: Sezione4Component;
  @ViewChild('approvazionePubblicazione')
  approvazionePubblicazioneComponent?: ApprovazionePubblicazioneComponent;
  @ViewChild('child') modalValidationChild?: ModalValidationComponent;

  // Variabile che contiene il valore del form quando viene creato
  sectionFormValue: any;

  activeSectionId!: string;
  title: string = '';
  subTitle: string = '';

  icon: string = WARNING_ICON;
  titleCardHeader: string = 'SCRIVANIA_PA.ALERT.TITLE';
  subTitleCardHeader: string = 'SCRIVANIA_PA.ALERT.SUB_TITLE';
  secondSubTitleCardHeader: string = 'SCRIVANIA_PA.ALERT.SECOND_SUB_TITLE';
  textHrefCardHeader: string = 'SCRIVANIA_PA.ALERT.TEXT_HREF';
  href: string = '/pages/area-privata-PA/mancata-compilazione';
  buttonCaricaApprovazione: string =
    'APPROVAZIONE_E_PUBBLICAZIONE.CARICA_APPROVAZIONE.BUTTON_LABEL';

  steps: StrutturaIndicePiaoDTO[] = [];

  private readonly subject$ = new Subject<void>();

  readonly forms: Record<string, FormGroup> = {};

  openModalNextStep: boolean = false;

  iconModalValidation: string = SHAPE_ICON;

  iconStyle: string = 'icon-modal';

  piaoDTO!: PIAODTO;

  openModalValidation: boolean = false;

  isNextSection!: boolean;

  openModalPublicazione: boolean = false;

  step5: StrutturaIndicePiaoDTO = {
    id: 0,
    numeroSezione: '5',
    statoSezione: SectionStatusEnum.DA_COMPILARE,
    testo: 'Approvazione e pubblicazione',
    disabled: false,
    children: [],
  };

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);

    // Imposto i titoli
    this.title = this.piaoDTO?.aggiornamento
      ? 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.T_AGGIORNA'
      : 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.T_REDIGI_COMPLETA';

    this.subTitle = this.piaoDTO?.aggiornamento
      ? 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ST_AGGIORNA'
      : 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ST_REDIGI_COMPLETA';

    this.loadStep();
    this.loadStakeholder();
  }

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

  handleFormValueChanged(formValue: any): void {
    this.sectionFormValue = formValue;
    console.log('Form value aggiornato:', this.sectionFormValue);
  }

  private loadStep() {
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
          map((value: StrutturaIndicePiaoDTO[]) => [...value, this.step5]),
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
          this.steps = steps;
        },
        error: (err) => {
          console.error('Errore nel caricamento dell’indice PIAO:', err);
        },
      });
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
  }

  handleValidationSection() {
    if (this.modalValidationChild?.formGroup) {
      this.modalValidationChild.formGroup.reset();
    } else {
      console.warn(
        'ModalValidationComponent non ancora disponibile o formGroup non inizializzato.'
      );
    }
    this.handleSaveToDraftSection(false, '', false);
    this.openModalValidation = true;
  }

  handleValidation() {
    const activeSezione = this.getActiveSezione();

    if (activeSezione) {
      // Usa il metodo validate della sezione
      activeSezione.validate().subscribe({
        next: (value: any) => {
          this.openModalValidation = false;
          this.postSaveSection(SectionStatusEnum.IN_VALIDAZIONE);
        },
      });
    } else {
      // Fallback per sezioni non ancora refactorizzate
      this.sezione1Service.validation(this.piaoDTO?.idSezione1 || -1).subscribe({
        next: (value: any) => {
          this.openModalValidation = false;
          this.postSaveSection(SectionStatusEnum.IN_VALIDAZIONE);
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

    // Refresh completo solo quando necessario (validazione, operazioni critiche)
    if (forceRefresh) {
      this.piaoService.getOrCreatePiao(this.piaoDTO).subscribe({
        next: (updatedPiao: PIAODTO) => {
          this.piaoDTO = updatedPiao;
          finalizeSave();
        },
        error: (err) => {
          console.error("Errore nell'aggiornamento del PIAO:", err);
          this.toastService.error("Errore durante l'aggiornamento del PIAO");
        },
      });
    } else {
      // Per i save normali, usa i dati già aggiornati dal buildSaveObservable
      finalizeSave();
    }
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
        console.log(fieldNull);
        for (let x of SECTION_FIELDS_REQUIRED[this.activeSectionId]) {
          requiredIsNotNull.push(fieldNull.includes(x));
        }
        console.log(requiredIsNotNull);
        if (requiredIsNotNull.includes(true)) {
          statoSezione = SectionStatusEnum.IN_COMPILAZIONE;
        } else {
          statoSezione = SectionStatusEnum.COMPILATA;
        }
      }
    }

    const save$ = this.buildSaveObservable(statoSezione);

    save$.subscribe({
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
        return of({});
      }
      case '3.2': {
        return of({});
      }
      case '3.3.1': {
        return of({});
      }
      case '3.3.2': {
        return of({});
      }
      case '4': {
        if (this.sezione4Component) {
          const sezione4ForSave = this.sezione4Component.prepareDataForSave();
          sezione4ForSave.statoSezione = statoSezione;

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
      const currentFormValue = this.activeForm.value;

      // Controlla se il valore del form è cambiato rispetto a quello salvato
      // Esclude la proprietà 'codice' dal confronto e ordina gli array per id
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
            if (key === 'codice') continue;
            result[key] = normalize(obj[key]);
          }
          return result;
        }
        return obj;
      };

      const hasFormChanged =
        JSON.stringify(normalize(currentFormValue)) !==
        JSON.stringify(normalize(this.sectionFormValue));

      console.log('Il form è cambiato?', hasFormChanged);

      if (hasFormChanged) {
        // Stampa le differenze tra i due form (deep)
        const current = normalize(currentFormValue);
        const saved = normalize(this.sectionFormValue);
        this.logDeepDiff(current, saved, '');
      }

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

    console.log(index);

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
    return this.getSectionStatus(this.activeForm) !== SectionStatusEnum.COMPILATA;
  }

  hasPatternErrors(): boolean {
    if (!this.activeForm) {
      return false;
    }
    return this.checkPatternErrorsRecursively(this.activeForm, '');
  }

  private logDeepDiff(current: any, saved: any, path: string): void {
    if (current === saved) return;
    if (current == null || saved == null || typeof current !== typeof saved) {
      console.log(`[DIFF] ${path || '(root)'}:`, { current, saved });
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
      console.log(`[DIFF] ${path}:`, { current, saved });
    }
  }

  private checkPatternErrorsRecursively(control: any, path: string = ''): boolean {
    if (control instanceof FormGroup || control instanceof FormArray) {
      for (const key in control.controls) {
        const childControl = control.get(key);
        const childPath = path ? `${path}.${key}` : key;
        if (childControl?.errors?.['pattern']) {
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

  handlePubblica() {
    if (this.modalValidationChild?.formGroup) {
      this.modalValidationChild.formGroup.reset();
    } else {
      console.warn(
        'ModalValidationComponent non ancora disponibile o formGroup non inizializzato.'
      );
    }
    this.handleSaveToDraftSection(false, '', false);
    this.openModalPublicazione = true;
  }

  override ngOnDestroy(): void {
    this.subject$.next();
    this.subject$.complete();
  }
}
