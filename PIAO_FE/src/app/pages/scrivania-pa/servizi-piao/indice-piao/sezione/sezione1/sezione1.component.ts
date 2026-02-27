import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild,
  inject,
} from '@angular/core';
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
import { INPUT_REGEX, KEY_PIAO, WARNING_ICON } from '../../../../../../shared/utils/constants';
import { CardAlertComponent } from '../../../../../../shared/ui/card-alert/card-alert.component';
import { SchedaAnagraficaComponent } from '../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-1/scheda-anagrafica/scheda-anagrafica.component';
import { AttachmentComponent } from '../../../../../../shared/ui/attachment/attachment.component';
import { PIAODTO } from '../../../../../../shared/models/classes/piao-dto';
import { CodTipologiaSezioneEnum } from '../../../../../../shared/models/enums/cod-tipologia-sezione.enum';
import { CodTipologiaAllegatoEnum } from '../../../../../../shared/models/enums/cod-tipologia-allegato.enum';
import { MatriceComponent } from '../../../../../../shared/components/matrice/matrice.component';
import { ISezioneBase } from '../../../../../../shared/models/interfaces/sezione-base.interface';
import { Observable, of } from 'rxjs';
import { Sezione1Service } from '../../../../../../shared/services/sezioni-1.service';
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
import { SessionStorageService } from '../../../../../../shared/services/session-storage.service';

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

  isFormReady = false;

  private sezione1Service = inject(Sezione1Service);
  private sessionStorageService = inject(SessionStorageService);

  @Input() piaoDTO!: PIAODTO;
  @Input() sezione1Data?: Sezione1DTO;
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

  codTipologia: string = CodTipologiaSezioneEnum.SEZ1;
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
      maxValidatorLengthTA: 2000,
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
      maxValidatorLengthTA: 2000,
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
      maxValidatorLengthTA: 2000,
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
  }

  private loadSezione1Data(): void {
    if (this.piaoDTO?.id) {
      this.sezione1Service.getSezione1ByIdPiao(this.piaoDTO.id).subscribe({
        next: (data) => {
          if (data) {
            this.sezione1Data = data;
            this.piaoDTO.idSezione1 = data.id;
            this.sessionStorageService.setItem(KEY_PIAO, this.piaoDTO);
          }
          this.createForm();
        },
        error: (err) => {
          this.createForm();
        },
      });
    } else {
      this.createForm();
    }
  }

  createForm() {
    const sezione1 = this.sezione1Data || new Sezione1DTO();

    this.form = this.fb.group({
      idPiao: this.fb.control<any | null>(this.piaoDTO.id || null),
      quadroNormativo: this.fb.control<string | null>(sezione1.quadroNormativo || null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
        Validators.required,
      ]),
      missione: this.fb.control<string | null>(sezione1.missione || null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
      ]),
      strutturaProgrammatica: this.fb.control<string | null>(
        sezione1.strutturaProgrammatica || null,
        [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]
      ),
      cronoprogramma: this.fb.control<string | null>(sezione1.cronoprogramma || null, [
        Validators.maxLength(2000),
        Validators.pattern(INPUT_REGEX),
      ]),
      statoSezione: this.fb.control<string | null>(sezione1.statoSezione || null, [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      ente: this.fb.control<string | null>('Comune di Rimini', [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      acronimoPA: this.fb.control<string | null>('Lorem ipsum', [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      codiceFiscale: this.fb.control<string | null>('001921992999012', [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      codiceIPA: this.fb.control<string | null>('001921992999012', [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      tipologiaPA: this.fb.control<string | null>('comuni', [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      partitaIVA: this.fb.control<string | null>('001921992999012', [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      indSedeL: this.fb.control<string | null>('Via Lorem Ipsum, 24', [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      indUffRelPub: this.fb.control<string | null>('Via Lorem Ipsum, 24', [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      sitoWeb: this.fb.control<string | null>('wwww.loremipsum.com', [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      email: this.fb.control<string | null>('loremipsum@email.com', [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      telefono: this.fb.control<string | null>('06 12345678901', [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      pec: this.fb.control<string | null>('nome.cognome@pec.com', [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      socialMedia: this.fb.control<string | null>('www.facebook.com/ComunediRimini', [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      nomeRPCT: this.fb.control<string | null>('Mario', [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      cognomeRPCT: this.fb.control<string | null>('Rossi', [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      ruoloRPCT: this.fb.control<string | null>('Lorem ipsum', [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      dataRPCT: this.fb.control<string | null>('gg/mm/aaaa', [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      nominativoRTD: this.fb.control<string | null>('Luca Bianchi', [
        Validators.maxLength(20),
        Validators.pattern(INPUT_REGEX),
      ]),
      strutturaRTD: this.fb.control<string | null>('Lorem ipsum', [
        Validators.maxLength(20),
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
        sezione1.ulterioriInfoDTO || new UlterioriInfoDTO(),
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

  // Apertura modale
  openDelete(stakeholderId: number) {
    this.selectedStakeholderId = stakeholderId;
    this.openDeleteModal = true;
  }

  // Conferma cancellazione
  confirmDelete(stakeholderId: number) {
    if (!stakeholderId) return;

    this.stakeholderService.delete(stakeholderId).subscribe({
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
      ulterioriInfoDTO: cleanSingleMongoDTO<UlterioriInfoDTO>(
        this.form.controls['ulterioriInfo'].value
      ),
      allegati: this.form.controls['allegati'].value || null,
      statoSezione: this.getSectionStatus(),
    };
  }

  validate(): Observable<any> {
    const sezione1Id = this.sezione1Data?.id || this.piaoDTO?.idSezione1 || -1;
    return this.sezione1Service.validation(sezione1Id);
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
}
