import {
  Component,
  computed,
  DoCheck,
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
import { getChangedFields } from '../../utils/utils';
import { takeUntil } from 'rxjs';

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
export class FasiComponent
  extends BaseComponent
  implements OnInit, AfterViewInit, OnChanges, DoCheck
{
  @Input() formGroup!: FormGroup;
  @Input() idSezione22!: number;
  @Input() idPiao!: number;
  // Paginazione
  @Input() itemsPerPage = 5;
  @Input() maxItems: number = 2;
  @Input() pagination: boolean = true;
  @Input() testoSezione!: string;
  @Input() isDettaglio: boolean = false;

  @ViewChildren('descrizioneScroll') descrizioneScrolls!: QueryList<ElementRef<HTMLDivElement>>;
  @ViewChildren('attivitaScroll') attivitaScrolls!: QueryList<ElementRef<HTMLDivElement>>;
  @ViewChildren('attoriScroll') attoriScrolls!: QueryList<ElementRef<HTMLDivElement>>;

  private fb = inject(FormBuilder);
  private faseService = inject(FaseService);

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
    refetch: () => {},
    invalidate: () => {},
  }));

  // Getter per il numero totale di items (sostituisce il computed signal)
  get totalItems(): number {
    return this.fasi?.length || 0;
  }

  // Computed: le fasi paginate. Memoizzato su currentPage() + lunghezza fasi.
  // Nota: usiamo this.fasi?.controls?.slice direttamente perch\u00e9 controls \u00e8 un array
  // mutato in-place dal FormArray; la dipendenza da currentPage() basta per invalidare
  // il computed nei casi di cambio pagina. Per cambi di fasi.length (push/remove) il
  // componente parent ricrea il formGroup -> ngOnChanges che gi\u00e0 resetta lo stato.
  paginatedFasi = computed<AbstractControl[]>(() => {
    const start = (this.currentPage() - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    // tocca explicitly i signal di refresh per re-runnare
    this.fasiVersion();
    return this.fasi?.controls?.slice(start, end) || [];
  });

  // Fase correntemente visibile
  get currentVisibleFase(): AbstractControl | null {
    const fasi = this.paginatedFasi();
    const index = this.currentFaseIndex();
    return fasi[index] || null;
  }

  paginatedAlerts = computed<any[]>(() => {
    this.fasiVersion();
    const fasi = this.fasi?.value;
    if (!fasi) return [];

    if (!this.pagination) {
      if (this.maxItems && this.maxItems > 0) {
        return fasi.slice(0, this.maxItems);
      }
      return fasi;
    }

    const startIndex = (this.currentPage() - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    return fasi.slice(startIndex, endIndex);
  });

  // Counter incrementato quando le fasi (o il formGroup) cambiano,
  // per invalidare i computed sopra senza dover trasformare il FormArray in signal.
  private fasiVersion = signal(0);

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['formGroup']) {
      // Invalida i computed paginatedFasi/Alerts notificando il cambio FormArray
      this.fasiVersion.update((v) => v + 1);
      // Reset dello scroll states
      this.scrollStates.set(new Map());
      // Reinizializza lo stato di scroll dopo che la view è aggiornata
      this.scheduleTimeout(() => this.initAllScrollStates(), 100);
      // Reset della pagina corrente se necessario
      const totalPages = Math.ceil((this.fasi?.length || 0) / this.itemsPerPage);
      if (this.currentPage() > totalPages && totalPages > 0) {
        this.currentPage.set(totalPages);
      } else if (totalPages === 0) {
        this.currentPage.set(1);
      }
    }
  }

  /**
   * Il parent (Sezione22Component.reloadForm) usa `form.setControl('fasi', nuovoFormArray)`
   * per ricaricare le fasi dal BE: la reference di `formGroup` NON cambia, quindi
   * `ngOnChanges` non scatta. Senza notifica, `paginatedFasi` (computed) continuerebbe
   * a mostrare la slice del vecchio FormArray.
   *
   * `ngDoCheck` confronta ref + length del FormArray 'fasi':
   * - se cambia la reference (reload BE) o la length (add/remove fase) invalida i
   *   computed via `fasiVersion` e ri-ordina UNA SOLA VOLTA il FormArray.
   * - altrimenti costo O(1) (solo lookup + 2 confronti).
   *
   * Cosi' evitiamo che il getter `fasi` mutasse il FormArray (clear+push) ad ogni
   * binding/CD, che con CD default avrebbe avuto costo significativo e side-effect
   * indesiderati su valueChanges/statusChanges.
   */
  ngDoCheck(): void {
    const currentFasiRef = this.formGroup?.get('fasi') as FormArray | null;
    if (!currentFasiRef) return;

    const refChanged = currentFasiRef !== this._lastFasiRef;
    const lengthChanged = currentFasiRef.length !== this._lastFasiLength;
    if (!refChanged && !lengthChanged) return;

    this._lastFasiRef = currentFasiRef;
    this._lastFasiLength = currentFasiRef.length;

    // Riordina solo se l'utente non ha fatto un sort manuale via UI.
    if (!this.isSortedManually) {
      this.sortFasiByIdInPlace(currentFasiRef);
    }

    this.fasiVersion.update((v) => v + 1);

    const totalPages = Math.ceil((currentFasiRef.length || 0) / this.itemsPerPage);
    if (this.currentPage() > totalPages && totalPages > 0) {
      this.currentPage.set(totalPages);
    } else if (totalPages === 0) {
      this.currentPage.set(1);
    }
  }
  private _lastFasiRef: FormArray | null = null;
  private _lastFasiLength = -1;

  /**
   * Ordina i control del FormArray per id crescente (null/undefined in coda) in place.
   * Chiamato SOLO da ngDoCheck quando ref o length cambiano.
   * Salta la mutazione se l'ordine e' gia' corretto -> niente clear/push inutili
   * e niente valueChanges/statusChanges spuri sul form.
   */
  private sortFasiByIdInPlace(formArray: FormArray): void {
    if (formArray.length <= 1) return;
    const controls = formArray.controls.slice() as FormGroup[];
    controls.sort((a, b) => {
      const idA = a.get('id')?.value;
      const idB = b.get('id')?.value;
      if (idA == null && idB == null) return 0;
      if (idA == null) return 1;
      if (idB == null) return -1;
      return idA - idB;
    });
    let alreadySorted = true;
    for (let i = 0; i < controls.length; i++) {
      if (controls[i] !== formArray.controls[i]) {
        alreadySorted = false;
        break;
      }
    }
    if (alreadySorted) return;
    formArray.clear({ emitEvent: false });
    controls.forEach((control) => formArray.push(control, { emitEvent: false }));
  }

  ngAfterViewInit(): void {
    // Inizializza lo stato di scroll per tutti i campi dopo che la view è pronta
    this.scheduleTimeout(() => this.initAllScrollStates(), 100);
  }

  override ngOnDestroy(): void {
    this.pendingTimeouts.forEach((id) => clearTimeout(id));
    this.pendingTimeouts = [];
    super.ngOnDestroy();
  }

  private pendingTimeouts: number[] = [];
  private scheduleTimeout(fn: () => void, ms: number): void {
    const id = window.setTimeout(() => {
      this.pendingTimeouts = this.pendingTimeouts.filter((x) => x !== id);
      fn();
    }, ms);
    this.pendingTimeouts.push(id);
  }

  private initAllScrollStates(): void {
    const paginatedFasi = this.paginatedFasi();
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
      this.scheduleTimeout(() => this.updateScrollStateForField(faseIndex, fieldName), 100);
    }
  }

  scrollFieldDown(faseIndex: number, fieldName: string): void {
    const el = this.getScrollElementForField(faseIndex, fieldName);
    if (el) {
      el.scrollBy({ top: this.scrollStep, behavior: 'smooth' });
      this.scheduleTimeout(() => this.updateScrollStateForField(faseIndex, fieldName), 100);
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
    // Se la fase ha un ID, significa che è stata salvata sul backend
    if (faseId) {
      // Chiama il backend per eliminarla
      this.faseService
        .deleteFase(faseId, this.idPiao, this.testoSezione)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
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
        this.fasiVersion.update((v) => v + 1);
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

      const faseRequest = {
        ...faseDTO,
        idPiao: this.idPiao,
        testoSezione: this.testoSezione,
        campiModificati: getChangedFields(
          this.child?.formGroup.value,
          this.faseToEdit,
          ['id', 'idSezione22', 'externalId', 'key', 'denominazione'], // campi da escludere dal confronto
          'fase'
        ),
      };

      this.faseService
        .save(faseRequest)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
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
    return fase.get('attivita.propertyAttivita') as FormArray;
  }

  getAttoreArray(fase: AbstractControl): FormArray {
    return fase.get('attore.properties') as FormArray;
  }

  get fasi(): FormArray {
    // Getter "hot": chiamato da template binding e dai computed signal.
    // L'ordinamento avviene una sola volta in ngDoCheck quando cambia ref/length,
    // qui ci limitiamo a restituire il FormArray (o un array vuoto se assente).
    return (this.formGroup?.get('fasi') as FormArray) || this.fb.array([]);
  }
}
