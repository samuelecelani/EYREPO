import { AfterViewInit, Component, NgZone, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HeaderComponent } from '../../ui/header/header.component';
import { FooterComponent } from '../../ui/footer/footer.component';
import { SidebarMenuComponent } from '../../ui/sidebar-menu/sidebar-menu.component';
import { BreadcrumbComponent } from '../../ui/breadcrumb/breadcrumb.component';

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
export class BaseLayoutComponent {}
