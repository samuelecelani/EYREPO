import { Component, inject, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../../../../../../module/shared/shared.module';
import { CategoriaObiettiviDTO } from '../../../../../../../models/classes/categoria-obiettivi-dto';
import { LabelValue } from '../../../../../../../models/interfaces/label-value';
import { PIAODTO } from '../../../../../../../models/classes/piao-dto';
import { DynamicTBConfig } from '../../../../../../../models/classes/config/dynamic-tb';
import { AttoreService } from '../../../../../../../services/attore.service';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ModalBodyComponent } from '../../../../../../../components/modal/modal-body/modal-body.component';
import { INPUT_REGEX } from '../../../../../../../utils/constants';
import {
  createFormMongoFromPiaoSession,
  createFormMongoAttivitaFromPiaoSession,
  createFormMongoFromPiaoSession4Item,
} from '../../../../../../../utils/utils';
import { AttoreDTO } from '../../../../../../../models/classes/attore-dto';
import { UlterioriInfoDTO } from '../../../../../../../models/classes/ulteriori-info-dto';
import { BaseMongoAttivitaDTO } from '../../../../../../../models/classes/base-mongo-attivita-dto';
import { DynmaicAttivitaConfig } from '../../../../../../../models/classes/config/dynamic-attivita-config';
import { DynamicDropdownFieldsComponent } from '../../../../../../../components/dynamic-dropdown-fields/dynamic-dropdown-fields.component';
import { DynamicTBFieldsComponent } from '../../../../../../../components/dynamic-tb-fields/dynamic-tb-fields.component';
import { DynamicAttivitaFieldsComponent } from '../../../../../../../components/dynamic-attivita-fields/dynamic-attivita-fields.component';
import { DropdownComponent } from '../../../../../../../components/dropdown/dropdown.component';
import { CodTipologiaSezioneEnum } from '../../../../../../../models/enums/cod-tipologia-sezione.enum';
import { CodTipologiaCategoriaEnum } from '../../../../../../../models/enums/cod-tipologia-categoria.enum';
import { CodTipologiaAttoreEnum } from '../../../../../../../models/enums/cod-tipologia-attore.enum';

@Component({
  selector: 'piao-modal-categoria-obiettivi',
  imports: [
    SharedModule,
    DynamicDropdownFieldsComponent,
    DynamicTBFieldsComponent,
    DynamicAttivitaFieldsComponent,
    DropdownComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './modal-categoria-obiettivi.component.html',
  styleUrl: './modal-categoria-obiettivi.component.scss',
})
export class ModalCategoriaObiettiviComponent extends ModalBodyComponent implements OnInit {
  @Input() categoriaObiettiviEdit!: CategoriaObiettiviDTO;
  @Input() piaoDTO!: PIAODTO;
  @Input() dropdownSottofaseMonitoraggio!: LabelValue[];
  @Input() dropdownCategoriaObiettivi!: LabelValue[];
  @Input() codTipologiaFK!: CodTipologiaCategoriaEnum;

  main: string = 'main';

  labelTBUlterioreInfo: string = 'Nome campo';

  fb: FormBuilder = inject(FormBuilder);
  attoreService = inject(AttoreService);

  ulterioreInfo!: DynamicTBConfig;

  attoriConfig!: DynamicTBConfig;

  attivitaConfig!: DynmaicAttivitaConfig;

  dropdownAttori: LabelValue[] = [];
  labelTitle: string = 'SEZIONE_4.MODAL_CATEGORIA_OBIETTIVO.TITLE';
  labelDesc: string = 'SEZIONE_4.MODAL_CATEGORIA_OBIETTIVO.DESC';
  labelSottoFase: string = 'SEZIONE_4.MODAL_CATEGORIA_OBIETTIVO.SOTTO_FASE_LABEL';
  labelCategoria: string = 'SEZIONE_4.MODAL_CATEGORIA_OBIETTIVO.CATEGORIA_LABEL';
  titleAttoriCoinvolti: string = 'SEZIONE_23.ATTIVITA_SENSIBILI.MODAL.ATTORI_COINVOLTI_TITLE';
  addAttoriCoinvolti: string = 'SEZIONE_23.ATTIVITA_SENSIBILI.MODAL.ADD_ATTORI_COINVOLTI';
  notFoundAttoriCoinvolti: string =
    'SEZIONE_23.ATTIVITA_SENSIBILI.MODAL.NOT_FOUND_ATTORI_COINVOLTI';

  ngOnInit(): void {
    this.initAttoriConfig();
    this.initUlterioriInfoConfig();
    this.initAttivitaConfig();
    this.createForm();
    console.log('CategoriaObiettiviDTO in edit:', this.categoriaObiettiviEdit);
    this.handleLoadAttore(this.categoriaObiettiviEdit?.attore?.externalIdFK || 0);
  }

  createForm() {
    console.log('CategoriaObiettiviDTO in edit:', this.categoriaObiettiviEdit);
    this.formGroup = this.fb.group({
      id: [this.categoriaObiettiviEdit?.id || null],
      idSezione4: [this.categoriaObiettiviEdit?.idSezione4 || this.piaoDTO.idSezione4 || null],
      idSottofase: [
        this.categoriaObiettiviEdit?.idSottofase || null,
        [Validators.maxLength(100), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
      idCategoriaObiettivi: [
        this.categoriaObiettiviEdit?.idCategoriaObiettivi || null,
        [Validators.maxLength(100), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
      codTipologiaFk: [
        this.categoriaObiettiviEdit?.codTipologiaFk || this.codTipologiaFK || null,
        [Validators.maxLength(100), Validators.pattern(INPUT_REGEX)],
      ],
    });

    //aggiunta campi mongo
    this.formGroup.addControl(
      'attivita',
      createFormMongoAttivitaFromPiaoSession<BaseMongoAttivitaDTO>(
        this.fb,
        this.categoriaObiettiviEdit?.attivita || new BaseMongoAttivitaDTO(),
        ['id', 'externalId', 'propertyAttivita'],
        INPUT_REGEX,
        50,
        false
      ) || null
    );

    this.formGroup.addControl(
      'attore',
      createFormMongoFromPiaoSession4Item<AttoreDTO>(
        this.fb,
        this.categoriaObiettiviEdit?.attore || new AttoreDTO(),
        ['id', 'externalId', 'externalIdFK', 'properties'],
        INPUT_REGEX,
        50,
        false
      ) || null
    );

    this.formGroup.addControl(
      'ulterioriInfo',
      createFormMongoFromPiaoSession<UlterioriInfoDTO>(
        this.fb,
        this.categoriaObiettiviEdit?.ulterioriInfo || new UlterioriInfoDTO(),
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

  private initAttivitaConfig(): void {
    this.attivitaConfig = {
      labelTB: 'Attività',
      typeTB: 'text',
      idTB: 'value',
      controlTB: 'value',
      controlTBMongo: 'key',
      classTB: 'input',
      maxValidatorLengthTB: 50,
      descTooltipTB: 'Inserisci attività',
      labelDataInizio: 'Data inizio',
      labelDataFine: 'Data fine',
      descTooltipDataInizio: 'Inserisci data inizio',
      descTooltipDataFine: 'Inserisci data fine',
    };
  }

  handleLoadAttore(externalId: number | string): void {
    this.dropdownAttori = [];

    console.log('ExternalId per caricamento attore:', externalId);
    console.log(
      'ExternalId per caricamento attore:',
      this.categoriaObiettiviEdit?.attore?.externalIdFK
    );
    console.log(
      'ExternalId per caricamento attore:',
      externalId && externalId === this.categoriaObiettiviEdit?.attore?.externalIdFK
    );

    if (externalId && externalId === this.categoriaObiettiviEdit?.attore?.externalIdFK) {
      this.categoriaObiettiviEdit!.attore!.externalIdFK = externalId as number;
      console.log(
        'Caricamento attore con externalId:',
        externalId,
        this.categoriaObiettiviEdit!.attore!.externalIdFK
      );
      this.formGroup.setControl(
        'attore',
        createFormMongoFromPiaoSession4Item<AttoreDTO>(
          this.fb,
          this.categoriaObiettiviEdit?.attore || new AttoreDTO(),
          ['id', 'externalId', 'externalIdFK', 'properties'],
          INPUT_REGEX,
          50,
          false
        ) || null
      );
    } else {
      this.formGroup.setControl(
        'attore',
        createFormMongoFromPiaoSession4Item<AttoreDTO>(
          this.fb,
          {
            externalIdFK: Number(externalId),
          } as AttoreDTO,
          ['id', 'externalId', 'externalIdFK', 'properties'],
          INPUT_REGEX,
          50,
          false
        ) || null
      );
    }

    console.log('FormGroup dopo setControl attore:', this.formGroup.get('attore')?.value);

    this.attoreService
      .getAttoreByTipologiaAndExternalId(
        CodTipologiaAttoreEnum.SEZIONE_4_SOTTOFASEMONITORAGGIO,
        externalId
      )
      .subscribe({
        next: (attori) => {
          this.dropdownAttori =
            attori?.properties?.map((prop) => ({
              label: prop.value || '',
              value: prop.value || '',
            })) || [];
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
