import { Component, inject, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { TextBoxComponent } from '../text-box/text-box.component';
import { DynamicTBConfig } from '../../models/classes/config/dynamic-tb';
import { CardInfoComponent } from '../../ui/card-info/card-info.component';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { INPUT_REGEX } from '../../utils/constants';
import { isEmpty } from '../../utils/utils';
import { ModalNewFieldComponent } from '../../ui/servizi-piao/indice-piao/sezione/sezione-1/modal-new-field/modal-new-field.component';
import { ModalComponent } from '../modal/modal.component';
import { BaseComponent } from '../base/base.component';

@Component({
  selector: 'piao-dynamic-tb',
  imports: [
    SharedModule,
    TextBoxComponent,
    CardInfoComponent,
    ModalComponent,
    ModalNewFieldComponent,
  ],
  templateUrl: './dynamic-tb.component.html',
  styleUrl: './dynamic-tb.component.scss',
})
export class DynamicTBComponent extends BaseComponent implements OnInit {
  @Input() title!: string;
  @Input() subTitle!: string;
  @Input() dynamicTBConfig!: DynamicTBConfig;
  @Input() form!: FormGroup;
  @Input() titleCardInfo!: string;
  @Input() titleLoadNewForm!: string;
  @Input() nameArrayForm!: string;
  @Input() removeForm: string = 'BUTTONS.REMOVE';
  @Input() notFoundMessage!: string;
  @Input() modalType!: string;
  openModal: boolean = false;
  iconPencil: string = 'Pencil';
  iconStyle: string = 'icon-modal';

  isMongo!: boolean;

  fb: FormBuilder = inject(FormBuilder);

  ngOnInit(): void {
    this.isMongo = this.dynamicTBConfig.controlTBMongo ? true : false;
  }

  private addNewForm(tb: string, tm: string | undefined) {
    console.log(this.child.formGroup.get('key')?.value);
    if (tm) {
      return this.fb.group({
        [tb]: [null, [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)]],
        [tm]: [
          this.modalType
            ? this.child.formGroup.get('key')?.value || null
            : this.arrayForm.length + 1 + '°' + this.dynamicTBConfig.labelTB || null,
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
        return;
      }
      this.openModal = true;
    } else {
      if (isComplete) {
        mark;
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
  }

  handleRemoveForm(index: number) {
    this.arrayForm.removeAt(index);
  }

  private isGroupComplete(group: FormGroup): boolean {
    const tb = group.get(this.dynamicTBConfig.controlTB)?.value;
    return !isEmpty(tb);
  }

  get arrayForm(): FormArray {
    return this.form.get(this.nameArrayForm) as FormArray;
  }
}
