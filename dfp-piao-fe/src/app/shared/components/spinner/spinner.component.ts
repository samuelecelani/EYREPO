import { ChangeDetectorRef, Component } from '@angular/core';
import { SpinnerService } from '../../services/spinner.service';
import { SharedModule } from '../../module/shared/shared.module';

@Component({
  selector: 'piao-spinner',
  templateUrl: './spinner.component.html',
  styleUrl: './spinner.component.scss',
  imports: [SharedModule],
})
export class SpinnerComponent {
  isLoading = false;

  constructor(
    private spinnerService: SpinnerService,
    private cd: ChangeDetectorRef
  ) {
    this.spinnerService.loading$.subscribe((state) => {
      this.isLoading = state;
      if (state && typeof document !== 'undefined') {
        // Chiude eventuali dropdown nativi aperti (es. <select>) togliendo il focus,
        // così l'overlay del loader non viene coperto dal menu nativo del browser.
        const active = document.activeElement as HTMLElement | null;
        active?.blur?.();
      }
      this.cd.markForCheck();
    });
  }
}
