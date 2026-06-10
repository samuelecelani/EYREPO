import {
  Component,
  DestroyRef,
  OnInit,
  ViewEncapsulation,
  computed,
  inject,
  signal,
} from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SharedModule } from '../../../shared/module/shared/shared.module';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { DropdownComponent } from '../../../shared/components/dropdown/dropdown.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { SvgComponent } from '../../../shared/components/svg/svg.component';
import { LabelValue } from '../../../shared/models/interfaces/label-value';
import {
  AutocompleteOption,
  AutocompleteTextBoxComponent,
} from '../../../shared/components/autocomplete-text-box/autocomplete-text-box.component';
import { AmministrazioneService } from '../../../shared/services/amministrazione.service';
import { GestionePiaoService } from '../../../shared/services/gestione-piao.service';
import { DichiarazioneScadenzaService } from '../../../shared/services/dichiarazione-scadenza.service';
import { AmministrazioneInternalDTO } from '../../../shared/models/classes/amministrazione-internal-dto';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { getCurrentTriennio } from '../../../shared/utils/utils';
import {
  SollecitiDichiarazioniDFPDTO,
  StatoDichiarazione as StatoDichiarazioneEnum,
} from '../../../shared/models/classes/solleciti-dichiarazioni-dfp-dto';
import { Page, Pageable, SortCriteria } from '../../../shared/models/interfaces/pageable';
import { CKEditorModule } from '@ckeditor/ckeditor5-angular';
import {
  ClassicEditor,
  Bold,
  Italic,
  Underline,
  Strikethrough,
  Heading,
  FontColor,
  FontBackgroundColor,
  FontSize,
  Alignment,
  Link,
  List,
  Table,
  TableToolbar,
  MediaEmbed,
  BlockQuote,
  HorizontalLine,
  Indent,
  IndentBlock,
  Undo,
  Essentials,
  Paragraph,
  SpecialCharacters,
  SpecialCharactersEssentials,
  ImageInsert,
  type EditorConfig,
} from 'ckeditor5';
import { SollecitiService } from 'src/app/shared/services/solleciti.service';
import { forkJoin } from 'rxjs';
import { ToastService } from '../../../shared/services/toast.service';
import { TEMPLATE_EMAIL_SOLLECITI } from '../../../shared/utils/constants';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

type StatoDichiarazione = 'Dichiarazione inviata' | 'Dichiarazione non inviata';

interface SollecitoRow {
  id: string;
  codePA: string;
  idPiao: number;
  amministrazione: string;
  stato: StatoDichiarazione;
  selected: boolean;
}

@Component({
  selector: 'dfp-solleciti',
  imports: [
    SharedModule,
    FormsModule,
    ReactiveFormsModule,
    ButtonComponent,
    DropdownComponent,
    PaginationComponent,
    AutocompleteTextBoxComponent,
    SvgComponent,
    CKEditorModule,
    ModalComponent,
  ],
  templateUrl: './solleciti.component.html',
  styleUrl: './solleciti.component.scss',
  encapsulation: ViewEncapsulation.None,
})
export class SollecitiComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);
  private readonly amministrazioneService = inject(AmministrazioneService);
  private readonly gestionePiaoService = inject(GestionePiaoService);
  private readonly dichiarazioneScadenzaService = inject(DichiarazioneScadenzaService);
  private readonly sollecitiService = inject(SollecitiService);
  private readonly toastService = inject(ToastService);

  readonly triennio = getCurrentTriennio();

  // ---- CKEditor ----
  readonly Editor = ClassicEditor;
  readonly editorConfig: EditorConfig = {
    plugins: [
      Essentials,
      Paragraph,
      Bold,
      Italic,
      Underline,
      Strikethrough,
      Heading,
      FontColor,
      FontBackgroundColor,
      FontSize,
      Alignment,
      Link,
      List,
      Table,
      TableToolbar,
      MediaEmbed,
      BlockQuote,
      HorizontalLine,
      Indent,
      IndentBlock,
      Undo,
      SpecialCharacters,
      SpecialCharactersEssentials,
      ImageInsert,
    ],
    toolbar: {
      items: [
        'undo',
        'redo',
        '|',
        'heading',
        'style',
        '|',
        'bold',
        'italic',
        'underline',
        'strikethrough',
        '|',
        'fontSize',
        'fontColor',
        'fontBackgroundColor',
        '|',
        'alignment',
        '|',
        'bulletedList',
        'numberedList',
        '|',
        'outdent',
        'indent',
        '|',
        'link',
        'insertTable',
        'mediaEmbed',
        'blockQuote',
        'horizontalLine',
        '|',
        'specialCharacters',
      ],
      shouldNotGroupWhenFull: false,
    },
    heading: {
      options: [
        { model: 'paragraph', title: 'Paragraph', class: 'ck-heading_paragraph' },
        { model: 'heading1', view: 'h1', title: 'Heading 1', class: 'ck-heading_heading1' },
        { model: 'heading2', view: 'h2', title: 'Heading 2', class: 'ck-heading_heading2' },
        { model: 'heading3', view: 'h3', title: 'Heading 3', class: 'ck-heading_heading3' },
      ],
    },
    table: {
      contentToolbar: ['tableColumn', 'tableRow', 'mergeTableCells'],
    },
    placeholder: 'Inserisci il testo del sollecito...',
    licenseKey: 'GPL',
  };

  ngOnInit(): void {
    forkJoin({
      tipologie: this.amministrazioneService.getTipologieAmministrazioni(),
      templateEmail: this.sollecitiService.getValoreFromCodice(TEMPLATE_EMAIL_SOLLECITI),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ tipologie, templateEmail }) => {
          this.tipologiaOptions = tipologie.map((t) => ({ label: t, value: t }));
          this.editorData = templateEmail as string;
        },
      });

    this.tipologiaControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.amministrazioneControl.setValue('', { emitEvent: false });
      this.amministrazioneSuggestions.set([]);
      this.selectedAmministrazione.set(null);
    });
  }

  readonly editorDataSignal = signal<string>('');

  get editorData(): string {
    return this.editorDataSignal();
  }
  set editorData(value: string) {
    this.editorDataSignal.set(value ?? '');
  }

  readonly wordCount = computed<{ words: number; characters: number }>(() => {
    const html = this.editorDataSignal();
    const text = html
      .replace(/<[^>]*>/g, ' ')
      .replace(/&nbsp;/g, ' ')
      .replace(/&[a-z]+;/gi, ' ')
      .replace(/\s+/g, ' ')
      .trim();
    const words = text.length === 0 ? 0 : text.split(/\s+/).length;
    const characters = text.replace(/\s/g, '').length;
    return { words, characters };
  });

  saveEditorContent(): void {
    this.sollecitiService
      .setValoreFromCodice(TEMPLATE_EMAIL_SOLLECITI, this.editorData)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toastService.success('Contenuto del sollecito salvato con successo.');
        },
        error: (err) => {},
      });
  }

  annulla(): void {
    this.router.navigate(['/area-privata-DFP']);
  }

  // ---- Modal ----
  openModalInvia = false;
  readonly iconModalInvia = 'Airplane';
  readonly iconStyle = 'icon-modal';

  invia(): void {
    this.openModalInvia = true;
  }

  confermaInvio(): void {
    this.openModalInvia = false;
    const selectedCodePA = this.paginatedRows()
      .filter((r) => r.selected)
      .map((r) => r.codePA);
    this.dichiarazioneScadenzaService
      .sendSollecito(selectedCodePA, TEMPLATE_EMAIL_SOLLECITI)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.toastService.success('Sollecito inviato con successo.');
          this.search(); // Ricarica la pagina per aggiornare gli stati
          this.selectedIds.set(new Set()); // Deseleziona tutte le righe dopo l'invio
        },
        error: (err) => {},
      });
  }

  // ---- Filters ----
  tipologiaOptions: LabelValue[] = [];

  readonly statoOptions: LabelValue[] = [
    { label: 'Dichiarazione inviata', value: 'INVIATA' },
    { label: 'Dichiarazione non inviata', value: 'NON_INVIATA' },
  ];

  readonly form = new FormGroup({
    tipologia: new FormControl<string | null>(null),
    amministrazione: new FormControl<string>(''),
    stato: new FormControl<string | null>(null),
  });

  get tipologiaControl(): FormControl {
    return this.form.controls['tipologia'] as FormControl;
  }
  get amministrazioneControl(): FormControl {
    return this.form.controls['amministrazione'] as FormControl;
  }
  get statoControl(): FormControl {
    return this.form.controls['stato'] as FormControl;
  }

  // ---- Autocomplete ----
  readonly amministrazioneSuggestions = signal<AutocompleteOption<AmministrazioneInternalDTO>[]>(
    []
  );
  readonly amministrazioneLoading = signal<boolean>(false);

  /** Amministrazione selezionata dall'autocomplete, usata per ricavare codPAFK in search(). */
  private readonly selectedAmministrazione = signal<AmministrazioneInternalDTO | null>(null);

  // ---- Table state ----
  readonly itemsPerPage = 6;

  /** Set degli id selezionati (mantenuto cross-pagina lato server-side). */
  private readonly selectedIds = signal<Set<string>>(new Set());

  readonly paginatedRows = computed<SollecitoRow[]>(() => {
    const page = this.serverPage();
    if (!page) return [];
    const selected = this.selectedIds();
    return page.content.map((d) => this.toRow(d, selected));
  });

  readonly totalItems = computed<number>(() => this.serverPage()?.totalElements ?? 0);

  readonly currentPage = computed<number>(() => (this.serverPage()?.number ?? 0) + 1);

  readonly selectedCount = computed<number>(() => this.selectedIds().size);

  private toRow(d: SollecitiDichiarazioniDFPDTO, selected: Set<string>): SollecitoRow {
    const id = String(d.idPiao);
    return {
      id,
      codePA: d.codePA,
      amministrazione: d.amministrazione,
      idPiao: d.idPiao,
      stato:
        d.statoDichiarazione === 'INVIATA' ? 'Dichiarazione inviata' : 'Dichiarazione non inviata',
      selected: selected.has(id),
    };
  }

  // ---- Server-side pagination state (modalità BE Pageable) ----
  /** Pagina Spring restituita dal BFF; quando valorizzata il template usa `serverPage` per la table+pagination. */
  readonly serverPage = signal<Page<SollecitiDichiarazioniDFPDTO> | null>(null);
  readonly loading = signal<boolean>(false);

  // ---- Autocomplete handlers ----
  onAmministrazioneSearch(query: string): void {
    const tipologia = this.tipologiaControl.value || undefined;
    this.amministrazioneLoading.set(true);
    this.gestionePiaoService
      .getAmministrazioneByTipologiaAndDenomAndCode(tipologia, query)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (list) => {
          const seen = new Set<string>();
          const unique = list.filter((a) => {
            const key = `${a.codiceIPA ?? ''}|${a.denominazioneEnte ?? ''}`;
            if (seen.has(key)) return false;
            seen.add(key);
            return true;
          });
          this.amministrazioneSuggestions.set(unique.map((a) => this.toAmministrazioneOption(a)));
          this.amministrazioneLoading.set(false);
        },
        error: () => this.amministrazioneLoading.set(false),
      });
  }

  private toAmministrazioneOption(
    a: AmministrazioneInternalDTO
  ): AutocompleteOption<AmministrazioneInternalDTO> {
    return {
      label: a.denominazioneEnte || '',
      sublabel: a.codiceIPA || '',
      value: a,
    };
  }

  clearAmministrazioneSuggestions(): void {
    this.amministrazioneSuggestions.set([]);
  }

  onAmministrazioneSelected(opt: AutocompleteOption<AmministrazioneInternalDTO>): void {
    const a = opt.value;
    this.selectedAmministrazione.set(a);
    this.amministrazioneControl.setValue(a.denominazioneEnte || '', { emitEvent: false });
    if (a.tipologiaIstat) {
      this.tipologiaControl.setValue(a.tipologiaIstat, { emitEvent: false });
    }
    this.amministrazioneSuggestions.set([]);
  }

  // ---- Filters ----
  resetFilters(): void {
    this.form.reset({ tipologia: '', amministrazione: '', stato: '' });
    this.amministrazioneSuggestions.set([]);
    this.selectedAmministrazione.set(null);
    this.serverPage.set(null);
    this.selectedIds.set(new Set());
    this.currentSort = [];
    this.sortDirections.clear();
    this.currentFilters = null;
  }

  search(): void {
    // Costruisco i filtri opzionali (label dropdown -> valore enum del BE)
    const tipologia = this.tipologiaControl.value || undefined;
    const denominazionePiao = 'PIAO ' + this.triennio;
    const statoValue = this.statoControl.value as StatoDichiarazioneEnum | null;
    const codPAFK =
      this.selectedAmministrazione() &&
      this.selectedAmministrazione()?.denominazioneEnte === this.amministrazioneControl.value
        ? this.selectedAmministrazione()?.codiceIPA
        : undefined;

    // Reset alla prima pagina ad ogni nuova ricerca
    this.fetchPage(
      { page: 0, size: this.itemsPerPage, sort: this.currentSort },
      {
        denominazionePiao,
        tipologiaIstat: tipologia,
        codPAFK,
        statoDichiarazione: statoValue ?? undefined,
      }
    );
  }

  /** Filtri attualmente applicati al server-side fetch (per gestire i page-change senza ricomporli). */
  private currentFilters: {
    denominazionePiao: string;
    tipologiaIstat?: string | null;
    codPAFK?: string | null;
    statoDichiarazione?: StatoDichiarazioneEnum | null;
  } | null = null;

  /** Esegue la chiamata BE paginata e aggiorna lo state. */
  private fetchPage(
    pageable: Pageable,
    filters: {
      denominazionePiao: string;
      tipologiaIstat?: string | null;
      codPAFK?: string | null;
      statoDichiarazione?: StatoDichiarazioneEnum | null;
    }
  ): void {
    this.currentFilters = filters;
    this.loading.set(true);
    this.dichiarazioneScadenzaService
      .searchSollecitiPaged(filters, pageable)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (page) => {
          if (page) {
            this.serverPage.set(page);
            this.loading.set(false);
          } else {
            this.serverPage.set(null);
            this.loading.set(false);
          }
        },
        error: () => {
          this.serverPage.set(null);
          this.loading.set(false);
        },
      });
  }

  /**
   * Handler emesso da `<piao-pagination [page]="serverPage()" (pageableChange)="onPageableChange($event)">`
   * quando il PaginationComponent è in modalità server-side.
   */
  onPageableChange(pageable: Pageable): void {
    if (!this.currentFilters) return;
    this.fetchPage(pageable, this.currentFilters);
  }

  // ---- Pagination ----
  onPageChange(page: number): void {
    if (!this.currentFilters) return;
    this.fetchPage(
      { page: page - 1, size: this.itemsPerPage, sort: this.currentSort },
      this.currentFilters
    );
  }

  // ---- Sorting ----
  /** Sort corrente passato al BE. */
  private currentSort: SortCriteria[] = [];

  /** Direzione ultima applicata per ciascuna property (per toggle ASC <-> DESC affidabile). */
  private readonly sortDirections = new Map<string, 'asc' | 'desc'>();

  handleSortAmministrazione(): void {
    this.toggleSort('amministrazione');
  }

  handleSortStato(): void {
    this.toggleSort('statoDichiarazione');
  }

  private toggleSort(property: string): void {
    const prev = this.sortDirections.get(property);
    const direction: 'asc' | 'desc' = prev === 'asc' ? 'desc' : 'asc';
    this.sortDirections.set(property, direction);
    this.currentSort = [{ property, direction }];
    if (!this.currentFilters) return;
    this.fetchPage(
      { page: 0, size: this.itemsPerPage, sort: this.currentSort },
      this.currentFilters
    );
  }

  // ---- Selection ----
  isReadOnly(row: SollecitoRow): boolean {
    return row.stato === 'Dichiarazione non inviata';
  }

  hasDettaglio(row: SollecitoRow): boolean {
    return row.stato === 'Dichiarazione inviata';
  }

  toggleRow(row: SollecitoRow, event: Event): void {
    if (this.isReadOnly(row)) return;
    const checked = (event.target as HTMLInputElement).checked;
    this.selectedIds.update((set) => {
      const next = new Set(set);
      if (checked) next.add(row.id);
      else next.delete(row.id);
      return next;
    });
  }

  selectAll(): void {
    const selectables = this.paginatedRows().filter((r) => !this.isReadOnly(r));
    const allSelected = selectables.length > 0 && selectables.every((r) => r.selected);
    this.selectedIds.update((set) => {
      const next = new Set(set);
      for (const r of selectables) {
        if (allSelected) next.delete(r.id);
        else next.add(r.id);
      }
      return next;
    });
  }

  // ---- Detail navigation (same as storico) ----
  goToDettaglio(row: SollecitoRow): void {
    if (!this.hasDettaglio(row)) return;
    this.router.navigate(['/solleciti/dettaglio-mancata-compilazione', row.id], {
      state: { amministrazione: row.amministrazione, idPiao: row.idPiao },
    });
  }
}
