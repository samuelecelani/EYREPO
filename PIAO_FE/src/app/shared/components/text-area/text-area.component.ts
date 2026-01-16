import { Component, Input } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../../module/shared/shared.module';
import { TooltipComponent } from '../tooltip/tooltip.component';

@Component({
  selector: 'piao-text-area',
  imports: [SharedModule, ReactiveFormsModule, TooltipComponent],
  templateUrl: './text-area.component.html',
  styleUrl: './text-area.component.scss',
})
export class TextAreaComponent {
  @Input() title!: string;
  @Input() titleClass: string = 'title';
  @Input() label!: string;
  @Input() id!: string;
  @Input() control!: FormControl;
  @Input() textAreaClass!: string;
  @Input() maxValidatorLength: number = 0;
  @Input() descTooltip!: string;
  @Input() isReadOnly: boolean = false;

  valueLength: number = 0;

  handleUpdateCountValueLenght(event: Event): void {
    const input = event.target as HTMLTextAreaElement;
    this.valueLength = input.value.length;
  }
}
