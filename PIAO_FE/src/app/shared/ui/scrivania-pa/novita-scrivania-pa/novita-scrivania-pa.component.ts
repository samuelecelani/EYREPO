import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { ButtonComponent } from '../../../components/button/button.component';
import { BaseComponent } from '../../../components/base/base.component';
import { SharedModule } from '../../../module/shared/shared.module';
import { AlertsService } from '../../../services/alerts.service';
import { Router } from '@angular/router';
import { NovitaComponent } from '../../../../pages/scrivania-pa/novita/novita.component';

@Component({
    selector: 'piao-novita-scrivania-pa',
    imports: [ButtonComponent, SharedModule, NovitaComponent],
    templateUrl: './novita-scrivania-pa.component.html',
    styleUrl: './novita-scrivania-pa.component.scss'
})
export class NovitaScrivaniaPAComponent implements OnInit {
  alertsService: AlertsService = inject(AlertsService);
  router: Router = inject(Router);

  ngOnInit(): void {}

  handleGoToNovita(): void {
    this.router.navigate(['/pages/novita']);
  }
}
