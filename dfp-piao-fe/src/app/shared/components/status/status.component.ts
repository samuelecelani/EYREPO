import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { SectionStatusEnum } from '../../models/enums/section-status.enum';
import { PiaoStatusEnum } from '../../models/enums/piao-status.enum';
import { SEZIONI_SEMPLIFICATO } from '../../utils/constants';
import { PiaoPDFStatusEnum } from '../../models/enums/piao-pdf-status.enum';
import { CodStatoAvvisi } from '../../models/enums/cod-stato-avvisi.enum';

@Component({
  selector: 'piao-status',
  imports: [SharedModule],
  templateUrl: './status.component.html',
  styleUrl: './status.component.scss',
})
export class StatusComponent implements OnInit, OnChanges {
  @Input() status: string = 'Da compilare';
  @Input() isSemplificato: boolean = false;
  @Input() numeroSezione: string = '';
  colorClass!: string;
  displayLabel!: string;

  private static readonly PDF_STATUS_LABELS: Record<string, string> = {
    [PiaoPDFStatusEnum.DA_GENERARE]: 'Da generare',
    [PiaoPDFStatusEnum.IN_GENERAZIONE]: 'In generazione',
    [PiaoPDFStatusEnum.GENERATO]: 'Generato',
    [PiaoPDFStatusEnum.ERRORE_GENERAZIONE]: 'Errore generazione',
    [PiaoPDFStatusEnum.ANTIVIRUS]: 'Antivirus',
    [PiaoPDFStatusEnum.ANTIVIRUS_OK]: 'Antivirus OK',
    [PiaoPDFStatusEnum.ANTIVIRUS_KO]: 'Antivirus KO',
  };

  private static readonly AVVISI_STATUS_LABELS: Record<string, string> = {
    [CodStatoAvvisi.BOZZA]: 'In bozza',
    [CodStatoAvvisi.PUBBLICATO]: 'Pubblicato',
  };

  ngOnInit(): void {
    this.updateColorClass();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['status']) {
      this.updateColorClass();
    }
  }

  private updateColorClass(): void {
    this.displayLabel =
      StatusComponent.PDF_STATUS_LABELS[this.status] ??
      StatusComponent.AVVISI_STATUS_LABELS[this.status] ??
      this.status;
    switch (this.status) {
      case SectionStatusEnum.VALIDATA:
      case PiaoStatusEnum.VALIDATO:
      case CodStatoAvvisi.PUBBLICATO:
        this.colorClass = 'status-validata';
        break;

      case SectionStatusEnum.DA_COMPILARE:
      case PiaoStatusEnum.DA_COMPILARE:
      case PiaoPDFStatusEnum.DA_GENERARE:
        this.colorClass = 'status-da-compilare';
        break;

      case SectionStatusEnum.IN_COMPILAZIONE:
      case PiaoStatusEnum.IN_COMPILAZIONE:
      case CodStatoAvvisi.BOZZA:
        this.colorClass = 'status-in-compilazione';
        break;

      case SectionStatusEnum.IN_VALIDAZIONE:
      case PiaoStatusEnum.IN_VALIDAZIONE:
        this.colorClass = 'status-in-validazione';
        break;

      case SectionStatusEnum.PUBBLICATO:
      case PiaoStatusEnum.PUBBLICATO:
        this.colorClass = 'status-pubblicato';
        break;

      case SectionStatusEnum.COMPILATA:
      case PiaoStatusEnum.COMPILATO:
        this.colorClass = 'status-compilata';
        break;

      case SectionStatusEnum.RICHIESTA_APPROVAZIONE:
      case PiaoStatusEnum.RICHIESTA_APPROVAZIONE:
        this.colorClass = 'status-richiesta-approvazione';
        break;

      case PiaoPDFStatusEnum.IN_GENERAZIONE:
        this.colorClass = 'status-in-generazione';
        break;

      case PiaoPDFStatusEnum.GENERATO:
      case PiaoPDFStatusEnum.ANTIVIRUS_OK:
        this.colorClass = 'status-generato';
        break;

      case PiaoPDFStatusEnum.ERRORE_GENERAZIONE:
      case PiaoPDFStatusEnum.ANTIVIRUS_KO:
        this.colorClass = 'status-da-compilare';
        break;

      case PiaoPDFStatusEnum.ANTIVIRUS:
        this.colorClass = 'status-in-generazione';
        break;

      default:
        this.colorClass = 'status-da-compilare';
        break;
    }
  }

  isSemplificatoStyle(): boolean {
    if (this.isSemplificato) {
      if (this.numeroSezione === '2' || this.numeroSezione === '3') {
        return false;
      } else {
        return !SEZIONI_SEMPLIFICATO.includes(this.numeroSezione);
      }
    }
    return false;
  }
}
