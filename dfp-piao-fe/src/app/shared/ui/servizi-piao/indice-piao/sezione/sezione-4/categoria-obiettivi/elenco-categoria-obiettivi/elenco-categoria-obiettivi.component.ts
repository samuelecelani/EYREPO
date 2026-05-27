import { Component, inject, Input, OnInit } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormGroup } from '@angular/forms';
import { SharedModule } from '../../../../../../../module/shared/shared.module';
import { ModalDeleteComponent } from '../../../../../../../components/modal-delete/modal-delete.component';
import { AzioniComponent } from '../../../../../../../components/azioni/azioni.component';
import { SvgComponent } from '../../../../../../../components/svg/svg.component';
import { IVerticalEllipsisActions } from '../../../../../../../models/interfaces/vertical-ellipsis-actions';
import { SessionStorageService } from '../../../../../../../services/session-storage.service';
import { PIAODTO } from '../../../../../../../models/classes/piao-dto';
import { KEY_PIAO, PENCIL_ICON } from '../../../../../../../utils/constants';
import { LabelValue } from '../../../../../../../models/interfaces/label-value';
import { ModalComponent } from '../../../../../../../components/modal/modal.component';
import { ModalCategoriaObiettiviComponent } from '../modal-categoria-obiettivi/modal-categoria-obiettivi.component';
import { BaseComponent } from '../../../../../../../components/base/base.component';
import { CategoriaObiettiviDTO } from '../../../../../../../models/classes/categoria-obiettivi-dto';
import { CategoriaObiettivoService } from '../../../../../../../services/categoria-obiettivo.service';
import { CodTipologiaSezioneEnum } from '../../../../../../../models/enums/cod-tipologia-sezione.enum';
import { CodTipologiaCategoriaEnum } from '../../../../../../../models/enums/cod-tipologia-categoria.enum';
import { PropertyAttivitaDTO } from '../../../../../../../models/classes/property-attivita-dto';
import { getChangedFields } from '../../../../../../../utils/utils';

@Component({
  selector: 'piao-elenco-categoria-obiettivi',
  imports: [
    SharedModule,
    ModalDeleteComponent,
    AzioniComponent,
    SvgComponent,
    ModalComponent,
    ModalCategoriaObiettiviComponent,
  ],
  templateUrl: './elenco-categoria-obiettivi.component.html',
  styleUrl: './elenco-categoria-obiettivi.component.scss',
})
export class ElencoCategoriaObiettiviComponent extends BaseComponent implements OnInit {
  @Input() formGroup!: FormGroup;
  @Input() formArrayName!: string;
  @Input() codTipologiaFK!: CodTipologiaCategoriaEnum;
  @Input() sottofaseMonitoraggioDropdown!: LabelValue[];
  @Input() testoSezione!: string;
  @Input() isDettaglio: boolean = false;

  openModalDelete = false;
  elementToDelete: any = null;

  icon: string = PENCIL_ICON;
  iconStyle: string = 'icon-modal';

  titleElenco: string = 'SEZIONE_4.ACCORDION_CONTENT.ELENCO_CATEGORIA_OBIETTIVI';
  notFoundElenco: string = 'SEZIONE_4.ACCORDION_CONTENT.NON_INSERITE_CATEGORIE';
  labelAdd: string = 'SEZIONE_4.ACCORDION_CONTENT.INSERISCI_CATEGORIA_OBIETTIVI';

  sortAscending: { [key: string]: boolean } = {};
  isSortedManually = false;

  fb: FormBuilder = inject(FormBuilder);

  categoriaObiettiviSerivice = inject(CategoriaObiettivoService);

  piaoDTO!: PIAODTO;

  dropdownCategoriaObiettivi: LabelValue[] = [];

  openModalCategorieObiettivi: boolean = false;
  categoriaObiettiviToEdit?: CategoriaObiettiviDTO;

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
    this.initDropdownCategoriaObiettivi(this.codTipologiaFK);
  }

  get obiettiviArray(): FormArray {
    const formArray = this.formGroup?.get(this.formArrayName) as FormArray;

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

  handleSortList(type: string): void {
    if (this.sortAscending[type] === undefined) {
      this.sortAscending[type] = true;
    }

    this.isSortedManually = true;
    const formArray = this.formGroup?.get(this.formArrayName) as FormArray;
    const controls = [...formArray.controls];
    const sorted = controls.sort((a, b) => {
      const valueA = (a.get(type)?.value || '').toString().toLowerCase();
      const valueB = (b.get(type)?.value || '').toString().toLowerCase();

      return this.sortAscending[type] ? valueA.localeCompare(valueB) : valueB.localeCompare(valueA);
    });

    formArray.clear();
    sorted.forEach((control) => formArray.push(control, { emitEvent: false }));

    this.sortAscending[type] = !this.sortAscending[type];
  }

  getActionsFor(index: number): IVerticalEllipsisActions[] {
    const formArray = this.formGroup?.get(this.formArrayName) as FormArray;
    const obiettivoControl = formArray.at(index);

    if (!obiettivoControl) {
      return [];
    }

    return [
      {
        label: 'Modifica',
        callback: () => {
          this.handleEditCategoriaObiettivi(obiettivoControl.value);
        },
      },
      {
        label: 'Elimina',
        callback: () => this.handleOpenModalDelete({ index, value: obiettivoControl.value }),
      },
    ];
  }

  trackByObiettivoId(index: number, item: any): any {
    return item.get('id')?.value || index;
  }

  handleEditCategoriaObiettivi(categoriaObiettivi: CategoriaObiettiviDTO): void {
    if (this.isDettaglio) return;
    this.categoriaObiettiviToEdit = categoriaObiettivi;
    this.openModalCategorieObiettivi = true;
  }

  handleInsertObiettivo(): void {
    if (this.isDettaglio) return;
    this.openModalCategorieObiettivi = true;
  }

  handleOpenModalDelete(element: any): void {
    if (this.isDettaglio) return;
    this.openModalDelete = true;
    this.elementToDelete = element;
  }

  handleRemoveForm(element: any): void {
    const formArray = this.formGroup?.get(this.formArrayName) as FormArray;
    if (element.value && element.value.id) {
      this.categoriaObiettiviSerivice
        .delete(element.value.id, this.piaoDTO.id || -1, this.testoSezione, this.formArrayName)
        .subscribe({
          next: () => {
            this.toastService.success('Categoria obiettivi eliminata con successo');
          },
          error: () => {
            console.log("Errore durante l'eliminazione della categoria obiettivi");
          },
        });
    } else {
      formArray.removeAt(element.index);
      this.toastService.success('Categoria obiettivi eliminata con successo');
    }

    this.handleCloseModalDelete();
  }

  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = undefined;
  }

  initDropdownCategoriaObiettivi(codTipologiaFK: string): void {
    this.categoriaObiettiviSerivice.getAllCategoriaObiettiviTip(codTipologiaFK).subscribe({
      next: (data) => {
        this.dropdownCategoriaObiettivi = data.map((item) => ({
          label: item.testo || '',
          value: item.id || 0,
        }));
      },
      error: () => {
        console.log('Errore durante il caricamento delle categorie di obiettivi');
      },
    });
  }

  handleSaveCategoriaObiettivo(): void {
    const formValue = this.child.formGroup.value;
    console.log('Valore del form da salvare:', formValue);
    const categoriaToSave: CategoriaObiettiviDTO = {
      ...formValue,
      attivita: {
        ...formValue.attivita,
        propertyAttivita: (formValue.attivita?.propertyAttivita || []).map((p: any) => {
          const prop = new PropertyAttivitaDTO();
          prop.key = p.key;
          prop.value = p.value;
          prop.keyDateInizio = p.keyDateInizio;
          prop.keyDateFine = p.keyDateFine;
          prop.valueDateInizio = p.valueDateInizio ? new Date(p.valueDateInizio) : undefined;
          prop.valueDateFine = p.valueDateFine ? new Date(p.valueDateFine) : undefined;
          return prop;
        }),
      },
    };

    const categoriaRequest = {
      ...categoriaToSave,
      testoSezione: this.testoSezione,
      idPiao: this.piaoDTO.id || -1,
      campiModificati: getChangedFields(
        formValue,
        this.categoriaObiettiviToEdit,
        [
          'id',
          'idSezione4',
          'externalId',
          'key',
          'codTipologiaFk',
          'valueDateInizio',
          'valueDateFine',
        ], // campi da escludere dal confronto
        this.formArrayName
      ),
    };
    this.categoriaObiettiviSerivice.save(categoriaRequest).subscribe({
      next: () => {
        this.toastService.success('Categoria obiettivi salvata con successo');
      },
      error: () => {
        console.log('Errore durante il salvataggio della categoria obiettivi');
      },
    });
    this.handleCloseModalCategorieObiettivi();
  }

  handleCloseModalCategorieObiettivi(): void {
    this.openModalCategorieObiettivi = false;
    this.categoriaObiettiviToEdit = undefined;
  }

  getSottofaseLabel(id: number | null | undefined): string {
    if (id == null) return '';
    return this.sottofaseMonitoraggioDropdown.find((item) => item.value === id)?.label || '';
  }

  getCategoriaLabel(id: number | null | undefined): string {
    if (id == null) return '';
    return this.dropdownCategoriaObiettivi.find((item) => item.value === id)?.label || '';
  }
}
