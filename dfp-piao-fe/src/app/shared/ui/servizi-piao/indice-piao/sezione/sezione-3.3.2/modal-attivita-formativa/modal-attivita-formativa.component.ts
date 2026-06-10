import { Component, inject, Input, OnInit } from '@angular/core';
import { ModalBodyComponent } from '../../../../../../components/modal/modal-body/modal-body.component';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AttivitaFormativeDTO } from '../../../../../../models/classes/attivita-formativa-dto';
import { PIAODTO } from '../../../../../../models/classes/piao-dto';
import { HOURS_REGEX, INPUT_REGEX, ONLY_NUMBERS_REGEX } from '../../../../../../utils/constants';
import { DropdownComponent } from '../../../../../../components/dropdown/dropdown.component';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';
import { TooltipComponent } from '../../../../../../components/tooltip/tooltip.component';
import { LabelValue } from '../../../../../../models/interfaces/label-value';

@Component({
  selector: 'piao-modal-attivita-formativa',
  imports: [
    SharedModule,
    ReactiveFormsModule,
    DropdownComponent,
    TextBoxComponent,
    TooltipComponent,
  ],
  templateUrl: './modal-attivita-formativa.component.html',
  styleUrl: './modal-attivita-formativa.component.scss',
})
export class ModalAttivitaFormativaComponent extends ModalBodyComponent implements OnInit {
  @Input() attivitaFormativaToEdit?: AttivitaFormativeDTO;
  @Input() piaoDTO!: PIAODTO;
  @Input() dropdownTipologiaAttivita: LabelValue[] = [];
  @Input() dropdownAmbitoCompetenza: LabelValue[] = [];
  @Input() dropdownAreaTematica: LabelValue[] = [];
  @Input() lengthArray: number = 0;
  @Input() rowIndex: number = -1;

  fb: FormBuilder = inject(FormBuilder);

  // Translation keys
  labelTipologiaAttivita = 'SEZIONE_332.MODAL.TIPOLOGIA_ATTIVITA';
  labelAreaTematica = 'SEZIONE_332.MODAL.AREA_TEMATICA';
  labelAmbitoCompetenza = 'SEZIONE_332.MODAL.AMBITO_COMPETENZA';
  labelNumeroNonDirigenti = 'SEZIONE_332.MODAL.NUMERO_NON_DIRIGENTI';
  labelNumeroDirigenti = 'SEZIONE_332.MODAL.NUMERO_DIRIGENTI';
  labelOreFormazione = 'SEZIONE_332.MODAL.ORE_FORMAZIONE';
  labelVerificaApprendimento = 'SEZIONE_332.MODAL.VERIFICA_APPRENDIMENTO';

  // Radio button options for verificaApprendimento
  inputContent: LabelValue[] = [
    {
      label: 'SEZIONE_332.MODAL.SI',
      value: 'true',
      formControlName: 'verificaApprendimento',
    },
    {
      label: 'SEZIONE_332.MODAL.NO',
      value: 'false',
      formControlName: 'verificaApprendimento',
    },
  ];

  ngOnInit(): void {
    this.createForm();
  }

  createForm() {
    this.formGroup = this.fb.group({
      id: [this.attivitaFormativaToEdit?.id || null],
      idSezione332: [
        this.attivitaFormativaToEdit?.idSezione332 || this.piaoDTO.idSezione332 || null,
      ],
      idTipologiaAttivita: [
        this.attivitaFormativaToEdit?.idTipologiaAttivita || null,
        [Validators.required],
      ],
      idAmbitoCompetenza: [
        this.attivitaFormativaToEdit?.idAmbitoCompetenza || null,
        [Validators.required],
      ],
      idAreaTematica: [this.attivitaFormativaToEdit?.idAreaTematica || null, [Validators.required]],
      numeroNonDirigenti: [
        this.formatNumberForForm(this.attivitaFormativaToEdit?.numeroNonDirigenti),
        [Validators.required, Validators.pattern(ONLY_NUMBERS_REGEX)],
      ],
      numeroDirigenti: [
        this.formatNumberForForm(this.attivitaFormativaToEdit?.numeroDirigenti),
        [Validators.required, Validators.pattern(ONLY_NUMBERS_REGEX)],
      ],
      oreFormazione: [
        this.formatNumberForForm(this.attivitaFormativaToEdit?.oreFormazione),
        [Validators.required, Validators.pattern(HOURS_REGEX)],
      ],
      verificaApprendimento: [
        this.attivitaFormativaToEdit?.verificaApprendimento !== undefined
          ? this.attivitaFormativaToEdit.verificaApprendimento
            ? 'true'
            : 'false'
          : null,
        [Validators.required],
      ],
    });
  }

  /**
   * Format large numbers for form display
   * Handles exponential notation conversion
   */
  formatNumberForForm(value: number | null | undefined): string {
    if (value === null || value === undefined) {
      return '';
    }

    const numValue = Number(value);
    if (!isFinite(numValue)) {
      return '';
    }

    // If number is expressible as regular notation, use it
    // Otherwise use exponential notation
    if (numValue.toString().length > 15) {
      return numValue.toExponential(2);
    }

    return numValue.toString();
  }

  /**
   * Get the row label for the modal title
   */
  getRowLabel(): string {
    if (this.rowIndex >= 0) {
      // Editing existing row
      return `Riga ${this.rowIndex + 1}`;
    }
    // New row (empty array => Riga 1)
    return `Riga ${this.lengthArray + 1}`;
  }
}
