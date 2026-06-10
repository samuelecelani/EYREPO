import {
  Component,
  EventEmitter,
  Input,
  Output,
  inject,
  ChangeDetectorRef,
  NgZone,
} from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SharedModule } from '../../module/shared/shared.module';
import { TextBoxComponent } from '../text-box/text-box.component';
import { DatePickerComponent } from '../date-picker/date-picker.component';
import { TooltipComponent } from '../tooltip/tooltip.component';
import { ModalDeleteComponent } from '../modal-delete/modal-delete.component';
import { DynmaicAttivitaConfig } from '../../models/classes/config/dynamic-attivita-config';
import { INPUT_REGEX } from '../../utils/constants';
import { getTodayISO, isEmpty } from '../../utils/utils';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'piao-dynamic-attivita-fields',
  imports: [
    SharedModule,
    TextBoxComponent,
    DatePickerComponent,
    TooltipComponent,
    ModalDeleteComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './dynamic-attivita-fields.component.html',
  styleUrl: './dynamic-attivita-fields.component.scss',
})
export class DynamicAttivitaFieldsComponent {
  @Input() formGroup!: FormGroup;
  @Input() dynamicAttivitaConfig!: DynmaicAttivitaConfig;
  @Input() nameArrayForm: string = 'properties';
  @Input() containerInputClass: string = 'container-width-18-5';
  @Input() titleLabel?: string;
  @Input() notFoundLabel: string = 'Non ci sono elementi';
  @Input() addFieldLabel: string = 'Aggiungi elemento';
  @Input() propertiesPath: string = 'properties';
  @Input() isTooltip: boolean = false;
  @Input() isRequired: boolean = false;
  @Input() isFirstRemove: boolean = true;
  @Input() columnClass: string = 'col-12';

  @Output() modalOpened = new EventEmitter<void>();
  @Output() modalClosed = new EventEmitter<void>();
  @Output() modalDeleteOpened = new EventEmitter<void>();
  @Output() modalDeleteClosed = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private cdr = inject(ChangeDetectorRef);
  private ngZone = inject(NgZone);
  private toastService = inject(ToastService);

  minDate: string = getTodayISO();

  openModalDelete: boolean = false;
  elementToDelete: any = null;

  get properties(): FormArray {
    return this.formGroup.get(this.propertiesPath) as FormArray;
  }

  get arrayForm(): FormArray {
    return this.formGroup.get(this.nameArrayForm) as FormArray;
  }

  handleAddNewField(): void {
    const arr = this.arrayForm;
    const last = arr.at(arr.length - 1) as FormGroup | undefined;

    if (last && !this.isGroupComplete(last)) {
      last.markAllAsTouched();
      this.toastService.warning(
        "Impossibile aggiungere un nuovo elemento. Compilare tutti i campi dell'ultimo elemento prima di procedere."
      );
      return;
    }

    this.cdr.detectChanges();

    const labelKey = this.dynamicAttivitaConfig.labelTB + ' ' + (arr.length + 1);

    arr.push(
      this.fb.group({
        key: [labelKey, [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)]],
        value: [
          null,
          [
            Validators.maxLength(this.dynamicAttivitaConfig.maxValidatorLengthTB),
            Validators.pattern(INPUT_REGEX),
          ],
        ],
        keyDateInizio: [
          this.dynamicAttivitaConfig.labelDataInizio,
          [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
        ],
        keyDateFine: [
          this.dynamicAttivitaConfig.labelDataFine,
          [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
        ],
        valueDateInizio: [null],
        valueDateFine: [null],
      })
    );

    this.cdr.detectChanges();
  }

  handleRemoveForm(index: number): void {
    this.arrayForm.removeAt(index);

    // Ricalcola i valori del campo key per tutti gli elementi rimanenti
    this.arrayForm.controls.forEach((control, i) => {
      const formGroup = control as FormGroup;
      const keyControl = formGroup.get('key');
      if (keyControl) {
        const newValue = this.dynamicAttivitaConfig.labelTB + ' ' + (i + 1);
        keyControl.setValue(newValue);
      }
    });

    this.handleCloseModalDelete();
  }

  handleOpenModalDelete(element: any): void {
    this.openModalDelete = true;
    this.elementToDelete = element;
    this.modalDeleteOpened.emit();
  }

  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = null;
    this.modalDeleteClosed.emit();
  }

  private isGroupComplete(group: FormGroup): boolean {
    const value = group.get('value')?.value;
    return !isEmpty(value);
  }

  getLabelForIndex(index: number): string {
    const lengthArray = this.arrayForm?.controls?.length || 0;
    if (lengthArray > 1) {
      return this.dynamicAttivitaConfig.labelTB + ' ' + (index + 1);
    }
    return this.dynamicAttivitaConfig.labelTB;
  }
}
