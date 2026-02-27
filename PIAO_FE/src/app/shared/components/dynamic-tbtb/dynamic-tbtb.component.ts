import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { CardInfoComponent } from '../../ui/card-info/card-info.component';
import { TextBoxComponent } from '../text-box/text-box.component';
import { SvgComponent } from '../svg/svg.component';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { INPUT_REGEX } from '../../utils/constants';
import { DynamicTBTBConfig } from '../../models/classes/config/dynamic-tbtb-config';
import { isEmpty } from '../../utils/utils';
import { ToastService } from '../../services/toast.service';
import { ModalDeleteComponent } from '../modal-delete/modal-delete.component';

@Component({
  selector: 'piao-dynamic-tbtb',
  templateUrl: './dynamic-tbtb.component.html',
  styleUrl: './dynamic-tbtb.component.scss',
  imports: [
    SharedModule,
    TextBoxComponent,
    CardInfoComponent,
    SvgComponent,
    ModalDeleteComponent,
    ReactiveFormsModule,
  ],
})
export class DynamicTBTBComponent {
  @Input() title!: string;
  @Input() subTitle!: string;
  @Input() form!: FormGroup;
  @Input() dynamicTBTBConfig!: DynamicTBTBConfig;
  @Input() titleCardInfo!: string;
  @Input() titleLoadNewForm!: string;
  @Input() nameArrayForm!: string;
  @Input() nameIdSezione!: string;
  @Input() idSezione!: number;
  @Output() deleteRequest = new EventEmitter<number>();

  fb: FormBuilder = inject(FormBuilder);
  toastService: ToastService = inject(ToastService);

  openModalDelete: boolean = false;
  elementToDelete: any = null;

  private addNewForm(tb: string, tb2: string) {
    return this.fb.group({
      id: [null, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
      [this.nameIdSezione]: [
        this.idSezione || null,
        [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
      ],
      [tb]: [null, [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)]],
      [tb2]: [null, [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)]],
    });
  }

  handleClickNewForm() {
    const arr = this.arrayForm;
    const last = arr.at(arr.length - 1) as FormGroup | undefined;

    if (last && !this.isGroupComplete(last)) {
      last.markAllAsTouched();
      this.toastService.warning(
        "Impossibile aggiungere un nuovo elemento. Compilare tutti i campi dell'ultimo elemento prima di procedere."
      );
      return;
    }
    this.arrayForm.push(
      this.addNewForm(this.dynamicTBTBConfig.controlTB, this.dynamicTBTBConfig.controlTB2)
    );
  }

  handleRemoveForm(element: any) {
    if (element?.id) {
      // Emetti l'ID dello stakeholder, non l'indice
      this.deleteRequest.emit(element.id);
    }
    this.arrayForm.removeAt(element.index);
    this.handleCloseModalDelete();
  }

  private isGroupComplete(group: FormGroup): boolean {
    const tb = group.get(this.dynamicTBTBConfig.controlTB)?.value;
    const tb2 = group.get(this.dynamicTBTBConfig.controlTB2)?.value;

    return !isEmpty(tb) && !isEmpty(tb2);
  }

  handleOpenModalDelete(element: any) {
    this.openModalDelete = true;
    this.elementToDelete = element;
  }
  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = null;
  }

  get arrayForm(): FormArray {
    const formArray = this.form.get(this.nameArrayForm) as FormArray;

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
