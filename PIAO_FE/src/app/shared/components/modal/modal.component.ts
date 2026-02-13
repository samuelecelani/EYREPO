import { Component, EventEmitter, Input, Output } from '@angular/core';
import { IconComponent } from '../icon/icon.component';
import { SharedModule } from '../../module/shared/shared.module';
import { ButtonComponent } from '../button/button.component';

@Component({
  selector: 'piao-modal',
  imports: [IconComponent, ButtonComponent, SharedModule],
  templateUrl: './modal.component.html',
  styleUrl: './modal.component.scss',
})
export class ModalComponent {
  @Input() icon!: string;
  @Input() isIcon!: boolean;
  @Input() iconStyle!: string;
  @Input() open!: boolean;
  @Input() title!: string;
  @Input() titleButtonConfirm: string = 'BUTTONS.CONFIRM';
  @Input() disabledButtonConfirm!: boolean;
  @Input() showButtonConfirm: boolean = true;
  @Input() showButtonCancel: boolean = true;
  @Input() showButtonNotSave: boolean = false;
  @Input() showButtonClose: boolean = false;
  @Input() showButtonCloseOutline: boolean = false;
  @Output() closed = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<void>();
  @Output() notSave = new EventEmitter<void>();

  handleClose(): void {
    this.closed.emit();
  }

  handleConfirm(): void {
    this.confirm.emit();
  }

  handleNotSave(): void {
    this.notSave.emit();
  }
}
