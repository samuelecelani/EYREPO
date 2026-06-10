import { Component, inject, OnInit, OnDestroy, HostListener, ElementRef } from '@angular/core';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { take, takeUntil } from 'rxjs';
import { SvgComponent } from '../../components/svg/svg.component';
import { SharedModule } from '../../module/shared/shared.module';
import { BaseComponent } from '../../components/base/base.component';
import { RoleRoutingService } from '../../services/role-routing.service';

interface MenuItem {
  label: string;
  icon: string;
  route: string;
  funzionalita?: string[];
}

@Component({
  selector: 'piao-sidebar-menu',
  imports: [SharedModule, RouterModule, SvgComponent],
  templateUrl: './sidebar-menu.component.html',
  styleUrl: './sidebar-menu.component.scss',
})
export class SidebarMenuComponent extends BaseComponent implements OnInit, OnDestroy {
  router: Router = inject(Router);
  elementRef: ElementRef = inject(ElementRef);
  roleRoutingService = inject(RoleRoutingService);

  activeRoute: string = '';
  private ticking = false;
  isDfpUser = false;
  homeRoute = '/area-privata-PA';
  private initTimeoutId: number | null = null;
  private mainScrollEl: HTMLElement | null = null;
  private mainScrollHandler = () => this.onWindowScroll();

  private paMenuItems: MenuItem[] = [
    { label: 'Scrivania', icon: 'Home', route: '/area-privata-PA' },
    { label: 'Profilo', icon: 'User', route: '/profilo' },
    { label: 'Servizi PIAO', icon: 'Docs', route: '/servizi-piao' },
    {
      label: 'Validazione',
      icon: 'CheckCircle',
      route: '/validazione',
      funzionalita: ['PIAO_AGGIORNA_CTA_VALID'],
    },
    { label: 'Novità', icon: 'Calendar', route: '/novita' },
    { label: 'Gestionale', icon: 'Settings', route: '/gestionale' },
    { label: 'Help Desk', icon: 'Help', route: '/help-desk' },
  ];

  private dfpMenuItems: MenuItem[] = [
    { label: 'Scrivania', icon: 'Home', route: '/area-privata-DFP' },
    { label: 'Profilo', icon: 'User', route: '/profilo' },
    {
      label: 'Cruscotti<br /> di analisi',
      icon: 'Analysis',
      route: '/cruscotti-di-analisi',
    },
    { label: 'Avvisi', icon: 'AlertCircle', route: '/avvisi' },
    { label: 'Gestione PIAO', icon: 'Pencil', route: '/gestione-piao' },
    { label: 'Gestionale', icon: 'Settings', route: '/gestionale' },
  ];

  private paBottomMenuRoutes: string[] = ['/gestionale', '/help-desk'];
  private dfpBottomMenuRoutes: string[] = ['/gestionale'];

  menuItems: MenuItem[] = this.paMenuItems;

  // Liste memoizzate (non getter): ricalcolate solo quando cambia menuItems / isDfpUser / funzionalita.
  // Usare getter sui *ngFor del template causa allocazione di un nuovo array ad ogni change detection,
  // e dato che hasFunzionalita() ha side effect (loadFunzionalita -> funzionalita$.next sincrono via of()),
  // si innesca un loop infinito di CD.
  topMenuItems: MenuItem[] = [];
  bottomMenuItems: MenuItem[] = [];

  private recomputeMenuItems(): void {
    const bottomRoutes = this.isDfpUser ? this.dfpBottomMenuRoutes : this.paBottomMenuRoutes;
    const filtered = this.menuItems.filter((item) => {
      if (item.funzionalita) {
        return this.hasFunzionalita(item.funzionalita);
      }
      return true;
    });
    this.topMenuItems = filtered.filter((item) => !bottomRoutes.includes(item.route));
    this.bottomMenuItems = filtered.filter((item) => bottomRoutes.includes(item.route));
  }

  ngOnInit(): void {
    this.accountService
      .getAccount()
      .pipe(take(1))
      .pipe(takeUntil(this.destroy$))
      .subscribe((user) => {
        this.isDfpUser = this.roleRoutingService.isDfpAuthority(user?.typeAuthority);
        this.homeRoute = this.roleRoutingService.getHomeRouteByAuthority(user?.typeAuthority);
        this.menuItems = this.isDfpUser
          ? this.dfpMenuItems.map((item) =>
              item.route === '/area-privata-DFP' ? { ...item, route: this.homeRoute } : item
            )
          : this.paMenuItems.map((item) =>
              item.route === '/area-privata-PA' ? { ...item, route: this.homeRoute } : item
            );
        this.recomputeMenuItems();
      });

    // Ricalcola le liste menu quando cambiano le funzionalità (evita di chiamare
    // hasFunzionalita() dentro un getter del template, che innescherebbe un CD loop).
    this.funzionalita$.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.recomputeMenuItems();
    });

    // Calcolo iniziale (anche prima che arrivi l'account, così il template ha array stabili)
    this.recomputeMenuItems();

    // Imposta la rotta attiva iniziale
    this.activeRoute = this.router.url;

    // Ascolta i cambiamenti di rotta
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe((event: any) => {
        this.activeRoute = event.urlAfterRedirects || event.url;
      });

    // Usa setTimeout per assicurarsi che il DOM sia completamente caricato
    this.initTimeoutId = window.setTimeout(() => {
      this.initTimeoutId = null;
      this.adjustSidebarPosition();
      // Aggiungi listener sullo scroll del main container
      const main = this.elementRef.nativeElement.closest('main');
      if (main) {
        this.mainScrollEl = main;
        main.addEventListener('scroll', this.mainScrollHandler, { passive: true });
      }
    }, 100);
  }

  override ngOnDestroy(): void {
    if (this.initTimeoutId !== null) {
      clearTimeout(this.initTimeoutId);
      this.initTimeoutId = null;
    }
    if (this.mainScrollEl) {
      this.mainScrollEl.removeEventListener('scroll', this.mainScrollHandler);
      this.mainScrollEl = null;
    }
    super.ngOnDestroy();
  }

  @HostListener('window:scroll', [])
  @HostListener('window:resize', [])
  onWindowScroll() {
    if (!this.ticking) {
      window.requestAnimationFrame(() => {
        this.adjustSidebarPosition();
        this.ticking = false;
      });
      this.ticking = true;
    }
  }

  private adjustSidebarPosition() {
    const sidebar = this.elementRef.nativeElement.querySelector('.sidebar-menu');
    if (!sidebar) return;

    const main = sidebar.closest('main');
    if (!main) return;

    const breadcrumb = document.querySelector('piao-breadcrumb');
    const footer = document.querySelector('footer');

    // Usa position absolute relativa al main
    sidebar.style.position = 'absolute';
    sidebar.style.left = '1.5rem';

    // Trova la posizione della breadcrumb o del container-child
    let initialTop = 0;
    const containerChild = main.querySelector('.container-child') as HTMLElement;

    if (breadcrumb && containerChild) {
      // Calcola la distanza dal top del main alla breadcrumb
      const mainTop = main.getBoundingClientRect().top + window.pageYOffset;
      const breadcrumbTop =
        (breadcrumb as HTMLElement).getBoundingClientRect().top + window.pageYOffset;
      initialTop = breadcrumbTop - mainTop;
    } else if (containerChild) {
      const mainTop = main.getBoundingClientRect().top + window.pageYOffset;
      const containerTop = containerChild.getBoundingClientRect().top + window.pageYOffset;
      initialTop = containerTop - mainTop;
    }

    // Calcola lo scroll
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
    const mainTop = main.getBoundingClientRect().top + window.pageYOffset;

    // La sidebar segue lo scroll ma parte da initialTop
    let topPosition = Math.max(scrollTop - mainTop, initialTop);

    // Limita per il footer
    if (footer) {
      const sidebarHeight = sidebar.offsetHeight;
      const mainTopPos = main.getBoundingClientRect().top + window.pageYOffset;
      const footerTopPos = (footer as HTMLElement).getBoundingClientRect().top + window.pageYOffset;
      const maxTopPosition = footerTopPos - mainTopPos - sidebarHeight - 20;

      topPosition = Math.min(topPosition, maxTopPosition);
      topPosition = Math.max(topPosition, initialTop);
    }

    sidebar.style.top = `${topPosition}px`;
  }

  isActive(route: string): boolean {
    if (route === '/area-privata-DFP') {
      return this.activeRoute === '/area-privata-DFP' || this.activeRoute === '/area-privata-DFP';
    }
    return this.activeRoute === route;
  }

  trackByMenuRoute(_index: number, item: MenuItem): string {
    return item.route;
  }
}
