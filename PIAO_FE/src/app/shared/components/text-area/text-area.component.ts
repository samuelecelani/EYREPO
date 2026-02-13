import {
  Component,
  Input,
  OnInit,
  OnDestroy,
  AfterViewChecked,
  ChangeDetectorRef,
} from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../../module/shared/shared.module';
import { TooltipComponent } from '../tooltip/tooltip.component';
import { INPUT_REGEX, ONLY_NUMBERS_REGEX } from '../../utils/constants';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'piao-text-area',
  imports: [SharedModule, ReactiveFormsModule, TooltipComponent],
  templateUrl: './text-area.component.html',
  styleUrl: './text-area.component.scss',
  host: {
    '[class]': 'containerClass',
  },
})
export class TextAreaComponent implements OnInit, AfterViewChecked, OnDestroy {
  @Input() title!: string;
  @Input() titleClass: string = 'title';
  @Input() label!: string;
  @Input() id: string = 'id';
  @Input() control!: FormControl;
  @Input() textAreaClass!: string;
  @Input() maxValidatorLength: number = 0;
  @Input() descTooltip!: string;
  @Input() isReadOnly: boolean = false;
  @Input() required?: boolean = false; // this only renders: * after label
  @Input() containerClass: string = '';

  valueLength: number = 0;
  private destroy$ = new Subject<void>();

  constructor(private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    // Sottoscrivi ai cambiamenti del valore per aggiornare sempre il conteggio
    if (this.control) {
      this.control.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(() => {
        this.updateValueLength();
      });
    }
    this.updateValueLength();
  }

  ngAfterViewChecked(): void {
    // Controlla sempre se il valore è cambiato
    this.updateValueLength();
  }

  handleUpdateCountValueLenght(event: Event): void {
    const input = event.target as HTMLTextAreaElement;
    const value = input.value;

    // Sanifica il valore: trasforma stringhe vuote o solo spazi in null
    if (value.trim() === '') {
      this.control.setValue(null);
    }
  }

  private updateValueLength(): void {
    const value = this.control?.value;
    this.valueLength = value !== null && value !== undefined ? value.length : 0;
  }

  get patternError(): string | null {
    if (this.control?.hasError('pattern')) {
      const pattern = this.control.errors?.['pattern']?.requiredPattern;
      let patternForMessage;
      switch (pattern) {
        case ONLY_NUMBERS_REGEX:
          patternForMessage = 'Solo numeri';
          break;
        case INPUT_REGEX:
          patternForMessage = 'Caratteri alfanumerici';
          break;

        default:
          break;
      }
      return pattern
        ? `Formato non valido. Pattern richiesto: ${patternForMessage}`
        : 'Formato non valido.';
    }
    return null;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
