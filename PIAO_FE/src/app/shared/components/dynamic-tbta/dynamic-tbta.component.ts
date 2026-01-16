import { Component, inject, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { TextBoxComponent } from '../text-box/text-box.component';
import { TextAreaComponent } from '../text-area/text-area.component';
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { DynamicTBTAConfig } from '../../models/classes/config/dynamic-tbta-config';
import { CardInfoComponent } from '../../ui/card-info/card-info.component';
import { INPUT_REGEX } from '../../utils/constants';
import { SvgComponent } from '../svg/svg.component';
import { isEmpty } from '../../utils/utils';

@Component({
  selector: 'piao-dynamic-tbta',
  imports: [
    SharedModule,
    TextBoxComponent,
    TextAreaComponent,
    CardInfoComponent,
    SvgComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './dynamic-tbta.component.html',
  styleUrl: './dynamic-tbta.component.scss',
})
export class DynamicTBTAComponent {
  @Input() title!: string;
  @Input() subTitle!: string;
  @Input() form!: FormGroup;
  @Input() dynamicTBTAConfig!: DynamicTBTAConfig;
  @Input() titleCardInfo!: string;
  @Input() titleLoadNewForm!: string;
  @Input() nameArrayForm!: string;
  @Input() nameIdSezione!: string;
  @Input() idSezione!: number;

  fb: FormBuilder = inject(FormBuilder);

  private addNewForm(tb: string, ta: string) {
    return this.fb.group({
      id: [null, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
      [this.nameIdSezione]: [
        this.idSezione || null,
        [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
      ],
      [tb]: [null, [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)]],
      [ta]: [null, [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]],
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
      this.addNewForm(this.dynamicTBTAConfig.controlTB, this.dynamicTBTAConfig.controlTA)
    );
  }

  handleRemoveForm(index: number) {
    this.arrayForm.removeAt(index);
  }

  private isGroupComplete(group: FormGroup): boolean {
    const tb = group.get(this.dynamicTBTAConfig.controlTB)?.value;
    const ta = group.get(this.dynamicTBTAConfig.controlTA)?.value;

    return !isEmpty(tb) && !isEmpty(ta);
  }

  get arrayForm(): FormArray {
    return this.form.get(this.nameArrayForm) as FormArray;
  }
}
