import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, forkJoin, of, Subject, switchMap, takeUntil } from 'rxjs';
import { LoginService } from '../../../services/login.service';
import { SharedModule } from '../../../module/shared/shared.module';
import { BaseComponent } from '../../../components/base/base.component';
import { CardActionServiziPiao } from '../../../models/interfaces/card-action-servizi-piao';
import { GET_ALL_AVVISI } from '../../../utils/funzionalita';
import { ButtonComponent } from '../../../components/button/button.component';
import { IconComponent } from '../../../components/icon/icon.component';
import { ModalComponent } from '../../../components/modal/modal.component';
import { ModalRedigiPiaoComponent } from '../modal-redigi-piao/modal-redigi-piao.component';
import { ReactiveFormsModule } from '@angular/forms';
import { PIAOService } from '../../../services/piao.service';
import {
  AGGIORNA,
  CONSULTA,
  KEY_PIAO,
  ONLINE,
  ORDINARIO,
  PDF,
  REDIGI,
  SEMPLIFICATO,
  SHAPE_ICON,
} from '../../../utils/constants';
import { PIAODTO } from '../../../models/classes/piao-dto';
import { SessionStorageService } from '../../../services/session-storage.service';

@Component({
  selector: 'piao-card-action-servizi-piao',
  imports: [
    ButtonComponent,
    IconComponent,
    ModalComponent,
    ModalRedigiPiaoComponent,
    ReactiveFormsModule,
    SharedModule,
  ],
  templateUrl: './card-action-servizi-piao.component.html',
  styleUrl: './card-action-servizi-piao.component.scss',
})
export class CardActionServiziPiaoComponent extends BaseComponent implements OnInit, OnDestroy {
  router: Router = inject(Router);
  piaoService: PIAOService = inject(PIAOService);

  openModal: boolean = false;
  icon: string = SHAPE_ICON;
  iconStyle: string = 'icon-modal';

  private subject$: Subject<void> = new Subject<void>();
  subTitleCardHeader!: string;
  titleCardHeader!: string;

  isAggiornamento!: boolean;

  cardAction: CardActionServiziPiao[] = [
    {
      id: CONSULTA,
      title: 'SCRIVANIA_PA.SERVIZI_PIAO.CARD_ACTIONS.CONSULTA.TITLE',
      subTitle: 'SCRIVANIA_PA.SERVIZI_PIAO.CARD_ACTIONS.CONSULTA.SUB_TITLE',
      path: '/servizi-piao/consulta-piao',
      icon: 'Search',
      titleClass: 'piao-card-action-servizi-piao-title',
    },
    {
      id: REDIGI,
      title: 'SCRIVANIA_PA.SERVIZI_PIAO.CARD_ACTIONS.REDIGI.TITLE',
      subTitle: 'SCRIVANIA_PA.SERVIZI_PIAO.CARD_ACTIONS.REDIGI.SUB_TITLE',
      path: 'modal',
      icon: 'Pencil',
      titleClass: 'piao-card-action-servizi-piao-title',
    },
    {
      id: AGGIORNA,
      title: 'SCRIVANIA_PA.SERVIZI_PIAO.CARD_ACTIONS.AGGIORNA.TITLE',
      subTitle: 'SCRIVANIA_PA.SERVIZI_PIAO.CARD_ACTIONS.AGGIORNA.SUB_TITLE',
      path: '/servizi-piao/indice-piao',
      icon: 'Note',
      titleClass: 'piao-card-action-servizi-piao-title',
    },
  ];

  ngOnInit(): void {
    this.getUserContext$()
      .pipe(
        switchMap(({ paRiferimento }) => {
          const streams = this.cardAction.map((card) => {
            if (card.id === REDIGI) {
              return this.piaoService
                .redigiPiaoIsAllowed(paRiferimento.codePA)
                .pipe(catchError(() => of<boolean>(false)));
            }
            return of(true);
          });
          return forkJoin(streams);
        }),
        takeUntil(this.subject$)
      )
      .subscribe({
        next: (results) => {
          results.forEach((res, index) => {
            let card = this.cardAction[index];
            if (card.id === AGGIORNA) {
              if (this.cardAction[1].disabled && this.cardAction[1].disabled === true) {
                this.cardAction[index].disabled = false;
              } else {
                this.cardAction[index].disabled = true;
              }
            } else {
              this.cardAction[index].disabled = res;
            }
          });
        },
      });
  }

  handleGoToPath(card: CardActionServiziPiao): void {
    this.isAggiornamento = card.id === AGGIORNA || false;
    this.sessionStorageService.removeItem(KEY_PIAO);
    if (card.path === 'modal') {
      this.openModal = true;
    } else if (card.id === AGGIORNA) {
      const piaoDTO = new PIAODTO();
      piaoDTO.codPAFK = this.paRiferimento.codePA;
      //triennio non gestito in aggiornamento
      //il piao che prenderà in considerazione sarà quello dell'anno corrente
      const triennioRiferimento = '';
      this.piaoService.getOrCreatePiao(piaoDTO, triennioRiferimento).subscribe({
        next: (res: any) => {
          res.aggiornamento = true;
          this.sessionStorageService.setItem(KEY_PIAO, res);
          const path =
            res.tipologia === ONLINE ? '/servizi-piao/indice-piao' : '/servizi-piao/piao-pdf';
          this.router.navigate([path]);
        },
      });
    } else {
      this.router.navigateByUrl(card.path);
    }
  }

  handleCloseModal(): void {
    this.child.formGroup.reset();
    this.openModal = false;
  }

  handleConfirmModal(): void {
    let typeOnline = this.child.formGroup.controls['numberOfEmployess'].value
      ? this.child.formGroup.controls['numberOfEmployess'].value === 'plus50'
        ? ORDINARIO
        : SEMPLIFICATO
      : undefined;

    const piaoDTO = new PIAODTO();
    piaoDTO.codPAFK = this.paRiferimento.codePA;
    piaoDTO.tipologia = this.child.formGroup.controls['choice'].value;
    piaoDTO.tipologiaOnline = typeOnline;

    const triennioRiferimento = this.child.formGroup.controls['triennio'].value;
    this.piaoService.getOrCreatePiao(piaoDTO, triennioRiferimento).subscribe({
      next: (res: any) => {
        res.aggiornamento = false;
        this.sessionStorageService.setItem(KEY_PIAO, res);
        this.child.formGroup.reset();
        this.openModal = false;
        const path =
          res.tipologia === ONLINE ? '/servizi-piao/indice-piao' : '/servizi-piao/piao-pdf';
        this.router.navigate([path]);
      },
    });
  }

  override ngOnDestroy(): void {
    super.ngOnDestroy();
    this.subject$.next();
    this.subject$.complete();
  }
}
