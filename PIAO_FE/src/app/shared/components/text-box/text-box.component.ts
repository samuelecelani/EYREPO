import { Component, Input } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { TooltipComponent } from '../tooltip/tooltip.component';

@Component({
  selector: 'piao-text-box',
  imports: [SharedModule, ReactiveFormsModule, TooltipComponent],
  templateUrl: './text-box.component.html',
  styleUrl: './text-box.component.scss',
})
export class TextBoxComponent {
  @Input() label!: string;
  @Input() typeInput!: string;
  @Input() id!: string;
  @Input() control!: FormControl;
  @Input() inputClass!: string;
  @Input() maxValidatorLength: number = 0;
  @Input() descTooltip!: string;
  @Input() isLabelTranslate: boolean = true;
  @Input() isReadOnly: boolean = false;
  @Input() isDetails: boolean = false;
}
