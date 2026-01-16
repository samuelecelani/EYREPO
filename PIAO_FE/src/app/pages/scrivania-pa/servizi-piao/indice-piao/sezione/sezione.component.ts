import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { WorkflowComponent } from '../../../../../shared/components/workflow/workflow.component';
import { ActivatedRoute, Router } from '@angular/router';
import { SharedModule } from '../../../../../shared/module/shared/shared.module';
import { Sezione331Component } from './sezione3/sezione3.3.1/sezione3.3.1.component';
import { CardAlertComponent } from '../../../../../shared/ui/card-alert/card-alert.component';
import {
  INPUT_REGEX,
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
} from 'rxjs';
import { ButtonComponent } from '../../../../../shared/components/button/button.component';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SvgComponent } from '../../../../../shared/components/svg/svg.component';
import { ModalComponent } from '../../../../../shared/components/modal/modal.component';
import {
  areAllValuesNull,
  collectNullPaths,
  createFormArrayFromPiaoSession,
  createFormArrayFromPiaoSessionMongo,
  filterNonNullFields,
} from '../../../../../shared/utils/utils';
import { Sezione1Service } from '../../../../../shared/services/sezioni-1.service';
import { PIAODTO } from '../../../../../shared/models/classes/piao-dto';
import { Sezione1DTO } from '../../../../../shared/models/classes/sezione-1-dto';
import { SECTION_FIELDS_REQUIRED } from '../../../../../shared/utils/section-fields-required';
import { SectionStatusEnum } from '../../../../../shared/models/enums/section-status.enum';
import { ModalValidationComponent } from '../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-1/modal-validation/modal-validation.component';
import { BaseComponent } from '../../../../../shared/components/base/base.component';
import { ToastService } from '../../../../../shared/services/toast.service';
import { PrincipioGuidaDTO } from '../../../../../shared/models/classes/principio-guida-dto';
import { IntegrationTeamDTO } from '../../../../../shared/models/classes/integration-team-dto';
import { AreaOrganizzativaDTO } from '../../../../../shared/models/classes/area-organizzativa-dto';
import { OrganoPoliticoDTO } from '../../../../../shared/models/classes/organo-politico-dto';
import { PrioritaPoliticaDTO } from '../../../../../shared/models/classes/priorita-politica-dto';
import { SectionEnum } from '../../../../../shared/models/enums/section.enum';
import { PropertyDTO } from '../../../../../shared/models/classes/property-dto';

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
  toastService: ToastService = inject(ToastService);

  activeSectionId!: string;
  title: string = '';
  subTitle: string = '';

  icon: string = WARNING_ICON;
  titleCardHeader: string = 'SCRIVANIA_PA.ALERT.TITLE';
  subTitleCardHeader: string = 'SCRIVANIA_PA.ALERT.SUB_TITLE';
  secondSubTitleCardHeader: string = 'SCRIVANIA_PA.ALERT.SECOND_SUB_TITLE';
  textHrefCardHeader: string = 'SCRIVANIA_PA.ALERT.TEXT_HREF';
  href: string = '';

  steps: StrutturaIndicePiaoDTO[] = [];

  private subject$ = new Subject<void>();

  readonly forms: Record<string, FormGroup> = {};

  openModalNextStep: boolean = false;

  validationOrSaveDraft: boolean = false;

  iconModalValidation: string = SHAPE_ICON;

  iconStyle: string = 'icon-modal';

  piaoDTO!: PIAODTO;

  openModalValidation: boolean = false;

  isNextSection!: boolean;

  step5: StrutturaIndicePiaoDTO = {
    id: 0,
    numeroSezione: '5',
    statoSezione: SectionStatusEnum.DA_COMPILARE,
    testo: 'Approvazione e pubblicazione',
    disabled: true,
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

    this.createForms();
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
    const steps$ =
      this.piaoDTO && this.piaoDTO.id
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

  private createForms() {
    //creazione form sezione 1
    this.forms['1'] = this.fb.group({
      quadroNormativo: this.fb.control<string | null>(
        this.piaoDTO.sezione1?.quadroNormativo || null,
        [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]
      ),
      missione: this.fb.control<string | null>(this.piaoDTO.sezione1?.missione || null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
      ]),
      strutturaProgrammatica: this.fb.control<string | null>(
        this.piaoDTO.sezione1?.strutturaProgrammatica || null,
        [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]
      ),
      cronoprogramma: this.fb.control<string | null>(
        this.piaoDTO.sezione1?.cronoprogramma || null,
        [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]
      ),
      statoSezione: this.fb.control<string | null>(this.piaoDTO.sezione1?.statoSezione || null, [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      ente: this.fb.control<string | null>(null, [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      tipologiaPA: this.fb.control<string | null>(null, [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      descrizioneAllegato: this.fb.control<string | null>(null, [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      principiGuida: createFormArrayFromPiaoSession<PrincipioGuidaDTO>(
        this.fb,
        this.piaoDTO.sezione1?.principiGuida || [],
        this.piaoDTO.sezione1?.id,
        ['id', 'idSezione1', 'nomePrincipioGuida', 'descrizionePrincipioGuida'],
        50,
        2000
      ),
      integrationTeams: createFormArrayFromPiaoSession<IntegrationTeamDTO>(
        this.fb,
        this.piaoDTO.sezione1?.integrationTeams || [],
        this.piaoDTO.sezione1?.id,
        ['id', 'idSezione1', 'membro', 'ruolo'],
        50,
        50
      ),
      organiPolitici: createFormArrayFromPiaoSession<OrganoPoliticoDTO>(
        this.fb,
        this.piaoDTO.sezione1?.organiPolitici || [],
        this.piaoDTO.sezione1?.id,
        ['id', 'idSezione1', 'organo', 'ruolo'],
        50,
        50
      ),
      prioritaPolitiche: createFormArrayFromPiaoSession<PrioritaPoliticaDTO>(
        this.fb,
        this.piaoDTO.sezione1?.prioritaPolitiche || [],
        this.piaoDTO.sezione1?.id,
        ['id', 'idSezione1', 'nomePrioritaPolitica', 'descrizionePrioritaPolitica'],
        50,
        2000
      ),
      ulterioriInfo: this.fb.group({
        id: [null, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
        externalId: [
          this.piaoDTO.sezione1?.id || null,
          [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
        ],
        tipoSezione: [SectionEnum.SEZIONE_1],
        properties: createFormArrayFromPiaoSessionMongo<PropertyDTO>(
          this.fb,
          this.piaoDTO.sezione1?.ulterioriInfoDTO?.properties || [],
          ['key', 'value'],
          50,
          50,
          false
        ),
      }),
      areeOrganizzative: createFormArrayFromPiaoSession<AreaOrganizzativaDTO>(
        this.fb,
        this.piaoDTO.sezione1?.areeOrganizzative || [],
        this.piaoDTO.sezione1?.id,
        ['id', 'idSezione1', 'nomeArea', 'descrizioneArea'],
        50,
        2000
      ),
      stakeholder: this.fb.array<FormGroup>([
        this.fb.group({
          id: [null, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
          idPiao: [
            this.piaoDTO.id || null,
            [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
          ],
          nomeStakeHolder: [null, [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)]],
          relazionePA: [null, [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)]],
        }),
      ]),
      allegati: this.fb.array<FormGroup>([
        this.fb.group({
          id: [
            this.piaoDTO.sezione1?.allegati?.[0]?.id || null,
            [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
          ],
          idEntitaFK: [
            this.piaoDTO.sezione1?.allegati?.[0]?.idEntitaFK || null,
            [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
          ],
          codDocumento: [
            this.piaoDTO.sezione1?.allegati?.[0]?.codDocumento || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          codTipologiaFK: [
            this.piaoDTO.sezione1?.allegati?.[0]?.codTipologiaFK || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          codTipologiaAllegato: [
            this.piaoDTO.sezione1?.allegati?.[0]?.codTipologiaAllegato || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          downloadUrl: [
            this.piaoDTO.sezione1?.allegati?.[0]?.downloadUrl || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          sizeAllegato: [
            this.piaoDTO.sezione1?.allegati?.[0]?.sizeAllegato || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          descrizione: [
            this.piaoDTO.sezione1?.allegati?.[0]?.descrizione || null,
            [Validators.maxLength(100), Validators.pattern(INPUT_REGEX)],
          ],
        }),
      ]),
    });

    this.forms['2.1'] = this.fb.group({
      statoSezione: this.fb.control<string | null>(this.piaoDTO.sezione1?.statoSezione || null, [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
    });
  }

  handleOpenPage(id: string) {
    this.validationOrSaveDraft = false;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { idSezione: id },
      queryParamsHandling: 'merge',
    });
  }

  handleValidationSection() {
    this.child.formGroup.reset();
    this.openModalValidation = true;
  }

  handleValidation() {
    // recupero del form e switch del servizio, per adesso non è utilizzato
    this.sezione1Service.validation(this.piaoDTO.sezione1?.id || -1).subscribe({
      next: (value: any) => {
        this.openModalValidation = false;
        this.postSaveSection(SectionStatusEnum.IN_VALIDAZIONE);
      },
    });
  }

  postSaveSection(statoSezione: string) {
    this.sessionStorageService.setItem(KEY_PIAO, this.piaoDTO);
    this.loadStep();
    this.activeForm.controls['statoSezione'].setValue(statoSezione);
    this.toastService.success('Richiesta inviata con successo!', { duration: 7000 });
  }

  handleSaveToDraftSection(statoSezione: string = ''): void {
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

    const save$ = this.buildSaveObservable(statoSezione);

    save$.subscribe({
      next: () => {
        this.validationOrSaveDraft = true;
        this.postSaveSection(statoSezione);
      },
    });
  }

  buildSaveObservable(statoSezione: string): Observable<any> {
    switch (this.activeSectionId) {
      case '1': {
        let sezione1 = this.piaoDTO.sezione1 || new Sezione1DTO();

        let sezione1ForSave = {
          ...sezione1,
          missione: this.activeForm.controls['missione'].value || null,
          cronoprogramma: this.activeForm.controls['cronoprogramma'].value || null,
          quadroNormativo: this.activeForm.controls['quadroNormativo'].value || null,
          strutturaProgrammatica: this.activeForm.controls['strutturaProgrammatica'].value || null,
          areeOrganizzative: filterNonNullFields<AreaOrganizzativaDTO>(
            this.activeForm.controls['areeOrganizzative'].value,
            ['nomeArea', 'descrizioneArea']
          ),
          prioritaPolitiche: filterNonNullFields<PrioritaPoliticaDTO>(
            this.activeForm.controls['prioritaPolitiche'].value,
            ['nomePrioritaPolitica', 'descrizionePrioritaPolitica']
          ),
          organiPolitici: filterNonNullFields<OrganoPoliticoDTO>(
            this.activeForm.controls['organiPolitici'].value,
            ['organo', 'ruolo']
          ),
          principiGuida: filterNonNullFields<PrincipioGuidaDTO>(
            this.activeForm.controls['principiGuida'].value,
            ['nomePrincipioGuida', 'descrizionePrincipioGuida']
          ),
          integrationTeams: filterNonNullFields<IntegrationTeamDTO>(
            this.activeForm.controls['integrationTeams'].value,
            ['membro', 'ruolo']
          ),
          allegati: this.activeForm.controls['allegati'].value || null,
          statoSezione: statoSezione,
        };

        return this.sezione1Service.save(sezione1ForSave).pipe(
          tap((value: any) => {
            this.piaoDTO.sezione1 = value;
          })
        );
      }

      case '2.1': {
        return of({});
      }
      case '2.2': {
        return of({});
      }
      case '2.3': {
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
        return of({});
      }

      default: {
        return of({});
      }
    }
  }

  handleNextPreviousStep(isNext: boolean) {
    const formIsNull = areAllValuesNull(this.activeForm);

    if (!formIsNull) {
      if (!this.validationOrSaveDraft) {
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
  }

  /** Getter del form attivo */
  get activeForm(): FormGroup {
    return this.forms[this.activeSectionId];
  }

  getDisableButtonValidation(): boolean {
    return this.activeForm.controls['statoSezione']?.value !== SectionStatusEnum.COMPILATA;
  }

  override ngOnDestroy(): void {
    this.subject$.next();
    this.subject$.complete();
  }
}
