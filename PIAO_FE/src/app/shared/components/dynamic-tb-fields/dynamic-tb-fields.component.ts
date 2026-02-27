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
import { DynamicTBComponent } from '../dynamic-tb/dynamic-tb.component';
import { DynamicTBConfig } from '../../models/classes/config/dynamic-tb';
import { TooltipComponent } from '../tooltip/tooltip.component';

@Component({
  selector: 'piao-dynamic-tb-fields',
  imports: [SharedModule, DynamicTBComponent, TooltipComponent],
  templateUrl: './dynamic-tb-fields.component.html',
  styleUrl: './dynamic-tb-fields.component.scss',
})
export class DynamicTBFieldsComponent {
  @Input() formGroup!: FormGroup;
  @Input() dynamicTBConfig!: DynamicTBConfig;
  @Input() nameArrayForm: string = 'properties';
  @Input() modalType?: string;
  @Input() containerInputClass: string = 'container-width-18-5';
  @Input() titleLabel?: string;
  @Input() notFoundLabel: string = 'Non ci sono elementi';
  @Input() addFieldLabel: string = 'Aggiungi elemento';
  @Input() propertiesPath: string = 'properties';
  @Input() isTooltip: boolean = false;
  @Input() isRequired: boolean = false;

  // Output per notificare il parent quando si apre/chiude la modale interna
  @Output() modalOpened = new EventEmitter<void>();
  @Output() modalClosed = new EventEmitter<void>();
  @Output() modalDeleteOpened = new EventEmitter<void>();
  @Output() modalDeleteClosed = new EventEmitter<void>();
  @Input() columnClass: string = 'col-12 col-lg-6 col-xl-4';

  @ViewChild(DynamicTBComponent) dynamicTbComponent!: DynamicTBComponent;

  private cdr = inject(ChangeDetectorRef);
  private ngZone = inject(NgZone);

  handleAddNewField() {
    if (this.dynamicTbComponent) {
      this.ngZone.run(() => {
        this.modalOpened.emit();
        this.cdr.detectChanges();
        this.dynamicTbComponent.handleClickNewForm();
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
