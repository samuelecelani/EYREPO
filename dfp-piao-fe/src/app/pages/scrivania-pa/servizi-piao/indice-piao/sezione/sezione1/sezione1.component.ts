import {
  Component,
  DestroyRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild,
  inject,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SharedModule } from '../../../../../../shared/module/shared/shared.module';
import { TextAreaComponent } from '../../../../../../shared/components/text-area/text-area.component';
import { ReactiveFormsModule, FormGroup, FormArray, FormBuilder, Validators } from '@angular/forms';
import { DynamicTBTAConfig } from '../../../../../../shared/models/classes/config/dynamic-tbta-config';
import { DynamicTBTAComponent } from '../../../../../../shared/components/dynamic-tbta/dynamic-tbta.component';
import { DynamicTBTBConfig } from '../../../../../../shared/models/classes/config/dynamic-tbtb-config';
import { DynamicTBTBComponent } from '../../../../../../shared/components/dynamic-tbtb/dynamic-tbtb.component';
import { DynamicTBConfig } from '../../../../../../shared/models/classes/config/dynamic-tb';
import { DynamicTBComponent } from '../../../../../../shared/components/dynamic-tb/dynamic-tb.component';
import { BaseComponent } from '../../../../../../shared/components/base/base.component';
import {
  DATE_REGEX,
  INPUT_REGEX,
  KEY_PIAO,
  WARNING_ICON,
} from '../../../../../../shared/utils/constants';
import { CardAlertComponent } from '../../../../../../shared/ui/card-alert/card-alert.component';
import { SchedaAnagraficaComponent } from '../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-1/scheda-anagrafica/scheda-anagrafica.component';
import { AttachmentComponent } from '../../../../../../shared/ui/attachment/attachment.component';
import { PIAODTO } from '../../../../../../shared/models/classes/piao-dto';
import { CodTipologiaSezioneEnum } from '../../../../../../shared/models/enums/cod-tipologia-sezione.enum';
import { OrganoPoliticoService } from 'src/app/shared/services/organo-politico.service';
import { CodTipologiaAllegatoEnum } from '../../../../../../shared/models/enums/cod-tipologia-allegato.enum';
import { MatriceComponent } from '../../../../../../shared/components/matrice/matrice.component';
import { ISezioneBase } from '../../../../../../shared/models/interfaces/sezione-base.interface';
import { Observable, catchError, map, of, switchMap } from 'rxjs';
import { Sezione1Service } from '../../../../../../shared/services/sezioni-1.service';
import { AnagraficaDTO } from '../../../../../../shared/models/classes/anagrafica-dto';
import {
  createFormArrayFromPiaoSession,
  createFormMongoFromPiaoSession,
  filterNonNullFields,
  areAllValuesNull,
  hasRequiredErrors,
  cleanSingleMongoDTO,
} from '../../../../../../shared/utils/utils';
import { PrincipioGuidaDTO } from '../../../../../../shared/models/classes/principio-guida-dto';
import { IntegrationTeamDTO } from '../../../../../../shared/models/classes/integration-team-dto';
import { AreaOrganizzativaDTO } from '../../../../../../shared/models/classes/area-organizzativa-dto';
import { OrganoPoliticoDTO } from '../../../../../../shared/models/classes/organo-politico-dto';
import { PrioritaPoliticaDTO } from '../../../../../../shared/models/classes/priorita-politica-dto';
import { UlterioriInfoDTO } from '../../../../../../shared/models/classes/ulteriori-info-dto';
import { Sezione1DTO } from '../../../../../../shared/models/classes/sezione-1-dto';
import { SectionStatusEnum } from '../../../../../../shared/models/enums/section-status.enum';
import { StakeHolderDTO } from '../../../../../../shared/models/classes/stakeholder-dto';
import { SectionEnum } from '../../../../../../shared/models/enums/section.enum';
import { StakeholderService } from '../../../../../../shared/services/stakeholder.service';
import { IntegrationTeamService } from 'src/app/shared/services/integration-team.service';
import { SessionStorageService } from '../../../../../../shared/services/session-storage.service';
import { AreaOrganizzativaService } from '../../../../../../shared/services/area-organizzativa.service';
import { PrioritaPoliticaService } from '../../../../../../shared/services/priorita-politica.service';
import { PrincipiGuidaService } from '../../../../../../shared/services/principi-guida.service';
import { ModalService } from '../../../../../../shared/services/modal.service';
import { CodDeleteImpactEnum } from '../../../../../../shared/models/enums/cod-delete-impact.enum';
import { StoricoStatoSezioneService } from '../../../../../../shared/services/storico-stato-sezione.service';

@Component({
  selector: 'piao-sezione-1',
  imports: [
    SharedModule,
    TextAreaComponent,
    DynamicTBTAComponent,
    DynamicTBTBComponent,
    DynamicTBComponent,
    SchedaAnagraficaComponent,
    CardAlertComponent,
    AttachmentComponent,
    MatriceComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './sezione1.component.html',
  styleUrl: './sezione1.component.scss',
})
export class Sezione1Component extends BaseComponent implements OnInit, ISezioneBase {
  private fb = inject(FormBuilder);
  private stakeholderService = inject(StakeholderService);
  private integrationTeamService = inject(IntegrationTeamService);
  private organoPoliticoService = inject(OrganoPoliticoService);

  isFormReady = false;
  anagraficaData?: AnagraficaDTO;

  private sezione1Service = inject(Sezione1Service);
  private areeOrganizzativaService = inject(AreaOrganizzativaService);
  private prioritaPoliticaService = inject(PrioritaPoliticaService);
  private pricipiGuidaService = inject(PrincipiGuidaService);
  private modalService = inject(ModalService);
  private destroyRef = inject(DestroyRef);
  private storicoStatoSezioneService = inject(StoricoStatoSezioneService);

  @Input() piaoDTO!: PIAODTO;
  @Input() sezione1Data?: Sezione1DTO;
  @Input() testoSezione!: string;
  @Input() isDettaglio: boolean = true;

  @Output() formValueChanged = new EventEmitter<any>();

  @ViewChild(MatriceComponent) matriceComponent?: MatriceComponent;
  @ViewChild(AttachmentComponent) attachmentComponent?: AttachmentComponent;

  form!: FormGroup;

  // MODALE STAKEHOLDER
  selectedStakeholderId?: number;
  openDeleteModal: boolean = false;

  title: string = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TITLE';
  subTitlePremessa: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.SUB_TITLE_PREMESSA';
  subTitlePriorita: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.SUB_TITLE_PRIORITA';
  subTitleAreeOrganizzative: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.SUB_TITLE_AREE_ORGANIZZATIVE';
  subTitleStakeholder: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.SUB_TITLE_STAKEHOLDER';

  /*TEXT AREA*/
  titleTAQuadroNormativo: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_AREA.QUADRO_NORMATIVO.TITLE';
  labelTAQuadroNormativo: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_AREA.QUADRO_NORMATIVO.LABEL';

  titleTAStrutturaProgrammatica: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_AREA.STRUTTURA_PROGRAMMATICA.TITLE';
  labelTAStrutturaProgrammatica: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_AREA.STRUTTURA_PROGRAMMATICA.LABEL';

  titleTACronoprogramma: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_AREA.CRONOPROGRAMMA.TITLE';
  labelTACronoprogramma: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_AREA.CRONOPROGRAMMA.LABEL';

  titleTAMissione: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_AREA.MISSIONE.TITLE';
  labelTAMissione: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_AREA.MISSIONE.LABEL';

  /*Principio Guida*/
  titleDynamicTBTAPrincipioGuida: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTA.PRINCIPIO_GUIDA.TITLE';
  subTitleDynamicTBTAPrincipioGuida: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTA.PRINCIPIO_GUIDA.TEXT_BOX.LABEL';
  labelTBPrincipioGuida: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTA.PRINCIPIO_GUIDA.TEXT_BOX.LABEL';
  labelTAPrincipioGuida: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTA.PRINCIPIO_GUIDA.TEXT_AREA.LABEL';
  titlePrincipioGuida: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTA.PRINCIPIO_GUIDA.CARD_INFO.TITLE';
  titleLoadNewFormPrincipioGuida: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTA.PRINCIPIO_GUIDA.CARD_INFO.ADD';

  /*Integration Team*/
  titleDynamicTBTBIntegrationTeam: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTB.INTEGRATION_TEAM.TITLE';
  subTitleDynamicTBTBIntegrationTeam: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTB.INTEGRATION_TEAM.TEXT_BOX.LABEL';
  labelTBIntegrationTeam: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTB.INTEGRATION_TEAM.TEXT_BOX.LABEL';
  labelTB2IntegrationTeam: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTB.INTEGRATION_TEAM.TEXT_BOX2.LABEL';
  titleIntegrationTeam: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTB.INTEGRATION_TEAM.CARD_INFO.TITLE';
  titleLoadNewFormIntegrationTeam: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTB.INTEGRATION_TEAM.CARD_INFO.ADD';

  /*Organo Politico*/
  titleDynamicTBTBOrganoPolitico: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTB.ORGANO_POLITICO.TITLE';
  subTitleDynamicTBTBOrganoPolitico: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTB.ORGANO_POLITICO.TEXT_BOX.LABEL';
  labelTBOrganoPolitico: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTB.ORGANO_POLITICO.TEXT_BOX.LABEL';
  labelTB2OrganoPolitico: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTB.ORGANO_POLITICO.TEXT_BOX2.LABEL';
  titleOrganoPolitico: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTB.ORGANO_POLITICO.CARD_INFO.TITLE';
  titleLoadNewFormOrganoPolitico: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTB.ORGANO_POLITICO.CARD_INFO.ADD';

  /*Priorita politiche*/
  titleDynamicTBTAPrioritaPolitiche: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTA.PRIORITA_POLITICHE.TITLE';
  subTitleDynamicTBTAPrioritaPolitiche: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTA.PRIORITA_POLITICHE.TEXT_BOX.LABEL';
  labelTBPrioritaPolitiche: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTA.PRIORITA_POLITICHE.TEXT_BOX.LABEL';
  labelTAPrioritaPolitiche: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTA.PRIORITA_POLITICHE.TEXT_AREA.LABEL';
  titlePrioritaPolitiche: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTA.PRIORITA_POLITICHE.CARD_INFO.TITLE';
  titleLoadNewFormPrioritaPolitiche: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTA.PRIORITA_POLITICHE.CARD_INFO.ADD';

  /*Ulteriore Info*/
  titleDynamicTBUlterioreInfo: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TB.ULTERIORE_INFO.TITLE';
  subTitleDynamicTBUlterioreInfo: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TB.ULTERIORE_INFO.SUB_TITLE';
  subTitleDynamicTBUlterioreInfoDettaglio: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TB.ULTERIORE_INFO.SUB_TITLE_DETTAGLIO';
  labelTBUlterioreInfo: string = 'Nome campo';
  titleUlterioreInfo: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TB.ULTERIORE_INFO.CARD_INFO.TITLE';
  titleLoadNewFormUlterioreInfo: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TB.ULTERIORE_INFO.CARD_INFO.ADD';
  titleUlterioreInfoAlert: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TB.ULTERIORE_INFO.CARD_ALERT.TITLE';
  subTitleUlterioreInfoAlert: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TB.ULTERIORE_INFO.CARD_ALERT.SUB_TITLE';
  iconAlert: string = WARNING_ICON;

  /*Matrice*/
  titleMatrice: string = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.MATRICE.TITLE';
  subTitleMatrice: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.MATRICE.SUB_TITLE';
  subTitleAlertMatrice: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.MATRICE.ALERT.SUB_TITLE';

  /*Aree Organizzative*/
  subTitleDynamicTBTAOrganizzative: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTA.AREE_ORGANIZZATIVE.TITLE';
  labelTBOrganizzative: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTA.AREE_ORGANIZZATIVE.TEXT_BOX.LABEL';
  labelTAOrganizzative: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTA.AREE_ORGANIZZATIVE.TEXT_AREA.LABEL';
  titleOrganizzative: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTA.AREE_ORGANIZZATIVE.CARD_INFO.TITLE';
  titleLoadNewFormOrganizzative: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTA.AREE_ORGANIZZATIVE.CARD_INFO.ADD';

  /*Stakeholder*/
  subTitleDynamicTBTBStakeholder: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTB.STAKEHOLDER.TEXT_BOX.LABEL';
  labelTBStakeholder: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTB.STAKEHOLDER.TEXT_BOX.LABEL';
  labelTB2Stakeholder: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTB.STAKEHOLDER.TEXT_BOX2.LABEL';
  titleStakeholder: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTB.STAKEHOLDER.CARD_INFO.TITLE';
  titleLoadNewFormStakeholder: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DYNAMIC_TBTB.STAKEHOLDER.CARD_INFO.ADD';

  iconPencil: string = 'Pencil';
  iconStyle: string = 'icon-modal';

  principioGuida!: DynamicTBTAConfig;
  integrationTeams!: DynamicTBTBConfig;
  organiPolitici!: DynamicTBTBConfig;
  prioritaPolitica!: DynamicTBTAConfig;
  ulterioreInfo!: DynamicTBConfig;
  areeOrganizzative!: DynamicTBTAConfig;
  stakeholder!: DynamicTBTBConfig;

  codTipologia: string = SectionEnum.SEZIONE_1;
  codTipologiaAllegato: string = CodTipologiaAllegatoEnum.ALLEGATO_SEZIONE;
  codTipologiaImmagine: string = CodTipologiaAllegatoEnum.LOGO_ANAGRAFICA;

  sezioneSection: SectionEnum = SectionEnum.SEZIONE_1;

  nameIdSezione1: string = 'idSezione1';

  ngOnInit(): void {
    this.principioGuida = {
      labelTB: this.labelTBPrincipioGuida,
      typeTB: 'text',
      idTB: 'nomePrincipioGuida',
      controlTB: 'nomePrincipioGuida',
      classTB: 'input',
      maxValidatorLengthTB: 50,
      descTooltipTB: this.labelTBPrincipioGuida,
      labelTA: this.labelTAPrincipioGuida,
      idTA: 'descrizionePrincipioGuida',
      controlTA: 'descrizionePrincipioGuida',
      classTA: 'text-area',
      maxValidatorLengthTA: 20000,
      descTooltipTA: this.labelTAPrincipioGuida,
    };

    this.integrationTeams = {
      labelTB: this.labelTBIntegrationTeam,
      typeTB: 'text',
      idTB: 'membro',
      controlTB: 'membro',
      classTB: 'input',
      maxValidatorLengthTB: 50,
      descTooltipTB: this.labelTBIntegrationTeam,
      labelTB2: this.labelTB2IntegrationTeam,
      typeTB2: 'text',
      idTB2: 'ruolo',
      controlTB2: 'ruolo',
      classTB2: 'input',
      maxValidatorLengthTB2: 50,
      descTooltipTB2: this.labelTB2IntegrationTeam,
    };

    this.organiPolitici = {
      labelTB: this.labelTBOrganoPolitico,
      typeTB: 'text',
      idTB: 'organo',
      controlTB: 'organo',
      classTB: 'input',
      maxValidatorLengthTB: 50,
      descTooltipTB: this.labelTBOrganoPolitico,
      labelTB2: this.labelTB2OrganoPolitico,
      typeTB2: 'text',
      idTB2: 'ruolo',
      controlTB2: 'ruolo',
      classTB2: 'input',
      maxValidatorLengthTB2: 50,
      descTooltipTB2: this.labelTB2OrganoPolitico,
    };

    this.prioritaPolitica = {
      labelTB: this.labelTBPrioritaPolitiche,
      typeTB: 'text',
      idTB: 'nomePrioritaPolitica',
      controlTB: 'nomePrioritaPolitica',
      classTB: 'input',
      maxValidatorLengthTB: 50,
      descTooltipTB: this.labelTBPrioritaPolitiche,
      labelTA: this.labelTAPrioritaPolitiche,
      idTA: 'descrizionePrioritaPolitica',
      controlTA: 'descrizionePrioritaPolitica',
      classTA: 'text-area',
      maxValidatorLengthTA: 20000,
      descTooltipTA: this.labelTAPrioritaPolitiche,
    };

    this.ulterioreInfo = {
      labelTB: this.labelTBUlterioreInfo,
      typeTB: 'text',
      idTB: 'value',
      controlTB: 'value',
      controlTBMongo: 'key',
      classTB: 'input',
      maxValidatorLengthTB: 50,
      descTooltipTB: this.labelTBUlterioreInfo,
    };

    this.areeOrganizzative = {
      labelTB: this.labelTBOrganizzative,
      typeTB: 'text',
      idTB: 'nomeArea',
      controlTB: 'nomeArea',
      classTB: 'input',
      maxValidatorLengthTB: 50,
      descTooltipTB: this.labelTBOrganizzative,
      labelTA: this.labelTAOrganizzative,
      idTA: 'descrizioneArea',
      controlTA: 'descrizioneArea',
      classTA: 'text-area',
      maxValidatorLengthTA: 20000,
      descTooltipTA: this.labelTBOrganizzative,
    };

    this.stakeholder = {
      labelTB: this.labelTBStakeholder,
      typeTB: 'text',
      idTB: 'nomeStakeHolder',
      controlTB: 'nomeStakeHolder',
      classTB: 'input',
      maxValidatorLengthTB: 50,
      descTooltipTB: this.labelTBStakeholder,
      labelTB2: this.labelTB2Stakeholder,
      typeTB2: 'text',
      idTB2: 'relazionePA',
      controlTB2: 'relazionePA',
      classTB2: 'input',
      maxValidatorLengthTB2: 50,
      descTooltipTB2: this.labelTB2Stakeholder,
    };

    // Carica i dati dal BE se esiste un PIAO, poi crea il form
    this.loadSezione1Data();

    // Sottoscrivi alle azioni di conferma eliminazione dal modale globale
    this.modalService.onConfirmAction$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(({ metadato }) => {
        switch (metadato.value as string) {
          case CodDeleteImpactEnum.PRIORITA_POLITICA:
            this.confirmDeletePrioritaPolitiche(metadato.idFK!, true);
            break;
          case CodDeleteImpactEnum.STAKEHOLDER:
            this.confirmDelete(metadato.idFK!, true);
            break;
          case CodDeleteImpactEnum.AREA_ORGANIZZATIVA:
            this.confirmDeleteAreeOrganizzative(metadato.idFK!, true);
            break;
          default:
            console.log('Metadato non gestito: ', metadato.value);
            break;
        }
      });
  }

  private loadSezione1Data(): void {
    if (this.piaoDTO?.id) {
      this.getPaRiferimento$()
        .pipe(
          switchMap((paRiferimento) => {
            console.log('PA Riferimento:', paRiferimento);
            const fiscalCode = paRiferimento?.fiscalCode || undefined;
            return this.sezione1Service.getSezione1ByIdPiao(this.piaoDTO.id!, fiscalCode);
          })
        )
        .subscribe({
          next: (data) => {
            if (data) {
              this.sezione1Data = data;
              this.piaoDTO.idSezione1 = data.id;
              this.sessionStorageService.setItem(KEY_PIAO, this.piaoDTO);
              if (data.anagrafica) {
                this.anagraficaData = data.anagrafica;
              }
            }
            this.createForm();
            // Salva lo storico dello stato solo se non è già in uno degli stati validi
            if (!this.noReloadStato(this.sezione1Data?.statoSezione)) {
              this.saveStoricoStatoSezione(this.sezione1Data || new Sezione1DTO());
            }
          },
          error: (err) => {
            this.createForm();
          },
        });
    } else {
      this.createForm();
    }
  }

  /**
   * Carica i dati della sezione 1 dal PIAO precedente senza modificare il piaoDTO in sessione.
   */
  loadFromPreviousPiao(previousPiaoId: number): Observable<boolean> {
    return this.getPaRiferimento$().pipe(
      switchMap((paRiferimento) => {
        const fiscalCode = paRiferimento?.fiscalCode || undefined;
        return this.sezione1Service.getSezione1ByIdPiao(previousPiaoId, fiscalCode);
      }),
      map((data) => {
        let success = false;
        if (data) {
          this.sezione1Data = data;
          if (data.anagrafica) {
            this.anagraficaData = data.anagrafica;
          }
          success = true;
        }
        this.createForm();
        return success;
      }),
      catchError((err) => {
        console.error('Errore nel caricamento dati dal PIAO precedente:', err);
        this.createForm();
        return of(false);
      })
    );
  }

  createForm() {
    const sezione1 = this.sezione1Data || new Sezione1DTO();
    const anagrafica = this.anagraficaData || new AnagraficaDTO();

    this.form = this.fb.group({
      idPiao: this.fb.control<any | null>(this.piaoDTO.id || null),
      quadroNormativo: this.fb.control<string | null>(sezione1.quadroNormativo || null, [
        Validators.maxLength(20000),
        Validators.pattern(INPUT_REGEX),
      ]),
      missione: this.fb.control<string | null>(sezione1.missione || null, [
        Validators.maxLength(20000),
        Validators.pattern(INPUT_REGEX),
      ]),
      strutturaProgrammatica: this.fb.control<string | null>(
        sezione1.strutturaProgrammatica || null,
        [Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)]
      ),
      cronoprogramma: this.fb.control<string | null>(sezione1.cronoprogramma || null, [
        Validators.maxLength(20000),
        Validators.pattern(INPUT_REGEX),
      ]),
      statoSezione: this.fb.control<string | null>(sezione1.statoSezione || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      denominazioneEnte: this.fb.control<string | null>(anagrafica.denominazioneEnte || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
        Validators.required,
      ]),
      acronimoPA: this.fb.control<string | null>(anagrafica.acronimoPA || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      codiceFiscale: this.fb.control<string | null>(anagrafica.codiceFiscale || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      codiceIPA: this.fb.control<string | null>(anagrafica.codiceIPA || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      tipologiaPA: this.fb.control<string | null>(anagrafica.tipologiaPA || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      tipologiaIstat: this.fb.control<string | null>(anagrafica.tipologiaIstat || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      piva: this.fb.control<string | null>(anagrafica.piva || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      indirizzoSedeLegale: this.fb.control<string | null>(anagrafica.indirizzoSedeLegale || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      indirizzoURP: this.fb.control<string | null>(anagrafica.indirizzoURP || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      www: this.fb.control<string | null>(anagrafica.www || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      mail: this.fb.control<string | null>(anagrafica.mail || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      telefono: this.fb.control<string | null>(anagrafica.telefono || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      pec: this.fb.control<string | null>(anagrafica.pec || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      social: this.fb.control<string | null>(anagrafica.social || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      nomeRPCT: this.fb.control<string | null>(anagrafica.nomeRPCT || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      cognomeRPCT: this.fb.control<string | null>(anagrafica.cognomeRCTP || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      ruoloRPCT: this.fb.control<string | null>(anagrafica.ruoloRPCT || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      dataNominaRPCT: this.fb.control<any | null>(anagrafica.dataNominaRPCT || null, [
        Validators.pattern(DATE_REGEX),
      ]),
      nomeRTD: this.fb.control<string | null>(anagrafica.nomeRTD || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      strutturaRifRTD: this.fb.control<string | null>(anagrafica.strutturaRifRTD || null, [
        Validators.maxLength(255),
        Validators.pattern(INPUT_REGEX),
      ]),
      descrizioneAllegato: this.fb.control<string | null>(null, [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      principiGuida: createFormArrayFromPiaoSession<PrincipioGuidaDTO>(
        this.fb,
        sezione1.principiGuida || [],
        sezione1.id,
        ['id', 'idSezione1', 'nomePrincipioGuida', 'descrizionePrincipioGuida'],
        50,
        2000
      ),
      integrationTeams: createFormArrayFromPiaoSession<IntegrationTeamDTO>(
        this.fb,
        sezione1.integrationTeams || [],
        sezione1.id,
        ['id', 'idSezione1', 'membro', 'ruolo'],
        50,
        50
      ),
      organiPolitici: createFormArrayFromPiaoSession<OrganoPoliticoDTO>(
        this.fb,
        sezione1.organiPolitici || [],
        sezione1.id,
        ['id', 'idSezione1', 'organo', 'ruolo'],
        50,
        50
      ),
      prioritaPolitiche: createFormArrayFromPiaoSession<PrioritaPoliticaDTO>(
        this.fb,
        sezione1.prioritaPolitiche || [],
        sezione1.id,
        ['id', 'idSezione1', 'nomePrioritaPolitica', 'descrizionePrioritaPolitica'],
        50,
        2000
      ),
      ulterioriInfo: createFormMongoFromPiaoSession<UlterioriInfoDTO>(
        this.fb,
        sezione1.ulterioriInfo || new UlterioriInfoDTO(),
        ['id', 'externalId', 'properties'],
        INPUT_REGEX,
        50,
        false
      ),
      areeOrganizzative: createFormArrayFromPiaoSession<AreaOrganizzativaDTO>(
        this.fb,
        sezione1.areeOrganizzative || [],
        sezione1.id,
        ['id', 'idSezione1', 'nomeArea', 'descrizioneArea'],
        50,
        2000
      ),
      stakeHolders: createFormArrayFromPiaoSession<StakeHolderDTO>(
        this.fb,
        sezione1.stakeHolders || [],
        this.piaoDTO.id,
        ['id', 'idPiao', 'nomeStakeHolder', 'relazionePA'],
        50,
        2000
      ),
      allegati: this.fb.array<FormGroup>([
        this.fb.group({
          id: [
            sezione1.allegati?.[0]?.id || null,
            [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
          ],
          idEntitaFK: [
            sezione1.allegati?.[0]?.idEntitaFK || null,
            [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
          ],
          codDocumento: [
            sezione1.allegati?.[0]?.codDocumento || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          codTipologiaFK: [
            sezione1.allegati?.[0]?.codTipologiaFK || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          codTipologiaAllegato: [
            sezione1.allegati?.[0]?.codTipologiaAllegato || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          downloadUrl: [
            sezione1.allegati?.[0]?.downloadUrl || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          sizeAllegato: [
            sezione1.allegati?.[0]?.sizeAllegato || null,
            [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
          ],
          descrizione: [
            sezione1.allegati?.[0]?.descrizione || null,
            [Validators.maxLength(100), Validators.pattern(INPUT_REGEX)],
          ],
          createdByNameSurname: [
            sezione1.allegati?.[0]?.createdByNameSurname || null,
            [Validators.maxLength(255)],
          ],
          createdByRole: [
            sezione1.allegati?.[0]?.createdByRole || null,
            [Validators.maxLength(255)],
          ],
        }),
      ]),
    });

    this.piaoDTO.stakeHolders = sezione1.stakeHolders || [];
    this.sessionStorageService.setItem(KEY_PIAO, this.piaoDTO);

    this.isFormReady = true;

    // Emetti il valore del form dopo la creazione
    this.formValueChanged.emit(this.form.value);
  }
  // GETTER per il FormArray degli stakeholder
  get stakeHolders(): FormArray {
    return this.form.get('stakeHolders') as FormArray;
  }
  // GETTER per il FormArray delle priorità politiche
  get prioritaPolitiche(): FormArray {
    return this.form.get('prioritaPolitiche') as FormArray;
  }
  // GETTER per il FormArray delle aree organizzative
  get areeOrganizzativeGetter(): FormArray {
    return this.form.get('areeOrganizzative') as FormArray;
  }

  // GETTER per il FormArray delle principio guida
  get principiGuida(): FormArray {
    return this.form.get('principiGuida') as FormArray;
  }

  // Apertura modale
  openDelete(stakeholderId: number) {
    this.selectedStakeholderId = stakeholderId;
    this.openDeleteModal = true;
  }

  // Conferma cancellazione
  confirmDelete(stakeholderId: number, forceDelete: boolean = false) {
    if (!stakeholderId) return;

    this.stakeholderService
      .delete(stakeholderId, this.piaoDTO.id || -1, this.testoSezione, forceDelete)
      .subscribe({
        next: () => {
          // Rimuovi dal FormArray
          const index = this.stakeHolders.controls.findIndex(
            (ctrl) => ctrl.value.id === stakeholderId
          );
          if (index !== -1) {
            this.stakeHolders.removeAt(index);
          }

          // Aggiorna anche la sorgente dati locale
          if (this.sezione1Data?.stakeHolders) {
            this.sezione1Data.stakeHolders = this.sezione1Data.stakeHolders.filter(
              (sh) => sh.id !== stakeholderId
            );
          }

          this.formValueChanged.emit(this.form.value);
        },
        error: (err) => {
          console.error('Errore durante la cancellazione', err);
        },
      });
  }

  confirmDeletePrincipiGuida(principioId: number) {
    if (!principioId) return;
    this.pricipiGuidaService
      .delete(principioId, this.piaoDTO.id || -1, this.testoSezione)
      .subscribe({
        next: () => {
          // Rimuovi dal FormArray
          const index = this.principiGuida.controls.findIndex(
            (ctrl) => ctrl.value.id === principioId
          );
          if (index !== -1) {
            this.principiGuida.removeAt(index);
          }

          this.formValueChanged.emit(this.form.value);
        },
        error: (err) => {
          console.error('Errore durante la cancellazione', err);
        },
      });
  }

  confirmDeleteIntegrationTeam(integrationTeamId: number) {
    if (!integrationTeamId) return;

    this.integrationTeamService
      .delete(integrationTeamId, this.piaoDTO.id || -1, this.testoSezione)
      .subscribe({
        next: () => {
          // Rimuovi dal FormArray
          const integrationTeamsArray = this.form.get('integrationTeams') as FormArray;
          const index = integrationTeamsArray.controls.findIndex(
            (ctrl) => ctrl.value.id === integrationTeamId
          );
          if (index !== -1) {
            integrationTeamsArray.removeAt(index);
          }

          // Aggiorna anche la sorgente dati locale
          if (this.sezione1Data?.integrationTeams) {
            this.sezione1Data.integrationTeams = this.sezione1Data.integrationTeams.filter(
              (it) => it.id !== integrationTeamId
            );
          }

          this.formValueChanged.emit(this.form.value);
        },
        error: (err) => {
          console.error('Errore durante la cancellazione', err);
        },
      });
  }

  confirmDeleteOrganoPolitico(organoPoliticoId: number) {
    if (!organoPoliticoId) return;

    this.organoPoliticoService
      .delete(organoPoliticoId, this.piaoDTO.id || -1, this.testoSezione)
      .subscribe({
        next: () => {
          // Rimuovi dal FormArray
          const organiPoliticiArray = this.form.get('organiPolitici') as FormArray;
          const index = organiPoliticiArray.controls.findIndex(
            (ctrl) => ctrl.value.id === organoPoliticoId
          );
          if (index !== -1) {
            organiPoliticiArray.removeAt(index);
          }

          // Aggiorna anche la sorgente dati locale
          if (this.sezione1Data?.organiPolitici) {
            this.sezione1Data.organiPolitici = this.sezione1Data.organiPolitici.filter(
              (op) => op.id !== organoPoliticoId
            );
          }

          this.formValueChanged.emit(this.form.value);
        },
        error: (err) => {
          console.error('Errore durante la cancellazione', err);
        },
      });
  }

  confirmDeletePrioritaPolitiche(prioritaId: number, forceDelete: boolean = false) {
    if (!prioritaId) return;

    this.prioritaPoliticaService
      .delete(prioritaId, this.piaoDTO.id || -1, this.testoSezione, forceDelete)
      .subscribe({
        next: () => {
          // Rimuovi dal FormArray
          const index = this.prioritaPolitiche.controls.findIndex(
            (ctrl) => ctrl.value.id === prioritaId
          );
          if (index !== -1) {
            this.prioritaPolitiche.removeAt(index);
          }

          // Aggiorna anche la sorgente dati locale
          if (this.sezione1Data?.prioritaPolitiche) {
            this.sezione1Data.prioritaPolitiche = this.sezione1Data.prioritaPolitiche.filter(
              (sh) => sh.id !== prioritaId
            );
          }

          this.formValueChanged.emit(this.form.value);
        },
        error: (err) => {
          console.error('Errore durante la cancellazione', err);
        },
      });
  }

  confirmDeleteAreeOrganizzative(areaId: number, forceDelete: boolean = false) {
    if (!areaId) return;
    this.areeOrganizzativaService
      .delete(areaId, this.piaoDTO.id || -1, this.testoSezione, forceDelete)
      .subscribe({
        next: () => {
          // Rimuovi dal FormArray
          const index = this.areeOrganizzativeGetter.controls.findIndex(
            (ctrl) => ctrl.value.id === areaId
          );
          if (index !== -1) {
            this.areeOrganizzativeGetter.removeAt(index);
          }

          // Aggiorna anche la sorgente dati locale
          if (this.sezione1Data?.areeOrganizzative) {
            this.sezione1Data.areeOrganizzative = this.sezione1Data.areeOrganizzative.filter(
              (sh) => sh.id !== areaId
            );
          }

          this.formValueChanged.emit(this.form.value);
        },
        error: (err) => {
          console.error('Errore durante la cancellazione', err);
        },
      });
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

  prepareDataForSave(): Sezione1DTO {
    let sezione1 = this.sezione1Data || new Sezione1DTO();

    return {
      ...sezione1,
      idPiao: this.form.controls['idPiao'].value || null,
      missione: this.form.controls['missione'].value || null,
      cronoprogramma: this.form.controls['cronoprogramma'].value || null,
      quadroNormativo: this.form.controls['quadroNormativo'].value || null,
      strutturaProgrammatica: this.form.controls['strutturaProgrammatica'].value || null,
      areeOrganizzative: filterNonNullFields<AreaOrganizzativaDTO>(
        this.form.controls['areeOrganizzative'].value,
        ['nomeArea', 'descrizioneArea']
      ),
      prioritaPolitiche: filterNonNullFields<PrioritaPoliticaDTO>(
        this.form.controls['prioritaPolitiche'].value,
        ['nomePrioritaPolitica', 'descrizionePrioritaPolitica']
      ),
      organiPolitici: filterNonNullFields<OrganoPoliticoDTO>(
        this.form.controls['organiPolitici'].value,
        ['organo', 'ruolo']
      ),
      principiGuida: filterNonNullFields<PrincipioGuidaDTO>(
        this.form.controls['principiGuida'].value,
        ['nomePrincipioGuida', 'descrizionePrincipioGuida']
      ),
      integrationTeams: filterNonNullFields<IntegrationTeamDTO>(
        this.form.controls['integrationTeams'].value,
        ['membro', 'ruolo']
      ),
      stakeHolders: filterNonNullFields<StakeHolderDTO>(this.form.controls['stakeHolders'].value, [
        'nomeStakeHolder',
        'relazionePA',
      ]),
      ulterioriInfo: cleanSingleMongoDTO<UlterioriInfoDTO>(
        this.form.controls['ulterioriInfo'].value
      ),
      allegati: this.form.controls['allegati'].value || null,
      anagrafica: {
        id: this.anagraficaData?.id || undefined,
        idSezione1: this.sezione1Data?.id || undefined,
        denominazioneEnte: this.form.controls['denominazioneEnte'].value || undefined,
        acronimoPA: this.form.controls['acronimoPA'].value || undefined,
        codiceFiscale: this.form.controls['codiceFiscale'].value || undefined,
        codiceIPA: this.form.controls['codiceIPA'].value || undefined,
        tipologiaPA: this.form.controls['tipologiaPA'].value || undefined,
        tipologiaIstat: this.form.controls['tipologiaIstat'].value || undefined,
        piva: this.form.controls['piva'].value || undefined,
        indirizzoSedeLegale: this.form.controls['indirizzoSedeLegale'].value || undefined,
        indirizzoURP: this.form.controls['indirizzoURP'].value || undefined,
        www: this.form.controls['www'].value || undefined,
        mail: this.form.controls['mail'].value || undefined,
        telefono: this.form.controls['telefono'].value || undefined,
        pec: this.form.controls['pec'].value || undefined,
        nomeRPCT: this.form.controls['nomeRPCT'].value || undefined,
        cognomeRCTP: this.form.controls['cognomeRPCT'].value || undefined,
        ruoloRPCT: this.form.controls['ruoloRPCT'].value || undefined,
        dataNominaRPCT: this.form.controls['dataNominaRPCT'].value || undefined,
        nomeRTD: this.form.controls['nomeRTD'].value || undefined,
        strutturaRifRTD: this.form.controls['strutturaRifRTD'].value || undefined,
        social: this.form.controls['social'].value || undefined,
      } as AnagraficaDTO,
      statoSezione: this.getSectionStatus(),
    };
  }

  validate(testoSezione: string, campiModificati: string): Observable<any> {
    const sezione1Id = this.sezione1Data?.id || this.piaoDTO?.idSezione1 || -1;
    return this.sezione1Service.validation(sezione1Id, testoSezione, campiModificati);
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

    //se i required non sono stati compilati, IN_COMPILAZIONE
    if (hasRequiredErrors(this.form)) {
      return SectionStatusEnum.IN_COMPILAZIONE;
    }

    //form valido e con i campi compilati, COMPILATA
    return SectionStatusEnum.COMPILATA;
  }

  get propertiesUltInfo() {
    return this.form.get('ulterioriInfo.properties') as FormArray;
  }

  reloadMatrice(): void {
    this.matriceComponent?.loadData();
  }

  reloadAttachment(): void {
    this.attachmentComponent?.getAllAttachment();
  }

  handleAttachmentLoaded(): void {
    // Riemetti il valore del form quando gli allegati vengono caricati
    this.formValueChanged.emit(this.form.value);
  }

  private saveStoricoStatoSezione(sezione1: Sezione1DTO): void {
    const newStatus = this.getSectionStatus();
    if (newStatus !== sezione1.statoSezione) {
      this.storicoStatoSezioneService
        .save({
          idEntitaFK: sezione1.id || -1,
          codTipologiaFK: this.codTipologia,
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
}
