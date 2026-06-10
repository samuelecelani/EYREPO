import { Component, Input } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { LabelValue } from '../../models/interfaces/label-value';
import { FormControl } from '@angular/forms';

@Component({
  selector: 'piao-button-list',
  imports: [SharedModule],
  templateUrl: './button-list.component.html',
  styleUrl: './button-list.component.scss',
})
export class ButtonListComponent {
  @Input() label!: string;
  @Input() buttonOptions!: LabelValue[];
  @Input() required?: boolean = false;
  @Input() multiSelect?: boolean = false;
  @Input() control!: FormControl;

  handleSelectedRadioProblem(value: number) {
    this.control.setValue(value);
  }

  handleSelectedCheckboxProblem(value: number) {
    if (value !== null && this.control.value === null) {
      this.control.setValue([value]);
      return;
    }
    if (!this.control.value.includes(value)) {
      this.control.setValue([...this.control.value, value]);
    } else {
      this.control.setValue(this.control.value.filter((v: number) => v !== value));
    }
  }

  isSelectedCheckbox(id: number): boolean {
    if (this.control.value === null) {
      return false;
    } else if (this.control.value.includes(id)) {
      return true;
    }
    return false;
  }

  // usa ngOnChanges

  isSelectedRadio(id: number): boolean {
    if (this.control.value === null) {
      return false;
    } else if (this.control.value === id) {
      return true;
    }
    return false;
  }
}
