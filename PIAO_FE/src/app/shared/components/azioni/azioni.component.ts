import { Component, HostListener, inject, Input } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { SvgComponent } from '../svg/svg.component';
import { IVerticalEllipsisActions } from '../../models/interfaces/vertical-ellipsis-actions';
import { Router } from '@angular/router';

@Component({
  selector: 'piao-azioni',
  imports: [SharedModule, SvgComponent],
  templateUrl: './azioni.component.html',
  styleUrl: './azioni.component.scss',
})
export class AzioniComponent {
  router: Router = inject(Router);

  @Input() idSelected!: string;
  @Input() isTitle: boolean = true;

  icon: string = 'VerticalEllipsis';

  openModal: boolean = false;

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

  handleOpenModal() {
    this.openModal = this.openModal ? (this.openModal = false) : (this.openModal = true);
  }

  handleGoToActions(action: IVerticalEllipsisActions) {
    this.openModal = false;

    if (action.callback) {
      action.callback();
    } else if (action.path) {
      this.router.navigateByUrl(action.path);
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (!this.openModal) return;

    const target = event.target as HTMLElement;
    const openPanelEl = document.querySelector<HTMLElement>('.popup-panel');

    if (openPanelEl && !openPanelEl.contains(target)) {
      this.openModal = false;
    }
  }
}
