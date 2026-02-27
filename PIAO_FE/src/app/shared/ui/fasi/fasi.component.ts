import {
  Component,
  computed,
  ElementRef,
  inject,
  Input,
  OnInit,
  OnChanges,
  SimpleChanges,
  signal,
  QueryList,
  ViewChildren,
  AfterViewInit,
} from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SharedModule } from '../../module/shared/shared.module';
import { KEY_PIAO, PENCIL_ICON } from '../../utils/constants';
import { ToastService } from '../../services/toast.service';
import { AzioniComponent } from '../../components/azioni/azioni.component';
import { IVerticalEllipsisActions } from '../../models/interfaces/vertical-ellipsis-actions';
import { FaseService } from '../../services/fase.service';
import { AbstractControl } from '@angular/forms';
import { PaginationComponent } from '../../components/pagination/pagination.component';
import { FaseDTO } from '../../models/classes/fase-dto';
import { BaseComponent } from '../../components/base/base.component';
import { ModalBodyFaseComponent } from './modal-body-fase/modal-body-fase.component';
import { ModalDeleteComponent } from '../../components/modal-delete/modal-delete.component';
import { ModalComponent } from '../../components/modal/modal.component';
import { SessionStorageService } from '../../services/session-storage.service';
import { PIAODTO } from '../../models/classes/piao-dto';
import { SvgComponent } from '../../components/svg/svg.component';

@Component({
  selector: 'piao-fasi',
  standalone: true,
  imports: [
    SharedModule,
    ReactiveFormsModule,
    AzioniComponent,
    PaginationComponent,
    ModalComponent,
    ModalBodyFaseComponent,
    ModalDeleteComponent,
    SvgComponent,
  ],
  templateUrl: './fasi.component.html',
  styleUrl: './fasi.component.scss',
})
export class FasiComponent extends BaseComponent implements OnInit, AfterViewInit, OnChanges {
  @Input() formGroup!: FormGroup;
  @Input() idSezione22!: number;
  @Input() idPiao!: number;
  // Paginazione
  @Input() itemsPerPage = 5;
  @Input() maxItems: number = 2;
  @Input() pagination: boolean = true;

  @ViewChildren('descrizioneScroll') descrizioneScrolls!: QueryList<ElementRef<HTMLDivElement>>;
  @ViewChildren('attivitaScroll') attivitaScrolls!: QueryList<ElementRef<HTMLDivElement>>;
  @ViewChildren('attoriScroll') attoriScrolls!: QueryList<ElementRef<HTMLDivElement>>;

  private fb = inject(FormBuilder);
  private toastService = inject(ToastService);
  private faseService = inject(FaseService);

  sessionStorageService: SessionStorageService = inject(SessionStorageService);

  openModalDelete: boolean = false;
  elementToDelete: any = null;
  openFasiModal: boolean = false;
  icon: string = PENCIL_ICON;
  iconStyle: string = 'icon-modal';
  faseToEdit?: FaseDTO;
  piaoDTO!: PIAODTO;
  sortAscending: boolean = true;
  isSortedManually: boolean = false;

  currentPage = signal(1);
  currentFaseIndex = signal(0); // Indice della fase visibile nella pagina corrente
  scrollDirection = signal<'up' | 'down'>('down'); // Direzione dello scroll per l'animazione

  private scrollStep = 40; // Pixel per ogni click di freccia

  // Mappa per tracciare lo stato di scroll per ogni fase e campo
  // Chiave: "faseIndex-fieldName" (es. "0-descrizione", "0-attivita", "0-attori")
  scrollStates = signal<
    Map<string, { canScrollUp: boolean; canScrollDown: boolean; needsScroll: boolean }>
  >(new Map());

  private activitiesStatus = signal<'idle' | 'loading' | 'success' | 'error'>('idle');
  private activitiesError = signal<any>(null);

  activityQueries = computed(() => ({
    data: this.fasi?.value || [],
    status: this.activitiesStatus(),
    error: this.activitiesError(),
    refetch: () => {
      console.log('Refetching activities...');
    },
    invalidate: () => {
      console.log('Invalidating activities cache...');
    },
  }));

  // Getter per il numero totale di items (sostituisce il computed signal)
  get totalItems(): number {
    return this.fasi?.length || 0;
  }

  // Getter per le fasi paginate (sostituisce il computed signal)
  get paginatedFasi(): AbstractControl[] {
    const start = (this.currentPage() - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return this.fasi?.controls?.slice(start, end) || [];
  }

  // Fase correntemente visibile
  get currentVisibleFase(): AbstractControl | null {
    const fasi = this.paginatedFasi;
    const index = this.currentFaseIndex();
    return fasi[index] || null;
  }

  get paginatedAlerts(): any[] {
    const fasi = this.fasi?.value;
    if (!fasi) return [];

    // Se la paginazione è disabilitata
    if (!this.pagination) {
      // Se maxItems è specificato, mostra solo i primi N elementi
      if (this.maxItems && this.maxItems > 0) {
        return fasi.slice(0, this.maxItems);
      }
      // Altrimenti mostra tutti i dati
      return fasi;
    }

    // Con paginazione attiva
    const startIndex = (this.currentPage() - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return fasi.slice(startIndex, endIndex);
  }

  ngOnInit(): void {
    console.log('[Fasi] ngOnInit, controls length:', this.fasi?.length);
    console.log('fasi', this.fasi?.value);
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['formGroup']) {
      console.log('[Fasi] formGroup changed, new length:', this.fasi?.length);
      // Reset dello scroll states
      this.scrollStates.set(new Map());
      // Reinizializza lo stato di scroll dopo che la view è aggiornata
      setTimeout(() => this.initAllScrollStates(), 100);
      // Reset della pagina corrente se necessario
      const totalPages = Math.ceil((this.fasi?.length || 0) / this.itemsPerPage);
      if (this.currentPage() > totalPages && totalPages > 0) {
        this.currentPage.set(totalPages);
      } else if (totalPages === 0) {
        this.currentPage.set(1);
      }
    }
  }

  ngAfterViewInit(): void {
    // Inizializza lo stato di scroll per tutti i campi dopo che la view è pronta
    setTimeout(() => this.initAllScrollStates(), 100);
  }

  private initAllScrollStates(): void {
    const paginatedFasi = this.paginatedFasi;
    paginatedFasi.forEach((_, index) => {
      this.updateScrollStateForField(index, 'descrizione');
      this.updateScrollStateForField(index, 'attivita');
      this.updateScrollStateForField(index, 'attori');
    });
  }

  private getScrollElementForField(faseIndex: number, fieldName: string): HTMLDivElement | null {
    let queryList: QueryList<ElementRef<HTMLDivElement>> | undefined;

    switch (fieldName) {
      case 'descrizione':
        queryList = this.descrizioneScrolls;
        break;
      case 'attivita':
        queryList = this.attivitaScrolls;
        break;
      case 'attori':
        queryList = this.attoriScrolls;
        break;
    }

    if (!queryList) return null;
    const elements = queryList.toArray();
    return elements[faseIndex]?.nativeElement || null;
  }

  private getScrollStateKey(faseIndex: number, fieldName: string): string {
    return `${faseIndex}-${fieldName}`;
  }

  updateScrollStateForField(faseIndex: number, fieldName: string): void {
    const el = this.getScrollElementForField(faseIndex, fieldName);
    const key = this.getScrollStateKey(faseIndex, fieldName);
    const currentStates = new Map(this.scrollStates());

    if (!el) {
      currentStates.set(key, { canScrollUp: false, canScrollDown: false, needsScroll: false });
      this.scrollStates.set(currentStates);
      return;
    }

    const needsScroll = el.scrollHeight > el.clientHeight;
    const canScrollUp = el.scrollTop > 0;
    const canScrollDown = el.scrollTop + el.clientHeight < el.scrollHeight - 1;

    currentStates.set(key, { canScrollUp, canScrollDown, needsScroll });
    this.scrollStates.set(currentStates);
  }

  scrollFieldUp(faseIndex: number, fieldName: string): void {
    const el = this.getScrollElementForField(faseIndex, fieldName);
    if (el) {
      el.scrollBy({ top: -this.scrollStep, behavior: 'smooth' });
      setTimeout(() => this.updateScrollStateForField(faseIndex, fieldName), 100);
    }
  }

  scrollFieldDown(faseIndex: number, fieldName: string): void {
    const el = this.getScrollElementForField(faseIndex, fieldName);
    if (el) {
      el.scrollBy({ top: this.scrollStep, behavior: 'smooth' });
      setTimeout(() => this.updateScrollStateForField(faseIndex, fieldName), 100);
    }
  }

  canFieldScrollUp(faseIndex: number, fieldName: string): boolean {
    const key = this.getScrollStateKey(faseIndex, fieldName);
    return this.scrollStates().get(key)?.canScrollUp ?? false;
  }

  canFieldScrollDown(faseIndex: number, fieldName: string): boolean {
    const key = this.getScrollStateKey(faseIndex, fieldName);
    return this.scrollStates().get(key)?.canScrollDown ?? false;
  }

  fieldNeedsScroll(faseIndex: number, fieldName: string): boolean {
    const key = this.getScrollStateKey(faseIndex, fieldName);
    return this.scrollStates().get(key)?.needsScroll ?? false;
  }

  // Controlla se almeno un campo della fase necessita di scroll
  faseNeedsScroll(faseIndex: number): boolean {
    return (
      this.fieldNeedsScroll(faseIndex, 'descrizione') ||
      this.fieldNeedsScroll(faseIndex, 'attivita') ||
      this.fieldNeedsScroll(faseIndex, 'attori')
    );
  }

  // Scroll tutti i campi che necessitano di scroll per una fase
  scrollAllFieldsUp(faseIndex: number): void {
    if (this.fieldNeedsScroll(faseIndex, 'descrizione')) {
      this.scrollFieldUp(faseIndex, 'descrizione');
    }
    if (this.fieldNeedsScroll(faseIndex, 'attivita')) {
      this.scrollFieldUp(faseIndex, 'attivita');
    }
    if (this.fieldNeedsScroll(faseIndex, 'attori')) {
      this.scrollFieldUp(faseIndex, 'attori');
    }
  }

  scrollAllFieldsDown(faseIndex: number): void {
    if (this.fieldNeedsScroll(faseIndex, 'descrizione')) {
      this.scrollFieldDown(faseIndex, 'descrizione');
    }
    if (this.fieldNeedsScroll(faseIndex, 'attivita')) {
      this.scrollFieldDown(faseIndex, 'attivita');
    }
    if (this.fieldNeedsScroll(faseIndex, 'attori')) {
      this.scrollFieldDown(faseIndex, 'attori');
    }
  }

  // Verifica se almeno un campo può scrollare su
  canAnyFieldScrollUp(faseIndex: number): boolean {
    return (
      this.canFieldScrollUp(faseIndex, 'descrizione') ||
      this.canFieldScrollUp(faseIndex, 'attivita') ||
      this.canFieldScrollUp(faseIndex, 'attori')
    );
  }

  // Verifica se almeno un campo può scrollare giù
  canAnyFieldScrollDown(faseIndex: number): boolean {
    return (
      this.canFieldScrollDown(faseIndex, 'descrizione') ||
      this.canFieldScrollDown(faseIndex, 'attivita') ||
      this.canFieldScrollDown(faseIndex, 'attori')
    );
  }

  trackByFaseId(index: number, item: any): any {
    return item.get('id')?.value || index;
  }

  handleOpenModalDelete(element: any) {
    this.openModalDelete = true;
    this.elementToDelete = element;
  }
  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = null;
  }

  handleEditFase(fase: FaseDTO) {
    this.faseToEdit = fase;
    this.openFasiModal = true;
  }

  getActionsFor(index: number): IVerticalEllipsisActions[] {
    const actualIndex = (this.currentPage() - 1) * this.itemsPerPage + index;
    const faseControl = this.fasi.at(actualIndex);

    // Verifica che la fase esista ancora (potrebbe essere stata eliminata)
    if (!faseControl) {
      return [];
    }

    const fase = faseControl.value;

    return [
      {
        label: 'Modifica',
        callback: () => {
          this.handleEditFase(fase);
          console.log('Apertura modale per modifica fase:', { id: fase.id, index: actualIndex });
        },
      },
      {
        label: 'Elimina',
        callback: () => this.handleOpenModalDelete(fase),
      },
    ];
  }

  handleRemoveFase(fase: FaseDTO): void {
    const faseId = fase.id;
    console.log('Eliminazione fase con ID:', faseId);
    // Se la fase ha un ID, significa che è stata salvata sul backend
    if (faseId) {
      // Chiama il backend per eliminarla
      this.faseService.deleteFase(faseId).subscribe({
        next: () => {
          this.toastService.success('Fase eliminata con successo');
          this.handleCloseModalDelete();
          // Il reload viene gestito automaticamente dal service via reloadSezione22AndUpdateSession()
          // che triggera la subscription nel parent component per ricreare il form
        },
        error: (err) => {
          console.error("Errore nell'eliminazione della fase:", err);
          this.toastService.error("Errore durante l'eliminazione della fase");
          this.handleCloseModalDelete();
        },
      });
    } else {
      // Se non ha ID, è solo locale, cerca l'indice e rimuovila dal FormArray
      const index = this.fasi.controls.findIndex(
        (control) => control.get('denominazione')?.value === fase.denominazione
      );
      if (index !== -1) {
        this.fasi.removeAt(index);
      }
      this.handleCloseModalDelete();
    }
  }

  handleAddFase(): void {
    const modalBody = this.child as ModalBodyFaseComponent;
    if (modalBody && modalBody.formGroup && modalBody.formGroup.valid) {
      const formValue = modalBody.formGroup.value;

      // Logica per la denominazione:
      // Se il campo 'denominazione' esiste (perché l'utente ha scelto "Aggiungi nuova"), usa quello
      // Altrimenti usa il valore di denominazioneDropdown
      const denominazione =
        formValue.denominazione != null ? formValue.denominazione : formValue.denominazioneDropdown;

      const faseDTO: FaseDTO = {
        id: formValue.id || null,
        idSezione22: formValue.idSezione22 || this.idSezione22,
        denominazione: denominazione,
        descrizione: formValue.descrizione,
        tempi: formValue.tempi,
        attore: formValue.attore,
        attivita: formValue.attivita,
      };

      this.faseService.save(faseDTO).subscribe({
        next: () => {
          this.toastService.success('Fase salvata con successo');
          this.openFasiModal = false;
          this.faseToEdit = undefined;
          this.child?.formGroup.reset();
        },
        error: (err) => {
          console.error('Errore nel salvare la fase:', err);
          this.toastService.error('Errore nel salvare la fase');
          this.child?.formGroup.reset();
        },
      });
    }
  }

  handleSortList() {
    this.isSortedManually = true;
    const formArray = this.formGroup?.get('fasi') as FormArray;
    const controls = [...formArray.controls];
    const sorted = controls.sort((a, b) => {
      const valueA = a.value;
      const valueB = b.value;
      const denominazioneA = valueA?.denominazione?.toLowerCase() || '';
      const denominazioneB = valueB?.denominazione?.toLowerCase() || '';

      if (this.sortAscending) {
        return denominazioneA.localeCompare(denominazioneB);
      } else {
        return denominazioneB.localeCompare(denominazioneA);
      }
    });

    formArray.clear();
    sorted.forEach((control) => formArray.push(control));

    this.sortAscending = !this.sortAscending;
  }

  handleOpenFaseModal(): void {
    this.openFasiModal = true;
    this.faseToEdit = undefined;
  }

  onPageChange(page: number): void {
    this.currentPage.set(page);
  }

  getAttivitaArray(fase: AbstractControl): FormArray {
    return fase.get('attivita.properties') as FormArray;
  }

  getAttoreArray(fase: AbstractControl): FormArray {
    return fase.get('attore.properties') as FormArray;
  }

  get fasi(): FormArray {
    const formArray = this.formGroup?.get('fasi') as FormArray;

    // Se l'utente ha fatto un sort manuale, non riordinare
    if (this.isSortedManually) {
      return formArray;
    }

    // Altrimenti ordina i controlli per id (dal più basso al più alto)
    // Gli elementi senza id vanno alla fine

    // Se l'array non esiste, crea uno nuovo
    if (!formArray) {
      return this.fb.array([]);
    }

    // Se l'array è vuoto, restituisci l'array originale senza modifiche
    if (formArray.length === 0) {
      return formArray;
    }

    const controls = formArray.controls.slice() as FormGroup[];
    controls.sort((a, b) => {
      const idA = a.get('id')?.value;
      const idB = b.get('id')?.value;

      // Se entrambi sono null/undefined, mantieni l'ordine
      if (idA == null && idB == null) return 0;
      // Se solo A è null/undefined, mettilo dopo
      if (idA == null) return 1;
      // Se solo B è null/undefined, mettilo dopo
      if (idB == null) return -1;

      // Altrimenti ordina per id crescente
      return idA - idB;
    });

    // Ricostruisci il FormArray con i controlli ordinati
    formArray.clear();
    controls.forEach((control) => formArray.push(control));

    return formArray;
  }
}
