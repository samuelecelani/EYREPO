import { Component, inject, OnInit } from '@angular/core';
import { SharedModule } from '../../../shared/module/shared/shared.module';
import { BaseComponent } from '../../../shared/components/base/base.component';
import { ButtonComponent } from 'src/app/shared/components/button/button.component';
import { ElencoPiaoPubblicatiComponent } from '../../../shared/ui/servizi-piao/consultazione/elenco-piao-pubblicati/elenco-piao-pubblicati.component';
import { Router } from '@angular/router';
import { CardPortalePerformanceComponent } from '../../../shared/ui/servizi-piao/consultazione/card-portale-performance/card-portale-performance.component';

@Component({
  selector: 'piao-consultazione',
  imports: [
    SharedModule,
    ButtonComponent,
    ElencoPiaoPubblicatiComponent,
    CardPortalePerformanceComponent,
  ],
  templateUrl: './consultazione.component.html',
  styleUrl: './consultazione.component.scss',
})
export class ConsultazioneComponent extends BaseComponent {
  title: string = 'SCRIVANIA_PA.SERVIZI_PIAO.CONSULTA_PIAO.TITLE';
  subtitle: string = 'SCRIVANIA_PA.SERVIZI_PIAO.CONSULTA_PIAO.SUBTITLE';
  buttonLabel: string = 'SCRIVANIA_PA.SERVIZI_PIAO.CONSULTA_PIAO.BUTTON_CRUSCOTTI_ANALISI';

  router: Router = inject(Router);

  clickCruscottoAnalisi(): void {
    this.router.navigate(['/area-privata-PA']).then(() => {
      setTimeout(() => {
        window.scrollTo(0, 0);
        // Fallback per alcuni browser
        document.documentElement.scrollTop = 0;
        document.body.scrollTop = 0;
      }, 0);
    });
  }
}
