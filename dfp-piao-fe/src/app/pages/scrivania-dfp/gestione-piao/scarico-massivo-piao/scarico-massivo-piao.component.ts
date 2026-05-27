import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SharedModule } from '../../../../shared/module/shared/shared.module';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { DropdownComponent } from '../../../../shared/components/dropdown/dropdown.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { SvgComponent } from '../../../../shared/components/svg/svg.component';
import { CardAlertComponent } from '../../../../shared/ui/card-alert/card-alert.component';
import { LabelValue } from '../../../../shared/models/interfaces/label-value';
import { PIAOService } from '../../../../shared/services/piao.service';
import { PIAODTO } from '../../../../shared/models/classes/piao-dto';
import { BaseComponent } from '../../../../shared/components/base/base.component';
import { AmministrazioneService } from '../../../../shared/services/amministrazione.service';
import { CodTipologiaAllegatoEnum } from '../../../../shared/models/enums/cod-tipologia-allegato.enum';
import { SectionEnum } from '../../../../shared/models/enums/section.enum';
import { AttachmentService } from '../../../../shared/services/attachment.service';
import { ScaricoMassivoService } from '../../../../shared/services/scarico-massivo.service';
import { SessionStorageService } from '../../../../shared/services/session-storage.service';

interface ScaricoPiaoRow {
  id: number;
  nomePiao: string;
  tipologia: string;
  selected: boolean;
  version: number;
}

@Component({
  selector: 'dfp-scarico-massivo-piao',
  imports: [
    SharedModule,
    ReactiveFormsModule,
    ButtonComponent,
    DropdownComponent,
    PaginationComponent,
    SvgComponent,
    CardAlertComponent,
  ],
  templateUrl: './scarico-massivo-piao.component.html',
  styleUrl: './scarico-massivo-piao.component.scss',
})
export class ScaricoMassivoPiaoComponent extends BaseComponent implements OnInit {
  private piaoService: PIAOService = inject(PIAOService);
  private attachmentService: AttachmentService = inject(AttachmentService);
  private amministrazioneService: AmministrazioneService = inject(AmministrazioneService);
  private scaricoMassivoService: ScaricoMassivoService = inject(ScaricoMassivoService);

  private piaoMap: Map<number, PIAODTO> = new Map();

  title: string = 'SCRIVANIA_DFP.GESTIONE_PIAO.SCARICO_MASSIVO.TITLE';
  subtitle: string = 'SCRIVANIA_DFP.GESTIONE_PIAO.SCARICO_MASSIVO.SUBTITLE';

  tipologiaOptions: LabelValue[] = [];

  triennioOptions: LabelValue[] = [];

  form: FormGroup = new FormGroup({
    tipologia: new FormControl<string | null>(null),
    triennio: new FormControl<string | null>(null, Validators.required),
  });

  get tipologiaControl(): FormControl {
    return this.form.controls['tipologia'] as FormControl;
  }

  get triennioControl(): FormControl {
    return this.form.controls['triennio'] as FormControl;
  }

  ngOnInit(): void {
    this.initDropdownTriennio();
    this.amministrazioneService.getTipologieAmministrazioni().subscribe({
      next: (tipologie) => {
        this.tipologiaOptions = tipologie.map((t) => ({ label: t, value: t }));
      },
    });
  }

  itemsPerPage: number = 3;
  currentPage = signal<number>(1);
  searchedQuery = signal<string>('');

  private allRows = signal<ScaricoPiaoRow[]>([]);
  rowsCount = computed<number>(() => this.allRows().length);

  totalItems = computed<number>(() => this.allRows().length);

  paginatedRows = computed<ScaricoPiaoRow[]>(() => {
    const start = (this.currentPage() - 1) * this.itemsPerPage;
    return this.allRows().slice(start, start + this.itemsPerPage);
  });

  selectedCount = computed<number>(() => this.allRows().filter((r) => r.selected).length);

  allSelected = computed<boolean>(() => {
    const rows = this.paginatedRows();
    return rows.length > 0 && rows.every((r) => r.selected);
  });

  readonly codTipologiaPiao: SectionEnum[] = [
    SectionEnum.PIAO,
    SectionEnum.SEZIONE_1,
    SectionEnum.SEZIONE_2_1,
    SectionEnum.SEZIONE_2_2,
    SectionEnum.SEZIONE_2_3,
    SectionEnum.SEZIONE_3_1,
    SectionEnum.SEZIONE_4,
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
    CodTipologiaAllegatoEnum.PIAO_PDF_GENERATO,
  ];

  onPageChange(page: number): void {
    this.currentPage.set(page);
  }

  resetFilters(): void {
    this.form.reset({ tipologia: '', triennio: null });
    this.allRows.set([]);
    this.searchedQuery.set('');
    this.currentPage.set(1);
  }

  search(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const tipologiaLabel =
      this.tipologiaOptions.find((o) => o.value === this.tipologiaControl.value)?.label || '';
    this.searchedQuery.set(
      tipologiaLabel ||
        (this.triennioControl.value === '%' ? 'Tutti' : this.triennioControl.value) ||
        ''
    );

    this.piaoMap.clear();

    this.piaoService
      .getPiaoPubblicatiSearchDFPMassivo(
        this.tipologiaControl.value || undefined,
        this.triennioControl.value || '%'
      )
      .subscribe({
        next: (response: PIAODTO[]) => {
          response.forEach((piao) => this.piaoMap.set(piao.id!, piao));

          const rows: ScaricoPiaoRow[] = response.map((piao) => ({
            id: piao.id || 0,
            nomePiao: piao.denominazione || '',
            tipologia: piao.tipologiaIstat || '',
            selected: false,
            version: Number(piao.versione) || 0,
          }));

          this.allRows.set(rows);
          this.currentPage.set(1);
        },
      });
  }

  toggleRow(row: ScaricoPiaoRow, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.allRows.update((rows) =>
      rows.map((r) => (r.id === row.id ? { ...r, selected: checked } : r))
    );
  }

  toggleSelectAll(): void {
    const selectAll = !this.allSelected();
    const pageIds = new Set(this.paginatedRows().map((r) => r.id));
    this.allRows.update((rows) =>
      rows.map((r) => (pageIds.has(r.id) ? { ...r, selected: selectAll } : r))
    );
  }

  scaricaPiao(row: ScaricoPiaoRow): void {
    let codePa = this.sessionStorageService.getItem('paAttivaDTO').codePA || '';
    this.scaricoMassivoService.scaricaDatiScaricoMassivo([row.id], codePa).subscribe({
      next: (data) => {
        this.toastService.success(
          'Generazione del documento avviata. Una volta completata riceverai una notifica per poter scaricare il file.'
        );
      },
      error: (err) => console.error('Errore scarico PIAO:', err),
    });
  }

  scaricaTutti(): void {
    this.scaricoMassivoService
      .scaricaDatiScaricoMassivo(
        this.allRows()
          .filter((r) => r.selected)
          .map((r) => r.id),
        this.sessionStorageService.getItem('paAttivaDTO').codePA || ''
      )
      .subscribe({
        next: (data) => {
          this.toastService.success(
            'Generazione del documento avviata. Una volta completata riceverai una notifica per poter scaricare il file.'
          );
        },
        error: (err) => console.error('Errore scarico PIAO:', err),
      });
  }

  private initDropdownTriennio(): void {
    this.piaoService.getPiaoPubblicati('%', '%').subscribe({
      next: (response) => {
        if (response && response.length > 0) {
          response.forEach((piao) => {
            this.piaoMap.set(piao.id!, piao);
            const denomPiao =
              '<strong>' + piao.denominazione?.replace('PIAO', 'Triennio') + '</strong>';
            if (!this.triennioOptions.find((option) => option.label === denomPiao)) {
              const value = piao.denominazione?.replace('PIAO', '').trim() || '';
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
      error: (err) => console.error('Errore caricamento trienni:', err),
    });
  }
}
