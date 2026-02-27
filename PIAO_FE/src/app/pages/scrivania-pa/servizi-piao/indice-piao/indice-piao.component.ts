import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { SharedModule } from '../../../../shared/module/shared/shared.module';
import { LabelValue } from '../../../../shared/models/interfaces/label-value';
import { PIAODTO } from '../../../../shared/models/classes/piao-dto';
import { SessionStorageService } from '../../../../shared/services/session-storage.service';
import { TranslateService } from '@ngx-translate/core';
import { KEY_PIAO, MIN_50, ORDINARIO, PLUS_50 } from '../../../../shared/utils/constants';
import { Observable, Subject, takeUntil } from 'rxjs';
import { StructureIndicePiaoComponent } from '../../../../shared/ui/structure-indice-piao/structure-indice-piao.component';
import { AttachmentComponent } from '../../../../shared/ui/attachment/attachment.component';
import { BaseComponent } from '../../../../shared/components/base/base.component';
import { LoginService } from '../../../../shared/services/login.service';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { StatusComponent } from '../../../../shared/components/status/status.component';
import { CodTipologiaSezioneEnum } from '../../../../shared/models/enums/cod-tipologia-sezione.enum';
import { CodTipologiaAllegatoEnum } from '../../../../shared/models/enums/cod-tipologia-allegato.enum';
import { SvgComponent } from '../../../../shared/components/svg/svg.component';

@Component({
  selector: 'piao-indice-piao',
  imports: [
    SharedModule,
    StructureIndicePiaoComponent,
    AttachmentComponent,
    ButtonComponent,
    StatusComponent,
    SvgComponent,
  ],
  templateUrl: './indice-piao.component.html',
  styleUrl: './indice-piao.component.scss',
})
export class IndicePiaoComponent extends BaseComponent implements OnInit, OnDestroy {
  sessionStorageService: SessionStorageService = inject(SessionStorageService);

  private subject$: Subject<void> = new Subject<void>();

  disablebuttonValidation: boolean = false;

  checkBoxLabel: string = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.CHECKBOX.LABEL';

  labelValueConfig: LabelValue[] = [
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.INFO.DESCRIPTION',
    },
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.INFO.VERSION',
    },
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.INFO.DETAILS',
    },
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.INFO.STATUS',
    },
  ];

  attachmentTitle: string = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.ATTACHMENT.TITLE';
  attachmentLoadAttachment: string = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.ATTACHMENT.OPEN_MODAL';

  codTipologia: string = CodTipologiaSezioneEnum.PIAO;
  codTipologiaAllegato: string = CodTipologiaAllegatoEnum.ALLEGATO_SEZIONE;

  piaoDTO!: PIAODTO;

  constructor(private translate: TranslateService) {
    super();
  }

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);

    if (this.piaoDTO) {
      let description: string = '';
      let emp: string = '';

      if (!this.piaoDTO.aggiornamento) {
        description = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.INFO.DETAILS_DESCRIPTION_REDIGI';
        emp =
          this.piaoDTO.tipologiaOnline && this.piaoDTO.tipologiaOnline === ORDINARIO
            ? PLUS_50
            : MIN_50;
      } else {
        description = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.INFO.DETAILS_DESCRIPTION_AGG';
      }

      let obsTranslate$: Observable<any> = description.includes('REDIGI')
        ? this.translate.get(description, { emp })
        : this.translate.get(description);

      obsTranslate$.pipe(takeUntil(this.subject$)).subscribe({
        next: (description: string) => {
          /*SET PIAO LabelValue*/
          this.labelValueConfig[0].value = this.piaoDTO.denominazione;
          this.labelValueConfig[1].value = this.piaoDTO.versione;
          this.labelValueConfig[2].value = description;
          this.labelValueConfig[3].value = this.piaoDTO.statoPiao;
        },
      });
    }
  }

  handleDownloadPiao() {
    console.log('download piao');
  }

  override ngOnDestroy() {
    super.ngOnDestroy();
    this.subject$.next();
    this.subject$.complete();
  }
}
