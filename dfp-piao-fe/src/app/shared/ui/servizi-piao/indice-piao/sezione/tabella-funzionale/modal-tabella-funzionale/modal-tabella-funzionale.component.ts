import { Component, inject, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { ModalBodyComponent } from '../../../../../../components/modal/modal-body/modal-body.component';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';
import { INPUT_REGEX, ONLY_NUMBERS_REGEX } from '../../../../../../utils/constants';
import { PIAODTO } from '../../../../../../models/classes/piao-dto';
import { TabellaFunzionaleDTO } from '../../../../../../models/classes/tabella-funzionale-dto';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LabelValue } from '../../../../../../models/interfaces/label-value';
import { DropdownComponent } from '../../../../../../components/dropdown/dropdown.component';
import { SectionEnum } from '../../../../../../models/enums/section.enum';

@Component({
  selector: 'piao-modal-tabella-funzionale',
  imports: [SharedModule, TextBoxComponent, DropdownComponent, ReactiveFormsModule],
  templateUrl: './modal-tabella-funzionale.component.html',
  styleUrl: './modal-tabella-funzionale.component.scss',
})
export class ModalTabellaFunzionaleComponent extends ModalBodyComponent implements OnInit {
  @Input() tabellaToEdit!: TabellaFunzionaleDTO;
  @Input() piaoDTO!: PIAODTO;
  @Input() lenghtArray!: number;
  @Input() ovpOptions!: LabelValue[];
  @Input() stakeholderOptions!: LabelValue[];
  @Input() codTipologiaFK!: string;
  @Input() idEntitaFK!: number;

  title: string = 'TABELLA_FUNZIONALE.MODAL.TITLE';
  subTitle: string = 'TABELLA_FUNZIONALE.MODAL.SUB_TITLE';
  labelCodice: string = 'TABELLA_FUNZIONALE.MODAL.CODICE_LABEL';
  labelOvp: string = 'TABELLA_FUNZIONALE.MODAL.OVP_LABEL';
  labelDenominazione: string = 'TABELLA_FUNZIONALE.MODAL.DENOMINAZIONE_LABEL';
  labelResponsabile: string = 'TABELLA_FUNZIONALE.MODAL.RESPONSABILE_LABEL';
  labelDimensioni: string = 'TABELLA_FUNZIONALE.MODAL.DIMENSIONI_LABEL';
  labelStakeholder: string = 'TABELLA_FUNZIONALE.MODAL.STAKEHOLDER_LABEL';
  labelFormula: string = 'TABELLA_FUNZIONALE.MODAL.FORMULA_LABEL';
  labelPolarita: string = 'TABELLA_FUNZIONALE.MODAL.POLARITA_LABEL';
  labelBaseline: string = 'TABELLA_FUNZIONALE.MODAL.BASELINE_LABEL';
  labelTargetN1: string = 'TABELLA_FUNZIONALE.MODAL.TARGET_N1_LABEL';
  labelTargetN2: string = 'TABELLA_FUNZIONALE.MODAL.TARGET_N2_LABEL';
  labelTargetN3: string = 'TABELLA_FUNZIONALE.MODAL.TARGET_N3_LABEL';
  labelFonte: string = 'TABELLA_FUNZIONALE.MODAL.FONTE_LABEL';

  main: string = 'main';

  fb: FormBuilder = inject(FormBuilder);

  codSuffix: string = '';

  ngOnInit(): void {
    switch (this.codTipologiaFK) {
      case SectionEnum.SEZIONE_3_1:
        this.codSuffix = 'ORG';
        break;
      case SectionEnum.SEZIONE_3_2:
        this.codSuffix = 'LA';
        break;
      case SectionEnum.SEZIONE_3_3_1:
        this.codSuffix = 'FABB';
        break;
      case SectionEnum.SEZIONE_3_3_2:
        this.codSuffix = 'FORM';
        break;
    }
    this.createForm();
  }

  createForm() {
    this.formGroup = this.fb.group({
      id: [this.tabellaToEdit?.id || null],
      idEntitaFK: [this.tabellaToEdit?.idEntitaFK || this.idEntitaFK || null],
      codTipologiaFK: [this.tabellaToEdit?.codTipologiaFK || this.codTipologiaFK || null],
      codice: [
        null,
        [Validators.required, Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
      ],
      idOVP: [this.tabellaToEdit?.idOVP || null, [Validators.required]],
      denominazioneSintetica: [
        this.tabellaToEdit?.denominazioneSintetica || null,
        [Validators.required, Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      responsabileAmministrativo: [
        this.tabellaToEdit?.responsabileAmministrativo || null,
        [Validators.required, Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      idStakeholder: [this.tabellaToEdit?.idStakeholder || null, [Validators.required]],
      dimensioni: [
        this.tabellaToEdit?.dimensioni || null,
        [Validators.required, Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      formula: [
        this.tabellaToEdit?.formula || null,
        [Validators.required, Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      polarita: [
        this.tabellaToEdit?.polarita || null,
        [Validators.required, Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      baseline: [
        this.tabellaToEdit?.baseline || null,
        [Validators.required, Validators.maxLength(250), Validators.pattern(ONLY_NUMBERS_REGEX)],
      ],
      targetAnnoN1: [
        this.tabellaToEdit?.targetAnnoN1 || null,
        [Validators.required, Validators.maxLength(250), Validators.pattern(ONLY_NUMBERS_REGEX)],
      ],
      targetAnnoN2: [
        this.tabellaToEdit?.targetAnnoN2 || null,
        [Validators.required, Validators.maxLength(250), Validators.pattern(ONLY_NUMBERS_REGEX)],
      ],
      targetAnnoN3: [
        this.tabellaToEdit?.targetAnnoN3 || null,
        [Validators.required, Validators.maxLength(250), Validators.pattern(ONLY_NUMBERS_REGEX)],
      ],
      fonte: [
        this.tabellaToEdit?.fonte || null,
        [Validators.required, Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
    });

    this.setCodiceValue(this.tabellaToEdit?.idOVP || null);
  }

  setCodiceValue(event: any): void {
    if (event) {
      this.ovpOptions.forEach((option) => {
        if (option.value === event) {
          this.formGroup
            .get('codice')
            ?.setValue(option.additionalField + '_' + this.codSuffix + (this.lenghtArray + 1));
        }
      });
    } else {
      this.formGroup.get('codice')?.setValue(this.codSuffix + (this.lenghtArray + 1));
    }
  }
}
