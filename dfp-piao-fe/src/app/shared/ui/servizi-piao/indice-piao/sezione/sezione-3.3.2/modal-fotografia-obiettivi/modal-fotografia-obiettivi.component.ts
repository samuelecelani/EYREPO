import { ObiettiviRisultatiFotografiaDTO } from '../../../../../../models/classes/obiettivi-risultati-fotografia-dto';
import {
  DATE_REGEX,
  HOURS_REGEX,
  INPUT_REGEX,
  ONLY_NUMBERS_REGEX,
  SHAPE_ICON,
} from './../../../../../../utils/constants';
import { Component, inject, Input, OnInit } from '@angular/core';
import { PIAODTO } from '../../../../../../models/classes/piao-dto';
import { LabelValue } from '../../../../../../models/interfaces/label-value';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { SessionStorageService } from '../../../../../../services/session-storage.service';
import { ModalBodyComponent } from '../../../../../../components/modal/modal-body/modal-body.component';
import { Sezione332Service } from '../../../../../../services/sezione332.service';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';
import { DropdownComponent } from '../../../../../../components/dropdown/dropdown.component';
import { TextAreaComponent } from '../../../../../../components/text-area/text-area.component';
import { CardAlertComponent } from 'src/app/shared/ui/card-alert/card-alert.component';
import { DatePickerComponent } from '../../../../../../components/date-picker/date-picker.component';
import { CodTipologiaFotoObiettivoEnum } from '../../../../../../models/enums/cod-tipologia-foto-obi.enum';
import { getTodayISO } from '../../../../../../utils/utils';

@Component({
  selector: 'piao-modal-fotografia-obiettivi',
  imports: [
    SharedModule,
    TextBoxComponent,
    TextAreaComponent,
    DropdownComponent,
    ReactiveFormsModule,
    DatePickerComponent,
  ],
  templateUrl: './modal-fotografia-obiettivi.component.html',
  styleUrl: './modal-fotografia-obiettivi.component.scss',
})
export class ModalFotografiaObiettiviComponent extends ModalBodyComponent implements OnInit {
  @Input() piaoDTO!: PIAODTO;
  @Input() fotografiaEdit!: ObiettiviRisultatiFotografiaDTO;
  @Input() dropdownTipologiaAttivita: LabelValue[] = [];
  @Input() dropdownAmbitoCompetenza: LabelValue[] = [];
  @Input() dropdownAreaTematica: LabelValue[] = [];
  @Input() dropdownTipologiaDestinatari: LabelValue[] = [];
  @Input() codTipologiaFK!: string;
  @Input() lenghtArray!: number;

  icon: string = 'SHAPE_ICON';

  labelTitle: string = 'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.TITLE';
  labelSubTitle: string = 'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.SUB_TITLE';
  labelAttivitaFormativa: string =
    'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.ATTIVITA_FORMATIVA';
  labelTipologiaAttivita: string =
    'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.TIPOLOGIA_ATTIVITA';
  labelTitoloAttivita: string = 'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.TITOLO_ATTIVITA';
  labelAmbitoCompetenza: string =
    'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.AMBITO_COMPETENZA';
  labelAreaTematica: string = 'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.AREA_TEMATICA';
  labelCarattereObbligarorio: string =
    'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.RATIO_CARATTERE_OBBLIGATORIO';
  labelRiferimentoNormativo: string =
    'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.RIFERIMENTO_NORMATIVO';
  labelDestinatari: string = 'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.DESTINATARI';
  labelTargetDirigenti: string = 'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.TARGET_DIRIGENTI';
  labelNumeroDirigenti: string = 'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.NUMERO_DIRIGENTI';
  labelTargetNonDirigenti: string =
    'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.TARGET_NON_DIRIGENTI';
  labelNumeroNonDirigenti: string =
    'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.NUMERO_NON_DIRIGENTI';
  labelOreFormazione: string = 'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.ORE_FORMAZIONE';
  labelVerificaApprendimento: string =
    'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.RATIO_VERIFICA_APPRENDIMENTO';
  labelCreditiFormativi: string =
    'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.CREDITI_FORMATIVI';
  labelModalitaGestioneFormazione: string =
    'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.MODALITA_GESTIONE_FORMAZIONE';
  labelEnteErogatore: string = 'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.ENTE_EROGATORE';
  labelCostoAttivita: string = 'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.COSTO_ATTIVITA';
  labelDataInizio: string = 'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.DATA_INIZIO';
  labelDataFine: string = 'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.DATA_FINE';

  radioCarattereOptions: LabelValue[] = [
    {
      label: 'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.CHOICE1_LABEL',
      value: true,
      formControlName: 'carattereObbligatorio',
    },
    {
      label: 'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.CHOICE2_LABEL',
      value: false,
      formControlName: 'carattereObbligatorio',
    },
  ];

  radioVerificaOptions: LabelValue[] = [
    {
      label: 'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.CHOICE1_LABEL',
      value: true,
      formControlName: 'verificaApprendimento',
    },
    {
      label: 'SEZIONE-3.3.2.MODAL.MODAL_FOTOGRAFIA_FORMAZIONE.CHOICE2_LABEL',
      value: false,
      formControlName: 'verificaApprendimento',
    },
  ];

  fb: FormBuilder = inject(FormBuilder);
  sessionStorageService = inject(SessionStorageService);
  main: string = 'main';
  isFormReady: boolean = false;
  sezione332Service: Sezione332Service = inject(Sezione332Service);

  minDate: string = getTodayISO();

  ngOnInit(): void {
    this.createForm();

    this.isFormReady = true;
  }

  createForm() {
    this.formGroup = this.fb.group({
      id: [this.fotografiaEdit?.id ?? null],

      idSezione332: [
        this.fotografiaEdit?.idSezione332 ?? this.piaoDTO?.idSezione332 ?? null,
        Validators.required,
      ],

      idTipologiaAttivita: [this.fotografiaEdit?.idTipologiaAttivita ?? null, Validators.required],

      idAmbitoCompetenza: [this.fotografiaEdit?.idAmbitoCompetenza ?? null, Validators.required],
      idAreaTematica: [this.fotografiaEdit?.idAreaTematica ?? null, Validators.required],
      idTipologiaDestinatari: [
        this.fotografiaEdit?.idTipologiaDestinatari ?? null,
        Validators.required,
      ],

      codTipologiaFK: [
        this.fotografiaEdit?.codTipologiaFK ?? this.codTipologiaFK ?? null,
        Validators.required,
      ],

      codice: [
        this.fotografiaEdit?.codice ?? null,
        [Validators.required, Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
      ],

      titolo: [
        this.fotografiaEdit?.titolo ?? null,
        [Validators.required, Validators.maxLength(20000), Validators.pattern(INPUT_REGEX)],
      ],

      carattereObbligatorio: [
        this.fotografiaEdit?.carattereObbligatorio ?? null,
        Validators.required,
      ],

      riferimentoNormativo: [
        this.fotografiaEdit?.riferimentoNormativo ?? null,
        [Validators.maxLength(20000), Validators.required, Validators.pattern(INPUT_REGEX)],
      ],

      targetDirigenti: [
        this.fotografiaEdit?.targetDirigenti ?? null,
        [Validators.maxLength(255), Validators.required, Validators.pattern(ONLY_NUMBERS_REGEX)],
      ],

      targetNonDirigenti: [
        this.fotografiaEdit?.targetNonDirigenti ?? null,
        [Validators.maxLength(255), Validators.required, Validators.pattern(ONLY_NUMBERS_REGEX)],
      ],

      numeroDirigenti: [
        this.fotografiaEdit?.numeroDirigenti ?? null,
        [Validators.pattern(ONLY_NUMBERS_REGEX), Validators.maxLength(10)],
      ],

      numeroNonDirigenti: [
        this.fotografiaEdit?.numeroNonDirigenti ?? null,
        [Validators.pattern(ONLY_NUMBERS_REGEX), Validators.maxLength(10)],
      ],

      oreFormazione: [
        this.fotografiaEdit?.oreFormazione ?? null,
        [Validators.maxLength(10), Validators.required, Validators.pattern(HOURS_REGEX)],
      ],

      verificaApprendimento: [
        this.fotografiaEdit?.verificaApprendimento ?? null,
        Validators.required,
      ],

      creditiFormativi: [
        this.fotografiaEdit?.creditiFormativi ?? null,
        [Validators.maxLength(10), Validators.required, Validators.pattern(ONLY_NUMBERS_REGEX)],
      ],

      modalitaGestioneFormazione: [
        this.fotografiaEdit?.modalitaGestioneFormazione ?? null,
        [Validators.maxLength(255), Validators.required, Validators.pattern(INPUT_REGEX)],
      ],

      enteErogatore: [
        this.fotografiaEdit?.enteErogatore ?? null,
        [Validators.maxLength(20000), Validators.required, Validators.pattern(INPUT_REGEX)],
      ],

      costoAttivita: [
        this.fotografiaEdit?.costoAttivita ?? null,
        [Validators.maxLength(50), Validators.required, Validators.pattern(ONLY_NUMBERS_REGEX)],
      ],

      dataInizio: [
        this.fotografiaEdit?.dataInizio ?? null,
        [Validators.pattern(DATE_REGEX), Validators.required],
      ],

      dataFine: [
        this.fotografiaEdit?.dataFine ?? null,
        [Validators.pattern(DATE_REGEX), Validators.required],
      ],
    });

    if (this.codTipologiaFK === CodTipologiaFotoObiettivoEnum.FOTOGRAFIA_FORMAZIONE) {
      this.formGroup.get('numeroDirigenti')?.setValidators([Validators.required]);
      this.formGroup.get('numeroNonDirigenti')?.setValidators([Validators.required]);
    }
  }

  handleTipologiaAttivitaChange(event: any): void {
    let codice = this.dropdownTipologiaAttivita.find(
      (item) => item.value === event
    )?.additionalField;
    if (codice) {
      this.formGroup?.get('codice')?.setValue(codice);
    }
  }

  resetForm(): void {
    if (this.formGroup) {
      this.formGroup.reset();
    }
  }
}
