import { Component, Input } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';
import { FormGroup } from '@angular/forms';
import { DropdownComponent } from '../../../../../../components/dropdown/dropdown.component';
import { LabelValue } from '../../../../../../models/interfaces/label-value';
import { AttachmentComponent } from '../../../../../attachment/attachment.component';
import { PIAODTO } from '../../../../../../models/classes/piao-dto';
import { CodTipologiaSezioneEnum } from '../../../../../../models/enums/cod-tipologia-sezione.enum';
import { CodTipologiaAllegatoEnum } from '../../../../../../models/enums/cod-tipologia-allegato.enum';

@Component({
  selector: 'piao-scheda-anagrafica',
  imports: [SharedModule, TextBoxComponent, DropdownComponent, AttachmentComponent],
  templateUrl: './scheda-anagrafica.component.html',
  styleUrl: './scheda-anagrafica.component.scss',
})
export class SchedaAnagraficaComponent {
  @Input() form!: FormGroup;
  @Input() piaoDTO!: PIAODTO;
  @Input() codTipologia!: CodTipologiaSezioneEnum;
  @Input() codTipologiaAllegato!: CodTipologiaAllegatoEnum;
  dropdown: LabelValue[] = [
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DROPDOWN.ANAGRAFICA.COMUNI',
      value: 'comuni',
    },
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DROPDOWN.ANAGRAFICA.REGIONI',
      value: 'regioni',
    },
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DROPDOWN.ANAGRAFICA.PROVINCE',
      value: 'province',
    },
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DROPDOWN.ANAGRAFICA.CITTA',
      value: 'citta',
    },
    {
      label:
        'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DROPDOWN.ANAGRAFICA.UNIVERSITA',
      value: 'universita',
    },
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DROPDOWN.ANAGRAFICA.ISTITUTI',
      value: 'istituti',
    },
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DROPDOWN.ANAGRAFICA.AGENZIE',
      value: 'agenzie',
    },
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DROPDOWN.ANAGRAFICA.MINISTRI',
      value: 'ministri',
    },
    {
      label:
        'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.DROPDOWN.ANAGRAFICA.ALTRE_ENTI',
      value: 'altreEnti',
    },
  ];

  title: string = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.ANAGRAFICA.TITLE';
  subTitleData: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.ANAGRAFICA.SUB_TITLE_DATA';
  subTitleLogic: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.ANAGRAFICA.SUB_TITLE_LOGIC';
  subTitleContact: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.ANAGRAFICA.SUB_TITLE_CONTACT';
  subTitleDataRPCT: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.ANAGRAFICA.SUB_TITLE_DATA_RPCT';
  subTitleDataRTD: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.ANAGRAFICA.SUB_TITLE_DATA_RTD';

  /*LABEL*/
  labelEnte: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.ENTE';
  labelPA: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.PA';
  labelCF: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.CF';
  labelIPA: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.IPA';
  labelTipPA: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.TIP_PA';
  labelIVA: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.IVA';
  labelIndSL: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.IND_S_L';
  labelIndUffRP: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.IND_UFF_R_P';
  labelWeb: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.WEB';
  labelEmail: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.EMAIL';
  labelTel: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.TEL';
  labelPEC: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.PEC';
  labelSocial: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.SOCIAL';
  labelName_RPCT: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.NAME_RPCT';
  labelSurname_RPCT: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.SURNAME_RPCT';
  labelRole_RPCT: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.ROLE_RPCT';
  labelDate_RPCT: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.DATE_RCPT';
  labelN_RTD: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.N_RTD';
  labelS_R_RTD: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_1.TEXT_BOX.ANAGRAFICA.S_R_RTD';

  changePA(event: any) {
    this.form.controls['tipologiaPA'].setValue(event);
  }
}
