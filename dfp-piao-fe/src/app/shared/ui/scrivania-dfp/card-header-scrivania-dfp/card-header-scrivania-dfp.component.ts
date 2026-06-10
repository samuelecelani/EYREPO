import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Subject, switchMap, takeUntil } from 'rxjs';
import { BaseComponent } from '../../../components/base/base.component';
import { ButtonComponent } from '../../../components/button/button.component';
import { CardComponent } from '../../../components/card/card.component';
import { IconComponent } from '../../../components/icon/icon.component';
import { SharedModule } from '../../../module/shared/shared.module';
import { ActionCardScrivaniaDfpComponent } from '../action-card-scrivania-dfp/action-card-scrivania-dfp.component';

@Component({
  selector: 'piao-card-header-scrivania-dfp',
  imports: [
    CardComponent,
    ButtonComponent,
    IconComponent,
    SharedModule,
    ActionCardScrivaniaDfpComponent,
  ],
  templateUrl: './card-header-scrivania-dfp.component.html',
  styleUrl: './card-header-scrivania-dfp.component.scss',
})
export class CardHeaderScrivaniaDfpComponent extends BaseComponent implements OnInit, OnDestroy {
  private translate = inject(TranslateService);
  private router = inject(Router);

  backgroundImgCardHeader: string = 'assets/img/backHeaderScrivania.png';
  piaoCardHeaderContainerClass: string = 'piao-card-header-scrivania-container';
  piaoCardHeaderBodyClass: string = 'piao-card-header-scrivania-body';
  piaoCardHeaderTitleClass: string = 'piao-card-header-scrivania-title';
  piaoCardHeaderSubTitleClass: string = 'piao-card-header-scrivania-subTitle';

  titleCardHeader: string = '';

  private subject$ = new Subject<void>();

  ngOnInit(): void {
    this.getUserContext$()
      .pipe(
        switchMap(({ user }) => {
          const name = user.nome || '';
          const cf = user.fiscalCode || '';
          const day = parseInt(cf.substring(9, 11), 10);
          const isFemale = !isNaN(day) && day > 40;
          const key = isFemale
            ? 'SCRIVANIA_DFP.HEADER.WELCOME_F'
            : 'SCRIVANIA_DFP.HEADER.WELCOME_M';
          return this.translate.get(key, { name });
        }),
        takeUntil(this.subject$)
      )
      .subscribe({
        next: (title) => (this.titleCardHeader = title),
      });
  }

  goToGestionePiao(): void {
    this.router.navigate(['/gestione-piao']);
  }

  goToAvvisi(): void {
    this.router.navigate(['/avvisi']);
  }

  goToCruscotti(): void {
    this.router.navigate(['/cruscotti-di-analisi']);
  }

  override ngOnDestroy(): void {
    super.ngOnDestroy();
    this.subject$.next();
    this.subject$.complete();
  }
}
