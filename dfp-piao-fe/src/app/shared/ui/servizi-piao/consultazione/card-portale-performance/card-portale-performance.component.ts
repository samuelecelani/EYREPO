import { Component, inject } from '@angular/core';
import { SharedModule } from '../../../../module/shared/shared.module';
import { IconComponent } from '../../../../components/icon/icon.component';
import { ButtonComponent } from '../../../../components/button/button.component';
import { Router } from '@angular/router';
import { SvgComponent } from '../../../../components/svg/svg.component';
import { getValue } from '../../../../config/loader-config';

@Component({
  selector: 'piao-card-portale-performance',
  imports: [SharedModule, IconComponent, ButtonComponent],
  templateUrl: './card-portale-performance.component.html',
  styleUrl: './card-portale-performance.component.scss',
})
export class CardPortalePerformanceComponent {
  private router = inject(Router);

  handleGoToPortale(): void {
    window.open(getValue('publicUrlPortalePerformance') ?? '', '_blank');
  }
}
