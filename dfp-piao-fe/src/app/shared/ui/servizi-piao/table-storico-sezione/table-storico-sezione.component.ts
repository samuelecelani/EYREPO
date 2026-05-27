import {
  Component,
  EventEmitter,
  Input,
  Output,
  inject,
  OnChanges,
  SimpleChanges,
} from '@angular/core';

import { BaseComponent } from '../../../components/base/base.component';
import { TabellaStoricoSezioneDTO } from '../../../models/classes/tabella-storico-sezione-dto';
import { PIAODTO } from '../../../models/classes/piao-dto';
import { PIAOService } from '../../../services/piao.service';
import { SharedModule } from '../../../module/shared/shared.module';
import { DatePipe } from '@angular/common';
import { FormatCampiModificatiPipe } from '../../../pipe/format-campi-modificati.pipe';

@Component({
  selector: 'piao-table-storico-sezione',
  imports: [SharedModule, DatePipe, FormatCampiModificatiPipe],
  templateUrl: './table-storico-sezione.component.html',
  styleUrls: ['./table-storico-sezione.component.scss'],
})
export class TableStoricoSezioneComponent extends BaseComponent implements OnChanges {
  // ====== INPUT / OUTPUT ======
  @Input() open: boolean = false;
  @Input() idSezione!: number;
  @Input() piaoDTO!: PIAODTO;
  @Input() codTipologiaFK!: string;

  @Output() close = new EventEmitter<void>();

  private piaoService = inject(PIAOService);

  tableData: TabellaStoricoSezioneDTO[] = [];
  isLoading: boolean = false;
  main: string = 'main';

  title = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ACTIONS.TABLE_STORICO_SEZIONI.TITLE';
  subTitle =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ACTIONS.TABLE_STORICO_SEZIONI.SUB_TITLE';

  idLabel = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ACTIONS.TABLE_STORICO_SEZIONI.ID_TH';
  nomeLabel =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ACTIONS.TABLE_STORICO_SEZIONI.NOME_COGNOME_TH';
  profiloLabel =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ACTIONS.TABLE_STORICO_SEZIONI.PROFILO_TH';
  dataLabel = 'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ACTIONS.TABLE_STORICO_SEZIONI.DATA_TH';
  sezioneLabel =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ACTIONS.TABLE_STORICO_SEZIONI.SEZIONE_SOTTOSEZIONE_TH';
  campiLabel =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.ACTIONS.TABLE_STORICO_SEZIONI.CAMPI_MODIFICATI_TH';

  ngOnChanges(changes: SimpleChanges): void {
    console.log(changes['open']?.currentValue === true, "changes")
    console.log(this.piaoDTO,"piaoDTO")
    console.log(this.idSezione, "idSezione")

    if (changes['open']?.currentValue === true && this.piaoDTO && this.idSezione) {
      this.loadStorico();
    }
  }


  formatDotNumberSezione(text?: string | null): string {
    if (!text) return '-';
    const t = text.trim();
    return t.replace(/^(\d+)(?!\.)\s+/, '$1. ');
  }

  private loadStorico(): void {
    this.isLoading = true;

    this.piaoService.getStoricoModificheSezione(this.idSezione, this.codTipologiaFK).subscribe({
      next: (res) => {
        console.log('Storico ricevuto:', res);
        this.tableData = res ?? [];
        this.isLoading = false;
      },
      error: () => {
        this.toastService.error('Errore nel caricamento dello storico modifiche');
        this.isLoading = false;
      },
    });
  }

  closeModal(): void {
    this.close.emit();
  }

  get tabellaStatoStorico(): TabellaStoricoSezioneDTO[] {
    if (!this.tableData) return [];

    // statement separato: è evidente che muta this.tableData
    this.tableData.sort((a, b) => {
      const idA = a.id;
      const idB = b.id;
      if (idA == null && idB == null) return 0;
      if (idA == null) return 1;
      if (idB == null) return -1;
      return idB - idA;
    });

    return this.tableData;
  }
}
