import { Component, Input, OnInit, OnChanges, SimpleChanges, OnDestroy } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { TooltipComponent } from '../tooltip/tooltip.component';
import { OVPStakeHolderDTO } from '../../models/classes/ovp-stakeholder-dto';
import { OVPAreaOrganizzativaDTO } from '../../models/classes/ovp-area-organizzativa-dto';
import { OVPPrioritaPoliticaDTO } from '../../models/classes/ovp-priorita-politica-dto';
import { Subscription } from 'rxjs';
import { INPUT_REGEX, ONLY_NUMBERS_REGEX, URL_REGEX } from '../../utils/constants';

@Component({
  selector: 'piao-text-box',
  imports: [SharedModule, ReactiveFormsModule, TooltipComponent],
  templateUrl: './text-box.component.html',
  styleUrl: './text-box.component.scss',
  host: {
    '[class]': 'containerClass',
  },
})
export class TextBoxComponent implements OnInit, OnChanges, OnDestroy {
  @Input() label!: string;
  @Input() additionalLabel!: string;
  @Input() typeInput!: string;
  @Input() id: string = 'id';
  @Input() control!: FormControl;
  @Input() inputClass!: string;
  @Input() maxValidatorLength: number = 0;
  @Input() descTooltip!: string;
  @Input() isLabelTranslate: boolean = true;
  @Input() isReadOnly: boolean = false;
  @Input() isDetails: boolean = false;
  @Input() required?: boolean = false; // this only renders: * after label
  @Input() valueClass = 'value';
  @Input() containerClass: string = '';

  value!: any;
  isArray = false;
  private valueChangesSubscription?: Subscription;

  ngOnInit() {
    this.updateValue();
    this.subscribeToControlChanges();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['control'] && !changes['control'].firstChange) {
      this.updateValue();
      this.subscribeToControlChanges();
    }
  }

  private updateValue() {
    const v = this.control?.value;
    this.isArray = Array.isArray(v);
    this.value = v;
  }

  private subscribeToControlChanges() {
    this.valueChangesSubscription?.unsubscribe();
    if (this.control) {
      this.valueChangesSubscription = this.control.valueChanges.subscribe((value) => {
        // Sanifica il valore: trasforma stringhe vuote o solo spazi in null
        if (typeof value === 'string' && value.trim() === '') {
          this.control.setValue(null, { emitEvent: false });
        }
        this.updateValue();
      });
    }
  }

  getItemString(item: any): string {
    let response: string = '';
    let x = null;
    switch (this.id) {
      case 'stakeholders':
        x = item as OVPStakeHolderDTO;
        response = x.stakeholder?.nomeStakeHolder + ' - ' + x.stakeholder?.relazionePA;
        break;
      case 'area':
        x = item as OVPAreaOrganizzativaDTO;
        response = x.areaOrganizzativa?.nomeArea || '';
        break;
      case 'prioritaPolitica':
        x = item as OVPPrioritaPoliticaDTO;
        response = x.prioritaPolitica?.nomePrioritaPolitica || '';
        break;

      default:
        break;
    }
    return response;
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
        case URL_REGEX:
          patternForMessage = '(https://www.example.com)';
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

  ngOnDestroy() {
    this.valueChangesSubscription?.unsubscribe();
  }
}
