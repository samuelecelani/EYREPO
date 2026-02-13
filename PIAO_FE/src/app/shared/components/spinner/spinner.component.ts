
import { ChangeDetectorRef, Component } from '@angular/core';
import { SpinnerService } from '../../services/spinner.service';
import { SharedModule } from '../../module/shared/shared.module';

@Component({
    selector: 'piao-spinner',
    templateUrl: './spinner.component.html',
    styleUrl: './spinner.component.scss',
    imports: [SharedModule]
})
export class SpinnerComponent {
  isLoading = false;

  constructor(private spinnerService: SpinnerService, private cd: ChangeDetectorRef) {
    this.spinnerService.loading$.subscribe(state => {
      this.isLoading = state;
      this.cd.markForCheck();
    });
  }
}
