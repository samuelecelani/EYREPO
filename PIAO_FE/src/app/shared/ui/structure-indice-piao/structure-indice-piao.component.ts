import { Component, inject, Input, OnDestroy, OnInit } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { PIAOService } from '../../services/piao.service';
import { StrutturaIndicePiaoDTO } from '../../models/classes/struttura-indice-piao-dto';
import { StatusComponent } from '../../components/status/status.component';
import { SessionStorageService } from '../../services/session-storage.service';
import { PIAODTO } from '../../models/classes/piao-dto';
import { KEY_PIAO } from '../../utils/constants';
import { RouterModule } from '@angular/router';
import { SectionStatusEnum } from '../../models/enums/section-status.enum';

@Component({
  selector: 'piao-structure-indice-piao',
  imports: [SharedModule, StatusComponent, RouterModule],
  templateUrl: './structure-indice-piao.component.html',
  styleUrl: './structure-indice-piao.component.scss',
})
export class StructureIndicePiaoComponent implements OnInit, OnDestroy {
  @Input() showColAction: boolean = false;
  @Input() sezioni!: StrutturaIndicePiaoDTO[];

  piaoService: PIAOService = inject(PIAOService);
  sessionStorageService: SessionStorageService = inject(SessionStorageService);

  piaoDTO!: PIAODTO;

  statusDaCompilare: string = SectionStatusEnum.DA_COMPILARE;

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
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

  ngOnDestroy(): void {}
}
