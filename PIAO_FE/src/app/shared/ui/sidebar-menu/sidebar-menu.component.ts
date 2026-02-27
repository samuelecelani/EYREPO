import { Component, inject, OnInit, HostListener, ElementRef } from '@angular/core';
import { Router, RouterModule, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { SvgComponent } from '../../components/svg/svg.component';
import { SharedModule } from '../../module/shared/shared.module';

interface MenuItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'piao-sidebar-menu',
  imports: [SharedModule, RouterModule, SvgComponent],
  templateUrl: './sidebar-menu.component.html',
  styleUrl: './sidebar-menu.component.scss',
})
export class SidebarMenuComponent implements OnInit {
  router: Router = inject(Router);
  elementRef: ElementRef = inject(ElementRef);

  activeRoute: string = '';
  private ticking = false;

  menuItems: MenuItem[] = [
    { label: 'Scrivania', icon: 'Home', route: '/pages/area-privata-PA' },
    { label: 'Profilo', icon: 'User', route: '/pages/profilo' },
    { label: 'Servizi PIAO', icon: 'Docs', route: '/pages/servizi-piao' },
    { label: 'Validazione', icon: 'CheckCircle', route: '/pages/validazione' },
    { label: 'Novità', icon: 'Calendar', route: '/pages/novita' },
    { label: 'Gestionale', icon: 'Settings', route: '/pages/gestionale' },
    { label: 'Help Desk', icon: 'Help', route: '/pages/help-desk' },
  ];

  ngOnInit(): void {
    // Imposta la rotta attiva iniziale
    this.activeRoute = this.router.url;

    // Ascolta i cambiamenti di rotta
    this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.activeRoute = event.urlAfterRedirects || event.url;
        setTimeout(() => {
          window.scrollTo(0, 0);
          // Fallback per alcuni browser
          document.documentElement.scrollTop = 0;
          document.body.scrollTop = 0;
        }, 0);
      });

    // Usa setTimeout per assicurarsi che il DOM sia completamente caricato
    setTimeout(() => {
      this.adjustSidebarPosition();
      // Aggiungi listener sullo scroll del main container
      const main = this.elementRef.nativeElement.closest('main');
      if (main) {
        main.addEventListener('scroll', () => this.onWindowScroll(), { passive: true });
      }
    }, 100);
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
    return this.activeRoute === route;
  }
}
