import { Component, DestroyRef, ElementRef, HostListener, Input, OnChanges, OnInit, QueryList, SimpleChanges, ViewChildren, inject } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { AccordionComponent } from '../../../../../../components/accordion/accordion.component';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';
import { CardInfoComponent } from '../../../../../card-info/card-info.component';
import { IndicatoriComponent } from '../../indicatori/indicatori.component';
import { PIAODTO } from '../../../../../../models/classes/piao-dto';
import { ToastService } from '../../../../../../services/toast.service';
import { INPUT_REGEX, KEY_PIAO } from '../../../../../../utils/constants';
import {
  canAddToFormArray,
  createFormArrayGenericIndicatoreFromPiaoSession,
  estraiAnniDaDenominazione,
  getChangedFields,
  minArrayLength,
} from '../../../../../../utils/utils';
import { CodTipologiaDimensioneEnum } from '../../../../../../models/enums/cod-tipologia-dimensione.enum';
import { CodTipologiaIndicatoreEnum } from '../../../../../../models/enums/cod-tipologia-indicatore.enum';
import { SectionEnum } from '../../../../../../models/enums/section.enum';
import { SessionStorageService } from '../../../../../../services/session-storage.service';
import { ObiettivoPrevenzioneIndicatoreDTO } from '../../../../../../models/classes/obiettivo-prevenzione-indicatore-dto';
import { ObiettivoPrevenzioneService } from '../../../../../../services/obiettivo-prevenzione.service';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';
import { ObiettivoPrevenzioneDTO } from '../../../../../../models/classes/obiettivo-prevenzione-dto';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'piao-obiettivi-generali',
  standalone: true,
  imports: [
    SharedModule,
    AccordionComponent,
    TextBoxComponent,
    CardInfoComponent,
    IndicatoriComponent,
    ReactiveFormsModule,
    ModalDeleteComponent,
  ],
  templateUrl: './obiettivi-generali.component.html',
  styleUrl: './obiettivi-generali.component.scss',
})
export class ObiettiviGeneraliComponent implements OnInit, OnChanges {
  private destroyRef = inject(DestroyRef);
  @Input() obiettiviControls!: FormArray;
  @Input() idSezione23!: number;
  @Input() idPiao!: number;
  @Input() testoSezione!: string;
  @Input() isDettaglio: boolean = false;

  @ViewChildren('obiettivoBody') obiettivoBodies!: QueryList<ElementRef>;

  /** Indice dell'obiettivo in cui l'utente sta interagendo */
  editingObiettivoIndex: number | null = null;
  /** Flag che indica se l'utente ha interagito con campi di un obiettivo */
  isEditingObiettivo: boolean = false;
  /** Snapshot JSON del valore dell'obiettivo al momento del focus (senza codice) */
  private obiettivoSnapshot: string | null = null;

  // Labels for translations
  labelObiettivoPrefix: string = 'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.ACCORDION_TITLE';
  labelObiettivoNotFound: string = 'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.NOT_FOUND';
  labelIntroText: string = 'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.DESCRIPTION';
  labelIntroTextDettaglio: string =
    'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.DESCRIPTION_DETTAGLIO';
  labelIdObiettivo: string = 'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.ID_OBIETTIVO_LABEL';
  labelDenominazione: string = 'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.DENOMINAZIONE_LABEL';
  labelDescrizione: string = 'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.DESCRIZIONE_LABEL';
  titleCardInfoObiettivo: string = 'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.CARD_INFO_TITLE';
  titleCardInfoNotFoundObiettivo: string =
    'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.CARD_INFO_NOT_FOUND';
  subTitleAddObiettivo: string = 'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.CARD_INFO_ADD';

  private fb = inject(FormBuilder);
  private sessionStorageService = inject(SessionStorageService);
  toastService = inject(ToastService);
  obiettivoPrevenzioneService = inject(ObiettivoPrevenzioneService);

  // Configurazione indicatori
  codTipologiaFK: string = CodTipologiaDimensioneEnum.OBB_2_3;
  codTipologiaIndicatoreFK: string = CodTipologiaIndicatoreEnum.OBIETTIVO_GENERALE;
  sectionEnum: string = SectionEnum.SEZIONE_2_3;

  openModalDelete: boolean = false;
  elementToDelete: any = null;
  openAccordionIndex!: any;

  savedObbiettivi: any[] = [];

  ngOnInit(): void {
    this.savedObbiettivi = structuredClone(this.obiettivi?.value ?? []);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['obiettiviControls']) {
    }
  }

  /** Chiamato dal (focusin) nel template per marcare che l'utente sta editando uno specifico obiettivo */
  onObiettivoFocusIn(obiettivoIndex: number): void {
    if (this.editingObiettivoIndex !== obiettivoIndex) {
      // Se stavamo editando un altro obiettivo, auto-save prima di switchare
      if (this.isEditingObiettivo && this.editingObiettivoIndex !== null) {
        const currentSnapshot = this.getObiettivoSnapshotJson(this.editingObiettivoIndex);
        if (this.obiettivoSnapshot !== null && currentSnapshot !== this.obiettivoSnapshot) {
          this.autoSaveSingoloObiettivo(this.editingObiettivoIndex);
        }
      }
      this.editingObiettivoIndex = obiettivoIndex;
      this.obiettivoSnapshot = this.getObiettivoSnapshotJson(obiettivoIndex);
    }
    this.isEditingObiettivo = true;
  }

  /** Crea un JSON stringify del valore dell'obiettivo escludendo la property 'codice' */
  private getObiettivoSnapshotJson(obiettivoIndex: number): string | null {
    if (obiettivoIndex >= this.obiettivi.length) return null;
    const fg = this.obiettivi.at(obiettivoIndex) as FormGroup;
    const value = { ...fg.value };
    delete value.codice;
    return JSON.stringify(value);
  }

  /** Rileva click fuori dall'obiettivo attivo e triggera l'auto-save */
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.isEditingObiettivo || this.editingObiettivoIndex === null) return;

    // Ignora se ci sono modal aperti
    if (this.openModalDelete) return;

    const target = event.target as HTMLElement;

    // Ignora click su elementi rimossi dal DOM (es. menu azioni CDK overlay già chiuso)
    if (!target.isConnected) return;

    // Ignora click dentro CDK overlay (es. menu azioni, popup ancora aperto)
    if (target.closest('.cdk-overlay-container')) return;

    const bodies = this.obiettivoBodies?.toArray() || [];

    const activeObiettivoBody = bodies.find((el) => el.nativeElement.contains(target));

    if (activeObiettivoBody) {
      const oIdx = activeObiettivoBody.nativeElement.getAttribute('data-obiettivo-index');
      if (Number(oIdx) === this.editingObiettivoIndex) {
        return; // Siamo dentro lo stesso obiettivo, non fare nulla
      }
    }

    // Click fuori → auto-save solo se il form è cambiato rispetto allo snapshot
    const currentSnapshot = this.getObiettivoSnapshotJson(this.editingObiettivoIndex);

    if (this.obiettivoSnapshot !== null && currentSnapshot !== this.obiettivoSnapshot) {
      this.autoSaveSingoloObiettivo(this.editingObiettivoIndex);
    }
    this.isEditingObiettivo = false;
    this.editingObiettivoIndex = null;
    this.obiettivoSnapshot = null;
  }

  /** Salva un singolo obiettivo generale se dirty e con id */
  private autoSaveSingoloObiettivo(obiettivoIndex: number): void {
    if (obiettivoIndex >= this.obiettivi.length) return;

    const fg = this.obiettivi.at(obiettivoIndex) as FormGroup;
    const obj = { ...fg.value };
    let campiModificati = getChangedFields(
      obj,
      this.savedObbiettivi?.[obiettivoIndex],
      ['id', 'indicatori', 'key', 'externalId', 'tipologia', 'idSezione23'], // campi da escludere dal confronto
      `obiettivoPrevenzione`
    );

    // Controlla se sono stati eliminati indicatori rispetto al saved
    const savedIndicatori = this.savedObbiettivi?.[obiettivoIndex]?.indicatori || [];
    const currentIndicatori = fg.value?.indicatori || [];
    if (
      savedIndicatori.length > currentIndicatori.length ||
      savedIndicatori.length < currentIndicatori.length
    ) {
      campiModificati = campiModificati
        ? campiModificati + `,obiettivoPrevenzione.indicatori`
        : `obiettivoPrevenzione.indicatori`;
    }

    const obiettiviRequest = {
      ...obj,
      idPiao: this.idPiao,
      testoSezione: this.testoSezione,
      campiModificati: campiModificati,
    };

    this.obiettivoPrevenzioneService.save(obiettiviRequest).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        fg.markAsPristine();
        this.savedObbiettivi = structuredClone(this.obiettivi?.value ?? []);
        this.toastService.success('Obiettivo salvato con successo');
      },
      error: (err) => {
        console.error("Errore nel salvataggio automatico dell'obiettivo:", err);
      },
    });
  }

  /**
   * Imposta il codice dell'obiettivo in base all'indice (ANTG1, ANTG2, ecc.)
   */
  setCodice(control: any, index: number): any {
    control.setValue('ANTG' + (index + 1));
    return control;
  }

  /**
   * Rimuove un obiettivo dal FormArray
   */
  handleRemoveObiettivo(index: number): void {
    const obiettivo = this.obiettivi.at(index);
    const obiettivoId = obiettivo?.get('id')?.value;

    if (obiettivoId) {
      this.obiettivoPrevenzioneService
        .delete(obiettivoId, this.idPiao, this.testoSezione)
        .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
          next: () => {
            this.toastService.success('Obiettivo eliminato con successo');
            this.obiettivi.removeAt(index);
            this.savedObbiettivi = structuredClone(this.obiettivi?.value ?? []);
          },
          error: (err) => {
            console.error("Errore nell'eliminazione dell'obiettivo:", err);
          },
        });
    } else {
      // Se non ha ID, è solo locale, rimuovilo dal FormArray
      this.obiettivi.removeAt(index);
      this.savedObbiettivi = structuredClone(this.obiettivi?.value ?? []);
      this.toastService.success('Obiettivo eliminato con successo');
    }
    this.handleCloseModalDelete();
  }

  /**
   * Aggiunge un nuovo obiettivo al FormArray
   */
  handleAddObiettivo(): void {
    const newObiettivo = this.fb.group({
      id: [null],
      idSezione23: [this.idSezione23],
      codice: [
        null,
        [Validators.required, Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
      ],
      denominazione: [
        null,
        [Validators.required, Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
      ],
      descrizione: [null, [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]],
      indicatori: (() => {
        const formArray =
          createFormArrayGenericIndicatoreFromPiaoSession<ObiettivoPrevenzioneIndicatoreDTO>(
            this.fb,
            [],
            ['id', 'indicatore'],
            INPUT_REGEX
          );
        formArray?.setValidators([Validators.required, minArrayLength(1)]);
        return formArray;
      })(),
    });

    this.setCodice(newObiettivo.controls['codice'], this.obiettivi.length);
    this.obiettivoPrevenzioneService.save(newObiettivo.value).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data: any) => {
        this.toastService.success('Obiettivo aggiunto con successo');
        newObiettivo.get('id')?.setValue(data.id || null); // Simula un ID univoco per il nuovo obiettivo
        //this.obiettivi.push(newObiettivo);
        this.openAccordionIndex = this.obiettivi.length + 1;
      },
      error: (err) => {
        console.error("Errore nell'aggiunta dell'obiettivo:", err);
      },
    });
  }

  /**
   * Getter per il FormArray degli obiettivi
   */
  get obiettivi(): FormArray {
    const formArray = this.obiettiviControls as FormArray;
    // Ordina i controlli per id (dal più basso al più alto)
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

  /**
   * Track function per ngFor
   */
  trackByObiettivoId(index: number, item: any): any {
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
}
