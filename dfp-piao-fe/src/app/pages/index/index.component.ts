import { Component, DestroyRef, OnDestroy, OnInit, inject } from '@angular/core';
import { CardComponent } from '../../shared/components/card/card.component';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';
import { CardNavigation } from '../../shared/models/interfaces/card-navigation';
import { LoginService } from '../../shared/services/login.service';
import { SharedModule } from '../../shared/module/shared/shared.module';
import { AccountService } from '../../shared/services/account.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'piao-index',
  imports: [CardComponent, SharedModule],
  templateUrl: './index.component.html',
  styleUrl: './index.component.scss',
})
export class IndexComponent implements OnInit, OnDestroy {
  private destroyRef = inject(DestroyRef);
  router = inject(Router);
  loginService = inject(LoginService);
  accountService = inject(AccountService);

  cardContainerClass: string = 'piao-card-index';
  cardBodyClass: string = 'piao-card-index-body';
  cardTitleClass: string = 'piao-card-index-title';
  cardSubTitleClass: string = 'piao-card-index-subTitle';

  subscription!: Subscription;

  cardNavigation: CardNavigation[] = [
    {
      title: 'INDEX_PAGE.CARD_NAVIGATION.TITLE.1',
      subTitle: 'INDEX_PAGE.CARD_NAVIGATION.SUB_TITLE.TITLE_CARD_PUBLIC',
      path: '/area-pubblica',
      disabled: false,
    },
    {
      title: 'INDEX_PAGE.CARD_NAVIGATION.TITLE.2',
      subTitle: 'INDEX_PAGE.CARD_NAVIGATION.SUB_TITLE.TITLE_CARD_PA',
      path: '/area-privata-PA',
      disabled: false,
    },
    {
      title: 'INDEX_PAGE.CARD_NAVIGATION.TITLE.3',
      subTitle: 'INDEX_PAGE.CARD_NAVIGATION.SUB_TITLE.TITLE_CARD_DFP',
      path: '/area-privata-DFP',
      disabled: false,
    },
  ];

  private disableCardByAuthority: Record<string, number[]> = {
    DFP: [1],
    PIAO: [0, 1],
    PA: [2],
    PA_CAPOFILA: [2],
  };

  ngOnInit(): void {
    this.subscription = this.accountService.getAccount().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (res: any) => {
        if (res && res.typeAuthority) {
          // redirect automatico in base all'authority
          if (res.typeAuthority === 'DFP') {
            this.navigateTo('/area-privata-DFP');
          } else if (res.typeAuthority === 'PA' || res.typeAuthority === 'PA_CAPOFILA') {
            this.navigateTo('/area-privata-PA');
          }
          //recupero dall'oggetto disableCardByAuthority,
          // l'array del numero delle card da disabilitare in base all'authorità

          /*const indexesCard = this.disableCardByAuthority[res.typeAuthority] || [];
          indexesCard.forEach((i) => (this.cardNavigation[i].disabled = true));*/
        } else {
          //this.loginService.logout();
        }
      },
      error: (err: any) => {
        console.error(err);
        //this.loginService.logout();
      },
    });
  }

  navigateTo(path: string) {
    if (path === '/area-privata-PA') {
      this.router.navigate([path], { state: { fromIndex: true } });
    } else {
      this.router.navigate([path]);
    }
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  trackByIndex(index: number): number {
    return index;
  }
}
