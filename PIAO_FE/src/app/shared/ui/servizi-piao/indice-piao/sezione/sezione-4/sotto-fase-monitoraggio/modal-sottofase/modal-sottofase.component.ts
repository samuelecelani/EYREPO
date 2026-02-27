import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import {
  AbstractControl,
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { SharedModule } from '../../../../../../../module/shared/shared.module';
import { ModalComponent } from '../../../../../../../components/modal/modal.component';
import { TextBoxComponent } from '../../../../../../../components/text-box/text-box.component';
import { TextAreaComponent } from '../../../../../../../components/text-area/text-area.component';
import { DatePickerComponent } from '../../../../../../../components/date-picker/date-picker.component';
import { SvgComponent } from '../../../../../../../components/svg/svg.component';
import { SottofaseMonitoraggioDTO } from '../../../../../../../models/classes/sezione-4-dto';
import { AttoreDTO } from '../../../../../../../models/classes/attore-dto';
import { PropertyDTO } from '../../../../../../../models/classes/property-dto';
import { DATE_REGEX, INPUT_REGEX } from '../../../../../../../utils/constants';
import {
  createFormMongoFromPiaoSession,
  getTodayISO,
  minArrayLength,
} from '../../../../../../../utils/utils';
import { DynamicTBFieldsComponent } from '../../../../../../../components/dynamic-tb-fields/dynamic-tb-fields.component';
import { DynamicTBConfig } from '../../../../../../../models/classes/config/dynamic-tb';

@Component({
  selector: 'piao-modal-sottofase',
  imports: [
    SharedModule,
    ReactiveFormsModule,
    ModalComponent,
    TextBoxComponent,
    TextAreaComponent,
    DatePickerComponent,
    DynamicTBFieldsComponent,
  ],
  templateUrl: './modal-sottofase.component.html',
  styleUrl: './modal-sottofase.component.scss',
})
export class ModalSottofaseComponent {
  @Input() open: boolean = false;
  @Input() sottoFaseToEdit?: SottofaseMonitoraggioDTO;
  @Output() closed = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<SottofaseMonitoraggioDTO>();

  private fb = inject(FormBuilder);

  sottofaseForm!: FormGroup;

  attoriConfig!: DynamicTBConfig;

  minDate: string = getTodayISO();

  // Labels
  labelModalSottofaseTitle: string = 'SEZIONE_4.MODAL_SOTTOFASE.TITLE';
  labelModalSottofaseDesc: string = 'SEZIONE_4.MODAL_SOTTOFASE.DESC';
  labelModalSottofaseCampiObbligatori: string = 'SEZIONE_4.MODAL_SOTTOFASE.CAMPI_OBBLIGATORI';
  labelModalFase: string = 'SEZIONE_4.MODAL_SOTTOFASE.FASE.LABEL';
  labelModalDenominazione: string = 'SEZIONE_4.MODAL_SOTTOFASE.DENOMINAZIONE.LABEL';
  labelModalDescrizioneSottofase: string = 'SEZIONE_4.MODAL_SOTTOFASE.DESCRIZIONE_SOTTOFASE.LABEL';
  labelModalDataInizio: string = 'SEZIONE_4.MODAL_SOTTOFASE.DATA_INIZIO.LABEL';
  labelModalDataFine: string = 'SEZIONE_4.MODAL_SOTTOFASE.DATA_FINE.LABEL';
  labelModalStrumenti: string = 'SEZIONE_4.MODAL_SOTTOFASE.STRUMENTI.LABEL';
  labelModalFonteDato: string = 'SEZIONE_4.MODAL_SOTTOFASE.FONTE_DATO.LABEL';
  labelModalAttoriCoinvolti: string = 'SEZIONE_4.MODAL_SOTTOFASE.ATTORI_COINVOLTI.LABEL';
  labelModalInserisciAttore: string = 'SEZIONE_4.MODAL_SOTTOFASE.ATTORI_COINVOLTI.INSERISCI_ATTORE';
  labelModalBtnAggiungiAttore: string = 'SEZIONE_4.MODAL_SOTTOFASE.BTN_AGGIUNGI_ATTORE';

  main: string = 'main';

  ngOnInit(): void {
    this.initAttoriConfig();
    this.initForm();
  }

  private initForm(): void {
    this.sottofaseForm = this.fb.group({
      // Campo "Fase" solo per UI (non inviato al backend)
      fase: this.fb.control<string | null>('Monitoraggio'),
      denominazione: this.fb.control<string | null>(this.sottoFaseToEdit?.denominazione || null, [
        Validators.maxLength(200),
        Validators.pattern(INPUT_REGEX),
      ]),
      descrizione: this.fb.control<string | null>(this.sottoFaseToEdit?.descrizione || null, [
        Validators.maxLength(500),
        Validators.pattern(INPUT_REGEX),
      ]),
      dataInizio: this.fb.control<any | null>(this.sottoFaseToEdit?.dataInizio || null, [
        Validators.required,
        Validators.pattern(DATE_REGEX),
      ]),
      dataFine: this.fb.control<any | null>(this.sottoFaseToEdit?.dataFine || null, [
        Validators.required,
        Validators.pattern(DATE_REGEX),
      ]),
      strumenti: this.fb.control<string | null>(this.sottoFaseToEdit?.strumenti || null, [
        Validators.maxLength(200),
        Validators.pattern(INPUT_REGEX),
      ]),
      fonteDato: this.fb.control<string | null>(this.sottoFaseToEdit?.fonteDato || null, [
        Validators.maxLength(200),
        Validators.pattern(INPUT_REGEX),
      ]),
    });

    //aggiunta campi mongo
    const attoreGroup = createFormMongoFromPiaoSession<AttoreDTO>(
      this.fb,
      this.sottoFaseToEdit?.attore || new AttoreDTO(),
      ['id', 'externalId', 'properties'],
      INPUT_REGEX,
      50,
      false
    );

    if (attoreGroup) {
      const propertiesArray = attoreGroup.get('properties') as FormArray;
      if (propertiesArray) {
        propertiesArray.setValidators([
          minArrayLength(1),
          (control: AbstractControl) => {
            const arr = control as FormArray;
            const allFilled = arr.controls.every((c) => {
              const val = (c as FormGroup).get('value')?.value;
              return val !== null && val !== undefined && val.toString().trim() !== '';
            });
            return allFilled ? null : { requiredValues: true };
          },
        ]);
        propertiesArray.updateValueAndValidity();
      }
    }

    this.sottofaseForm.addControl('attore', attoreGroup || null);
  }

  handleClose(): void {
    this.resetForm();
    this.closed.emit();
  }

  handleConfirm(): void {
    const formValue = this.sottofaseForm.getRawValue();

    const nuovaSottofase: SottofaseMonitoraggioDTO = {
      denominazione: formValue.denominazione,
      descrizione: formValue.descrizione,
      dataInizio: formValue.dataInizio,
      dataFine: formValue.dataFine,
      strumenti: formValue.strumenti,
      fonteDato: formValue.fonteDato,
      attore: formValue.attore,
    };

    // TODO: Implementare chiamata REST per salvare la sottofase
    // this.sezione4Service.saveSottofase(nuovaSottofase).subscribe({...});

    console.log('=== MODAL SOTTOFASE - DATI INSERITI ===');
    console.log(JSON.stringify(nuovaSottofase, null, 2));
    console.log('=======================================');

    this.confirm.emit(nuovaSottofase);
    this.resetForm();
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

  handleAddModalOpened(): void {
    this.main = 'main-double-modal';
  }

  handleAddModalClosed(): void {
    this.main = 'main';
  }

  private resetForm(): void {
    this.sottofaseForm.reset({ fase: 'Monitoraggio' });
  }
}
