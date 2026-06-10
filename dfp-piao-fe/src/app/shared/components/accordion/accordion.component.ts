import { Component, EventEmitter, HostBinding, Input, Output } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';

@Component({
  selector: 'piao-accordion',
  imports: [SharedModule],
  templateUrl: './accordion.component.html',
  styleUrl: './accordion.component.scss',
})
export class AccordionComponent {
  @Input() title: string = '';
  @Input() index: number = 1;
  @Input() isButton: boolean = false;
  @Input() isOpen: boolean = false;
  @Input() isDettaglio: boolean = false;
  @Output() clickButton = new EventEmitter<void>();

  @HostBinding('class.is-open') get openClass() {
    return this.isOpen;
  }

  @HostBinding('class.is-closed') get closedClass() {
    return !this.isOpen;
  }

  toggle(): void {
    this.isOpen = !this.isOpen;
  }

  handleClickButton(): void {
    this.clickButton.emit();
  }
}
