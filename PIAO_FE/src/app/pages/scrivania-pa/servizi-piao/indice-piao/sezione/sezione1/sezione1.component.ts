import { Component, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../../../../../shared/module/shared/shared.module';
import { TextAreaComponent } from '../../../../../../shared/components/text-area/text-area.component';
import { ReactiveFormsModule, FormGroup, FormArray } from '@angular/forms';
import { DynamicTBTAConfig } from '../../../../../../shared/models/classes/config/dynamic-tbta-config';
import { DynamicTBTAComponent } from '../../../../../../shared/components/dynamic-tbta/dynamic-tbta.component';
import { DynamicTBTBConfig } from '../../../../../../shared/models/classes/config/dynamic-tbtb-config';
import { DynamicTBTBComponent } from '../../../../../../shared/components/dynamic-tbtb/dynamic-tbtb.component';
import { DynamicTBConfig } from '../../../../../../shared/models/classes/config/dynamic-tb';
import { DynamicTBComponent } from '../../../../../../shared/components/dynamic-tb/dynamic-tb.component';
import { BaseComponent } from '../../../../../../shared/components/base/base.component';
import { WARNING_ICON } from '../../../../../../shared/utils/constants';
import { CardAlertComponent } from '../../../../../../shared/ui/card-alert/card-alert.component';
import { SchedaAnagraficaComponent } from '../../../../../../shared/ui/servizi-piao/indice-piao/sezione/sezione-1/scheda-anagrafica/scheda-anagrafica.component';
import { AttachmentComponent } from '../../../../../../shared/ui/attachment/attachment.component';
import { PIAODTO } from '../../../../../../shared/models/classes/piao-dto';
import { CodTipologiaSezioneEnum } from '../../../../../../shared/models/enums/cod-tipologia-sezione.enum';
import { CodTipologiaAllegatoEnum } from '../../../../../../shared/models/enums/cod-tipologia-allegato.enum';
import { MatriceComponent } from '../../../../../../shared/components/matrice/matrice.component';

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
export class Sezione1Component extends BaseComponent implements OnInit {
  @Input() form!: FormGroup;
  @Input() piaoDTO!: PIAODTO;

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

    console.log(this.propertiesUltInfo.controls);
  }

  get propertiesUltInfo() {
    return this.form.get('ulterioriInfo.properties') as FormArray;
  }
}
