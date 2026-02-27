import { Component, EventEmitter, HostBinding, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { trigger, state, style, transition, animate } from '@angular/animations';
import { SharedModule } from '../../module/shared/shared.module';

@Component({
  selector: 'piao-accordion',
  imports: [SharedModule],
  templateUrl: './accordion.component.html',
  styleUrl: './accordion.component.scss',
  animations: [
    trigger('expandCollapse', [
      state(
        'collapsed',
        style({
          height: '0',
          opacity: '0',
          overflow: 'hidden',
        })
      ),
      state(
        'expanded',
        style({
          height: '*',
          opacity: '1',
          overflow: 'visible',
        })
      ),
      transition('collapsed <=> expanded', [animate('300ms ease-in-out')]),
    ]),
    trigger('rotateIcon', [
      state(
        'collapsed',
        style({
          transform: 'rotate(0deg)',
        })
      ),
      state(
        'expanded',
        style({
          transform: 'rotate(180deg)',
        })
      ),
      transition('collapsed <=> expanded', [animate('300ms ease-in-out')]),
    ]),
  ],
})
export class AccordionComponent {
  @Input() title: string = '';
  @Input() index: number = 1;
  @Input() isButton: boolean = false;
  @Input() isOpen: boolean = false;
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

  get animationState(): string {
    return this.isOpen ? 'expanded' : 'collapsed';
  }

  handleClickButton(): void {
    this.clickButton.emit();
  }
}
