import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { ModalDeleteComponent } from '../modal-delete/modal-delete.component';
import { BaseComponent } from '../base/base.component';
import { FormGroup, FormBuilder, Validators, FormArray } from '@angular/forms';
import { DynamicTBConfig } from '../../models/classes/config/dynamic-tb';
import { ToastService } from '../../services/toast.service';
import { INPUT_REGEX } from '../../utils/constants';
import { isEmpty } from '../../utils/utils';
import { DropdownComponent } from '../dropdown/dropdown.component';
import { LabelValue } from '../../models/interfaces/label-value';

@Component({
  selector: 'piao-dynamic-dropdown',
  imports: [SharedModule, DropdownComponent, ModalDeleteComponent],
  templateUrl: './dynamic-dropdown.component.html',
  styleUrl: './dynamic-dropdown.component.scss',
})
export class DynamicDropdownComponent extends BaseComponent implements OnInit {
  @Input() title!: string;
  @Input() subTitle!: string;
  @Input() dynamicTBConfig!: DynamicTBConfig;
  @Input() form!: FormGroup;
  @Input() titleCardInfo!: string;
  @Input() titleLoadNewForm!: string;
  @Input() nameArrayForm!: string;
  @Input() removeForm: string = 'BUTTONS.REMOVE';
  @Input() addButton: string = 'BUTTONS.ADD_CONTRIBUTOR';
  @Input() notFoundMessage!: string;
  @Input() modalType!: string;
  @Input() showAddCardInfo: boolean = true;
  @Output() fieldAdded = new EventEmitter<void>();
  @Output() closed = new EventEmitter<void>();
  @Input() containerInputClass: string = 'container-width-21';
  @Input() isFirstRemove: boolean = true;
  @Input() isAddButtonInContainer: boolean = false;
  @Input() dropdown: LabelValue[] = [];
  @Output() modalDeleteOpened = new EventEmitter<void>();
  @Output() modalDeleteClosed = new EventEmitter<void>();
  @Input() columnClass: string = 'col-12 col-lg-6 col-xl-4';
  openModal: boolean = false;
  iconPencil: string = 'Pencil';
  iconStyle: string = 'icon-modal';

  openModalDelete: boolean = false;
  elementToDelete: any = null;

  isMongo!: boolean;

  fb: FormBuilder = inject(FormBuilder);
  toastService: ToastService = inject(ToastService);

  ngOnInit(): void {
    this.isMongo = this.dynamicTBConfig.controlTBMongo ? true : false;
    console.log(this.form);
    console.log(this.arrayForm);
  }

  private addNewForm(tb: string, tm: string | undefined) {
    if (tm) {
      return this.fb.group({
        [tb]: [null, [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)]],
        [tm]: [
          this.modalType
            ? this.child.formGroup.get('key')?.value || null
            : this.dynamicTBConfig.labelTB + ' ' + (this.arrayForm.length + 1) || null,
          [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
        ],
      });
    }

    return this.fb.group({
      [tb]: [null, [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)]],
    });
  }

  handleClickNewForm() {
    const arr = this.arrayForm;
    const last = arr.at(arr.length - 1) as FormGroup | undefined;
    console.log(arr);

    const isComplete = last && !this.isGroupComplete(last);

    const mark = last?.markAllAsTouched();

    if (this.modalType) {
      if (isComplete) {
        mark;
        this.openModal = false;
        this.toastService.warning(
          "Impossibile aggiungere un nuovo elemento. Compilare tutti i campi dell'ultimo elemento prima di procedere."
        );

        return;
      }
      this.openModal = true;
    } else {
      if (isComplete) {
        mark;
        this.toastService.warning(
          "Impossibile aggiungere un nuovo elemento. Compilare tutti i campi dell'ultimo elemento prima di procedere."
        );

        return;
      } else {
        this.arrayForm.push(
          this.addNewForm(this.dynamicTBConfig.controlTB, this.dynamicTBConfig.controlTBMongo)
        );
      }
    }
  }

  handleAddNewForm() {
    this.arrayForm.push(
      this.addNewForm(this.dynamicTBConfig.controlTB, this.dynamicTBConfig.controlTBMongo)
    );
    this.openModal = false;
    this.fieldAdded.emit();
  }

  handleRemoveForm(index: number) {
    this.arrayForm.removeAt(index);

    // Ricalcola i valori del campo tm per tutti gli elementi rimanenti
    if (!this.modalType && this.dynamicTBConfig.controlTBMongo) {
      this.arrayForm.controls.forEach((control, i) => {
        const formGroup = control as FormGroup;
        const tmControl = formGroup.get(this.dynamicTBConfig.controlTBMongo!);
        if (tmControl) {
          const newValue = this.dynamicTBConfig.labelTB + ' ' + (i + 1);
          tmControl.setValue(newValue);
        }
      });
    }

    this.openModalDelete = false;
    this.elementToDelete = null;
    this.modalDeleteClosed.emit();
  }

  private isGroupComplete(group: FormGroup): boolean {
    const tb = group.get(this.dynamicTBConfig.controlTB)?.value;
    return !isEmpty(tb);
  }

  handleOpenModalDelete(element: any) {
    this.openModalDelete = true;
    this.elementToDelete = element;
    this.modalDeleteOpened.emit();
  }
  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = null;
    this.modalDeleteClosed.emit();
  }

  get arrayForm(): FormArray {
    return this.form.get(this.nameArrayForm) as FormArray;
  }
}
