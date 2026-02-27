import { Component, inject, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { ModalBodyComponent } from '../../../../../../components/modal/modal-body/modal-body.component';
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { LabelValue } from '../../../../../../models/interfaces/label-value';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';
import { DropdownComponent } from '../../../../../../components/dropdown/dropdown.component';
import { IndicatoreDTO } from '../../../../../../models/classes/indicatore-dto';
import { createFormMongoFromPiaoSession, mapToLabelValue } from '../../../../../../utils/utils';
import { UlterioriInfoDTO } from '../../../../../../models/classes/ulteriori-info-dto';
import { INPUT_REGEX, KEY_PIAO, ONLY_NUMBERS_REGEX } from '../../../../../../utils/constants';
import { SessionStorageService } from '../../../../../../services/session-storage.service';
import { IndicatoreService } from '../../../../../../services/indicatore.service';
import { DimensioneIndicatoreService } from '../../../../../../services/dimensione-indicatore.service';
import { TargetIndicatoreService } from '../../../../../../services/target-indicatore.service';
import { PIAODTO } from '../../../../../../models/classes/piao-dto';
import { IIndicatoreWrapper } from '../../../../../../models/interfaces/indicatore-wrapper';
import { forkJoin } from 'rxjs';
import { DynamicTBConfig } from '../../../../../../models/classes/config/dynamic-tb';
import { CodTipologiaIndicatoreEnum } from '../../../../../../models/enums/cod-tipologia-indicatore.enum';
import { CodTipologiaDimensioneEnum } from '../../../../../../models/enums/cod-tipologia-dimensione.enum';
import { DynamicTBFieldsComponent } from '../../../../../../components/dynamic-tb-fields/dynamic-tb-fields.component';

@Component({
  selector: 'piao-modal-body-indicatore',
  imports: [
    SharedModule,
    TextBoxComponent,
    DropdownComponent,
    DynamicTBFieldsComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './modal-body-indicatore.component.html',
  styleUrl: './modal-body-indicatore.component.scss',
})
export class ModalBodyIndicatoreComponent extends ModalBodyComponent implements OnInit {
  @Input() isAddNewIndicatore: boolean = true;
  @Input() indicatoreToEdit!: IndicatoreDTO;
  @Input() codTipologiaFK!: string;
  @Input() codTipologiaIndicatoreFK!: string;
  @Input() idEntitaFK!: number;
  @Input() indicatoriControls?: FormArray;
  @Input() years: number[] = [];
  @Output() addInfoFieldAdded = new EventEmitter<void>();

  @Input() dimensioniDropdown: LabelValue[] = [];
  @Input() subDimensioniDropdown: LabelValue[] = [];
  @Input() targetDropdown: LabelValue[] = [];

  indicatoriDropdown: LabelValue[] = [];
  indicatoriComplete: IndicatoreDTO[] = [];
  allIndicatori: IndicatoreDTO[] = [];
  piaoDTO!: PIAODTO;

  fb: FormBuilder = inject(FormBuilder);
  sessionStorageService = inject(SessionStorageService);
  private indicatoreService = inject(IndicatoreService);

  main: string = 'main';

  labelDenominazione: string = 'INDICATORI.TH_INDICATORI.DENOMINAZIONE';
  labelDimensione: string = 'INDICATORI.TH_INDICATORI.DIMENSIONE';
  labelSubDimensione: string = 'INDICATORI.TH_INDICATORI.SUB_DIMENSIONE';
  labelUnita: string = 'INDICATORI.TH_INDICATORI.UNITA_MISURA';
  labelFormula: string = 'INDICATORI.TH_INDICATORI.FORMULA';
  labelPeso: string = 'INDICATORI.TH_INDICATORI.PESO';
  labelPolarita: string = 'INDICATORI.TH_INDICATORI.POLARITA';
  labelBaseline: string = 'INDICATORI.TH_INDICATORI.BASELINE';
  labelConsuntivo: string = 'INDICATORI.TH_INDICATORI.CONSUNTIVO';
  labelAndamento: string = 'INDICATORI.TH_INDICATORI.ANDAMENTO';
  labelValore: string = 'INDICATORI.TH_INDICATORI.VALORE';
  labelFonteDati: string = 'INDICATORI.TH_INDICATORI.FONTE_DATI';
  labelSelectedIndicatore: string = 'INDICATORI.MODAL.FORM.SELECT_INDICATORE';

  labelTBUlterioreInfo: string = 'Nome campo';

  ulterioreInfo!: DynamicTBConfig;

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);

    if (this.codTipologiaFK !== CodTipologiaDimensioneEnum.OVP) {
      this.main = 'main-obb';
    }

    this.formGroup = new FormGroup({
      choice: new FormControl<string | null>(null),
    });

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

    console.log(this.isAddNewIndicatore);

    if (this.isAddNewIndicatore) {
      console.log('Aggiungo validatore required al choice');
      const control = this.formGroup.controls['choice'];
      control.addValidators([Validators.required]);
    } else {
      console.log(this.indicatoreToEdit);
      this.formGroup.controls['choice'].setValue('inserisci');
      this.createFormIndicatore();
      console.log(this.indicatoreToEdit);
      console.log(this.formGroup);
    }
    this.formGroup.updateValueAndValidity();
  }

  private loadDropdownData(): void {
    forkJoin({
      indicatoriSelect: this.indicatoreService.getIndicatoriByIdPiaoAndIdEntitaAndCodTipologia(
        this.piaoDTO.id || 0,
        this.idEntitaFK || 0,
        this.codTipologiaIndicatoreFK
      ),
    }).subscribe({
      next: ({ indicatoriSelect }) => {
        // Gestione dimensioni in base al tipo di indicatore
        // Salva tutti gli indicatori disponibili
        this.allIndicatori = indicatoriSelect;

        // Filtra la dropdown con i dati già caricati
        this.filterIndicatoriDropdown();
      },
      error: (err) => console.error('Errore nel caricamento dati:', err),
    });
  }

  filterIndicatoriDropdown(): void {
    const indicatoriPresenti = this.indicatoriControls
      ? this.indicatoriControls.controls
          .map((control) => {
            const value = control.value as IIndicatoreWrapper;
            return value.indicatore?.id;
          })
          .filter((id) => id !== undefined && id !== null)
      : [];

    const indicatoriFiltrati = this.allIndicatori.filter(
      (indicatore) => indicatore.id && !indicatoriPresenti.includes(indicatore.id)
    );

    this.indicatoriComplete = indicatoriFiltrati;
    this.indicatoriDropdown = indicatoriFiltrati.map((indicatore) => ({
      label: indicatore.denominazione || '',
      value: indicatore.id || 0,
    }));
  }

  refreshIndicatoriFromServer(): void {
    this.indicatoreService
      .getIndicatoriByIdPiaoAndIdEntitaAndCodTipologia(
        this.piaoDTO.id || 0,
        this.idEntitaFK || 0,
        this.codTipologiaIndicatoreFK
      )
      .subscribe({
        next: (indicatoriSelect) => {
          this.allIndicatori = indicatoriSelect;
          this.filterIndicatoriDropdown();
        },
        error: (err) => console.error('Errore nel caricamento indicatori:', err),
      });
  }

  inputContent: LabelValue[] = [
    {
      label: 'INDICATORI.MODAL.RADIO_BUTTONS.CHOICE_1',
      value: 'recupera',
      formControlName: 'choice',
    },
    {
      label: 'INDICATORI.MODAL.RADIO_BUTTONS.CHOICE_2',
      value: 'inserisci',
      formControlName: 'choice',
    },
  ];

  createFormIndicatore(): void {
    this.formGroup.addControl('id', new FormControl<any | null>(this.indicatoreToEdit?.id || null));
    this.formGroup.addControl(
      'denominazione',
      new FormControl<any | null>(this.indicatoreToEdit?.denominazione || null, Validators.required)
    );
    this.formGroup.addControl(
      'dimensione',
      new FormControl<any | null>(this.indicatoreToEdit?.idDimensioneFK || null)
    );
    this.formGroup.addControl(
      'subDimensione',
      new FormControl<any | null>(this.indicatoreToEdit?.idSubDimensioneFK || null)
    );
    this.formGroup.addControl(
      'unitaMisura',
      new FormControl<any | null>(this.indicatoreToEdit?.unitaMisura || null)
    );
    this.formGroup.addControl(
      'formula',
      new FormControl<any | null>(this.indicatoreToEdit?.formula || null)
    );
    this.formGroup.addControl(
      'peso',
      new FormControl<any | null>(
        this.indicatoreToEdit?.peso || null,
        Validators.pattern(ONLY_NUMBERS_REGEX)
      )
    );
    this.formGroup.addControl(
      'polarita',
      new FormControl<any | null>(this.indicatoreToEdit?.polarita || null)
    );
    this.formGroup.addControl(
      'baseLine',
      new FormControl<any | null>(
        this.indicatoreToEdit?.baseLine || null,
        Validators.pattern(ONLY_NUMBERS_REGEX)
      )
    );
    this.formGroup.addControl(
      'consuntivo',
      new FormControl<any | null>(
        this.indicatoreToEdit?.consuntivo || null,
        Validators.pattern(ONLY_NUMBERS_REGEX)
      )
    );
    this.formGroup.addControl(
      'idTip1',
      new FormControl<any | null>(this.indicatoreToEdit?.tipAndValAnno1?.id || null)
    );
    this.formGroup.addControl(
      'andamento1',
      new FormControl<any | null>(
        this.indicatoreToEdit?.tipAndValAnno1?.idTargetFK || null,
        Validators.required
      )
    );
    this.formGroup.addControl(
      'valore1',
      new FormControl<any | null>(
        this.indicatoreToEdit?.tipAndValAnno1?.valore || null,
        Validators.required
      )
    );

    if (this.codTipologiaFK !== 'OBB_PER_IND') {
      this.formGroup.addControl(
        'idTip2',
        new FormControl<any | null>(this.indicatoreToEdit?.tipAndValAnno2?.id || null)
      );
      this.formGroup.addControl(
        'andamento2',
        new FormControl<any | null>(
          this.indicatoreToEdit?.tipAndValAnno2?.idTargetFK || null,
          Validators.required
        )
      );
      this.formGroup.addControl(
        'valore2',
        new FormControl<any | null>(
          this.indicatoreToEdit?.tipAndValAnno2?.valore || null,
          Validators.required
        )
      );
      this.formGroup.addControl(
        'idTip3',
        new FormControl<any | null>(this.indicatoreToEdit?.tipAndValAnnoCorrente?.id || null)
      );
      this.formGroup.addControl(
        'andamento3',
        new FormControl<any | null>(
          this.indicatoreToEdit?.tipAndValAnnoCorrente?.idTargetFK || null,
          Validators.required
        )
      );
      this.formGroup.addControl(
        'valore3',
        new FormControl<any | null>(
          this.indicatoreToEdit?.tipAndValAnnoCorrente?.valore || null,
          Validators.required
        )
      );
    }

    this.formGroup.addControl(
      'fonteDati',
      new FormControl<any | null>(this.indicatoreToEdit?.fonteDati || null)
    );
    this.formGroup.addControl(
      'addInfo',
      createFormMongoFromPiaoSession<UlterioriInfoDTO>(
        this.fb,
        this.indicatoreToEdit?.addInfo || new UlterioriInfoDTO(),
        ['id', 'externalId', 'properties'],
        INPUT_REGEX,
        50,
        false
      ) || null
    );
  }

  removeFormControls(): void {
    const controlsToRemove = [
      'denominazione',
      'dimensione',
      'unita',
      'formula',
      'peso',
      'polarita',
      'baseline',
      'consuntivo',
      'andamento2024',
      'valore2024',
      'andamento2025',
      'valore2025',
      'andamento2026',
      'valore2026',
      'fonteDati',
      'addInfo',
      'selectedIndicatore',
    ];

    controlsToRemove.forEach((controlName) => {
      if (this.formGroup.contains(controlName)) {
        this.formGroup.removeControl(controlName);
      }
    });
  }

  handleChoice(value: string | boolean | undefined): void {
    this.removeFormControls();

    if (value === 'inserisci') {
      this.createFormIndicatore();
    } else if (value === 'recupera') {
      // Aggiungo il control per la selezione dell'indicatore da recuperare
      this.formGroup.addControl('selectedIndicatore', new FormControl<any | null>(null));
      // Carica dimensioni, target e indicatori in parallelo
      this.loadDropdownData();
    }

    //aggiornamento del control e del form dopo aggiunta/rimozione validator
    this.formGroup.updateValueAndValidity();
    //this.debugFormState();
  }

  handleAddInfoModalOpened() {
    this.main = 'main-double-modal';
  }

  handleAddInfoModalClosed() {
    if (this.codTipologiaFK === CodTipologiaIndicatoreEnum.OVP) {
      this.main = 'main';
    } else {
      this.main = 'main-obb';
    }
  }

  handleSelectIndicatore(selectedId: any) {
    const indicatoreCompleto = this.indicatoriComplete.find((ind) => ind.id == selectedId);

    console.log('Ricerca indicatore completo con id:', selectedId, indicatoreCompleto);

    if (indicatoreCompleto) {
      this.indicatoreToEdit = indicatoreCompleto;
      this.removeFormControls();
      this.createFormIndicatore();
    }
  }
}
