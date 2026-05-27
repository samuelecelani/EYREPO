import { BaseComponent } from 'src/app/shared/components/base/base.component';
import { Component, EventEmitter, inject, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { PIAOService } from '../../services/piao.service';
import { StrutturaIndicePiaoDTO } from '../../models/classes/struttura-indice-piao-dto';
import { StatusComponent } from '../../components/status/status.component';
import { SessionStorageService } from '../../services/session-storage.service';
import { PIAODTO } from '../../models/classes/piao-dto';
import { KEY_PA_ATTIVA, KEY_PIAO, SEZIONI_ACTIVE_ID, SEZIONI_SEMPLIFICATO } from '../../utils/constants';
import { Router, RouterModule } from '@angular/router';
import { SectionStatusEnum } from '../../models/enums/section-status.enum';
import { AzioniComponent } from '../../components/azioni/azioni.component';
import { IVerticalEllipsisActions } from '../../models/interfaces/vertical-ellipsis-actions';
import { TooltipComponent } from '../../components/tooltip/tooltip.component';
import { RoleRoutingService } from '../../services/role-routing.service';
import { AccountService } from '../../services/account.service';
import { takeUntil } from 'rxjs';
import { NotificaService } from '../../services/notifica.service';

@Component({
  selector: 'piao-structure-indice-piao',
  imports: [SharedModule, StatusComponent, AzioniComponent, TooltipComponent, RouterModule],
  templateUrl: './structure-indice-piao.component.html',
  styleUrl: './structure-indice-piao.component.scss',
})
export class StructureIndicePiaoComponent extends BaseComponent implements OnInit {
  @Input() showColAction: boolean = false;
  @Input() sezioni!: StrutturaIndicePiaoDTO[];
  @Input() tooltipStatus: boolean = false;
  @Input() piaoDTO!: PIAODTO;
  @Input() isSemplificato: boolean = false;
  @Output() piaoStatusChange: EventEmitter<string> = new EventEmitter<string>();

  piaoService: PIAOService = inject(PIAOService);
  notificaService: NotificaService = inject(NotificaService);
  router: Router = inject(Router);
  private roleRoutingService: RoleRoutingService = inject(RoleRoutingService);

  private isDfp: boolean = false;

  statusDaCompilare: string = SectionStatusEnum.DA_COMPILARE;

  ngOnInit(): void {
    this.accountService.getAccount().subscribe((user) => {
      this.isDfp = this.roleRoutingService.isDfpAuthority(user?.typeAuthority);
    });

    if (!this.sezioni) {
      if (this.piaoDTO && this.piaoDTO.id) {
        this.piaoService.getStructureIndicePIAO(this.piaoDTO.id).subscribe({
          next: (value: StrutturaIndicePiaoDTO[]) => {
            if (value && value.length > 0) {
              let piaoSection = value.find((section) => section.numeroSezione === '0');
              let approvazioneSection = value.find((section) => section.numeroSezione === '5');
              this.sezioni = value.filter(
                (section) => section !== piaoSection && section !== approvazioneSection
              );
              this.piaoStatusChange.emit(piaoSection?.statoSezione || '');
              this.piaoDTO.statoPiao = piaoSection?.statoSezione || this.piaoDTO.statoPiao;
              this.sessionStorageService.setItem(KEY_PIAO, this.piaoDTO);
            }
          },
          error: (err): any => {
            console.log(err);
          },
        });
      }
    }
    console.log('SEZIONI: ', this.sezioni);
  }

  getAction(numeroSezione: string): IVerticalEllipsisActions[] {
    return [
      {
        label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.TABLE.PLACEHOLDER_ACT',
        callback: () => {
          const basePath = this.isDfp
            ? '/gestione-piao/revisione/indice-piao/sezione'
            : '/servizi-piao/indice-piao/sezione';
          this.router.navigate([basePath], {
            queryParams: { idSezione: numeroSezione },
          });
        },
      },
      {
        label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.TABLE.PLACEHOLDER_ACT_DWL',
        callback: () => {
          const pa = this.sessionStorageService.getItem(KEY_PA_ATTIVA);
          const codicePa = pa?.codePA;
          this.notificaService
            .generatePdf(this.piaoDTO.id!, SEZIONI_ACTIVE_ID[numeroSezione], codicePa)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: () => {
                // La generazione è stata avviata con successo, ora aspettiamo che il BE aggiorni lo stato tramite WebSocket
                console.log(
                  'Generazione del documento avviata. Una volta completata riceverai una notifica per poter scaricare il file.'
                );
              },
            });
        },
      },
    ];
  }

  isSezioneRequired(numeroSezione: string): boolean {
    return !this.isSemplificato || SEZIONI_SEMPLIFICATO.includes(numeroSezione);
  }

  isSezioneRequiredTesto(numeroSezione: string): boolean {
    return this.isSemplificato && SEZIONI_SEMPLIFICATO.includes(numeroSezione);
  }

  isSemplificatoRequired(numeroSezione: string): boolean {
    if (this.isSemplificato) {
      if (numeroSezione === '2' || numeroSezione === '3') {
        return false;
      } else {
        return !SEZIONI_SEMPLIFICATO.includes(numeroSezione);
      }
    }
    return false;
  }

}
