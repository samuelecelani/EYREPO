import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../../../../shared/module/shared/shared.module';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { DropdownComponent } from '../../../../shared/components/dropdown/dropdown.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { LabelValue } from '../../../../shared/models/interfaces/label-value';
import { StatusComponent } from '../../../../shared/components/status/status.component';
import { PIAOService } from '../../../../shared/services/piao.service';
import { PIAODTO } from '../../../../shared/models/classes/piao-dto';
import { Router } from '@angular/router';
import { KEY_PIAO, ONLINE, PDF } from '../../../../shared/utils/constants';
import { BaseComponent } from '../../../../shared/components/base/base.component';
import { GestionePiaoService } from '../../../../shared/services/gestione-piao.service';
import {
  AutocompleteOption,
  AutocompleteTextBoxComponent,
} from '../../../../shared/components/autocomplete-text-box/autocomplete-text-box.component';
import { AmministrazioneInternalDTO } from '../../../../shared/models/classes/amministrazione-internal-dto';
import { AmministrazioneService } from '../../../../shared/services/amministrazione.service';
import { takeUntil } from 'rxjs';

interface RicercaPiaoRow {
  id: number;
  nomePiao: string;
  amministrazione: string;
  versione: number;
  tipologia: string;
  stato: string;
  isLatest: boolean;
}

@Component({
  selector: 'dfp-revisione-piao',
  imports: [
    SharedModule,
    ReactiveFormsModule,
    ButtonComponent,
    DropdownComponent,
    AutocompleteTextBoxComponent,
    PaginationComponent,
    StatusComponent,
  ],
  templateUrl: './revisione-piao.component.html',
  styleUrl: './revisione-piao.component.scss',
})
export class RevisionePiaoComponent extends BaseComponent implements OnInit {
  private piaoService: PIAOService = inject(PIAOService);
  private router: Router = inject(Router);
  private gestionePiaoService: GestionePiaoService = inject(GestionePiaoService);
  private amministrazioneService: AmministrazioneService = inject(AmministrazioneService);

  piaoMap: Map<number, PIAODTO> = new Map();

  title: string = 'SCRIVANIA_DFP.GESTIONE_PIAO.RICERCA_PIAO.TITLE';
  subtitle: string = 'SCRIVANIA_DFP.GESTIONE_PIAO.RICERCA_PIAO.SUBTITLE';

  tipologiaOptions: LabelValue[] = [];

  ngOnInit(): void {
    this.amministrazioneService.getTipologieAmministrazioni().pipe(takeUntil(this.destroy$)).subscribe({
      next: (tipologie) => {
        this.tipologiaOptions = tipologie.map((t) => ({ label: t, value: t }));
      },
    });

    this.tipologiaControl.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(() => {
      this.amministrazioneControl.setValue('', { emitEvent: false });
      this.codiceIpaControl.setValue('', { emitEvent: false });
      this.denominazioneSuggestions.set([]);
      this.codiceIpaSuggestions.set([]);
    });
  }

  form: FormGroup = new FormGroup({
    tipologia: new FormControl<string | null>(null),
    amministrazione: new FormControl<string>(''),
    codiceIpa: new FormControl<string>(''),
  });

  get tipologiaControl(): FormControl {
    return this.form.controls['tipologia'] as FormControl;
  }

  get amministrazioneControl(): FormControl {
    return this.form.controls['amministrazione'] as FormControl;
  }

  get codiceIpaControl(): FormControl {
    return this.form.controls['codiceIpa'] as FormControl;
  }

  onlyLastVersion = signal<boolean>(false);

  itemsPerPage: number = 3;
  currentPage = signal<number>(1);

  private allRows = signal<RicercaPiaoRow[]>([]);

  filteredRows = computed<RicercaPiaoRow[]>(() => {
    const rows = this.allRows();
    if (!this.onlyLastVersion()) {
      return rows;
    }
    const maxVersionMap = new Map<string, number>();
    for (const row of rows) {
      const key = `${row.nomePiao}::${row.amministrazione}`;
      const current = maxVersionMap.get(key) ?? 0;
      if (row.versione > current) {
        maxVersionMap.set(key, row.versione);
      }
    }
    return rows.filter((row) => {
      const key = `${row.nomePiao}::${row.amministrazione}`;
      return row.versione === maxVersionMap.get(key);
    });
  });

  totalItems = computed<number>(() => this.filteredRows().length);

  paginatedRows = computed<RicercaPiaoRow[]>(() => {
    const start = (this.currentPage() - 1) * this.itemsPerPage;
    return this.filteredRows().slice(start, start + this.itemsPerPage);
  });

  onPageChange(page: number): void {
    this.currentPage.set(page);
  }

  toggleOnlyLastVersion(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.onlyLastVersion.set(target.checked);
    this.currentPage.set(1);
  }

  // ----- Autocomplete state -----
  denominazioneSuggestions = signal<AutocompleteOption<AmministrazioneInternalDTO>[]>([]);
  denominazioneLoading = signal<boolean>(false);

  codiceIpaSuggestions = signal<AutocompleteOption<AmministrazioneInternalDTO>[]>([]);
  codiceIpaLoading = signal<boolean>(false);

  private toDenomOption(
    a: AmministrazioneInternalDTO
  ): AutocompleteOption<AmministrazioneInternalDTO> {
    return {
      label: a.denominazioneEnte || '',
      sublabel: a.codiceIPA || '',
      value: a,
    };
  }

  private toCodiceIpaOption(
    a: AmministrazioneInternalDTO
  ): AutocompleteOption<AmministrazioneInternalDTO> {
    return {
      label: a.codiceIPA || '',
      sublabel: a.denominazioneEnte || '',
      value: a,
    };
  }

  /*
  onTipologiaChange(_value: unknown): void {
    const tipologia = this.tipologiaControl.value || undefined;
    if (!tipologia) return;
    this.denominazioneLoading.set(true);
    this.gestionePiaoService.getAmministrazioneByTipologiaAndDenomAndCode(tipologia).pipe(takeUntil(this.destroy$)).subscribe({
      next: (list) => {
        this.denominazioneSuggestions.set(list.map((a) => this.toDenomOption(a)));
        this.codiceIpaSuggestions.set(list.map((a) => this.toCodiceIpaOption(a)));
        this.denominazioneLoading.set(false);
      },
      error: () => this.denominazioneLoading.set(false),
    });
  }
    */

  onDenominazioneSearch(query: string): void {
    const tipologia = this.tipologiaControl.value || undefined;
    this.denominazioneLoading.set(true);
    this.gestionePiaoService
      .getAmministrazioneByTipologiaAndDenomAndCode(tipologia, query)
      .pipe(takeUntil(this.destroy$)).subscribe({
        next: (list) => {
          const unique = this.dedupAmministrazioni(list);
          this.denominazioneSuggestions.set(unique.map((a) => this.toDenomOption(a)));
          this.denominazioneLoading.set(false);
        },
        error: () => this.denominazioneLoading.set(false),
      });
  }

  onCodiceIpaSearch(query: string): void {
    const tipologia = this.tipologiaControl.value || undefined;
    this.codiceIpaLoading.set(true);
    this.gestionePiaoService
      .getAmministrazioneByTipologiaAndDenomAndCode(tipologia, undefined, query)
      .pipe(takeUntil(this.destroy$)).subscribe({
        next: (list) => {
          const unique = this.dedupAmministrazioni(list);
          this.codiceIpaSuggestions.set(unique.map((a) => this.toCodiceIpaOption(a)));
          this.codiceIpaLoading.set(false);
        },
        error: () => this.codiceIpaLoading.set(false),
      });
  }

  private dedupAmministrazioni(list: AmministrazioneInternalDTO[]): AmministrazioneInternalDTO[] {
    const seen = new Set<string>();
    return list.filter((a) => {
      const key = `${a.codiceIPA ?? ''}|${a.denominazioneEnte ?? ''}`;
      if (seen.has(key)) return false;
      seen.add(key);
      return true;
    });
  }

  clearDenominazioneSuggestions(): void {
    this.denominazioneSuggestions.set([]);
  }

  clearCodiceIpaSuggestions(): void {
    this.codiceIpaSuggestions.set([]);
  }

  onAmministrazioneSelected(opt: AutocompleteOption<AmministrazioneInternalDTO>): void {
    const a = opt.value;
    this.amministrazioneControl.setValue(a.denominazioneEnte || '', { emitEvent: false });
    this.codiceIpaControl.setValue(a.codiceIPA || '', { emitEvent: false });
    if (a.tipologiaIstat) {
      this.tipologiaControl.setValue(a.tipologiaIstat, { emitEvent: false });
    }
    this.denominazioneSuggestions.set([]);
    this.codiceIpaSuggestions.set([]);
  }

  resetFilters(): void {
    this.form.reset({ tipologia: '', amministrazione: '', codiceIpa: '' });
    this.denominazioneSuggestions.set([]);
    this.codiceIpaSuggestions.set([]);
  }

  search(): void {
    const codiceIpa = this.codiceIpaControl.value?.trim() || undefined;
    const tipologia = this.tipologiaControl.value?.trim() || undefined;

    this.piaoMap.clear();

    this.piaoService.getPiaoPubblicatiSearchDFP(codiceIpa, tipologia).pipe(takeUntil(this.destroy$)).subscribe({
      next: (response: PIAODTO[]) => {
        response.forEach((piao) => this.piaoMap.set(piao.id!, piao));

        const rows: RicercaPiaoRow[] = response.map((piao) => ({
          id: piao.id || 0,
          nomePiao: piao.denominazione || '',
          amministrazione: piao.denominazionePA || '-',
          versione: Number(piao.versione) || 0,
          tipologia: piao.tipologia || '',
          stato: piao.statoPiao || '',
          isLatest: false,
        }));

        const maxVersionMap = new Map<string, number>();
        for (const row of rows) {
          const key = `${row.nomePiao}::${row.amministrazione}`;
          const current = maxVersionMap.get(key) ?? 0;
          if (row.versione > current) {
            maxVersionMap.set(key, row.versione);
          }
        }
        for (const row of rows) {
          const key = `${row.nomePiao}::${row.amministrazione}`;
          row.isLatest = row.versione === maxVersionMap.get(key);
        }

        this.allRows.set(
          rows.sort((a, b) => {
            const nameCompare = b.nomePiao.localeCompare(a.nomePiao);
            if (nameCompare !== 0) return nameCompare;
            return b.versione - a.versione;
          })
        );
        this.currentPage.set(1);
      },
    });
  }

  vediPiao(row: RicercaPiaoRow): void {
    const piao = this.piaoMap.get(row.id);
    this.sessionStorageService.setItem(KEY_PIAO, piao);
    if (piao?.tipologia === ONLINE) {
      this.router.navigate(['/gestione-piao/revisione/indice-piao']);
      return;
    } else if (piao?.tipologia === PDF) {
      this.router.navigate(['/gestione-piao/revisione/piao-pdf'], {
        state: { isRevisione: true },
      });

      return;
    }
  }
}
