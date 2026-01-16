import { Component, Input } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { SvgComponent } from '../svg/svg.component';

@Component({
  selector: 'piao-tooltip',
  imports: [SharedModule, SvgComponent],
  templateUrl: './tooltip.component.html',
  styleUrl: './tooltip.component.scss',
})
export class TooltipComponent {
  @Input() descTooltip!: string;
  @Input() icon!: string;
}
