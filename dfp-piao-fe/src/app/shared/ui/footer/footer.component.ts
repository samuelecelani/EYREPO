import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SharedModule } from '../../module/shared/shared.module';
import { AccountService } from '../../services/account.service';
import { RoleRoutingService } from '../../services/role-routing.service';
import { take } from 'rxjs';

interface FooterLink {
  label: string;
  route: string;
  funzionalita?: string[];
}

@Component({
  selector: 'piao-footer',
  standalone: true,
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss',
  imports: [RouterLink, SharedModule],
})
export class FooterComponent implements OnInit {
  private readonly accountService = inject(AccountService);
  private readonly roleRoutingService = inject(RoleRoutingService);

  // Angular template non permette 'new' direttamente: computiamo nel componente
  currentYear = new Date().getFullYear();

  homeRoute = '/area-privata-PA';

  siteMapLinks: FooterLink[] = [
    { label: 'Scrivania', route: '/area-privata-PA' },
    { label: 'Profilo', route: '/profilo' },
    { label: 'Servizi PIAO', route: '/servizi-piao' },
    { label: 'Novita', route: '/novita' },
    { label: 'Gestionale', route: '/gestionale' },
    {
      label: 'Validazione',
      route: '/validazione',
      funzionalita: ['PIAO_AGGIORNA_CTA_VALID'],
    },
  ];

  ngOnInit(): void {
    this.accountService
      .getAccount()
      .pipe(take(1))
      .subscribe((user) => {
        const isDfp = this.roleRoutingService.isDfpAuthority(user?.typeAuthority);
        this.homeRoute = this.roleRoutingService.getHomeRouteByAuthority(user?.typeAuthority);
        this.siteMapLinks = isDfp
          ? [
              { label: 'Scrivania', route: this.homeRoute },
              { label: 'Profilo', route: '/profilo' },
              { label: 'Cruscotti di analisi', route: '/cruscotti-di-analisi' },
              { label: 'Documenti', route: '/documenti' },
              { label: 'Avvisi', route: '/avvisi' },
              { label: 'Gestionale', route: '/gestionale' },
            ]
          : [
              { label: 'Scrivania', route: this.homeRoute },
              { label: 'Profilo', route: '/profilo' },
              { label: 'Servizi PIAO', route: '/servizi-piao' },
              { label: 'Novita', route: '/novita' },
              { label: 'Gestionale', route: '/gestionale' },
              {
                label: 'Validazione',
                route: '/validazione',
                funzionalita: ['PIAO_AGGIORNA_CTA_VALID'],
              },
            ];
      });
  }
}
