import { Component, inject, Input } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { CardInfoComponent } from '../../ui/card-info/card-info.component';
import { TextBoxComponent } from '../text-box/text-box.component';
import { SvgComponent } from '../svg/svg.component';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { INPUT_REGEX } from '../../utils/constants';
import { DynamicTBTBConfig } from '../../models/classes/config/dynamic-tbtb-config';
import { isEmpty } from '../../utils/utils';

@Component({
  selector: 'piao-dynamic-tbtb',
  templateUrl: './dynamic-tbtb.component.html',
  styleUrl: './dynamic-tbtb.component.scss',
  imports: [SharedModule, TextBoxComponent, CardInfoComponent, SvgComponent, ReactiveFormsModule],
})
export class DynamicTBTBComponent {
  @Input() title!: string;
  @Input() subTitle!: string;
  @Input() form!: FormGroup;
  @Input() dynamicTBTBConfig!: DynamicTBTBConfig;
  @Input() titleCardInfo!: string;
  @Input() titleLoadNewForm!: string;
  @Input() nameArrayForm!: string;
  @Input() nameIdSezione!: string;
  @Input() idSezione!: number;

  fb: FormBuilder = inject(FormBuilder);

  private addNewForm(tb: string, tb2: string) {
    return this.fb.group({
      id: [null, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
      [this.nameIdSezione]: [
        this.idSezione || null,
        [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
      ],
      [tb]: [null, [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)]],
      [tb2]: [null, [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)]],
    });
  }

  handleClickNewForm() {
    const arr = this.arrayForm;
    const last = arr.at(arr.length - 1) as FormGroup | undefined;

    if (last && !this.isGroupComplete(last)) {
      last.markAllAsTouched();
      return;
    }
    this.arrayForm.push(
      this.addNewForm(this.dynamicTBTBConfig.controlTB, this.dynamicTBTBConfig.controlTB2)
    );
  }

  handleRemoveForm(index: number) {
    this.arrayForm.removeAt(index);
  }

  private isGroupComplete(group: FormGroup): boolean {
    const tb = group.get(this.dynamicTBTBConfig.controlTB)?.value;
    const tb2 = group.get(this.dynamicTBTBConfig.controlTB2)?.value;

    return !isEmpty(tb) && !isEmpty(tb2);
  }

  get arrayForm(): FormArray {
    return this.form.get(this.nameArrayForm) as FormArray;
  }
}
