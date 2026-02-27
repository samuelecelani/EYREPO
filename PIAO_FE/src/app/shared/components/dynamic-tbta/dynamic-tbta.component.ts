import { Component, inject, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { TextBoxComponent } from '../text-box/text-box.component';
import { TextAreaComponent } from '../text-area/text-area.component';
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { DynamicTBTAConfig } from '../../models/classes/config/dynamic-tbta-config';
import { CardInfoComponent } from '../../ui/card-info/card-info.component';
import { INPUT_REGEX } from '../../utils/constants';
import { SvgComponent } from '../svg/svg.component';
import { isEmpty } from '../../utils/utils';
import { ToastService } from '../../services/toast.service';
import { ModalDeleteComponent } from '../modal-delete/modal-delete.component';

@Component({
  selector: 'piao-dynamic-tbta',
  imports: [
    SharedModule,
    TextBoxComponent,
    TextAreaComponent,
    CardInfoComponent,
    SvgComponent,
    ModalDeleteComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './dynamic-tbta.component.html',
  styleUrl: './dynamic-tbta.component.scss',
})
export class DynamicTBTAComponent {
  @Input() title!: string;
  @Input() subTitle!: string;
  @Input() form!: FormGroup;
  @Input() dynamicTBTAConfig!: DynamicTBTAConfig;
  @Input() titleCardInfo!: string;
  @Input() titleLoadNewForm!: string;
  @Input() nameArrayForm!: string;
  @Input() nameIdSezione!: string;
  @Input() idSezione!: number;

  fb: FormBuilder = inject(FormBuilder);
  toastService: ToastService = inject(ToastService);

  openModalDelete: boolean = false;
  elementToDelete: any = null;

  private addNewForm(tb: string, ta: string) {
    return this.fb.group({
      id: [null, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
      [this.nameIdSezione]: [
        this.idSezione || null,
        [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
      ],
      [tb]: [null, [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)]],
      [ta]: [null, [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]],
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
      this.addNewForm(this.dynamicTBTAConfig.controlTB, this.dynamicTBTAConfig.controlTA)
    );
  }

  handleRemoveForm(index: number) {
    this.arrayForm.removeAt(index);
    this.handleCloseModalDelete();
  }

  private isGroupComplete(group: FormGroup): boolean {
    const tb = group.get(this.dynamicTBTAConfig.controlTB)?.value;
    const ta = group.get(this.dynamicTBTAConfig.controlTA)?.value;

    return !isEmpty(tb) && !isEmpty(ta);
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
