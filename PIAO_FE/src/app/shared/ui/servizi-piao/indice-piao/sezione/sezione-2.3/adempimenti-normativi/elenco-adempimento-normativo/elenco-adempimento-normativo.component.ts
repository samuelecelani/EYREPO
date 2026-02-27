import { Component, inject, Input, OnInit } from '@angular/core';
import { ModalComponent } from '../../../../../../../components/modal/modal.component';
import { SharedModule } from '../../../../../../../module/shared/shared.module';
import { BaseComponent } from '../../../../../../../components/base/base.component';
import { KEY_PIAO, PENCIL_ICON } from '../../../../../../../utils/constants';
import { SessionStorageService } from '../../../../../../../services/session-storage.service';
import { PIAODTO } from '../../../../../../../models/classes/piao-dto';
import { ModalAdempimentoNormativoComponent } from '../modal-adempimento-normativo/modal-adempimento-normativo.component';
import { FormGroup, FormArray, Form, FormBuilder } from '@angular/forms';
import { IVerticalEllipsisActions } from '../../../../../../../models/interfaces/vertical-ellipsis-actions';
import { AdempimentoNormativoDTO } from '../../../../../../../models/classes/adempimento-normativo-dto';
import { ModalDeleteComponent } from '../../../../../../../components/modal-delete/modal-delete.component';
import { SvgComponent } from '../../../../../../../components/svg/svg.component';
import { AzioniComponent } from '../../../../../../../components/azioni/azioni.component';
import { AdempimentoNormativoService } from '../../../../../../../services/adempimento-normativo.service';
import { ToastService } from '../../../../../../../services/toast.service';

@Component({
  selector: 'piao-elenco-adempimento-normativo',
  imports: [
    SharedModule,
    ModalComponent,
    ModalAdempimentoNormativoComponent,
    ModalDeleteComponent,
    SvgComponent,
    AzioniComponent,
  ],
  templateUrl: './elenco-adempimento-normativo.component.html',
  styleUrl: './elenco-adempimento-normativo.component.scss',
})
export class ElencoAdempimentoNormativoComponent extends BaseComponent implements OnInit {
  @Input() formGroup!: FormGroup;

  adempimentoNormativoService = inject(AdempimentoNormativoService);
  toastService = inject(ToastService);

  icon: string = PENCIL_ICON;
  iconStyle: string = 'icon-modal';

  piaoDTO!: PIAODTO;
  sessionStorageService: SessionStorageService = inject(SessionStorageService);
  adempimentoToEdit?: AdempimentoNormativoDTO;
  openModalElencoAdempimento = false;

  fb: FormBuilder = inject(FormBuilder);

  titleElencoAzioni: string = 'SEZIONE_23.ELENCO_AZIONI.TITLE_ELENCO_AZIONI';
  notFoundElencoAzioni: string = 'SEZIONE_23.ELENCO_AZIONI.NOT_FOUND_AZIONI';
  labelAddElencoAzioni: string = 'SEZIONE_23.ELENCO_AZIONI.ADD_AZIONE';

  openModalDelete: boolean = false;
  elementToDelete: any = null;

  sortAscending: { [key: string]: boolean } = {};
  isSortedManually: boolean = false;

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
    console.log(this.adempimentiNormativi);
  }

  handleSortList(type: string): void {
    // Mappa il tipo al campo corretto nel form
    const fieldMap: { [key: string]: string } = {
      normativa: 'normativa',
    };

    const fieldName = fieldMap[type] || type;

    // Inizializza l'ordinamento per questo campo se non esiste
    if (this.sortAscending[type] === undefined) {
      this.sortAscending[type] = true;
    }

    this.isSortedManually = true;
    const formArray = this.formGroup.get('adempimentiNormativi') as FormArray;
    const controls = [...formArray.controls];
    const sorted = controls.sort((a, b) => {
      const valueA = a.get(fieldName)?.value?.toLowerCase() || '';
      const valueB = b.get(fieldName)?.value?.toLowerCase() || '';

      if (this.sortAscending[type]) {
        return valueA.localeCompare(valueB);
      } else {
        return valueB.localeCompare(valueA);
      }
    });

    formArray.clear();
    sorted.forEach((control) => formArray.push(control, { emitEvent: false }));

    // Alterna l'ordinamento per il prossimo click
    this.sortAscending[type] = !this.sortAscending[type];
  }

  handleAddElencoAdempimentoNormativo(): void {
    this.adempimentoNormativoService.save(this.child.formGroup.value).subscribe({
      next: () => {
        this.toastService.success('Adempimento normativo salvato con successo');
        this.child?.formGroup.reset();
      },
      error: (err) => {
        console.error("Errore nel salvare l'Adempimento normativo:", err);
        this.child?.formGroup.reset();
      },
    });
    this.openModalElencoAdempimento = false;
    this.adempimentoToEdit = undefined;
  }

  trackByAttivitaId(index: number, item: any): any {
    return item.get('id')?.value || index;
  }

  getActionsFor(index: number): IVerticalEllipsisActions[] {
    const azioneControl = this.adempimentiNormativi.at(index);

    // Verifica che la fase esista ancora (potrebbe essere stata eliminata)
    if (!azioneControl) {
      return [];
    }

    const azione = azioneControl.value;

    return [
      {
        label: 'Modifica',
        callback: () => {
          this.handleEditAzione(azione);
          console.log('Apertura modale per modifica adempimento:', { id: azione.id, index });
        },
      },
      {
        label: 'Elimina',
        callback: () => this.handleOpenModalDelete(azione),
      },
    ];
  }

  handleOpenAzioneModal(): void {
    this.adempimentoToEdit = undefined;
    this.openModalElencoAdempimento = true;
  }

  handleEditAzione(azione: AdempimentoNormativoDTO): void {
    this.adempimentoToEdit = azione;
    this.openModalElencoAdempimento = true;
  }

  handleRemoveForm(adempimento: AdempimentoNormativoDTO) {
    // Trova l'index dell'adempimento nel FormArray
    const index = this.adempimentiNormativi.controls.findIndex(
      (control) => control.get('id')?.value === adempimento.id
    );

    if (index === -1) {
      console.error('Adempimento non trovato nel FormArray');
      this.handleCloseModalDelete();
      return;
    }

    //se ho l'id elimino puntuale, senno elimino dall'array
    if (adempimento.id) {
      this.adempimentoNormativoService.delete(adempimento.id).subscribe({
        next: () => {
          this.toastService.success('Adempimento normativo eliminato con successo');
          this.adempimentiNormativi.removeAt(index);
        },
        error: (err) => {
          console.error("Errore nell'eliminazione dell'Adempimento normativo:", err);
        },
      });
    } else {
      this.adempimentiNormativi.removeAt(index);
      this.toastService.success('Adempimento normativo eliminato con successo');
    }

    this.handleCloseModalDelete();
  }

  handleOpenModalDelete(element: any) {
    console.log('Apertura modale delete per adempimento normativo:', element);
    this.openModalDelete = true;
    this.elementToDelete = element;
  }

  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = undefined;
  }

  get adempimentiNormativi(): FormArray {
    const formArray = this.formGroup.get('adempimentiNormativi') as FormArray;

    // Se l'utente ha fatto un sort manuale, non riordinare
    if (this.isSortedManually) {
      return formArray;
    }

    // Se l'array non esiste, crea uno nuovo
    if (!formArray) {
      return this.fb.array([]);
    }

    // Se l'array è vuoto, restituisci l'array originale senza modifiche
    if (formArray.length === 0) {
      return formArray;
    }

    // Altrimenti ordina i controlli per id (dal più basso al più alto)
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
}
