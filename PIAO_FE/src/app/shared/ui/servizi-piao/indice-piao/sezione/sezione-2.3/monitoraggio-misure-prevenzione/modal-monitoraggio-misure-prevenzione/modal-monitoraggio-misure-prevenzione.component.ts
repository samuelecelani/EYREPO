import { Component, inject, Input, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ModalBodyComponent } from '../../../../../../../components/modal/modal-body/modal-body.component';
import { SharedModule } from '../../../../../../../module/shared/shared.module';
import { TextBoxComponent } from '../../../../../../../components/text-box/text-box.component';
import { TextAreaComponent } from '../../../../../../../components/text-area/text-area.component';
import { DropdownComponent } from '../../../../../../../components/dropdown/dropdown.component';
import { PIAODTO } from '../../../../../../../models/classes/piao-dto';
import { SessionStorageService } from '../../../../../../../services/session-storage.service';
import { LabelValue } from '../../../../../../../models/interfaces/label-value';
import { MonitoraggioPrevenzioneDTO } from '../../../../../../../models/classes/monitoraggio-prevenzione-dto';
import { INPUT_REGEX } from '../../../../../../../utils/constants';

@Component({
  selector: 'piao-modal-elenco-monitoraggio-misure-prevenzione',
  imports: [
    SharedModule,
    TextBoxComponent,
    TextAreaComponent,
    DropdownComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './modal-monitoraggio-misure-prevenzione.component.html',
  styleUrl: './modal-monitoraggio-misure-prevenzione.component.scss',
})
export class ModalMonitoraggioMisurePrevenzioneComponent
  extends ModalBodyComponent
  implements OnInit
{
  @Input() piaoDTO!: PIAODTO;
  @Input() monitoraggioPrevenzioneEdit!: MonitoraggioPrevenzioneDTO;
  @Input() dropdownMisuraPrenvenzione: LabelValue[] = [];

  fb: FormBuilder = inject(FormBuilder);
  sessionStorageService = inject(SessionStorageService);
  main: string = 'main';

  misurePrevenzioneOptions: LabelValue[] = [];

  labelTitle: string = 'SEZIONE_23.MONITORAGGIO_MISURE.ELENCO_MODALITA_MONITORAGGIO.MODAL.TITLE';
  labelSubTitle: string =
    'SEZIONE_23.MONITORAGGIO_MISURE.ELENCO_MODALITA_MONITORAGGIO.MODAL.SUB_TITLE';
  labelMisuraPrevenzione: string =
    'SEZIONE_23.MONITORAGGIO_MISURE.ELENCO_MODALITA_MONITORAGGIO.MODAL.MISURA_PREVENZIONE_LABEL';
  labelTipologiaMisuraPrevenzione: string =
    'SEZIONE_23.MONITORAGGIO_MISURE.ELENCO_MODALITA_MONITORAGGIO.MODAL.TIPOLOGIA_MISURA_PREVENZIONE_LABEL';
  labelModalitaMonitoraggio: string =
    'SEZIONE_23.MONITORAGGIO_MISURE.ELENCO_MODALITA_MONITORAGGIO.MODAL.MODALITA_MONITORAGGIO_LABEL';
  labelResponsabileMonitoraggio: string =
    'SEZIONE_23.MONITORAGGIO_MISURE.ELENCO_MODALITA_MONITORAGGIO.MODAL.RESPONSABILE_MONITORAGGIO_LABEL';
  labelTempisticheMonitoraggio: string =
    'SEZIONE_23.MONITORAGGIO_MISURE.ELENCO_MODALITA_MONITORAGGIO.MODAL.TEMPISTICHE_MONITORAGGIO_LABEL';

  ngOnInit(): void {
    this.createForm();
  }

  createForm() {
    this.formGroup = this.fb.group({
      id: [this.monitoraggioPrevenzioneEdit?.id || null],
      idMisuraPrevenzioneEventoRischio: [
        this.monitoraggioPrevenzioneEdit?.idMisuraPrevenzioneEventoRischio || null,
        Validators.required,
      ],
      tipologia: [
        this.monitoraggioPrevenzioneEdit?.tipologia || null,
        [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
      ],
      descrizione: [
        this.monitoraggioPrevenzioneEdit?.descrizione || null,
        [Validators.required, Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
      ],
      responsabile: [
        this.monitoraggioPrevenzioneEdit?.responsabile || null,
        [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
      ],
      tempistiche: [
        this.monitoraggioPrevenzioneEdit?.tempistiche || null,
        [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
      ],
    });
  }

  handleChangeMisura(event: any) {
    const selectedOption = this.dropdownMisuraPrenvenzione.find((option) => option.value === event);
    if (selectedOption) {
      this.formGroup?.patchValue({
        idMisuraPrevenzioneEventoRischio: selectedOption.value,
        tipologia: selectedOption.additionalField,
      });
    }
  }
}
