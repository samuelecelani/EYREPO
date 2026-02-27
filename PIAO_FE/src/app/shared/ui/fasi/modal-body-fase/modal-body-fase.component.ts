import { Component, inject, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../../module/shared/shared.module';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { ModalBodyComponent } from '../../../components/modal/modal-body/modal-body.component';
import { FaseDTO } from '../../../models/classes/fase-dto';
import { createFormMongoFromPiaoSession } from '../../../utils/utils';
import { AttoreDTO } from '../../../models/classes/attore-dto';
import { AttivitaDTO } from '../../../models/classes/attivita-dto';
import { PIAODTO } from '../../../models/classes/piao-dto';
import { INPUT_REGEX } from '../../../utils/constants';
import { DynamicTBConfig } from '../../../models/classes/config/dynamic-tb';
import { TextAreaComponent } from '../../../components/text-area/text-area.component';
import { TextBoxComponent } from '../../../components/text-box/text-box.component';
import { DropdownComponent } from '../../../components/dropdown/dropdown.component';
import { LabelValue } from '../../../models/interfaces/label-value';
import { FaseEnum, isFaseValue } from '../../../models/enums/fase-enum';
import { DynamicTBFieldsComponent } from '../../../components/dynamic-tb-fields/dynamic-tb-fields.component';

@Component({
  selector: 'piao-modal-body-fase',
  imports: [
    SharedModule,
    DropdownComponent,
    TextBoxComponent,
    TextAreaComponent,
    ReactiveFormsModule,
    DynamicTBFieldsComponent,
  ],
  templateUrl: './modal-body-fase.component.html',
  styleUrl: './modal-body-fase.component.scss',
})
export class ModalBodyFaseComponent extends ModalBodyComponent implements OnInit {
  @Input() faseToEdit!: FaseDTO;
  @Input() piaoDTO!: PIAODTO;
  title: string = 'SEZIONE_22.FASI.MODAL.TITLE';
  subTitle: string = 'SEZIONE_22.FASI.MODAL.SUB_TITLE';
  labelDenominazione: string = 'SEZIONE_22.FASI.MODAL.DENOMINAZIONE_LABEL';
  labelNewDenominazione: string = 'SEZIONE_22.FASI.MODAL.NUOVA_DENOMINAZIONE_LABEL';
  labelDescrizione: string = 'SEZIONE_22.FASI.MODAL.DESCRIZIONE_LABEL';
  labelTempi: string = 'SEZIONE_22.FASI.MODAL.TEMPI_LABEL';

  main: string = 'main';

  attivitaConfig!: DynamicTBConfig;

  attoriConfig!: DynamicTBConfig;

  fb: FormBuilder = inject(FormBuilder);

  dropdown: LabelValue[] = [
    { label: 'Pianificazione delle performance', value: FaseEnum.PIANIFICAZIONE },
    { label: 'Valutazione delle performance', value: FaseEnum.VALUTAZIONE },
    { label: 'Monitoraggio delle performance', value: FaseEnum.MONITORAGGIO },
    { label: 'Aggiungi nuova delle performance', value: FaseEnum.AGGIUNGI_NUOVA },
  ];

  FaseEnum: any = FaseEnum;

  ngOnInit(): void {
    this.initAttoriConfig();
    this.initAttivitaConfig();
    this.createForm();
  }

  createForm() {
    this.formGroup = this.fb.group({
      id: [this.faseToEdit?.id || null],
      idSezione22: [this.faseToEdit?.idSezione22 || this.piaoDTO.idSezione22 || null],
      denominazioneDropdown: [null, [Validators.required]],
      descrizione: [
        this.faseToEdit?.descrizione || null,
        [Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
      ],
      tempi: [
        this.faseToEdit?.tempi || null,
        [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
      ],
    });

    //aggiunta campi mongo
    this.formGroup.addControl(
      'attore',
      createFormMongoFromPiaoSession<AttoreDTO>(
        this.fb,
        this.faseToEdit?.attore || new AttoreDTO(),
        ['id', 'externalId', 'properties'],
        INPUT_REGEX,
        50,
        false
      ) || null
    );

    this.formGroup.addControl(
      'attivita',
      createFormMongoFromPiaoSession<AttivitaDTO>(
        this.fb,
        this.faseToEdit?.attivita || new AttivitaDTO(),
        ['id', 'externalId', 'properties'],
        INPUT_REGEX,
        50,
        false
      ) || null
    );

    this.formGroup.patchValue({
      denominazioneDropdown: this.ctrlDenominazione(this.faseToEdit),
    });
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

  private initAttivitaConfig(): void {
    this.attivitaConfig = {
      labelTB: 'Attività',
      typeTB: 'text',
      idTB: 'value',
      controlTB: 'value',
      controlTBMongo: 'key',
      classTB: 'input',
      maxValidatorLengthTB: 50,
      descTooltipTB: 'Attività',
    };
  }

  handleAddModalOpened() {
    this.main = 'main-double-modal';
  }

  handleAddModalClosed() {
    this.main = 'main';
  }

  handleChoice(): void {
    if (this.formGroup.controls['denominazioneDropdown'].value === FaseEnum.AGGIUNGI_NUOVA) {
      this.formGroup.addControl(
        'denominazione',
        new FormControl<any | null>(null, [
          Validators.required,
          Validators.maxLength(50),
          Validators.pattern(INPUT_REGEX),
        ])
      );
    } else {
      this.formGroup.removeControl('denominazione');
    }

    //aggiornamento del control e del form dopo aggiunta/rimozione validator
    this.formGroup.updateValueAndValidity();
  }

  ctrlDenominazione(faseToEdit: FaseDTO): any {
    console.log(faseToEdit);
    if (faseToEdit && faseToEdit.denominazione) {
      console.log('entrato');
      if (isFaseValue(faseToEdit.denominazione)) {
        return faseToEdit.denominazione;
      } else {
        if (this.formGroup?.contains('denominazione')) {
          this.formGroup.removeControl('denominazione');
        }
        this.formGroup?.addControl(
          'denominazione',
          new FormControl<any | null>(faseToEdit.denominazione || null, [
            Validators.required,
            Validators.maxLength(50),
            Validators.pattern(INPUT_REGEX),
          ])
        );
        this.formGroup.updateValueAndValidity();
        return FaseEnum.AGGIUNGI_NUOVA;
      }
    }
    return null;
  }
}
