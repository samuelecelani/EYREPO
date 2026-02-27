import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { CardComponent } from '../../shared/components/card/card.component';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';
import { CardNavigation } from '../../shared/models/interfaces/card-navigation';
import { LoginService } from '../../shared/services/login.service';
import { SharedModule } from '../../shared/module/shared/shared.module';
import { AccountService } from '../../shared/services/account.service';

@Component({
  selector: 'piao-index',
  imports: [CardComponent, SharedModule],
  templateUrl: './index.component.html',
  styleUrl: './index.component.scss',
})
export class IndexComponent implements OnInit, OnDestroy {
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
      path: '/pages/area-pubblica',
      disabled: false,
    },
    {
      title: 'INDEX_PAGE.CARD_NAVIGATION.TITLE.2',
      subTitle: 'INDEX_PAGE.CARD_NAVIGATION.SUB_TITLE.TITLE_CARD_PA',
      path: '/pages/area-privata-PA',
      disabled: false,
    },
    {
      title: 'INDEX_PAGE.CARD_NAVIGATION.TITLE.3',
      subTitle: 'INDEX_PAGE.CARD_NAVIGATION.SUB_TITLE.TITLE_CARD_DFP',
      path: '/pages/area-privata-DFP',
      disabled: false,
    },
  ];

  private disableCardByAuthority: Record<string, number[]> = {
    DFP: [1],
    PA: [2],
    PA_CAPOFILA: [2],
  };

  ngOnInit(): void {
    this.subscription = this.accountService.getAccount().subscribe({
      next: (res: any) => {
        if (res && res.typeAuthority) {
          //recupero dall'oggetto disableCardByAuthority,
          // l'array del numero delle card da disabilitare in base all'authorità
          const indexesCard = this.disableCardByAuthority[res.typeAuthority] || [];
          indexesCard.forEach((i) => (this.cardNavigation[i].disabled = true));
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
    console.log(path);
    this.router.navigate([path]);
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}
