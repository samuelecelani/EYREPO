import { Component, inject, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { ModalComponent } from '../../../../../../components/modal/modal.component';
import { ModalElencoAttivitaSensibileComponent } from './modal-attivita-sensibile/modal-elenco-attivita-sensibile.component';
import { KEY_PIAO, PENCIL_ICON } from '../../../../../../utils/constants';
import { BaseComponent } from '../../../../../../components/base/base.component';
import { SessionStorageService } from '../../../../../../services/session-storage.service';
import { PIAODTO } from '../../../../../../models/classes/piao-dto';
import { AttivitaSensibileDTO } from '../../../../../../models/classes/attivita-sensibile-dto';
import { AbstractControl, Form, FormArray, FormBuilder, FormGroup } from '@angular/forms';
import { SvgComponent } from '../../../../../../components/svg/svg.component';
import { AzioniComponent } from '../../../../../../components/azioni/azioni.component';
import { IVerticalEllipsisActions } from '../../../../../../models/interfaces/vertical-ellipsis-actions';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';
import { AttivitaSensibileRischioService } from '../../../../../../services/attivita-sensibile-rischio.service';
import { ToastService } from '../../../../../../services/toast.service';

@Component({
  selector: 'piao-elenco-attivita-sensibile',
  imports: [
    SharedModule,
    ModalComponent,
    ModalElencoAttivitaSensibileComponent,
    SvgComponent,
    AzioniComponent,
    ModalDeleteComponent,
  ],
  templateUrl: './elenco-attivita-sensibile.component.html',
  styleUrl: './elenco-attivita-sensibile.component.scss',
})
export class ElencoAttivitaSensibileComponent extends BaseComponent implements OnInit {
  @Input() formGroup!: FormGroup;
  openModalElencoAttivita = false;
  icon: string = PENCIL_ICON;
  iconStyle: string = 'icon-modal';

  titleElencoAttivita: string = 'SEZIONE_23.ATTIVITA_SENSIBILI.TITLE_ELENCO_ATTIVITA_SENSIBILE';
  notFoundElencoAttivita: string = 'SEZIONE_23.ATTIVITA_SENSIBILI.NOT_FOUND_ATTIVITA_SENSIBILI';
  labelAddElencoAttivita: string = 'SEZIONE_23.ATTIVITA_SENSIBILI.ADD_ATTIVITA';

  openModalDelete: boolean = false;
  elementToDelete: any = null;

  attivitaToEdit?: AttivitaSensibileDTO;

  piaoDTO!: PIAODTO;

  sortAscending: { [key: string]: boolean } = {};
  isSortedManually: boolean = false;

  sessionStorageService: SessionStorageService = inject(SessionStorageService);

  attivitaSensibileRischioService = inject(AttivitaSensibileRischioService);

  fb: FormBuilder = inject(FormBuilder);

  toastService = inject(ToastService);

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
    this.sortAttivitaSensibileById();
  }

  private sortAttivitaSensibileById(): void {
    // Non ordinare se l'utente ha già fatto un sort manuale
    if (this.isSortedManually) return;

    const formArray = this.formGroup.get('attivitaSensibile') as FormArray;
    if (!formArray) return;

    // Se l'array non esiste, crea uno nuovo
    if (!formArray) {
      return;
    }

    // Se l'array è vuoto, restituisci l'array originale senza modifiche
    if (formArray.length === 0) {
      return;
    }

    const controls = formArray.controls.slice() as FormGroup[];
    controls.sort((a, b) => {
      const idA = a.get('id')?.value;
      const idB = b.get('id')?.value;

      if (idA == null && idB == null) return 0;
      if (idA == null) return 1;
      if (idB == null) return -1;

      return idA - idB;
    });

    formArray.clear();
    controls.forEach((control) => formArray.push(control, { emitEvent: false }));
  }

  handleOpenAttivitaModal(): void {
    this.openModalElencoAttivita = true;
    this.attivitaToEdit = undefined;
  }

  handleEditAttivita(attivita: AttivitaSensibileDTO): void {
    this.attivitaToEdit = attivita;
    this.openModalElencoAttivita = true;
  }

  handleAddElencoAttivita(): void {
    this.attivitaSensibileRischioService.save(this.child.formGroup.value).subscribe({
      next: () => {
        this.toastService.success('Attivita sensibile salvata con successo');
      },
      error: (err) => {
        console.error("Errore nel salvare l'Attivita sensibile:", err);
      },
    });
    this.attivitaToEdit = undefined;
    this.child?.formGroup.reset();
    this.openModalElencoAttivita = false;
  }

  trackByAttivitaId(index: number, item: any): any {
    return item.get('id')?.value || index;
  }

  getAttoreArray(attivita: AbstractControl): FormArray {
    return attivita.get('attore.properties') as FormArray;
  }

  handleSortList(type: string): void {
    console.log('Ordinamento per tipo:', type);
    // Mappa il tipo al campo corretto nel form
    const fieldMap: { [key: string]: string } = {
      attivita: 'denominazione',
      descrizione: 'descrizione',
      processoCollegato: 'processoCollegato',
    };

    const fieldName = fieldMap[type] || type;

    // Inizializza a true al primo click, così !true = false (decrescente al primo click)
    if (this.sortAscending[type] === undefined) {
      this.sortAscending[type] = true;
    }

    // Alterna l'ordinamento
    const isAscending = !this.sortAscending[type];
    this.sortAscending[type] = isAscending;

    this.isSortedManually = true;
    const formArray = this.formGroup.get('attivitaSensibile') as FormArray;
    const controls = [...formArray.controls];

    const sorted = controls.sort((a, b) => {
      const valueA = a.get(fieldName)?.value?.toLowerCase() || '';
      const valueB = b.get(fieldName)?.value?.toLowerCase() || '';

      if (isAscending) {
        return valueA.localeCompare(valueB);
      } else {
        return valueB.localeCompare(valueA);
      }
    });

    formArray.clear();
    sorted.forEach((control) => formArray.push(control, { emitEvent: false }));

    console.log(
      'Ordinamento completato:',
      isAscending ? 'crescente' : 'decrescente',
      formArray.value
    );
  }

  getActionsFor(index: number): IVerticalEllipsisActions[] {
    const formArray = this.formGroup.get('attivitaSensibile') as FormArray;
    const attivitaControl = formArray.at(index);

    // Verifica che la fase esista ancora (potrebbe essere stata eliminata)
    if (!attivitaControl) {
      return [];
    }

    const attivita = attivitaControl.value;

    return [
      {
        label: 'Modifica',
        callback: () => {
          this.handleEditAttivita(attivita);
          console.log('Apertura modale per modifica attivita:', { id: attivita.id, index });
        },
      },
      {
        label: 'Elimina',
        callback: () => this.handleOpenModalDelete({ attivita, index }),
      },
    ];
  }

  handleRemoveForm(element: any) {
    const { attivita, index } = element;
    if (attivita.id) {
      this.attivitaSensibileRischioService.delete(attivita.id).subscribe({
        next: () => {
          this.toastService.success('Attivita sensibile eliminata con successo');
        },
        error: (err) => {
          console.error("Errore nell'eliminare l'Attivita sensibile:", err);
        },
      });
    } else {
      // Se non c'è un ID, rimuoviamo semplicemente il form senza chiamare il servizio
      const formArray = this.formGroup.get('attivitaSensibile') as FormArray;
      formArray.removeAt(index);
      this.toastService.success('Attivita sensibile eliminata con successo');
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

  get attivitaSensibile(): FormArray {
    return this.formGroup.get('attivitaSensibile') as FormArray;
  }
}
