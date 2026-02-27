import {
  Component,
  EventEmitter,
  forwardRef,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  HostListener,
  ElementRef,
} from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { LabelValue } from '../../models/interfaces/label-value';
import { ControlValueAccessor, FormControl, NG_VALUE_ACCESSOR } from '@angular/forms';
import { TooltipComponent } from '../tooltip/tooltip.component';
import { TranslateService } from '@ngx-translate/core';

type LabelValueWithChildren = LabelValue & { children?: LabelValueWithChildren[] };
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
  host: {
    '[class]': 'dropContainerClass',
  },
})
export class DropdownComponent implements ControlValueAccessor, OnInit, OnChanges {
  @Input() dropdown!: LabelValueWithChildren[];
  @Input() formControlName: string = 'dropdown';
  @Input() formControl!: FormControl;
  @Input() title!: string;
  @Input() additionalLabel!: string;
  @Input() dropContainerClass: string = 'piao-modal-redigi-drop-container';
  @Input() dropLabelClass: string = 'label';
  @Input() dropClass: string = 'piao-modal-redigi-drop';
  @Output() isChange: EventEmitter<any> = new EventEmitter<any>();
  @Input() firstValue!: any;
  @Input() isReadOnly: boolean = false;
  @Input() isTooltip: boolean = false;
  @Input() isTranslate: boolean = true;
  @Input() isRequired: boolean = false;

  // Nuovi input per multi-select con gerarchia
  @Input() isMulti: boolean = false;
  @Input() hasHierarchy: boolean = false;
  @Input() showSelectAll: boolean = false;
  @Input() selectAllLabel: string = 'Seleziona tutti';

  value: string | string[] | null = null;
  disabled = false;
  selectedValues: Set<string> = new Set();
  isDropdownOpen = false;
  openDirection: 'up' | 'down' = 'down';

  private onChange: (value: any) => void = () => {};
  private onTouched: () => void = () => {};

  constructor(
    private translate: TranslateService,
    private elementRef: ElementRef
  ) {}

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    // Chiude il dropdown multiselect se il click è fuori dal componente
    if (
      this.isMulti &&
      this.isDropdownOpen &&
      !this.elementRef.nativeElement.contains(event.target)
    ) {
      this.isDropdownOpen = false;
    }
  }
  ngOnInit(): void {
    if (this.firstValue) {
      this.value = this.firstValue;
      if (this.isMulti && Array.isArray(this.firstValue)) {
        this.selectedValues = new Set(this.firstValue);
      }
    }
    if (this.formControl && this.formControl.value && this.formControl.value !== null) {
      this.value = this.formControl.value;
      if (this.isMulti && Array.isArray(this.formControl.value)) {
        this.selectedValues = new Set(this.formControl.value);
      }
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Quando cambiano le opzioni del dropdown, ri-sincronizza il valore del select
    // con il valore attuale del FormControl. Questo risolve il caso in cui
    // il valore non cambia (es. null → null) ma le opzioni sì.
    if (changes['dropdown'] && !changes['dropdown'].firstChange) {
      if (this.formControl) {
        this.value = this.formControl.value ?? '';
      }
    }
  }

  writeValue(value: any): void {
    this.value = value;
    if (this.isMulti && Array.isArray(value)) {
      this.selectedValues = new Set(value);
    }
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
    if (this.isMulti) {
      return; // In multi-select mode, use checkbox logic instead
    }
    let val: any = (event.target as HTMLSelectElement).value;

    // Trova l'opzione selezionata nell'array dropdown
    const selectedOption = this.dropdown?.find((opt) => String(opt.value) === val);

    // Se l'opzione originale ha un value di tipo Number, converti val in Number
    if (selectedOption && typeof selectedOption.value === 'number') {
      val = Number(val);
    }

    this.value = val;
    this.onChange(val);
    this.onTouched();
    this.isChange.emit(val);
    if (this.formControl) {
      this.formControl.setValue(val);
    }
  }

  toggleDropdown(): void {
    if (!this.disabled && this.isMulti) {
      this.isDropdownOpen = !this.isDropdownOpen;

      if (this.isDropdownOpen) {
        // Calcola se c'è spazio sotto per aprire il dropdown
        const trigger = this.elementRef.nativeElement.querySelector('.multi-select-trigger');
        if (trigger) {
          const rect = trigger.getBoundingClientRect();
          const dropdownHeight = 250; // max-height del dropdown
          const viewportHeight = window.innerHeight;
          const spaceBelow = viewportHeight - rect.bottom;
          const spaceAbove = rect.top;

          // Se c'è abbastanza spazio sotto, apri sotto; altrimenti apri sopra
          this.openDirection =
            spaceBelow >= dropdownHeight || spaceBelow >= spaceAbove ? 'down' : 'up';
        }
      }
    }
  }

  getSelectedLabel(): string {
    if (this.isMulti) {
      if (this.selectedValues.size === 0) return '';
      const count = this.selectedValues.size;
      return count === 1 ? '1 elemento selezionato' : `${count} elementi selezionati`;
    }

    // Single select logic
    if (!this.value || !this.dropdown) {
      return '';
    }
    const selected = this.dropdown.find((opt) => opt.value === this.value);
    if (!selected) {
      return '';
    }
    return this.isTranslate ? this.translate.instant(selected.label) : selected.label;
  }

  onCheckboxChange(option: LabelValueWithChildren, event: Event): void {
    event.stopPropagation();
    const checked = (event.target as HTMLInputElement).checked;

    if (checked) {
      this.selectOption(option);
    } else {
      this.deselectOption(option);
    }

    this.updateValue();
  }

  private selectOption(option: LabelValueWithChildren): void {
    this.selectedValues.add(option.value);

    // Se ha figli (padre), seleziona automaticamente tutti i figli
    if (this.hasHierarchy && option.children && option.children.length > 0) {
      this.selectAllChildren(option.children);
    }
  }

  private selectAllChildren(children: LabelValueWithChildren[]): void {
    children.forEach((child) => {
      this.selectedValues.add(child.value);
      if (child.children && child.children.length > 0) {
        this.selectAllChildren(child.children);
      }
    });
  }

  private deselectOption(option: LabelValueWithChildren): void {
    this.selectedValues.delete(option.value);

    // Se ha figli, deseleziona anche tutti i figli
    if (this.hasHierarchy && option.children && option.children.length > 0) {
      this.deselectAllChildren(option.children);
    }
  }

  private deselectAllChildren(children: LabelValueWithChildren[]): void {
    children.forEach((child) => {
      this.selectedValues.delete(child.value);
      if (child.children && child.children.length > 0) {
        this.deselectAllChildren(child.children);
      }
    });
  }

  isChecked(value: string): boolean {
    return this.selectedValues.has(value);
  }

  private updateValue(): void {
    const values = Array.from(this.selectedValues);
    this.value = values;
    this.onChange(values);
    this.onTouched();
    this.isChange.emit(values);
    if (this.formControl) {
      this.formControl.setValue(values);
    }
  }

  onSelectAll(event: Event): void {
    event.stopPropagation();
    const checked = (event.target as HTMLInputElement).checked;

    if (checked) {
      this.selectAllOptions();
    } else {
      this.selectedValues.clear();
    }

    this.updateValue();
  }

  private selectAllOptions(): void {
    if (Array.isArray(this.dropdown)) {
      this.dropdown.forEach((option) => {
        this.selectedValues.add(option.value);
        if (this.hasHierarchy && (option as LabelValueWithChildren).children) {
          this.selectAllChildren((option as LabelValueWithChildren).children!);
        }
      });
    }
  }

  isAllSelected(): boolean {
    if (!Array.isArray(this.dropdown)) return false;
    const allValues = this.getAllValues(this.dropdown);
    return allValues.length > 0 && allValues.every((v) => this.selectedValues.has(v));
  }

  private getAllValues(options: LabelValueWithChildren[]): string[] {
    const values: string[] = [];
    options.forEach((opt) => {
      values.push(opt.value);
      if (opt.children && opt.children.length > 0) {
        values.push(...this.getAllValues(opt.children));
      }
    });
    return values;
  }

  closeDropdown(): void {
    this.isDropdownOpen = false;
  }

  getIndentClass(level: number): string {
    return `indent-level-${level}`;
  }
}
