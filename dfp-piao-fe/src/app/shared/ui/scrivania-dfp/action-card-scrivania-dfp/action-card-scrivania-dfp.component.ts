import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ButtonComponent } from '../../../components/button/button.component';
import { SharedModule } from '../../../module/shared/shared.module';

@Component({
  selector: 'piao-action-card-scrivania-dfp',
  imports: [SharedModule, ButtonComponent],
  templateUrl: './action-card-scrivania-dfp.component.html',
  styleUrl: './action-card-scrivania-dfp.component.scss',
})
export class ActionCardScrivaniaDfpComponent {
  @Input() title!: string;
  @Input() description!: string;
  @Input() ctaLabel!: string;

  @Output() ctaClicked = new EventEmitter<void>();

  onCta(): void {
    this.ctaClicked.emit();
  }
}
