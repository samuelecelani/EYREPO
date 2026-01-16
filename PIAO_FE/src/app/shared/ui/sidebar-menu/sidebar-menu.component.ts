import { Component, inject, OnInit } from '@angular/core';
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
    styleUrl: './sidebar-menu.component.scss'
})
export class SidebarMenuComponent implements OnInit {
  router: Router = inject(Router);

  activeRoute: string = '';

  menuItems: MenuItem[] = [
    { label: 'Scrivania', icon: 'Home', route: '/pages/area-privata-PA' },
    { label: 'Profilo', icon: 'User', route: '/pages/profilo' },
    { label: 'Servizi PIAO', icon: 'Docs', route: '/pages/servizi-piao' },
    { label: 'Novità', icon: 'Bookmark', route: '/pages/novita' },
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
      });
  }

  isActive(route: string): boolean {
    return this.activeRoute === route;
  }
}
