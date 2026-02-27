import { Component, ElementRef, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { SharedModule } from '../../module/shared/shared.module';
import { TooltipComponent } from '../tooltip/tooltip.component';
import { SvgComponent } from '../svg/svg.component';
import { Subscription } from 'rxjs';

@Component({
  selector: 'piao-date-picker',
  imports: [SharedModule, ReactiveFormsModule, TooltipComponent, SvgComponent],
  templateUrl: './date-picker.component.html',
  styleUrls: ['./date-picker.component.scss'],
  host: {
    '[class]': 'containerClass',
  },
})
export class DatePickerComponent implements OnInit, OnDestroy {
  @Input() label!: string;
  @Input() id: string = 'datePicker';
  @Input() control!: FormControl;
  @Input() placeholder: string = 'gg/mm/aaaa';
  @Input() descTooltip!: string;
  @Input() isLabelTranslate: boolean = true;
  @Input() isReadOnly: boolean = false;
  @Input() required: boolean = false;
  @Input() containerClass: string = '';
  @Input() min?: string;
  @Input() max?: string;
  @Input() minErrorMsg?: string;
  @Input() maxErrorMsg?: string;

  @ViewChild('hiddenDateInput') hiddenDateInput!: ElementRef<HTMLInputElement>;

  displayValue: string = '';
  errorMessage: string = '';
  private sub?: Subscription;

  ngOnInit(): void {
    // Sincronizza il valore iniziale del FormControl -> display
    if (this.control?.value) {
      const normalized = this.normalizeToIsoDate(this.control.value);
      if (normalized !== this.control.value) {
        this.control.setValue(normalized, { emitEvent: false });
      }
      this.displayValue = this.toDisplayFormat(normalized);
    }
    // Ascolta i cambiamenti del FormControl (es. patchValue esterno)
    this.sub = this.control?.valueChanges.subscribe((val) => {
      if (!val) {
        this.displayValue = '';
        return;
      }
      const normalized = this.normalizeToIsoDate(val);
      if (normalized !== val) {
        this.control.setValue(normalized, { emitEvent: false });
      }
      this.displayValue = this.toDisplayFormat(normalized);
    });
  }

  openPicker(): void {
    this.hiddenDateInput?.nativeElement?.showPicker();
  }

  onHiddenDateChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const isoValue = input.value; // yyyy-MM-dd
    this.control.setValue(isoValue);
    this.control.markAsTouched();
    this.control.markAsDirty();
    this.displayValue = isoValue ? this.toDisplayFormat(isoValue) : '';
    this.validateMinMax(isoValue);
  }

  onDisplayInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const inputEvent = event as InputEvent;
    const isDeleting = inputEvent.inputType?.startsWith('delete');
    let raw = input.value;

    // Rimuovi tutto tranne numeri e /
    raw = raw.replace(/[^\d/]/g, '');

    // Auto-inserisci / solo se l'utente sta digitando (non cancellando)
    if (!isDeleting) {
      const digits = raw.replace(/\//g, '');
      if (digits.length >= 4) {
        raw = digits.slice(0, 2) + '/' + digits.slice(2, 4) + '/' + digits.slice(4, 8);
      } else if (digits.length >= 2) {
        raw = digits.slice(0, 2) + '/' + digits.slice(2);
      } else {
        raw = digits;
      }
    }

    this.displayValue = raw;
    input.value = raw;

    // Se l'utente ha digitato una data completa gg/mm/aaaa, aggiorna il FormControl
    const match = raw.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
    if (match) {
      const [, dd, mm, yyyy] = match;
      const isoDate = `${yyyy}-${mm}-${dd}`;
      this.control.setValue(isoDate);
      this.control.markAsTouched();
      this.control.markAsDirty();
      this.validateMinMax(isoDate);
    } else {
      // Se il campo è vuoto o la data non è completa, resetta il FormControl
      this.errorMessage = '';
      if (!raw) {
        this.control.setValue(null);
        this.control.markAsTouched();
        this.control.markAsDirty();
        // Rimuovi errore invalidDate se presente
        if (this.control.errors) {
          const { invalidDate, ...rest } = this.control.errors;
          this.control.setErrors(Object.keys(rest).length ? rest : null);
        }
      } else {
        // Data parziale: setta errore invalidDate
        this.control.markAsTouched();
        this.control.markAsDirty();
        this.control.setErrors({ ...this.control.errors, invalidDate: true });
      }
    }
  }

  /** Valida la data rispetto a min e max */
  private validateMinMax(isoDate: string): void {
    this.errorMessage = '';
    if (!isoDate) return;

    if (this.min && isoDate < this.min) {
      this.errorMessage =
        this.minErrorMsg || `La data non può essere anteriore al ${this.toDisplayFormat(this.min)}`;
      this.control.setErrors({ ...this.control.errors, minDate: true });
    } else if (this.max && isoDate > this.max) {
      this.errorMessage =
        this.maxErrorMsg ||
        `La data non può essere successiva al ${this.toDisplayFormat(this.max)}`;
      this.control.setErrors({ ...this.control.errors, maxDate: true });
    } else {
      // Rimuovi errori minDate/maxDate se presenti
      if (this.control.errors) {
        const { minDate, maxDate, ...rest } = this.control.errors;
        this.control.setErrors(Object.keys(rest).length ? rest : null);
      }
    }
  }

  /**
   * Normalizza un valore Date, stringa ISO datetime o stringa yyyy-MM-dd
   * nel formato yyyy-MM-dd atteso dal componente.
   */
  private normalizeToIsoDate(value: any): string {
    if (value instanceof Date) {
      const yyyy = value.getFullYear();
      const mm = String(value.getMonth() + 1).padStart(2, '0');
      const dd = String(value.getDate()).padStart(2, '0');
      return `${yyyy}-${mm}-${dd}`;
    }
    if (typeof value === 'string' && value.includes('T')) {
      return value.split('T')[0];
    }
    return value;
  }

  /** Converte yyyy-MM-dd -> dd/MM/yyyy. Se non è una data valida, restituisce '' */
  private toDisplayFormat(isoDate: string): string {
    if (!isoDate) return '';
    const match = isoDate.match(/^(\d{4})-(\d{2})-(\d{2})$/);
    if (!match) return '';
    return `${match[3]}/${match[2]}/${match[1]}`;
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }
}
