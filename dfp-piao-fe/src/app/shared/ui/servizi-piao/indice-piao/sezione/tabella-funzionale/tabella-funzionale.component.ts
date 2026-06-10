import { Component, inject, Input, OnInit, ChangeDetectorRef } from '@angular/core';
import { SharedModule } from '../../../../../module/shared/shared.module';
import { FormGroup, FormArray, FormBuilder } from '@angular/forms';
import { BaseComponent } from '../../../../../components/base/base.component';
import { ModalComponent } from '../../../../../components/modal/modal.component';
import { ModalTabellaFunzionaleComponent } from './modal-tabella-funzionale/modal-tabella-funzionale.component';
import { ModalDeleteComponent } from '../../../../../components/modal-delete/modal-delete.component';
import { SvgComponent } from '../../../../../components/svg/svg.component';
import { AzioniComponent } from '../../../../../components/azioni/azioni.component';
import { IVerticalEllipsisActions } from '../../../../../models/interfaces/vertical-ellipsis-actions';
import { TabellaFunzionaleDTO } from '../../../../../models/classes/tabella-funzionale-dto';
import { TabellaFunzionaleService } from '../../../../../services/tabella-funzionale.service';
import { SessionStorageService } from '../../../../../services/session-storage.service';
import { PIAODTO } from '../../../../../models/classes/piao-dto';
import { KEY_PIAO, SHAPE_ICON } from '../../../../../utils/constants';
import { LabelValue } from '../../../../../models/interfaces/label-value';
import { OvpService } from '../../../../../services/ovp.service';
import { OVPDTO } from '../../../../../models/classes/ovp-dto';
import { getChangedFields } from '../../../../../utils/utils';
import { SectionEnum } from '../../../../../models/enums/section.enum';
import { takeUntil } from 'rxjs';

@Component({
  selector: 'piao-tabella-funzionale',
  imports: [
    SharedModule,
    ModalComponent,
    ModalTabellaFunzionaleComponent,
    ModalDeleteComponent,
    SvgComponent,
    AzioniComponent,
  ],
  templateUrl: './tabella-funzionale.component.html',
  styleUrl: './tabella-funzionale.component.scss',
})
export class TabellaFunzionaleComponent extends BaseComponent implements OnInit {
  @Input() form!: FormGroup;
  @Input() codTipologiaFK!: string;
  @Input() idEntitaFK!: number;
  @Input() testoSezione!: string;
  @Input() isDettaglio: boolean = false;

  tabellaFunzionaleService = inject(TabellaFunzionaleService);
  fb = inject(FormBuilder);
  private ovpService = inject(OvpService);
  private cdr = inject(ChangeDetectorRef);

  private ovpMap: Map<number, OVPDTO> = new Map();

  icon: string = SHAPE_ICON;
  iconStyle: string = 'icon-modal';

  piaoDTO!: PIAODTO;
  tabellaToEdit?: TabellaFunzionaleDTO;
  tabellaIndex: number = 0;
  openModalTabella = false;

  titleElenco: string = 'TABELLA_FUNZIONALE.TITLE_ELENCO';
  labelNotFound: string = 'TABELLA_FUNZIONALE.LABEL_NOT_FOUND';
  labelAddElenco: string = 'TABELLA_FUNZIONALE.LABEL_ADD_ELENCO';

  openModalDelete: boolean = false;
  elementToDelete: any = null;
  ovpOptions: LabelValue[] = [];
  stakeholderOptions: LabelValue[] = [];

  // Mappe di lookup O(1) per evitare .find() ad ogni CD cycle dentro al template.
  // Popolate da loadOvpOptions / initStakeholderOptions.
  private ovpLabelById = new Map<number, string>();
  private ovpCodiceById = new Map<number, string>();
  private stakeholderLabelById = new Map<number, string>();

  // Cache delle azioni vertical-ellipsis per AbstractControl (stessa riga -> stesso array,
  // stesse callback). Evita di creare nuovi riferimenti ad ogni CD cycle.
  private actionsCache = new WeakMap<object, IVerticalEllipsisActions[]>();

  // Fallback stabile per il getter `tabelleFunzionaliArray` (evita di creare un nuovo
  // FormArray ad ogni accesso quando `form` non è ancora disponibile).
  private readonly emptyFormArray: FormArray = this.fb.array([]);

  codSuffix: string = '';

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
    this.tabellaIndex = this.tabelleFunzionaliArray.length;
    this.loadOvpOptions();
    this.initStakeholderOptions();

    switch (this.codTipologiaFK) {
      case SectionEnum.SEZIONE_3_1:
        this.codSuffix = 'ORG';
        break;
      case SectionEnum.SEZIONE_3_2:
        this.codSuffix = 'LA';
        break;
      case SectionEnum.SEZIONE_3_3_1:
        this.codSuffix = 'FABB';
        break;
      case SectionEnum.SEZIONE_3_3_2:
        this.codSuffix = 'FORM';
        break;
    }

    // Ordina i controls una volta dopo l'init e ad ogni modifica strutturale del FormArray
    // (es. quando il parent ricostruisce l'array dopo un salvataggio).
    // Il sort è idempotente e usa { emitEvent: false } per evitare loop di change detection.
    this.sortTabelleFunzionali();
    const tabelleFunzionali = this.form.get('tabelleFunzionali') as FormArray | null;
    tabelleFunzionali?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.sortTabelleFunzionali());
  }

  private sortTabelleFunzionali(): void {
    const formArray = this.form.get('tabelleFunzionali') as FormArray | null;
    if (!formArray || formArray.length <= 1) return;

    const current = formArray.controls.slice() as FormGroup[];
    const sorted = [...current].sort((a, b) => {
      const idA = a.get('id')?.value;
      const idB = b.get('id')?.value;
      if (idA == null && idB == null) return 0;
      if (idA == null) return 1;
      if (idB == null) return -1;
      return idA - idB;
    });

    // Guard: se l'ordine è già corretto non mutare, evita CD loop
    const alreadySorted = current.every((c, i) => c === sorted[i]);
    if (alreadySorted) return;

    formArray.clear({ emitEvent: false });
    sorted.forEach((control) => formArray.push(control, { emitEvent: false }));
  }

  private loadOvpOptions(): void {
    this.ovpService
      .getAllOvpByIdPiao(this.piaoDTO.id || -1)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (ovpList: OVPDTO[]) => {
          this.ovpOptions = this.getOvpDropdownOptions(ovpList);
          // Crea la mappa degli OVP per lookup veloce
          ovpList.forEach((ovp) => {
            if (ovp.id) {
              this.ovpMap.set(ovp.id, ovp);
            }
          });
          // Popola le mappe di lookup usate dai metodi chiamati dal template (CD-safe O(1))
          this.ovpLabelById = new Map(this.ovpOptions.map((o) => [o.value as number, o.label]));
          this.ovpCodiceById = new Map(
            this.ovpOptions.map((o) => [o.value as number, (o.additionalField as string) || ''])
          );
          // Il parent (es. Sezione331Component) usa OnPush: senza markForCheck il subtree
          // non viene re-renderizzato dopo la risposta async e le celle OVP restano vuote.
          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error('Errore nel caricamento degli OVP:', err);
        },
      });
  }

  private getOvpDropdownOptions(ovpList: OVPDTO[]): LabelValue[] {
    return ovpList.map((ovp) => ({
      label: ovp.denominazione || '',
      additionalField: ovp.codice || '',
      value: ovp.id || 0,
    }));
  }

  private initStakeholderOptions(): void {
    // Inizializza le opzioni degli stakeholder (puoi sostituire con un servizio se necessario)
    this.stakeholderOptions =
      this.piaoDTO.stakeHolders?.map((stakeholder) => ({
        label: stakeholder.nomeStakeHolder || '',
        value: stakeholder.id || 0,
      })) || [];
    // Mappa lookup O(1) per template binding
    this.stakeholderLabelById = new Map(
      this.stakeholderOptions.map((s) => [s.value as number, s.label])
    );
  }

  getOVPLabel(idOVP: number | undefined): string {
    if (!idOVP) return '';
    return this.ovpLabelById.get(idOVP) || '';
  }

  getStakeholderLabel(idStakeholder: number | undefined): string {
    if (!idStakeholder) return '';
    return this.stakeholderLabelById.get(idStakeholder) || '';
  }

  handleAddTabellaFunzionale(): void {
    const formValue = {
      ...this.child.formGroup.value,
      idPiao: this.piaoDTO.id,
      testoSezione: this.testoSezione,
      campiModificati: getChangedFields(
        this.child.formGroup.value,
        this.tabellaToEdit,
        ['id', 'idEntitaFK', 'codTipologiaFK'],
        'tabelleFunzionali'
      ),
    };
    this.tabellaFunzionaleService
      .save(formValue)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.toastService.success('Tabella funzionale salvata con successo');
          this.child?.formGroup.reset();
        },
        error: (err) => {
          console.error('Errore nel salvare la tabella funzionale:', err);
          this.child?.formGroup.reset();
        },
      });
    this.openModalTabella = false;
    this.tabellaToEdit = undefined;
    this.tabellaIndex = this.tabelleFunzionaliArray.length;
  }

  trackByTabellaId(index: number, item: any): any {
    return item.get('id')?.value || index;
  }

  getActionsFor(index: number): IVerticalEllipsisActions[] {
    const control = this.tabelleFunzionaliArray.at(index);

    if (!control) {
      return [];
    }

    // Cache per identità del control: stessa riga -> stesso array stabile.
    const cached = this.actionsCache.get(control);
    if (cached) return cached;

    const tabella = control.value;
    const actions: IVerticalEllipsisActions[] = [
      {
        label: 'Modifica',
        callback: () => {
          this.handleEditTabella(tabella, index);
        },
      },
      {
        label: 'Elimina',
        callback: () => this.handleOpenModalDelete(tabella),
      },
    ];
    this.actionsCache.set(control, actions);
    return actions;
  }

  handleOpenTabellaModal(): void {
    this.tabellaToEdit = undefined;
    this.openModalTabella = true;
    this.tabellaIndex = this.tabelleFunzionaliArray.length;
  }

  handleEditTabella(tabella: TabellaFunzionaleDTO, index: number): void {
    this.tabellaToEdit = tabella;
    this.openModalTabella = true;
    this.tabellaIndex = index;
  }

  handleRemoveForm(tabella: TabellaFunzionaleDTO) {
    const index = this.tabelleFunzionaliArray.controls.findIndex(
      (control) => control.get('id')?.value === tabella.id
    );

    if (index === -1) {
      console.error('Tabella funzionale non trovata nel FormArray');
      this.handleCloseModalDelete();
      return;
    }

    if (tabella.id) {
      this.tabellaFunzionaleService
        .delete(tabella.id, this.piaoDTO.id || -1, this.codTipologiaFK, this.testoSezione)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.toastService.success('Tabella funzionale eliminata con successo');
          },
          error: (err) => {
            console.error("Errore nell'eliminazione della tabella funzionale:", err);
          },
        });
    } else {
      this.toastService.success('Tabella funzionale eliminata con successo');
    }

    this.handleCloseModalDelete();
  }

  handleOpenModalDelete(element: any) {
    this.openModalDelete = true;
    this.elementToDelete = element;
  }

  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = undefined;
  }

  get tabelleFunzionaliArray(): FormArray {
    const formArray = this.form?.get('tabelleFunzionali') as FormArray | null;
    // Fallback stabile (stesso riferimento) per evitare nuovi FormArray ad ogni CD cycle
    return formArray ?? this.emptyFormArray;
  }

  setCodice(idOVP: number | undefined, index: number): string {
    if (!idOVP) return `${this.codSuffix}${index + 1}`;
    const codice = this.ovpCodiceById.get(idOVP) || '';
    if (codice && this.codSuffix) {
      return `${codice}_${this.codSuffix}${index + 1}`;
    }
    return `${this.codSuffix}${index + 1}`;
  }
}
