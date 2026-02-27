import { Component, EventEmitter, Input, Output } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { ModalComponent } from '../modal/modal.component';

@Component({
  selector: 'piao-modal-delete',
  imports: [SharedModule, ModalComponent],
  templateUrl: './modal-delete.component.html',
  styleUrl: './modal-delete.component.scss',
})
export class ModalDeleteComponent {
  @Input() icon!: string;
  @Input() title!: string;
  @Input() message!: string;
  @Input() subMessage!: string;
  @Input() openModalDelete: boolean = false;
  @Input() elementToDelete!: any;
  @Output() confirm = new EventEmitter<any>();
  @Output() closed = new EventEmitter<void>();

  iconStyle: string = 'icon-modal';

  handleConfirm(): void {
    console.log('Emitting confirm with element:', this.elementToDelete);
    this.confirm.emit(this.elementToDelete);
  }
  handleClose(): void {
    this.openModalDelete = false;
    this.elementToDelete = null;
    this.closed.emit();
  }
}
