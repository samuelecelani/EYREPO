import {
  Component,
  EventEmitter,
  Input,
  Output,
  ViewChild,
  ChangeDetectorRef,
  NgZone,
  inject,
} from '@angular/core';
import { FormArray, FormGroup } from '@angular/forms';
import { SharedModule } from '../../module/shared/shared.module';
import { DynamicDropdownComponent } from '../dynamic-dropdown/dynamic-dropdown.component';
import { DynamicTBConfig } from '../../models/classes/config/dynamic-tb';
import { LabelValue } from '../../models/interfaces/label-value';
import { TooltipComponent } from '../tooltip/tooltip.component';

@Component({
  selector: 'piao-dynamic-dropdown-fields',
  imports: [SharedModule, TooltipComponent, DynamicDropdownComponent],
  templateUrl: './dynamic-dropdown-fields.component.html',
  styleUrl: './dynamic-dropdown-fields.component.scss',
})
export class DynamicDropdownFieldsComponent {
  @Input() formGroup!: FormGroup;
  @Input() dynamicTBConfig!: DynamicTBConfig;
  @Input() nameArrayForm: string = 'properties';
  @Input() containerInputClass: string = 'container-width-28';
  @Input() titleLabel: string = '';
  @Input() notFoundLabel: string = 'Non ci sono elementi';
  @Input() addFieldLabel: string = 'Aggiungi elemento';
  @Input() propertiesPath: string = 'properties';
  @Input() dropdown: LabelValue[] = [];
  @Input() removeForm: string = 'BUTTONS.REMOVE';
  @Input() isFirstRemove: boolean = true;
  @Input() isTooltip: boolean = false;
  @Input() columnClass: string = 'col-12 col-lg-6 col-xl-4';

  // Output per notificare il parent quando si apre/chiude la modale interna
  @Output() modalOpened = new EventEmitter<void>();
  @Output() modalClosed = new EventEmitter<void>();
  @Output() modalDeleteOpened = new EventEmitter<void>();
  @Output() modalDeleteClosed = new EventEmitter<void>();

  @ViewChild(DynamicDropdownComponent) dynamicDropdownComponent!: DynamicDropdownComponent;

  private cdr = inject(ChangeDetectorRef);
  private ngZone = inject(NgZone);

  handleAddNewField() {
    if (this.dynamicDropdownComponent) {
      this.ngZone.run(() => {
        this.dynamicDropdownComponent.handleClickNewForm();
      });
    }
  }

  handleResetModal() {
    this.ngZone.run(() => {
      this.modalClosed.emit();
      this.cdr.detectChanges();
    });
  }

  handleModalDeleteOpened() {
    this.modalDeleteOpened.emit();
  }

  handleModalDeleteClosed() {
    this.modalDeleteClosed.emit();
  }

  get properties() {
    return this.formGroup.get(this.propertiesPath) as FormArray;
  }
}
