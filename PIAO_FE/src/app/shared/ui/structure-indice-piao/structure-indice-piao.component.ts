import { Component, inject, Input, OnDestroy, OnInit } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { PIAOService } from '../../services/piao.service';
import { StrutturaIndicePiaoDTO } from '../../models/classes/struttura-indice-piao-dto';
import { StatusComponent } from '../../components/status/status.component';
import { SessionStorageService } from '../../services/session-storage.service';
import { PIAODTO } from '../../models/classes/piao-dto';
import { KEY_PIAO } from '../../utils/constants';
import { Router, RouterModule } from '@angular/router';
import { SectionStatusEnum } from '../../models/enums/section-status.enum';
import { AzioniComponent } from '../../components/azioni/azioni.component';
import { IVerticalEllipsisActions } from '../../models/interfaces/vertical-ellipsis-actions';
import { TooltipComponent } from '../../components/tooltip/tooltip.component';

@Component({
  selector: 'piao-structure-indice-piao',
  imports: [SharedModule, StatusComponent, AzioniComponent, TooltipComponent, RouterModule],
  templateUrl: './structure-indice-piao.component.html',
  styleUrl: './structure-indice-piao.component.scss',
})
export class StructureIndicePiaoComponent implements OnInit, OnDestroy {
  @Input() showColAction: boolean = false;
  @Input() sezioni!: StrutturaIndicePiaoDTO[];
  @Input() tooltipStatus: boolean = false;
  @Input() piaoDTO!: PIAODTO;

  piaoService: PIAOService = inject(PIAOService);
  sessionStorageService: SessionStorageService = inject(SessionStorageService);
  router: Router = inject(Router);

  statusDaCompilare: string = SectionStatusEnum.DA_COMPILARE;

  ngOnInit(): void {
    console.log(this.piaoDTO);

    if (!this.sezioni) {
      if (this.piaoDTO && this.piaoDTO.id) {
        this.piaoService.getStructureIndicePIAO(this.piaoDTO.id).subscribe({
          next: (value: StrutturaIndicePiaoDTO[]) => {
            this.sezioni = value;
          },
          error: (err): any => {
            console.log(err);
          },
        });
      }
    }
  }

  getAction(numeroSezione: string): IVerticalEllipsisActions[] {
    return [
      {
        label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.TABLE.PLACEHOLDER_ACT',
        callback: () => {
          this.router.navigate(['/pages/servizi-piao/indice-piao/sezione'], {
            queryParams: { idSezione: numeroSezione },
          });
        },
      },
      {
        label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.TABLE.PLACEHOLDER_ACT_DWL',
      },
    ];
  }

  ngOnDestroy(): void {}
}
