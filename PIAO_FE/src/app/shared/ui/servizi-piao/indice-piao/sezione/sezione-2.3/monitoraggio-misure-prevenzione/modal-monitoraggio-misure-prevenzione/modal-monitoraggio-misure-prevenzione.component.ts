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

  dropdownMisuraPrenvenzione: LabelValue[] = [
    { label: 'Formazione', value: 1 },
    { label: 'Procedure', value: 2 },
  ];

  ngOnInit(): void {
    this.createForm();
  }

  createForm() {
    this.formGroup = this.fb.group({
      id: [this.monitoraggioPrevenzioneEdit?.id || null],
      misuraPrevenzione: [
        this.monitoraggioPrevenzioneEdit?.misuraPrevenzione || null,
        Validators.required,
      ],
      misuraPrevenzioneLabel: [this.monitoraggioPrevenzioneEdit?.misuraPrevenzioneLabel || null],
      tipologiaMisuraPrevenzione: [
        this.monitoraggioPrevenzioneEdit?.tipologiaMisuraPrevenzione || 'Lorem ipsum',
        [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
      ],
      modalitaMonitoraggio: [
        this.monitoraggioPrevenzioneEdit?.modalitaMonitoraggio || null,
        [Validators.required, Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
      ],
      responsabileMonitoraggio: [
        this.monitoraggioPrevenzioneEdit?.responsabileMonitoraggio || null,
        [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
      ],
      tempisticheMonitoraggio: [
        this.monitoraggioPrevenzioneEdit?.tempisticheMonitoraggio || null,
        [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
      ],
    });
  }

  private initializeDropdowns(): void {
    // Inizializza le opzioni dei dropdown dalle liste passate
    this.misurePrevenzioneOptions = this.dropdownMisuraPrenvenzione.map((misure) => ({
      label: misure.label || '',
      value: misure.value?.toString() || '',
    }));
  }
}
