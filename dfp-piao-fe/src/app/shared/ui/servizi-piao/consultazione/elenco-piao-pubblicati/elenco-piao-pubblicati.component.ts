import {
  Component,
  computed,
  inject,
  Input,
  OnInit,
  QueryList,
  signal,
  ViewChildren,
} from '@angular/core';
import { BaseComponent } from '../../../../components/base/base.component';
import { SharedModule } from '../../../../module/shared/shared.module';
import { DropdownComponent } from 'src/app/shared/components/dropdown/dropdown.component';
import { PIAOService } from '../../../../services/piao.service';
import { switchMap, takeUntil } from 'rxjs';
import { PIAODTO } from '../../../../models/classes/piao-dto';
import { LabelValue } from '../../../../models/interfaces/label-value';
import { TabellaConsultazioneDTO } from '../../../../models/classes/tabella-consultazione-dto';
import { AzioniComponent } from '../../../../components/azioni/azioni.component';
import { IVerticalEllipsisActions } from '../../../../models/interfaces/vertical-ellipsis-actions';
import { SvgComponent } from 'src/app/shared/components/svg/svg.component';
import { PaginationComponent } from '../../../../components/pagination/pagination.component';
import { AttachmentComponent } from '../../../../ui/attachment/attachment.component';
import { SectionEnum } from '../../../../models/enums/section.enum';
import { CodTipologiaAllegatoEnum } from '../../../../models/enums/cod-tipologia-allegato.enum';
import { Router } from '@angular/router';
import { KEY_PIAO } from '../../../../utils/constants';

@Component({
  selector: 'piao-elenco-piao-pubblicati',
  imports: [
    SharedModule,
    DropdownComponent,
    PaginationComponent,
    AzioniComponent,
    SvgComponent,
    AttachmentComponent,
  ],
  templateUrl: './elenco-piao-pubblicati.component.html',
  styleUrl: './elenco-piao-pubblicati.component.scss',
})
export class ElencoPiaoPubblicatiComponent extends BaseComponent implements OnInit {
  listaPiaoPubblicatiLabel: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.CONSULTA_PIAO.ELENCO_PIAO_PUBBLICATI.TITLE';
  triennioDropdownLabel: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.CONSULTA_PIAO.ELENCO_PIAO_PUBBLICATI.TRIENNIO_DROPDOWN';

  piaoService: PIAOService = inject(PIAOService);
  router: Router = inject(Router);

  piaoMap: Map<number, PIAODTO> = new Map();

  triennioOptions: LabelValue[] = [];
  tableData = signal<TabellaConsultazioneDTO[]>([]);
  expandedRows = signal<Set<number>>(new Set<number>());

  readonly codTipologiaPiao: SectionEnum[] = [
    SectionEnum.PIAO,
    SectionEnum.SEZIONE_1,
    SectionEnum.SEZIONE_2_1,
    SectionEnum.SEZIONE_2_2,
    SectionEnum.SEZIONE_2_3,
    SectionEnum.SEZIONE_3_1,
    SectionEnum.SEZIONE_4,
    SectionEnum.SEZIONE_APPROVAZIONE_PUBBLICAZIONE,
  ];
  readonly codTipologiaAllegatoPiao: CodTipologiaAllegatoEnum[] = [
    CodTipologiaAllegatoEnum.PIAO,
    CodTipologiaAllegatoEnum.ULTERIORI_ALLEGATI_PIAO,
    CodTipologiaAllegatoEnum.ALLEGATO_SEZIONE,
    CodTipologiaAllegatoEnum.IMMAGINE_SEZIONE_21_1,
    CodTipologiaAllegatoEnum.IMMAGINE_SEZIONE_21_2,
    CodTipologiaAllegatoEnum.DOCUMENTAZIONE_MONITORAGGIO,
    CodTipologiaAllegatoEnum.APPROVAZIONE_PUBBLICAZIONE,
    CodTipologiaAllegatoEnum.ANALISI_CONTESTO,
    CodTipologiaAllegatoEnum.LOGO_ANAGRAFICA,
    CodTipologiaAllegatoEnum.RICHIESTA_APPROVAZIONE,
    CodTipologiaAllegatoEnum.IMMAGINE_SEZIONE_31,
  ];

  @Input() itemsPerPage: number = 3;
  @ViewChildren('attachmentAllegato') attachmentAllegatoComponents?: QueryList<AttachmentComponent>;

  currentPage = signal(1);

  ngOnInit(): void {
    this.initDropdownPiao();
  }

  onPageChange(page: number): void {
    this.currentPage.set(page);
    this.expandedRows.set(new Set<number>());
  }

  totalItems = computed(() => {
    return this.tableData().length;
  });

  paginatedTableData = computed(() => {
    const current = this.currentPage();
    const start = (current - 1) * this.itemsPerPage;
    return this.tableData().slice(start, start + this.itemsPerPage);
  });

  private initDropdownPiao(): void {
    this.getPaRiferimento$()
      .pipe(switchMap((pa) => this.piaoService.getPiaoPubblicati(pa.codePA, '%')))
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          if (response && response.length > 0) {
            response.forEach((piao) => {
              this.piaoMap.set(piao.id!, piao);
              let denomPiao =
                '<strong>' + piao.denominazione?.replace('PIAO', 'Triennio') + '</strong>';
              if (!this.triennioOptions.find((option) => option.label === denomPiao)) {
                let value = piao.denominazione?.replace('PIAO', '').trim() || '';
                this.triennioOptions.push({
                  label: denomPiao || '',
                  value: value,
                });
              }
            });
            if (this.triennioOptions.length > 1) {
              this.triennioOptions.unshift({ label: 'Tutti', value: '%' });
            }
          }
        },
        error: (err) => console.error('Errore caricamento PIAO:', err),
      });
  }

  populatePiaoPubblicati(triennio: string): void {
    if (!triennio) {
      this.tableData.set([]);
      this.currentPage.set(1);
      this.expandedRows.set(new Set<number>());
      return;
    }

    this.getPaRiferimento$()
      .pipe(switchMap((pa) => this.piaoService.getPiaoPubblicati(pa.codePA, triennio)))
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: PIAODTO[]) => {
          const rows: TabellaConsultazioneDTO[] = [];
          response.forEach((x: PIAODTO) => {
            rows.push({
              id: x.id || 0,
              triennio: x.denominazione || '',
              versione: x.versione || '',
              autore: x.createdByNameSurname || '-',
              dataApprovazione: x.updatedTs,
              dataPubblicazione: x.createdTs,
              tipologia: x.tipologia || '',
            });
          });

          this.tableData.set(rows);
          this.currentPage.set(1);
          this.expandedRows.set(new Set<number>());
        },
      });
  }

  toggleAccordion(rowId: number): void {
    const expanded = new Set(this.expandedRows());

    if (expanded.has(rowId)) {
      expanded.delete(rowId);
    } else {
      expanded.add(rowId);
    }

    this.expandedRows.set(expanded);
  }

  isRowExpanded(rowId: number): boolean {
    return this.expandedRows().has(rowId);
  }

  getActionsFor(row: TabellaConsultazioneDTO): IVerticalEllipsisActions[] {
    return [
      {
        icon: 'Eye',
        label: 'Visualizza PIAO on-line',
        callback: () => {
          this.visualizzaPiaoOnline(row.id);
        },
      },
      {
        icon: 'Download',
        label: 'Scarica tutti gli allegati',
        callback: () => {
          this.downloadAllAttachmentsForRow(row.id);
        },
      },
    ];
  }

  private downloadAllAttachmentsForRow(rowId: number): void {
    if (!this.isRowExpanded(rowId)) {
      this.toggleAccordion(rowId);
    }

    setTimeout(() => {
      const attachmentComponent = this.attachmentAllegatoComponents
        ?.toArray()
        .find((component) => component.idPiao === rowId);

      attachmentComponent?.downloadAllAttachments();
    });
  }

  getPiaoExternalUrl(rowId: number): string | undefined {
    const piao = this.piaoMap.get(rowId);
    const candidate = piao?.tipologiaOnline;

    if (!candidate) {
      return undefined;
    }

    return /^https?:\/\//i.test(candidate) ? candidate : undefined;
  }

  getZipFileBaseName(row: TabellaConsultazioneDTO): string {
    const piaoName = (row.triennio || 'PIAO').replace(/\s+/g, '_').replace(/[^a-zA-Z0-9_-]/g, '');
    const date = this.formatDate(row.dataPubblicazione) || this.formatDate(new Date());

    return `${piaoName}_${date}`;
  }

  private formatDate(dateValue?: Date): string {
    if (!dateValue) {
      return 'gg/mm/aaaa';
    }

    const date = new Date(dateValue);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();

    return `${day}/${month}/${year}`;
  }

  visualizzaPiaoOnline(idPiao: number): void {
    const piao = this.piaoMap.get(idPiao);

    sessionStorage.setItem(KEY_PIAO, JSON.stringify(piao));

    this.router.navigate(['/servizi-piao/indice-piao']).then(() => {
      setTimeout(() => {
        window.scrollTo(0, 0);
        // Fallback per alcuni browser
        document.documentElement.scrollTop = 0;
        document.body.scrollTop = 0;
      }, 0);
    });
  }
}
