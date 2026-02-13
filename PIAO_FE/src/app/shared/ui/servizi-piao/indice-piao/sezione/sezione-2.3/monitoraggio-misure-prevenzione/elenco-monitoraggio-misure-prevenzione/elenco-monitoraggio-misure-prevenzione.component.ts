import { Component, inject, Input, OnInit } from '@angular/core';
import { BaseComponent } from '../../../../../../../components/base/base.component';
import { KEY_PIAO, PENCIL_ICON } from '../../../../../../../utils/constants';
import { SessionStorageService } from '../../../../../../../services/session-storage.service';
import { PIAODTO } from '../../../../../../../models/classes/piao-dto';
import { ModalComponent } from '../../../../../../../components/modal/modal.component';
import { SharedModule } from '../../../../../../../module/shared/shared.module';
import { ModalMonitoraggioMisurePrevenzioneComponent } from '../modal-monitoraggio-misure-prevenzione/modal-monitoraggio-misure-prevenzione.component';
import { ModalDeleteComponent } from '../../../../../../../components/modal-delete/modal-delete.component';
import { AzioniComponent } from '../../../../../../../components/azioni/azioni.component';
import { SvgComponent } from '../../../../../../../components/svg/svg.component';
import { MonitoraggioPrevenzioneDTO } from '../../../../../../../models/classes/monitoraggio-prevenzione-dto';
import { FormArray, FormGroup } from '@angular/forms';
import { IVerticalEllipsisActions } from '../../../../../../../models/interfaces/vertical-ellipsis-actions';

@Component({
  selector: 'piao-elenco-monitoraggio-misure-prevenzione',
  imports: [
    SharedModule,
    ModalComponent,
    ModalMonitoraggioMisurePrevenzioneComponent,
    ModalDeleteComponent,
    AzioniComponent,
    SvgComponent,
  ],
  templateUrl: './elenco-monitoraggio-misure-prevenzione.component.html',
  styleUrl: './elenco-monitoraggio-misure-prevenzione.component.scss',
})
export class ElencoMonitoraggioMisurePrevenzioneComponent extends BaseComponent implements OnInit {
  @Input() formGroup!: FormGroup;
  openModalElencoMisurePrevenzione = false;

  icon: string = PENCIL_ICON;
  iconStyle: string = 'icon-modal';

  titleElencoModalita: string =
    'SEZIONE_23.MONITORAGGIO_MISURE.ELENCO_MODALITA_MONITORAGGIO.TITLE_MODALITA_MONITORAGGIO';
  notFoundElencoModalita: string =
    'SEZIONE_23.MONITORAGGIO_MISURE.ELENCO_MODALITA_MONITORAGGIO.NOT_FOUND_MODALITA_MONITORAGGIO';
  labelAddElencoModalita: string =
    'SEZIONE_23.MONITORAGGIO_MISURE.ELENCO_MODALITA_MONITORAGGIO.ADD_MODALITA_MONITORAGGIO';

  openModalDelete: boolean = false;
  elementToDelete: any = null;

  sortAscending: { [key: string]: boolean } = {};
  isSortedManually: boolean = false;

  monitoraggioPrevenzioneEdit?: MonitoraggioPrevenzioneDTO;

  sessionStorageService: SessionStorageService = inject(SessionStorageService);
  piaoDTO!: PIAODTO;

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
  }

  handleSortList(type: string): void {
    // Mappa il tipo al campo corretto nel form
    const fieldMap: { [key: string]: string } = {
      misuraPrevenzioneLabel: 'misuraPrevenzioneLabel',
      tipologiaMisuraPrevenzione: 'tipologiaMisuraPrevenzione',
    };

    const fieldName = fieldMap[type] || type;

    // Inizializza l'ordinamento per questo campo se non esiste
    if (this.sortAscending[type] === undefined) {
      this.sortAscending[type] = true;
    }

    this.isSortedManually = true;
    const formArray = this.formGroup?.get('modalitaMonitoraggio') as FormArray;
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

  getActionsFor(index: number): IVerticalEllipsisActions[] {
    const formArray = this.formGroup?.get('modalitaMonitoraggio') as FormArray;
    const modalitaControl = formArray.at(index);

    // Verifica che la fase esista ancora (potrebbe essere stata eliminata)
    if (!modalitaControl) {
      return [];
    }

    const modalita = modalitaControl.value;

    return [
      {
        label: 'Modifica',
        callback: () => {
          this.handleEditModalita(modalita);
          console.log('Apertura modale per modifica modalita:', { id: modalita.id, index });
        },
      },
      {
        label: 'Elimina',
        callback: () => this.handleOpenModalDelete(modalita),
      },
    ];
  }

  trackByModalitaId(index: number, item: any): any {
    return item.get('id')?.value || index;
  }

  handleAddElencoMonitoraggioMisurePrevenzione(): void {
    console.log(this.child.formGroup.value);
  }

  handleOpenModalElencoMisurePrevenzione(): void {
    this.openModalElencoMisurePrevenzione = true;
    this.monitoraggioPrevenzioneEdit = undefined;
  }

  handleRemoveForm(index: number) {
    const formArray = this.formGroup?.get('modalitaMonitoraggio') as FormArray;
    formArray.removeAt(index);
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

  handleEditModalita(modalita: MonitoraggioPrevenzioneDTO): void {
    this.monitoraggioPrevenzioneEdit = modalita;
    this.openModalElencoMisurePrevenzione = true;
  }

  get modalitaMonitoraggio(): FormArray {
    const formArray = this.formGroup?.get('modalitaMonitoraggio') as FormArray;

    // Se l'utente ha fatto un sort manuale, non riordinare
    if (this.isSortedManually) {
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
