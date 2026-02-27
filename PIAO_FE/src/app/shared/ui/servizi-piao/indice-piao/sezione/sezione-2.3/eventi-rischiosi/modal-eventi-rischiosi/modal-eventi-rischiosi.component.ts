import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { SharedModule } from '../../../../../../../module/shared/shared.module';
import { ModalBodyComponent } from '../../../../../../../components/modal/modal-body/modal-body.component';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { TextBoxComponent } from '../../../../../../../components/text-box/text-box.component';
import { TextAreaComponent } from '../../../../../../../components/text-area/text-area.component';
import { DropdownComponent } from '../../../../../../../components/dropdown/dropdown.component';
import { DynamicTBFieldsComponent } from '../../../../../../../components/dynamic-tb-fields/dynamic-tb-fields.component';
import { DynamicTBComponent } from '../../../../../../../components/dynamic-tb/dynamic-tb.component';
import { DynamicTBConfig } from '../../../../../../../models/classes/config/dynamic-tb';
import { LabelValue } from '../../../../../../../models/interfaces/label-value';
import { EventoRischiosoDTO } from '../../../../../../../models/classes/evento-rischioso-dto';
import { UlterioriInfoDTO } from '../../../../../../../models/classes/ulteriori-info-dto';
import { createFormMongoFromPiaoSession } from '../../../../../../../utils/utils';
import { INPUT_REGEX, ONLY_NUMBERS_REGEX } from '../../../../../../../utils/constants';
import { BaseMongoDTO } from '../../../../../../../models/classes/base-mongo-dto';
import { FattoreDTO } from '../../../../../../../models/classes/fattore-dto';

//cleanSingleMongoDTO<UlterioriInfoDTO>(form.controls['addInfo'].value)

@Component({
  selector: 'piao-modal-eventi-rischiosi',
  imports: [
    SharedModule,
    TextBoxComponent,
    TextAreaComponent,
    DropdownComponent,
    ReactiveFormsModule,
    DynamicTBFieldsComponent,
  ],
  templateUrl: './modal-eventi-rischiosi.component.html',
  styleUrl: './modal-eventi-rischiosi.component.scss',
})
export class ModalEventiRischiosiComponent extends ModalBodyComponent implements OnInit {
  @Input() eventoToEdit?: EventoRischiosoDTO;
  @Input() idAttivitaSensibile?: number;
  @Input() livelloRischioOptions: LabelValue[] = [];
  @Output() addInfoModalOpened = new EventEmitter<void>();
  @Output() addInfoModalClosed = new EventEmitter<void>();

  fb: FormBuilder = inject(FormBuilder);

  main: string = 'main';

  // Labels
  title: string = 'SEZIONE_23.EVENTI_RISCHIOSI.MODAL.TITLE';
  subTitle: string = 'SEZIONE_23.EVENTI_RISCHIOSI.MODAL.SUB_TITLE';

  labelDenominazione: string = 'SEZIONE_23.EVENTI_RISCHIOSI.MODAL.DENOMINAZIONE_LABEL';
  labelProbabilita: string = 'SEZIONE_23.EVENTI_RISCHIOSI.MODAL.PROBABILITA_LABEL';
  labelImpatto: string = 'SEZIONE_23.EVENTI_RISCHIOSI.MODAL.IMPATTO_LABEL';
  labelControlli: string = 'SEZIONE_23.EVENTI_RISCHIOSI.MODAL.CONTROLLI_LABEL';
  labelValutazione: string = 'SEZIONE_23.EVENTI_RISCHIOSI.MODAL.VALUTAZIONE_LABEL';
  labelLivelloRischio: string = 'SEZIONE_23.EVENTI_RISCHIOSI.MODAL.LIVELLO_RISCHIO_LABEL';
  labelMotivazione: string = 'SEZIONE_23.EVENTI_RISCHIOSI.MODAL.MOTIVAZIONE_LABEL';
  titleFattori: string = 'SEZIONE_23.EVENTI_RISCHIOSI.MODAL.FATTORI_TITLE';
  titleUlterioriInfo: string = 'SEZIONE_23.EVENTI_RISCHIOSI.MODAL.ULTERIORI_INFO_TITLE';

  // Config for dynamic fields
  fattoriConfig!: DynamicTBConfig;
  ulterioriInfoConfig!: DynamicTBConfig;

  ngOnInit(): void {
    this.initConfigs();
    this.createForm();
    this.setupValutazioneCalculation();
  }

  private initConfigs(): void {
    this.fattoriConfig = {
      labelTB: 'Fattore',
      typeTB: 'text',
      idTB: 'value',
      controlTB: 'value',
      controlTBMongo: 'key',
      classTB: 'input',
      maxValidatorLengthTB: 100,
      descTooltipTB: 'Fattore',
    };

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

  createForm(): void {
    const evento = this.eventoToEdit || new EventoRischiosoDTO();

    this.formGroup = this.fb.group({
      id: new FormControl<number | null>(evento.id || null),
      idAttivitaSensibile: new FormControl<number | null>(
        evento.idAttivitaSensibile || this.idAttivitaSensibile || null
      ),
      denominazione: new FormControl<string | null>(evento.denominazione || null, [
        Validators.required,
        Validators.maxLength(200),
        Validators.pattern(INPUT_REGEX),
      ]),
      probabilita: new FormControl<string | null>(evento.probabilita || null, [
        Validators.maxLength(100),
        Validators.pattern(ONLY_NUMBERS_REGEX),
      ]),
      impatto: new FormControl<string | null>(evento.impatto || null, [
        Validators.maxLength(100),
        Validators.pattern(ONLY_NUMBERS_REGEX),
      ]),
      controlli: new FormControl<string | null>(evento.controlli || null, [
        Validators.maxLength(100),
        Validators.pattern(ONLY_NUMBERS_REGEX),
      ]),
      valutazione: new FormControl<string | number | null>(evento.valutazione || null),
      idLivelloRischio: new FormControl<number | null>(evento.idLivelloRischio || null),
      motivazione: new FormControl<string | null>(evento.motivazione || null, [
        Validators.required,
        Validators.maxLength(500),
        Validators.pattern(INPUT_REGEX),
      ]),
      fattore: createFormMongoFromPiaoSession<FattoreDTO>(
        this.fb,
        evento.fattore || new FattoreDTO(),
        ['id', 'externalId', 'properties'],
        INPUT_REGEX,
        100,
        false,
        this.fattoriConfig.labelTB
      ),
      ulterioriInfo: createFormMongoFromPiaoSession<UlterioriInfoDTO>(
        this.fb,
        evento.ulterioriInfo || new UlterioriInfoDTO(),
        ['id', 'externalId', 'properties'],
        INPUT_REGEX,
        50,
        false
      ),
    });
  }

  private setupValutazioneCalculation(): void {
    const probabilitaControl = this.formGroup.get('probabilita');
    const impattoControl = this.formGroup.get('impatto');
    const controlliControl = this.formGroup.get('controlli');

    // Subscribe to changes in probabilita, impatto, and controlli
    [probabilitaControl, impattoControl, controlliControl].forEach((control) => {
      control?.valueChanges.subscribe(() => {
        this.calculateValutazione();
      });
    });

    // Calculate initial value
    this.calculateValutazione();
  }

  private calculateValutazione(): void {
    const probabilita = this.formGroup.get('probabilita')?.value;
    const impatto = this.formGroup.get('impatto')?.value;
    const controlli = this.formGroup.get('controlli')?.value;
    const valutazioneControl = this.formGroup.get('valutazione');

    // Check if all three fields are filled
    if (probabilita && impatto && controlli) {
      const prob = parseFloat(probabilita);
      const imp = parseFloat(impatto);
      const ctrl = parseFloat(controlli);

      if (!isNaN(prob) && !isNaN(imp) && !isNaN(ctrl)) {
        const result = prob * imp * ctrl;
        valutazioneControl?.setValue(result.toString(), { emitEvent: false });
      } else {
        valutazioneControl?.setValue(null, { emitEvent: false });
      }
    } else {
      valutazioneControl?.setValue(null, { emitEvent: false });
    }
  }

  handleModalDeleteOpened(): void {
    this.main = 'main-double-modal';
    this.addInfoModalOpened.emit();
  }

  handleModalDeleteClosed(): void {
    this.main = 'main';
    this.addInfoModalClosed.emit();
  }
}
