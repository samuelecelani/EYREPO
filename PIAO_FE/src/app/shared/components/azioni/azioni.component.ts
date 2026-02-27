import { Component, inject, Input, OnInit, ViewContainerRef } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { SvgComponent } from '../svg/svg.component';
import { IVerticalEllipsisActions } from '../../models/interfaces/vertical-ellipsis-actions';
import { Router } from '@angular/router';
import { Overlay, OverlayRef } from '@angular/cdk/overlay';
import { TemplatePortal } from '@angular/cdk/portal';
import { TemplateRef, ViewChild } from '@angular/core';

@Component({
  selector: 'piao-azioni',
  imports: [SharedModule, SvgComponent],
  templateUrl: './azioni.component.html',
  styleUrl: './azioni.component.scss',
})
export class AzioniComponent implements OnInit {
  private overlay = inject(Overlay);
  private viewContainerRef = inject(ViewContainerRef);
  router: Router = inject(Router);

  @ViewChild('actionsMenu') actionsMenuTemplate!: TemplateRef<any>;

  @Input() idSelected!: string;
  @Input() isTitle: boolean = true;
  @Input() isReadOnly: boolean = false;

  icon: string = 'VerticalEllipsis';

  private overlayRef: OverlayRef | null = null;

  @Input() verticalEllipsis: IVerticalEllipsisActions[] = [
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ACTIONS.VIEW_CHANGE_HISTORY',
      path: '/',
    },
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ACTIONS.RECOVER_INFO_PIAO',
      path: '/',
    },
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ACTIONS.DOWNLOAD_DRAFT_PDF',
      path: '/',
    },
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ACTIONS.SECTION_GUIDE',
      path: '/',
    },
  ];

  ngOnInit(): void {
    console.log('Vertical Ellipsis Actions:', this.verticalEllipsis);
  }

  handleToggleMenu(trigger: HTMLElement) {
    if (this.overlayRef) {
      this.closeMenu();
    } else {
      this.openMenu(trigger);
    }
  }

  private openMenu(trigger: HTMLElement) {
    const positionStrategy = this.overlay
      .position()
      .flexibleConnectedTo(trigger)
      .withPositions([
        {
          originX: 'end',
          originY: 'bottom',
          overlayX: 'end',
          overlayY: 'top',
          offsetY: 8,
        },
        {
          originX: 'start',
          originY: 'bottom',
          overlayX: 'start',
          overlayY: 'top',
          offsetY: 8,
        },
      ])
      .withPush(false);

    this.overlayRef = this.overlay.create({
      positionStrategy,
      scrollStrategy: this.overlay.scrollStrategies.close(),
      hasBackdrop: true,
      backdropClass: 'cdk-overlay-transparent-backdrop',
    });

    const portal = new TemplatePortal(this.actionsMenuTemplate, this.viewContainerRef);
    this.overlayRef.attach(portal);

    this.overlayRef.backdropClick().subscribe(() => this.closeMenu());
    this.overlayRef.detachments().subscribe(() => {
      this.overlayRef = null;
    });
  }

  private closeMenu() {
    if (this.overlayRef) {
      this.overlayRef.dispose();
      this.overlayRef = null;
    }
  }

  handleGoToActions(action: IVerticalEllipsisActions) {
    this.closeMenu();

    if (action.callback) {
      action.callback();
    } else if (action.path) {
      this.router.navigateByUrl(action.path);
    }
  }

  get isMenuOpen(): boolean {
    return this.overlayRef !== null;
  }
}
