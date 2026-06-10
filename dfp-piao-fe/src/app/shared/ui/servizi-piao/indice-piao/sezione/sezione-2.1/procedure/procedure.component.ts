import { Component, DestroyRef, EventEmitter, Input, Output, inject } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TextAreaComponent } from '../../../../../../components/text-area/text-area.component';
import { CardInfoComponent } from '../../../../../card-info/card-info.component';
import { INPUT_REGEX, ONLY_NUMBERS_REGEX } from '../../../../../../utils/constants';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';
import { ProcedureService } from '../../../../../../services/procedure.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'piao-procedure',
  imports: [
    SharedModule,
    TextAreaComponent,
    CardInfoComponent,
    TextBoxComponent,
    ModalDeleteComponent,
  ],
  templateUrl: './procedure.component.html',
  styleUrl: './procedure.component.scss',
})
export class ProcedureComponent {
  private destroyRef = inject(DestroyRef);
  @Input() form!: FormGroup;
  @Input() isDettaglio: boolean = false;
  @Input() idPiao: number = -1;
  @Input() testoSezione: string = '';
  @Output() procedureChanged = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private procedureService = inject(ProcedureService);

  // Procedure da semplificare
  subTitleProcedureSemplificare: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.PROCEDURE_SEMPLIFICARE.TITLE';
  descriptionProcedureSemplificare: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.PROCEDURE_SEMPLIFICARE.DESCRIPTION';
  descriptionProcedureSemplificareDettaglio: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.PROCEDURE_SEMPLIFICARE.DESCRIPTION_DETTAGLIO';
  labelTAIntroduzioneProcedure: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.PROCEDURE_SEMPLIFICARE.LABEL_INTRO';
  titleCardProcedura: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.PROCEDURE_SEMPLIFICARE.CARD_TITLE';
  labelDenominazioneProcedura: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.PROCEDURE_SEMPLIFICARE.LABEL_DENOMINAZIONE';
  labelDescrizioneProcedura: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.PROCEDURE_SEMPLIFICARE.LABEL_DESCRIZIONE';
  labelUnitaMisuraProcedura: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.PROCEDURE_SEMPLIFICARE.LABEL_UNITA_MISURA';
  labelMisurazioneProcedura: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.PROCEDURE_SEMPLIFICARE.LABEL_MISURAZIONE';
  labelTargetProcedura: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.PROCEDURE_SEMPLIFICARE.LABEL_TARGET';
  labelUfficioResponsabileProcedura: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.PROCEDURE_SEMPLIFICARE.LABEL_RESPONSABILE';
  labelBtnAddProcedura: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.PROCEDURE_SEMPLIFICARE.BTN_ADD';

  labelBtnRimuovi: string = 'BUTTONS.REMOVE';

  openModalDelete: boolean = false;
  elementToDelete: any = null;
  elementToDeleteIndex: number = -1;

  handleAddProcedura() {
    if (this.isDettaglio) return;

    const newProcedura = this.fb.group({
      id: [null, [Validators.maxLength(20), Validators.pattern(ONLY_NUMBERS_REGEX)]],
      denominazione: [
        null,
        [Validators.maxLength(250), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
      descrizione: [null, [Validators.maxLength(500), Validators.pattern(INPUT_REGEX)]],
      unitaMisura: [null, [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)]],
      misurazione: [null, [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)]],
      target: [
        null,
        [Validators.maxLength(50), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
      uffResponsabile: [
        null,
        [Validators.maxLength(50), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
    });

    this.procedureArray.push(newProcedura);
  }

  handleRemoveProcedura(index: number) {
    if (this.isDettaglio) return;

    if (this.procedureArray.length > 0) {
      this.procedureArray.removeAt(index);
    }
  }
  handleOpenModalDelete(element: any, index: number) {
  if (this.isDettaglio) return;

  // se element è un FormGroup, salva il value (così hai id)
  if (element instanceof FormGroup) {
    element = element.value;
  }

  this.openModalDelete = true;
  this.elementToDelete = element;
  this.elementToDeleteIndex = index;
}


 handleCloseModalDelete(): void {
  this.openModalDelete = false;
  this.elementToDelete = null;
  this.elementToDeleteIndex = -1;
}

  confirmDeleteProcedura(forceDelete: boolean = false): void {
  if (this.isDettaglio) return;

  const index = this.elementToDeleteIndex;
  const proceduraId = this.elementToDelete?.id;

  // Se non ho un index valido, chiudo
  if (index == null || index < 0) {
    this.handleCloseModalDelete();
    return;
  }

  // Se la procedura NON è mai stata salvata (id null) => rimuovo solo dal form
  if (!proceduraId) {
    this.procedureArray.removeAt(index);
    this.procedureChanged.emit();
    this.handleCloseModalDelete();
    return;
  }

  // Se la procedura esiste a DB => chiamata DELETE puntuale
  this.procedureService
    .delete(proceduraId, this.idPiao || -1, this.testoSezione)
    .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.procedureArray.removeAt(index);
        this.procedureChanged.emit();
        this.handleCloseModalDelete();
      },
      error: (err) => {
        console.error('Errore durante la cancellazione procedura', err);
        this.handleCloseModalDelete();
      },
    });
}


  get procedureArray(): FormArray {
    const formArray = this.form.get('procedure') as FormArray;

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
}
