import { Component, inject, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ModalBodyComponent } from '../../../../../../../components/modal/modal-body/modal-body.component';
import { SharedModule } from '../../../../../../../module/shared/shared.module';
import { TextBoxComponent } from '../../../../../../../components/text-box/text-box.component';
import { TextAreaComponent } from '../../../../../../../components/text-area/text-area.component';
import { DynamicTBFieldsComponent } from '../../../../../../../components/dynamic-tb-fields/dynamic-tb-fields.component';
import { PIAODTO } from '../../../../../../../models/classes/piao-dto';
import { SessionStorageService } from '../../../../../../../services/session-storage.service';
import { DynamicTBConfig } from '../../../../../../../models/classes/config/dynamic-tb';
import { UlterioriInfoDTO } from '../../../../../../../models/classes/ulteriori-info-dto';
import { DatiPubblicatiDTO } from '../../../../../../../models/classes/dati-pubblicati-dto';
import { INPUT_REGEX } from '../../../../../../../utils/constants';
import { createFormMongoFromPiaoSession } from '../../../../../../../utils/utils';

@Component({
  selector: 'piao-modal-dati-pubblicazione',
  imports: [
    SharedModule,
    TextBoxComponent,
    TextAreaComponent,
    DynamicTBFieldsComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './modal-dati-pubblicazione.component.html',
  styleUrl: './modal-dati-pubblicazione.component.scss',
})
export class ModalDatiPubblicazioneComponent extends ModalBodyComponent implements OnInit {
  @Input() piaoDTO!: PIAODTO;
  @Input() datiPubblicatiEdit!: DatiPubblicatiDTO;
  @Input() idObbligoLegge!: number;
  @Output() close = new EventEmitter<void>();

  fb: FormBuilder = inject(FormBuilder);
  sessionStorageService = inject(SessionStorageService);
  main: string = 'main';

  labelTitle: string = 'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.TITLE';
  labelSubTitle: string = 'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.SUB_TITLE';
  labelDenominazione: string = 'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.DENOMINAZIONE_LABEL';
  labelTipologia: string = 'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.TIPOLOGIA_LABEL';
  labelResponsabile: string = 'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.RESPONSABILE_LABEL';
  labelTerminiScadenza: string = 'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.TERMINI_SCADENZA_LABEL';
  labelModalitaMonitoraggio: string =
    'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.MODALITA_MONITORAGGIO_LABEL';
  labelMotivazioneImpossibilita: string =
    'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.MOTIVAZIONE_IMPOSSIBILITA_LABEL';
  labelCampiAggiuntivi: string = 'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.CAMPI_AGGIUNTIVI_TITLE';
  labelNonInseritiCampi: string =
    'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.CAMPI_AGGIUNTIVI_DESCRIPTION';
  labelAggiungiCampi: string = 'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.CAMPI_AGGIUNTIVI_ADD';

  // Config per campi aggiuntivi
  ulterioriInfoConfig!: DynamicTBConfig;
  titleUlterioriInfo: string = 'SEZIONE_23.DATI_PUBBLICAZIONE.MODAL.CAMPI_AGGIUNTIVI_TITLE';

  ngOnInit(): void {
    this.createForm();
    this.initializeUlterioriInfo();
  }

  createForm() {
    console.log('dati pubblicati edit', this.datiPubblicatiEdit);
    this.formGroup = this.fb.group({
      id: [this.datiPubblicatiEdit?.id || null],
      idObbligoLegge: [this.idObbligoLegge || null],
      denominazione: [
        this.datiPubblicatiEdit?.denominazione || null,
        [Validators.required, Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      tipologia: [
        this.datiPubblicatiEdit?.tipologia || null,
        [Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      responsabile: [
        this.datiPubblicatiEdit?.responsabile || null,
        [Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      terminiScadenza: [
        this.datiPubblicatiEdit?.terminiScadenza || null,
        [Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      modalitaMonitoraggio: [
        this.datiPubblicatiEdit?.modalitaMonitoraggio || null,
        [Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
      ],
      motivazioneImpossibilita: [
        this.datiPubblicatiEdit?.motivazioneImpossibilita || null,
        [Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
      ],
      // Creiamo manualmente il FormGroup per ulterioriInfo per preservare le properties
      // anche quando id è null (dati nuovi non ancora salvati)
      ulterioriInfo: createFormMongoFromPiaoSession<UlterioriInfoDTO>(
        this.fb,
        this.datiPubblicatiEdit?.ulterioriInfo || new UlterioriInfoDTO(),
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
      this.closeModal();
    }
  }

  handleAddModalOpened() {
    this.main = 'main-double-modal';
  }

  handleAddModalClosed() {
    this.main = 'main';
  }
}
