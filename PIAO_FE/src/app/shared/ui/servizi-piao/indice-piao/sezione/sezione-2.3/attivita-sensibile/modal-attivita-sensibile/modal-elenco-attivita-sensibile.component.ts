import { Component, inject, Input, OnInit } from '@angular/core';
import { ModalBodyComponent } from '../../../../../../../components/modal/modal-body/modal-body.component';
import { SharedModule } from '../../../../../../../module/shared/shared.module';
import { DynamicTBFieldsComponent } from '../../../../../../../components/dynamic-tb-fields/dynamic-tb-fields.component';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DynamicDropdownFieldsComponent } from '../../../../../../../components/dynamic-dropdown-fields/dynamic-dropdown-fields.component';
import { TextBoxComponent } from '../../../../../../../components/text-box/text-box.component';
import { TextAreaComponent } from '../../../../../../../components/text-area/text-area.component';
import { AttivitaSensibileDTO } from '../../../../../../../models/classes/attivita-sensibile-dto';
import { INPUT_REGEX } from '../../../../../../../utils/constants';
import { AttoreDTO } from '../../../../../../../models/classes/attore-dto';
import { createFormMongoFromPiaoSession } from '../../../../../../../utils/utils';
import { UlterioriInfoDTO } from '../../../../../../../models/classes/ulteriori-info-dto';
import { DynamicTBConfig } from '../../../../../../../models/classes/config/dynamic-tb';
import { LabelValue } from '../../../../../../../models/interfaces/label-value';
import { PIAODTO } from '../../../../../../../models/classes/piao-dto';
import { AttoreService } from '../../../../../../../services/attore.service';

@Component({
  selector: 'piao-modal-elenco-attivita-sensibile',
  imports: [
    SharedModule,
    ReactiveFormsModule,
    TextBoxComponent,
    TextAreaComponent,
    DynamicDropdownFieldsComponent,
    DynamicTBFieldsComponent,
  ],
  templateUrl: './modal-elenco-attivita-sensibile.component.html',
  styleUrl: './modal-elenco-attivita-sensibile.component.scss',
})
export class ModalElencoAttivitaSensibileComponent extends ModalBodyComponent implements OnInit {
  @Input() attivitaToEdit!: AttivitaSensibileDTO;
  @Input() piaoDTO!: PIAODTO;

  title: string = 'SEZIONE_23.ATTIVITA_SENSIBILI.MODAL.TITLE';
  subTitle: string = 'SEZIONE_23.ATTIVITA_SENSIBILI.MODAL.SUB_TITLE';
  labelAttivitaSensibile: string = 'SEZIONE_23.ATTIVITA_SENSIBILI.MODAL.ATTIVITA_SENSIBILE_LABEL';
  labelProcessoCollegato: string = 'SEZIONE_23.ATTIVITA_SENSIBILI.MODAL.PROCESSO_COLLEGATO_LABEL';
  labelDescrizione: string = 'SEZIONE_23.ATTIVITA_SENSIBILI.MODAL.DESCRIZIONE_LABEL';
  titleAttoriCoinvolti: string = 'SEZIONE_23.ATTIVITA_SENSIBILI.MODAL.ATTORI_COINVOLTI_TITLE';
  addAttoriCoinvolti: string = 'SEZIONE_23.ATTIVITA_SENSIBILI.MODAL.ADD_ATTORI_COINVOLTI';
  notFoundAttoriCoinvolti: string =
    'SEZIONE_23.ATTIVITA_SENSIBILI.MODAL.NOT_FOUND_ATTORI_COINVOLTI';

  main: string = 'main';

  labelTBUlterioreInfo: string = 'Nome campo';

  fb: FormBuilder = inject(FormBuilder);
  attoreService = inject(AttoreService);

  ulterioreInfo!: DynamicTBConfig;

  attoriConfig!: DynamicTBConfig;

  dropdownAttori: LabelValue[] = [];

  ngOnInit(): void {
    this.initAttoriConfig();
    this.initUlterioriInfoConfig();
    this.createForm();
    this.handleLoadAttore();
  }

  createForm() {
    this.formGroup = this.fb.group({
      id: [this.attivitaToEdit?.id || null],
      idSezione23: [this.attivitaToEdit?.idSezione23 || this.piaoDTO.idSezione23 || null],
      denominazione: [
        this.attivitaToEdit?.denominazione || null,
        [Validators.maxLength(100), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
      processoCollegato: [
        this.attivitaToEdit?.processoCollegato || null,
        [Validators.maxLength(100), Validators.pattern(INPUT_REGEX)],
      ],
      descrizione: [
        this.attivitaToEdit?.descrizione || null,
        [Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
      ],
    });

    //aggiunta campi mongo
    this.formGroup.addControl(
      'attore',
      createFormMongoFromPiaoSession<AttoreDTO>(
        this.fb,
        this.attivitaToEdit?.attore || new AttoreDTO(),
        ['id', 'externalId', 'properties'],
        INPUT_REGEX,
        50,
        false
      ) || null
    );

    this.formGroup.addControl(
      'ulterioriInfo',
      createFormMongoFromPiaoSession<UlterioriInfoDTO>(
        this.fb,
        this.attivitaToEdit?.ulterioriInfo || new UlterioriInfoDTO(),
        ['id', 'externalId', 'properties'],
        INPUT_REGEX,
        50,
        false
      ) || null
    );
  }

  private initAttoriConfig(): void {
    this.attoriConfig = {
      labelTB: 'Inserisci attore',
      typeTB: 'text',
      idTB: 'value',
      controlTB: 'value',
      controlTBMongo: 'key',
      classTB: 'input',
      maxValidatorLengthTB: 50,
      descTooltipTB: 'Inserisci attore',
    };
  }

  private initUlterioriInfoConfig(): void {
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
  }

  handleLoadAttore(): void {
    this.attoreService.getAll(this.piaoDTO.id || 0).subscribe({
      next: (attori) => {
        this.dropdownAttori = attori.flatMap(
          (attore) =>
            attore.properties?.map((prop) => ({
              label: prop.value || '',
              value: prop.value || '',
            })) || []
        );
      },
      error: (err) => {
        console.error('Errore nel caricare gli attori:', err);
      },
    });
  }

  handleAddModalOpened() {
    this.main = 'main-double-modal';
  }

  handleAddModalClosed() {
    this.main = 'main';
  }
}
