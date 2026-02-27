import { Component, inject, Input } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TextAreaComponent } from '../../../../../../components/text-area/text-area.component';
import { CardInfoComponent } from '../../../../../card-info/card-info.component';
import { INPUT_REGEX, ONLY_NUMBERS_REGEX } from '../../../../../../utils/constants';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';

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
  @Input() form!: FormGroup;

  private fb = inject(FormBuilder);

  // Procedure da semplificare
  subTitleProcedureSemplificare: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.PROCEDURE_SEMPLIFICARE.TITLE';
  descriptionProcedureSemplificare: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.PROCEDURE_SEMPLIFICARE.DESCRIPTION';
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

  handleAddProcedura() {
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
    if (this.procedureArray.length > 0) {
      this.procedureArray.removeAt(index);
    }
  }

  handleOpenModalDelete(element: any) {
    this.openModalDelete = true;
    this.elementToDelete = element;
  }

  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = null;
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
