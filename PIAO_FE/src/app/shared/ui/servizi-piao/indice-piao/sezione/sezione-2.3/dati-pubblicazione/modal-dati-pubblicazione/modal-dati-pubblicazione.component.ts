import { Component, inject, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ModalBodyComponent } from '../../../../../../../components/modal/modal-body/modal-body.component';
import { SharedModule } from '../../../../../../../module/shared/shared.module';
import { TextBoxComponent } from '../../../../../../../components/text-box/text-box.component';
import { TextAreaComponent } from '../../../../../../../components/text-area/text-area.component';
import { DropdownComponent } from '../../../../../../../components/dropdown/dropdown.component';
import { DynamicTBFieldsComponent } from '../../../../../../../components/dynamic-tb-fields/dynamic-tb-fields.component';
import { PIAODTO } from '../../../../../../../models/classes/piao-dto';
import { SessionStorageService } from '../../../../../../../services/session-storage.service';
import { LabelValue } from '../../../../../../../models/interfaces/label-value';
import { DynamicTBConfig } from '../../../../../../../models/classes/config/dynamic-tb';
import { UlterioriInfoDTO } from '../../../../../../../models/classes/ulteriori-info-dto';
import { INPUT_REGEX } from '../../../../../../../utils/constants';
import { createFormMongoFromPiaoSession } from '../../../../../../../utils/utils';

export interface DatiPubblicazioneDTO {
  id?: number;
  datoPubblicare?: string;
  tipologiaDato?: string;
  responsabileTrasmissione?: string;
  terminiScadenza?: string;
  modalitaMonitoraggio?: string;
  motivazioneImpossibilita?: string;
  campiAggiuntivi?: any[];
}

@Component({
  selector: 'piao-modal-dati-pubblicazione',
  imports: [
    SharedModule,
    TextBoxComponent,
    TextAreaComponent,
    DropdownComponent,
    DynamicTBFieldsComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './modal-dati-pubblicazione.component.html',
  styleUrl: './modal-dati-pubblicazione.component.scss',
})
export class ModalDatiPubblicazioneComponent extends ModalBodyComponent implements OnInit {
  @Input() piaoDTO!: PIAODTO;
  @Input() datiPubblicazioneEdit!: DatiPubblicazioneDTO;
  @Output() close = new EventEmitter<void>();

  fb: FormBuilder = inject(FormBuilder);
  sessionStorageService = inject(SessionStorageService);
  main: string = 'main';

  labelTitle: string = 'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.TITLE';
  labelSubTitle: string = 'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.SUB_TITLE';
  labelDatoPubblicare: string = 'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.DATO_PUBBLICARE_LABEL';
  labelTipologiaDato: string = 'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.TIPOLOGIA_DATO_LABEL';
  labelResponsabileTrasmissione: string =
    'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.RESPONSABILE_TRASMISSIONE_LABEL';
  labelTerminiScadenza: string = 'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.TERMINI_SCADENZA_LABEL';
  labelModalitaMonitoraggio: string =
    'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.MODALITA_MONITORAGGIO_LABEL';
  labelMotivazioneImpossibilita: string =
    'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.MOTIVAZIONE_IMPOSSIBILITA_LABEL';
  labelCampiAggiuntivi: string = 'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.CAMPI_AGGIUNTIVI_TITLE';
  labelNonInseritiCampi: string =
    'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.CAMPI_AGGIUNTIVI_DESCRIPTION';
  labelAggiungiCampi: string = 'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.CAMPI_AGGIUNTIVI_ADD';

  dropdownTipologiaDato: LabelValue[] = [
    { label: 'Tipologia A', value: 1 },
    { label: 'Tipologia B', value: 2 },
  ];

  // Config per campi aggiuntivi
  ulterioriInfoConfig!: DynamicTBConfig;
  titleUlterioriInfo: string = 'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.CAMPI_AGGIUNTIVI_TITLE';

  ngOnInit(): void {
    this.createForm();
    this.initializeUlterioriInfo();
  }

  createForm() {
    this.formGroup = this.fb.group({
      id: [this.datiPubblicazioneEdit?.id || null],
      datoPubblicare: [
        this.datiPubblicazioneEdit?.datoPubblicare || null,
        [Validators.required, Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      tipologiaDato: [this.datiPubblicazioneEdit?.tipologiaDato || null, [Validators.required]],
      responsabileTrasmissione: [
        this.datiPubblicazioneEdit?.responsabileTrasmissione || null,
        [Validators.required, Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      terminiScadenza: [
        this.datiPubblicazioneEdit?.terminiScadenza || null,
        [Validators.required, Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      modalitaMonitoraggio: [
        this.datiPubblicazioneEdit?.modalitaMonitoraggio || null,
        [Validators.required, Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
      ],
      motivazioneImpossibilita: [
        this.datiPubblicazioneEdit?.motivazioneImpossibilita || null,
        [Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
      ],
      ulterioriInfo: createFormMongoFromPiaoSession<UlterioriInfoDTO>(
        this.fb,
        new UlterioriInfoDTO(),
        ['id', 'externalId', 'properties'],
        INPUT_REGEX,
        50,
        false
      ),
    });
  }

  /**
   * Inizializza la configurazione per i campi aggiuntivi
   */
  private initializeUlterioriInfo(): void {
    this.ulterioriInfoConfig = {
      labelTB: 'Nome campo',
      typeTB: 'text',
      idTB: 'value',
      controlTB: 'value',
      controlTBMongo: 'key',
      classTB: 'input',
      maxValidatorLengthTB: 50,
      descTooltipTB: 'Nome campo',
    };
  }

  /**
   * Chiude la modale
   */
  closeModal(): void {
    this.close.emit();
  }

  /**
   * Salva i dati della modale
   */
  saveData(): void {
    if (this.formGroup.valid) {
      const datiPubblicazione: DatiPubblicazioneDTO = this.formGroup.value;
      console.log('Dati pubblicazione salvati:', datiPubblicazione);
      this.closeModal();
    }
  }
}
