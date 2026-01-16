import { Component, EventEmitter, forwardRef, Input, OnInit, Output } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { LabelValue } from '../../models/interfaces/label-value';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { TooltipComponent } from '../tooltip/tooltip.component';
@Component({
  selector: 'piao-dropdown',
  imports: [SharedModule, TooltipComponent],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DropdownComponent),
      multi: true,
    },
  ],
  templateUrl: './dropdown.component.html',
  styleUrl: './dropdown.component.scss',
})
export class DropdownComponent implements ControlValueAccessor, OnInit {
  @Input() dropdown!: LabelValue;
  @Input() formControlName!: string;
  @Input() title!: string;
  @Input() dropContainerClass: string = 'piao-modal-redigi-drop-container';
  @Input() dropLabelClass: string = 'label';
  @Input() dropClass: string = 'piao-modal-redigi-drop';
  @Output() isChange: EventEmitter<any> = new EventEmitter<any>();
  @Input() firstValue!: any;
  @Input() isTooltip: boolean = false;

  value: string | null = null;
  disabled = false;

  private onChange: (value: any) => void = () => {};
  private onTouched: () => void = () => {};

  ngOnInit(): void {
    if (this.firstValue) {
      this.value = this.firstValue;
    }
  }

  writeValue(value: any): void {
    this.value = value;
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onSelectChange(event: Event): void {
    const val = (event.target as HTMLSelectElement).value;
    this.value = val;
    this.onChange(val);
    this.onTouched();
    this.isChange.emit(val);
  }
}
