import { AfterViewInit, Component, DestroyRef, ElementRef, OnDestroy, ViewChild, inject } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../../ui/header/header.component';
import { FooterComponent } from '../../ui/footer/footer.component';
import { SidebarMenuComponent } from '../../ui/sidebar-menu/sidebar-menu.component';
import { BreadcrumbComponent } from '../../ui/breadcrumb/breadcrumb.component';
import { Subscription, filter } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'piao-base-layout-component',
  imports: [
    RouterOutlet,
    HeaderComponent,
    SidebarMenuComponent,
    FooterComponent,
    BreadcrumbComponent,
  ],
  templateUrl: './base-layout.component.html',
  styleUrl: './base-layout.component.scss',
})
export class BaseLayoutComponent implements AfterViewInit, OnDestroy {
  private destroyRef = inject(DestroyRef);
  @ViewChild('layoutWrapper', { static: false }) layoutWrapper!: ElementRef<HTMLElement>;
  private routerSub?: Subscription;
  isLoginRoute = false;

  constructor(private router: Router) {
    this.isLoginRoute = this.router.url.startsWith('/auth/login');
  }

  ngAfterViewInit(): void {
    this.routerSub = this.router.events
      .pipe(filter((e) => e instanceof NavigationEnd))
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe((e) => {
        this.isLoginRoute = (e as NavigationEnd).urlAfterRedirects.startsWith('/auth/login');
        // Forza scroll istantaneo per evitare che scroll-behavior:smooth interrompa il reset
        window.scrollTo({ top: 0, left: 0, behavior: 'instant' });
        document.documentElement.scrollTop = 0;
        document.body.scrollTop = 0;
        const el = this.layoutWrapper?.nativeElement;
        if (el) {
          el.scrollTop = 0;
        }
      });
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
  }
}
