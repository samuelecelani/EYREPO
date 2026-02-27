import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { CardComponent } from '../../../components/card/card.component';
import { Router } from '@angular/router';
import { catchError, forkJoin, map, of, Subject, switchMap, takeUntil, tap } from 'rxjs';
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
  ORDINARIO,
  REDIGI,
  SEMPLIFICATO,
  SHAPE_ICON,
} from '../../../utils/constants';
import { PIAODTO } from '../../../models/classes/piao-dto';
import { SessionStorageService } from '../../../services/session-storage.service';

@Component({
  selector: 'piao-card-action-servizi-piao',
  imports: [
    CardComponent,
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
  sessionStorageService: SessionStorageService = inject(SessionStorageService);

  piaoCardHeaderContainerClass: string = 'piao-card-action-servizi-piao-container';
  piaoCardHeaderBodyClass: string = 'piao-card-action-servizi-piao-body';
  piaoCardHeaderTitleClass: string = 'piao-card-action-servizi-piao-title';
  piaoCardHeaderSubTitleClass: string = 'piao-card-action-servizi-piao-subTitle';

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
      path: '/pages/servizi-piao/indice-piao',
      icon: 'Search',
      codFunzionalita: GET_ALL_AVVISI,
      titleClass: 'piao-card-action-servizi-piao-title',
    },
    {
      id: REDIGI,
      title: 'SCRIVANIA_PA.SERVIZI_PIAO.CARD_ACTIONS.REDIGI.TITLE',
      subTitle: 'SCRIVANIA_PA.SERVIZI_PIAO.CARD_ACTIONS.REDIGI.SUB_TITLE',
      path: 'modal',
      icon: 'Pencil',
      codFunzionalita: GET_ALL_AVVISI,
      titleClass: 'piao-card-action-servizi-piao-title',
    },
    {
      id: AGGIORNA,
      title: 'SCRIVANIA_PA.SERVIZI_PIAO.CARD_ACTIONS.AGGIORNA.TITLE',
      subTitle: 'SCRIVANIA_PA.SERVIZI_PIAO.CARD_ACTIONS.AGGIORNA.SUB_TITLE',
      path: '/pages/servizi-piao/indice-piao',
      icon: 'Note',
      codFunzionalita: GET_ALL_AVVISI,
      titleClass: 'piao-card-action-servizi-piao-title',
    },
  ];

  ngOnInit(): void {
    const streams = this.cardAction.map((card) => {
      return this.getVisibility(card.codFunzionalita).pipe(
        switchMap((functionality) => {
          if (!this.isVisible) {
            return of(false);
          }
          if (card.id === REDIGI) {
            return this.piaoService.redigiPiaoIsAllowed(this.paRiferimento.codePA);
          }
          return of(true);
        }),
        catchError(() => of<boolean>(false))
      );
    });

    forkJoin(streams)
      .pipe(takeUntil(this.subject$))
      .subscribe({
        next: (results) => {
          results.forEach((res, index) => {
            console.log(index);
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
    if (card.path === 'modal') {
      this.openModal = true;
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

    this.piaoService.getOrCreatePiao(piaoDTO).subscribe({
      next: (res: any) => {
        res.aggiornamento = this.isAggiornamento;
        this.sessionStorageService.setItem(KEY_PIAO, res);
        this.child.formGroup.reset();
        this.openModal = false;
        this.router.navigateByUrl('/pages/servizi-piao/indice-piao');
      },
    });
  }

  override ngOnDestroy(): void {
    super.ngOnDestroy();
    this.subject$.next();
    this.subject$.complete();
  }
}
