import {
  Component,
  ElementRef,
  HostListener,
  inject,
  input,
  Input,
  OnInit,
  QueryList,
  ViewChild,
  ViewChildren,
} from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { AccordionComponent } from '../../../../../../components/accordion/accordion.component';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';
import { CardInfoComponent } from '../../../../../card-info/card-info.component';
import { PIAODTO } from '../../../../../../models/classes/piao-dto';
import { INPUT_REGEX, PENCIL_ICON } from '../../../../../../utils/constants';
import { getChangedFields, minArrayLength } from '../../../../../../utils/utils';
import { SvgComponent } from '../../../../../../components/svg/svg.component';
import { AzioniComponent } from '../../../../../../components/azioni/azioni.component';
import { ModalComponent } from '../../../../../../components/modal/modal.component';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';
import { IVerticalEllipsisActions } from '../../../../../../models/interfaces/vertical-ellipsis-actions';
import { ModalDatiPubblicazioneComponent } from '../dati-pubblicazione/modal-dati-pubblicazione/modal-dati-pubblicazione.component';
import { BaseComponent } from '../../../../../../components/base/base.component';
import { DatiPubblicatiDTO } from '../../../../../../models/classes/dati-pubblicati-dto';
import { ObbligoLeggeService } from '../../../../../../services/obbligo-legge.service';
import { DatiPubblicatiService } from '../../../../../../services/dati-pubblicati.service';
import { ObbligoLeggeDTO } from '../../../../../../models/classes/obbligo-legge-dto';
import { takeUntil } from 'rxjs';

@Component({
  selector: 'piao-denominazione-obbligo-legge',
  standalone: true,
  imports: [
    SharedModule,
    AccordionComponent,
    TextBoxComponent,
    CardInfoComponent,
    SvgComponent,
    AzioniComponent,
    ModalComponent,
    ModalDeleteComponent,
    ModalDatiPubblicazioneComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './denominazione-obbligo-legge.component.html',
  styleUrl: './denominazione-obbligo-legge.component.scss',
})
export class DenominazioneObbligoLeggeComponent extends BaseComponent implements OnInit {
  @Input() obblighiControls!: FormArray;
  @Input() idSezione23!: number;
  @Input() piaoDTO!: PIAODTO;
  @Input() testoSezione!: string;
  @Input() isDettaglio: boolean = false;

  @ViewChildren('obbligoBody') obbligoBodies!: QueryList<ElementRef>;

  /** Indice dell'obbligo in cui l'utente sta interagendo */
  editingObbligoIndex: number | null = null;
  /** Flag che indica se l'utente ha interagito con campi di un obbligo */
  isEditingObbligo: boolean = false;
  /** Snapshot JSON del valore dell'obbligo al momento del focus */
  private obbligoSnapshot: string | null = null;

  private fb = inject(FormBuilder);
  private obbligoLeggeService = inject(ObbligoLeggeService);
  private datiPubblicatiService = inject(DatiPubblicatiService);

  // Icons
  icon: string = PENCIL_ICON;
  iconStyle: string = 'icon-modal';

  // Labels for translations
  labelAccordionTitle: string = 'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.ACCORDION_TITLE';
  labelObbligoLeggeNotFound: string =
    'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.OBBLIGO_LEGGE_NOT_FOUND';
  labelIntroText: string = 'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.INTRO_TEXT';
  labelIntroTextDettaglio: string = 'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.INTRO_TEXT_DETTAGLIO';
  labelDenominazione: string = 'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.DENOMINAZIONE_LABEL';
  labelDescrizione: string = 'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.DESCRIZIONE_LABEL';
  labelElencoDati: string = 'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.ELENCO_DATI_LABEL';
  labelElencoDatiDettaglio: string =
    'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.ELENCO_DATI_LABEL_DETTAGLIO';
  labelNotFoundDati: string = 'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.NOT_FOUND_DATI';
  labelAddDati: string = 'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.ADD_DATI';
  titleCardInfoObbligo: string = 'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.CARD_INFO_TITLE';
  titleCardInfoNotFoundObbligo: string = 'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.CARD_INFO_NOT_FOUND';
  subTitleAddObbligo: string = 'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.CARD_INFO_ADD';

  // Table headers
  thDatoPubblicare: string = 'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.TH.DATO_PUBBLICARE';
  thTipologiaDato: string = 'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.TH.TIPOLOGIA_DATO';
  thResponsabile: string = 'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.TH.RESPONSABILE';
  thTerminiScadenza: string = 'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.TH.TERMINI_SCADENZA';
  thModalitaMonitoraggio: string = 'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.TH.MODALITA_MONITORAGGIO';
  thMotivazioneImpossibilita: string =
    'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.TH.MOTIVAZIONE_IMPOSSIBILITA';
  thAzioni: string = 'SEZIONE_23.TRASPARENZA.OBBLIGO_LEGGE.TH.AZIONI';

  // Modal state
  openModalDatiPubblicati: boolean = false;
  openModalDeleteObbligo: boolean = false;
  openModalDeleteDato: boolean = false;
  currentObbligoIndex: number = -1;
  currentDatoIndex: number = -1;
  currentDatoEditIndex: number = -1; // Indice del dato in modifica
  datiPubblicatiToEdit?: DatiPubblicatiDTO;
  elementToDelete: any = null;

  // Sort state
  sortAscending: { [key: string]: boolean } = {};

  openAccordionIndex: number | null = null;

  savedObblighi: any[] = [];

  ngOnInit(): void {
    this.savedObblighi = structuredClone(this.obblighi?.value ?? []);
  }

  /** Chiamato dal (focusin) nel template per marcare che l'utente sta editando uno specifico obbligo */
  onObbligoFocusIn(obbligoIndex: number): void {
    if (this.editingObbligoIndex !== obbligoIndex) {
      // Se stavamo editando un altro obbligo, auto-save prima di switchare
      if (this.isEditingObbligo && this.editingObbligoIndex !== null) {
        const currentSnapshot = this.getObbligoSnapshotJson(this.editingObbligoIndex);
        if (this.obbligoSnapshot !== null && currentSnapshot !== this.obbligoSnapshot) {
          this.autoSaveSingoloObbligo(this.editingObbligoIndex);
        }
      }
      this.editingObbligoIndex = obbligoIndex;
      this.obbligoSnapshot = this.getObbligoSnapshotJson(obbligoIndex);
    }
    this.isEditingObbligo = true;
  }

  /** Crea un JSON stringify del valore dell'obbligo */
  private getObbligoSnapshotJson(obbligoIndex: number): string | null {
    if (obbligoIndex >= this.obblighi.length) return null;
    const fg = this.obblighi.at(obbligoIndex) as FormGroup;
    return JSON.stringify(fg.value);
  }

  /** Rileva click fuori dall'obbligo attivo e triggera l'auto-save */
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.isEditingObbligo || this.editingObbligoIndex === null) return;

    // Ignora se ci sono modal aperti
    if (this.openModalDeleteObbligo || this.openModalDeleteDato || this.openModalDatiPubblicati)
      return;

    const target = event.target as HTMLElement;

    // Ignora click su elementi rimossi dal DOM (es. menu azioni CDK overlay già chiuso)
    if (!target.isConnected) return;

    // Ignora click dentro CDK overlay (es. menu azioni, popup ancora aperto)
    if (target.closest('.cdk-overlay-container')) return;

    const bodies = this.obbligoBodies?.toArray() || [];

    const activeObbligoBody = bodies.find((el) => el.nativeElement.contains(target));

    if (activeObbligoBody) {
      const oIdx = activeObbligoBody.nativeElement.getAttribute('data-obbligo-index');
      if (Number(oIdx) === this.editingObbligoIndex) {
        return; // Siamo dentro lo stesso obbligo, non fare nulla
      }
    }

    // Click fuori → auto-save solo se il form è cambiato rispetto allo snapshot
    const currentSnapshot = this.getObbligoSnapshotJson(this.editingObbligoIndex);

    if (this.obbligoSnapshot !== null && currentSnapshot !== this.obbligoSnapshot) {
      this.autoSaveSingoloObbligo(this.editingObbligoIndex);
    }
    this.isEditingObbligo = false;
    this.editingObbligoIndex = null;
    this.obbligoSnapshot = null;
  }

  /** Salva un singolo obbligo di legge se dirty e con id */
  private autoSaveSingoloObbligo(obbligoIndex: number): void {
    if (obbligoIndex >= this.obblighi.length) return;

    const fg = this.obblighi.at(obbligoIndex) as FormGroup;
    const obj = { ...fg.value };

    // Confronta PRIMA della trasformazione stakeholders, così entrambi i lati sono array di ID piatti
    let campiModificati = getChangedFields(
      obj,
      this.savedObblighi?.[obbligoIndex],
      ['id', 'datiPubblicati', 'idSezione23'], // campi da escludere dal confronto
      `obblighiLegge`
    );
    const obiettiviRequest = {
      ...obj,
      idPiao: this.piaoDTO.id,
      testoSezione: this.testoSezione,
      campiModificati: campiModificati,
    };

    this.obbligoLeggeService.save(obiettiviRequest).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        fg.markAsPristine();
        this.savedObblighi = structuredClone(this.obblighi?.value ?? []);
        this.toastService.success('Obbligo di legge salvato con successo');
      },
      error: (err) => {
        console.error("Errore nel salvataggio automatico dell'obbligo di legge:", err);
      },
    });
  }

  /**
   * Getter per il FormArray degli obblighi
   */
  get obblighi(): FormArray {
    const formArray = this.obblighiControls as FormArray;
    // Se l'array non esiste, crea uno nuovo
    if (!formArray) {
      return this.fb.array([]);
    }

    // Se l'array è vuoto, restituisci l'array originale senza modifiche
    if (formArray.length === 0) {
      return formArray;
    }

    // Ordina i controlli per id (dal più basso al più alto)
    // Gli elementi senza id vanno alla fine
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
   * Rimuove un obbligo dal FormArray
   */
  handleRemoveObbligo(index: number): void {
    this.currentObbligoIndex = index;
    this.openModalDeleteObbligo = true;
  }

  /**
   * Conferma rimozione obbligo
   */
  confirmRemoveObbligo(): void {
    if (this.currentObbligoIndex >= 0) {
      const obbligo = this.obblighi.at(this.currentObbligoIndex) as FormGroup;
      const obbligoId = obbligo.get('id')?.value;

      if (obbligoId) {
        // Se l'obbligo ha un ID, chiamare il backend per eliminarlo
        this.obbligoLeggeService
          .delete(obbligoId, this.piaoDTO.id || -1, this.testoSezione)
          .pipe(takeUntil(this.destroy$)).subscribe({
            next: () => {
              this.toastService.success('Obbligo di legge eliminato con successo');
              this.obblighi.removeAt(this.currentObbligoIndex);
              this.savedObblighi = structuredClone(this.obblighi?.value ?? []);
              this.closeModalDeleteObbligo();
            },
            error: (error) => {
              console.error(
                '[DenominazioneObbligoLegge] Errore durante eliminazione obbligo:',
                error
              );
              this.toastService.error("Errore durante l'eliminazione dell'obbligo");
              this.closeModalDeleteObbligo();
            },
          });
      } else {
        // Se non ha ID, rimuovi solo dal FormArray (non ancora salvato sul backend)
        this.obblighi.removeAt(this.currentObbligoIndex);
        this.savedObblighi = structuredClone(this.obblighi?.value ?? []);
        this.toastService.success('Obbligo di legge eliminato con successo');
        this.closeModalDeleteObbligo();
      }
    } else {
      this.closeModalDeleteObbligo();
    }
  }

  /**
   * Chiude la modale di eliminazione obbligo
   */
  closeModalDeleteObbligo(): void {
    this.openModalDeleteObbligo = false;
    this.currentObbligoIndex = -1;
  }

  /**
   * Aggiunge un nuovo obbligo al FormArray
   */
  handleAddObbligo(): void {
    const newObbligo = this.fb.group({
      id: [null],
      idSezione23: [this.idSezione23],
      denominazione: [
        null,
        [Validators.required, Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
      ],
      descrizione: [null, [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]],
      datiPubblicati: (() => {
        const arr = this.fb.array<FormGroup>([]);
        arr.setValidators(minArrayLength(1));
        arr.updateValueAndValidity();
        return arr;
      })(),
    });

    this.obbligoLeggeService.save(newObbligo.value).pipe(takeUntil(this.destroy$)).subscribe({
      next: (data: any) => {
        newObbligo.get('id')?.setValue(data.id); // Aggiorna l'ID del nuovo obbligo con quello restituito dal backend
        //this.obblighi.push(newObbligo);
        this.openAccordionIndex = this.obblighi.length; // Apri l'accordion del nuovo obbligo
        this.toastService.success('Obbligo di legge aggiunto con successo');
      },
      error: (err) => {
        console.error("Errore nell'aggiunta dell'obbligo di legge:", err);
      },
    });
  }

  /**
   * Apre la modale per aggiungere/modificare dati pubblicati
   */
  handleOpenDatiPubblicatiModal(
    obbligoIndex: number,
    datoToEdit?: DatiPubblicatiDTO,
    datoIndex: number = -1
  ): void {
    this.currentObbligoIndex = obbligoIndex;
    this.datiPubblicatiToEdit = datoToEdit;
    this.currentDatoEditIndex = datoIndex;
    this.openModalDatiPubblicati = true;
  }

  /**
   * Chiude la modale dati pubblicati
   */
  closeDatiPubblicatiModal(): void {
    this.openModalDatiPubblicati = false;
    this.datiPubblicatiToEdit = undefined;
    this.currentObbligoIndex = -1;
    this.currentDatoEditIndex = -1;
  }

  /**
   * Conferma e salva i dati pubblicati tramite backend, poi aggiorna il FormArray locale.
   */
  confirmDatiPubblicati(): void {
    if (this.child && this.currentObbligoIndex >= 0) {
      const obbligo = this.obblighi.at(this.currentObbligoIndex) as FormGroup;
      const datiPubblicatiArray = obbligo.get('datiPubblicati') as FormArray;
      const formValue = this.child.formGroup.value;

      // Imposta l'idObbligoLegge dal parent
      const dto: DatiPubblicatiDTO = {
        ...formValue,
        idObbligoLegge: obbligo.get('id')?.value,
      };

      const dtoRequest = {
        ...dto,
        idPiao: this.piaoDTO.id,
        testoSezione: this.testoSezione,
        campiModificati: getChangedFields(
          formValue,
          this.datiPubblicatiToEdit,
          ['id', 'idObbligoLegge', 'key', 'externalId'], // campi da escludere dal confronto
          `datiPubblicati`
        ),
      };

      this.datiPubblicatiService.save(dtoRequest).pipe(takeUntil(this.destroy$)).subscribe({
        next: () => {
          // Aggiorna il FormArray locale
          const newDatoFormGroup = this.createDatoFormGroup(formValue);

          /*
          if (this.datiPubblicatiToEdit) {
            let editIndex = this.currentDatoEditIndex;

            if (editIndex < 0 && this.datiPubblicatiToEdit.id) {
              editIndex = datiPubblicatiArray.controls.findIndex(
                (ctrl) => ctrl.get('id')?.value === this.datiPubblicatiToEdit?.id
              );
            }

            if (editIndex >= 0) {
              datiPubblicatiArray.setControl(editIndex, newDatoFormGroup);
            }
          } else {
            datiPubblicatiArray.push(newDatoFormGroup);
          }
            */

          this.toastService.success('Dato pubblicato salvato con successo');
          this.closeDatiPubblicatiModal();
        },
        error: (err) => {
          console.error('[DenominazioneObbligoLegge] Errore nel salvataggio dato pubblicato:', err);
          this.closeDatiPubblicatiModal();
        },
      });
    } else {
      this.closeDatiPubblicatiModal();
    }
  }

  /**
   * Crea un FormGroup per un dato pubblicato con la struttura corretta
   */
  private createDatoFormGroup(dato: any): FormGroup {
    return this.fb.group({
      id: [dato.id],
      idObbligoLegge: [dato.idObbligoLegge],
      denominazione: [
        dato.denominazione,
        [Validators.required, Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      tipologia: [dato.tipologia, [Validators.required]],
      responsabile: [
        dato.responsabile,
        [Validators.required, Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      terminiScadenza: [
        dato.terminiScadenza,
        [Validators.required, Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      modalitaMonitoraggio: [
        dato.modalitaMonitoraggio,
        [Validators.required, Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
      ],
      motivazioneImpossibilita: [
        dato.motivazioneImpossibilita,
        [Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
      ],
      ulterioriInfo: this.fb.group({
        id: [dato.ulterioriInfo?.id || null],
        externalId: [dato.ulterioriInfo?.externalId || null],
        properties: this.fb.array(
          (dato.ulterioriInfo?.properties || []).map((prop: any) =>
            this.fb.group({
              key: [prop.key],
              value: [prop.value],
            })
          )
        ),
      }),
    });
  }

  /**
   * Ottiene il FormArray dei dati pubblicati per un obbligo
   */
  getDatiPubblicati(obbligoIndex: number): FormArray {
    const obbligo = this.obblighi.at(obbligoIndex) as FormGroup;
    return obbligo.get('datiPubblicati') as FormArray;
  }

  /**
   * Ottiene l'ID dell'obbligo per un dato indice
   */
  getObbligoId(obbligoIndex: number): number {
    const obbligo = this.obblighi.at(obbligoIndex) as FormGroup;
    return obbligo.get('id')?.value || 0;
  }

  /**
   * Apre la modale per eliminare un dato di pubblicazione
   */
  handleOpenModalDeleteDato(obbligoIndex: number, datoIndex: number): void {
    this.currentObbligoIndex = obbligoIndex;
    this.currentDatoIndex = datoIndex;
    this.openModalDeleteDato = true;
  }

  /**
   * Conferma eliminazione dato pubblicato
   */
  confirmRemoveDato(): void {
    if (this.currentObbligoIndex >= 0 && this.currentDatoIndex >= 0) {
      const datiArray = this.getDatiPubblicati(this.currentObbligoIndex);
      const datoControl = datiArray.at(this.currentDatoIndex);
      const datoId = datoControl?.get('id')?.value;

      if (datoId) {
        // Se il dato ha un ID, chiamare il backend per eliminarlo
        this.datiPubblicatiService
          .delete(datoId, this.piaoDTO.id || -1, this.testoSezione)
          .pipe(takeUntil(this.destroy$)).subscribe({
            next: () => {
              this.toastService.success('Dato pubblicato eliminato con successo');
              this.closeModalDeleteDato();
            },
            error: (error) => {
              console.error('[DenominazioneObbligoLegge] Errore durante eliminazione dato:', error);
              this.toastService.error("Errore durante l'eliminazione del dato pubblicato");
              this.closeModalDeleteDato();
            },
          });
      } else {
        // Se non ha ID, rimuovi solo dal FormArray (non ancora salvato sul backend)
        datiArray.removeAt(this.currentDatoIndex);
        this.toastService.success('Dato pubblicato eliminato con successo');
        this.closeModalDeleteDato();
      }
    } else {
      this.closeModalDeleteDato();
    }
  }

  /**
   * Chiude la modale di eliminazione dato
   */
  closeModalDeleteDato(): void {
    this.openModalDeleteDato = false;
    this.currentObbligoIndex = -1;
    this.currentDatoIndex = -1;
  }

  /**
   * Ottiene le azioni per un dato pubblicato
   */
  getActionsForDato(obbligoIndex: number, datoIndex: number): IVerticalEllipsisActions[] {
    const datiArray = this.getDatiPubblicati(obbligoIndex);
    const datoControl = datiArray.at(datoIndex);

    if (!datoControl) {
      return [];
    }

    const dato = datoControl.value;

    return [
      {
        label: 'Modifica',
        callback: () => {
          this.handleOpenDatiPubblicatiModal(obbligoIndex, dato, datoIndex);
        },
      },
      {
        label: 'Elimina',
        callback: () => this.handleOpenModalDeleteDato(obbligoIndex, datoIndex),
      },
    ];
  }

  /**
   * Ordina la tabella dei dati pubblicati
   */
  handleSortList(obbligoIndex: number, field: string): void {
    const sortKey = `${obbligoIndex}_${field}`;

    if (this.sortAscending[sortKey] === undefined) {
      this.sortAscending[sortKey] = true;
    }

    const datiArray = this.getDatiPubblicati(obbligoIndex);
    const controls = [...datiArray.controls];

    const sorted = controls.sort((a, b) => {
      const valueA = a.get(field)?.value?.toLowerCase() || '';
      const valueB = b.get(field)?.value?.toLowerCase() || '';

      if (this.sortAscending[sortKey]) {
        return valueA.localeCompare(valueB);
      } else {
        return valueB.localeCompare(valueA);
      }
    });

    datiArray.clear();
    sorted.forEach((control) => datiArray.push(control, { emitEvent: false }));

    this.sortAscending[sortKey] = !this.sortAscending[sortKey];
  }

  /**
   * Track function per ngFor
   */
  trackByObbligoId(index: number, item: any): any {
    return item.get('id')?.value || index;
  }

  /**
   * Track function per ngFor dati
   */
  trackByDatoId(index: number, item: any): any {
    return item.get('id')?.value || index;
  }
}
