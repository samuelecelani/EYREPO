import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { CardComponent } from '../../../components/card/card.component';
import { EMPTY, forkJoin, Subject, switchMap, takeUntil, tap } from 'rxjs';
import { LoginService } from '../../../services/login.service';
import { ButtonComponent } from '../../../components/button/button.component';
import { IconComponent } from '../../../components/icon/icon.component';
import { BaseComponent } from '../../../components/base/base.component';
import { SharedModule } from '../../../module/shared/shared.module';
import { GET_ALL_AVVISI } from '../../../utils/funzionalita';
import { AlertsService } from '../../../services/alerts.service';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'piao-card-header-scrivania-pa',
  imports: [CardComponent, ButtonComponent, IconComponent, SharedModule],
  templateUrl: './card-header-scrivania-pa.component.html',
  styleUrl: './card-header-scrivania-pa.component.scss',
})
export class CardHeaderScrivaniaPAComponent extends BaseComponent implements OnInit, OnDestroy {
  backgroundImgCardHeader: string = 'assets/img/backHeaderScrivania.png';
  piaoCardHeaderContainerClass: string = 'piao-card-header-scrivania-container';
  piaoCardHeaderBodyClass: string = 'piao-card-header-scrivania-body';
  piaoCardHeaderTitleClass: string = 'piao-card-header-scrivania-title';
  piaoCardHeaderSubTitleClass: string = 'piao-card-header-scrivania-subTitle';
  alertsService: AlertsService = inject(AlertsService);
  router: Router = inject(Router);

  private subject$: Subject<void> = new Subject<void>();
  subTitleCardHeader!: string;
  titleCardHeader!: string;

  constructor(private translate: TranslateService) {
    super();
  }

  ngOnInit(): void {
    this.getVisibility(GET_ALL_AVVISI)
      .pipe(
        switchMap((functionality) => {
          let name;
          if (this.user) {
            name = this.user ? this.user.nome : '';
            this.subTitleCardHeader = this.user.paRiferimento.filter(
              (x) => x.attiva
            )[0].denominazionePA;
          }

          // utilizzo la forkjoin per poter intercettare sia la chiamata della traduzione del welcome
          // che quella di tutti gli alerts
          return forkJoin({
            title: this.translate.get('SCRIVANIA_PA.HEADER.WELCOME', { name }),
            alerts: this.alertsService.getAllAlerts(),
          });
        }),
        takeUntil(this.subject$)
      )
      .subscribe({
        next: (res) => {
          // Assegna il titolo tradotto
          this.titleCardHeader = res.title;

          // Gestisci gli avvisi
          if (res.alerts.data && this.isVisible) {
            for (let x of res.alerts.data) {
              if (!x.visualizzato) {
                this.isVisible = true;
                return;
              }
            }
          }
          this.isVisible = false;
        },
      });
  }

  handleGoToAlerts(): void {
    const element = document.getElementById('avvisi-section');
    if (element) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  override ngOnDestroy(): void {
    super.ngOnDestroy();
    this.subject$.next();
    this.subject$.complete();
  }
}
