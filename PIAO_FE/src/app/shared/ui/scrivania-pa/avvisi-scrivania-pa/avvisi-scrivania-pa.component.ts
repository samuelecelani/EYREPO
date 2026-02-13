import { Component, inject, OnInit } from '@angular/core';
import { SharedModule } from '../../../module/shared/shared.module';
import { AlertsService } from '../../../services/alerts.service';
import { Router } from '@angular/router';
import { AvvisiComponent } from '../../../../pages/scrivania-pa/avvisi/avvisi.component';

@Component({
    selector: 'piao-avvisi-scrivania-pa',
    imports: [SharedModule, AvvisiComponent],
    templateUrl: './avvisi-scrivania-pa.component.html',
    styleUrl: './avvisi-scrivania-pa.component.scss'
})
export class AvvisiScrivaniaPAComponent implements OnInit {
  alertsService: AlertsService = inject(AlertsService);
  router: Router = inject(Router);

  ngOnInit(): void {}
}
