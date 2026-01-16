import { Component, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { SectionStatusEnum } from '../../models/enums/section-status.enum';
import { PiaoStatusEnum } from '../../models/enums/piao-status.enum';

@Component({
  selector: 'piao-status',
  imports: [SharedModule],
  templateUrl: './status.component.html',
  styleUrl: './status.component.scss',
})
export class StatusComponent implements OnInit {
  @Input() status: string = 'Da compilare';
  colorClass!: string;

  ngOnInit(): void {
    switch (this.status) {
      case SectionStatusEnum.VALIDATA || PiaoStatusEnum.VALIDATO:
        this.colorClass = 'status-validata';
        break;

      case SectionStatusEnum.DA_COMPILARE || PiaoStatusEnum.DA_COMPILARE:
        this.colorClass = 'status-da-compilare';
        break;

      case SectionStatusEnum.IN_COMPILAZIONE || PiaoStatusEnum.IN_COMPILAZIONE:
        this.colorClass = 'status-in-compilazione';
        break;

      case SectionStatusEnum.IN_VALIDAZIONE || PiaoStatusEnum.IN_VALIDAZIONE:
        this.colorClass = 'status-in-validazione';
        break;

      case SectionStatusEnum.COMPILATA || PiaoStatusEnum.COMPILATO:
        this.colorClass = 'status-compilata';
        break;

      default:
        this.colorClass = 'status-da-compilare';
        break;
    }
  }
}
