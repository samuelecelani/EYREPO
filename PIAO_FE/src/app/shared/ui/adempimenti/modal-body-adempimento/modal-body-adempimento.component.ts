import { Component, inject, Input, OnInit } from '@angular/core';
import { ModalBodyComponent } from '../../../components/modal/modal-body/modal-body.component';
import { SharedModule } from '../../../module/shared/shared.module';
import { TextAreaComponent } from '../../../components/text-area/text-area.component';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DynamicTBFieldsComponent } from '../../../components/dynamic-tb-fields/dynamic-tb-fields.component';
import { AdempimentoDTO } from '../../../models/classes/adempimento-dto';
import { DynamicTBConfig } from '../../../models/classes/config/dynamic-tb';
import { UlterioriInfoDTO } from '../../../models/classes/ulteriori-info-dto';
import { INPUT_REGEX } from '../../../utils/constants';
import { createFormMongoFromPiaoSession } from '../../../utils/utils';
import { AzioneDTO } from '../../../models/classes/azione-dto';
import { PIAODTO } from '../../../models/classes/piao-dto';

@Component({
  selector: 'piao-modal-body-adempimento',
  imports: [SharedModule, ReactiveFormsModule, TextAreaComponent, DynamicTBFieldsComponent],
  templateUrl: './modal-body-adempimento.component.html',
  styleUrl: './modal-body-adempimento.component.scss',
})
export class ModalBodyAdempimentoComponent extends ModalBodyComponent implements OnInit {
  @Input() title!: string;
  @Input() subTitle!: string;
  @Input() labelDesc!: string;
  @Input() adempimentoToEdit?: AdempimentoDTO;
  @Input() tipologia!: string;
  @Input() piaoDTO!: PIAODTO;

  main: string = 'main';

  labelTBUlterioreInfo: string = 'Nome campo';

  fb: FormBuilder = inject(FormBuilder);

  ulterioreInfo!: DynamicTBConfig;

  azioneConfig!: DynamicTBConfig;

  ngOnInit(): void {
    console.log('Adempimento to edit:', this.adempimentoToEdit);
    this.initAzioneConfig();
    this.initUlterioriInfoConfig();
    this.createForm();
  }

  createForm() {
    this.formGroup = this.fb.group({
      id: [this.adempimentoToEdit?.id || null],
      idSezione22: [this.adempimentoToEdit?.idSezione22 || this.piaoDTO.idSezione22 || null],
      tipologia: [this.adempimentoToEdit?.tipologia || this.tipologia || null],
      denominazione: [
        this.adempimentoToEdit?.denominazione || null,
        [Validators.maxLength(100), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
    });

    //aggiunta campi mongo
    this.formGroup.addControl(
      'azione',
      createFormMongoFromPiaoSession<AzioneDTO>(
        this.fb,
        this.adempimentoToEdit?.azione || new AzioneDTO(),
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
        this.adempimentoToEdit?.ulterioriInfo || new UlterioriInfoDTO(),
        ['id', 'externalId', 'properties'],
        INPUT_REGEX,
        50,
        false
      ) || null
    );
  }

  private initAzioneConfig(): void {
    this.azioneConfig = {
      labelTB: 'Azione proposta',
      typeTB: 'text',
      idTB: 'value',
      controlTB: 'value',
      controlTBMongo: 'key',
      classTB: 'input',
      maxValidatorLengthTB: 50,
      descTooltipTB: 'Azione proposta',
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

  handleAddModalOpened() {
    this.main = 'main-double-modal';
  }

  handleAddModalClosed() {
    this.main = 'main';
  }
}
