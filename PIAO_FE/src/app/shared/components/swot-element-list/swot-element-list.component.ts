import { Component, Input, OnInit, inject } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SharedModule } from '../../module/shared/shared.module';
import { TextAreaComponent } from '../text-area/text-area.component';
import { TooltipComponent } from '../tooltip/tooltip.component';
import { INPUT_REGEX } from '../../utils/constants';
import { ModalDeleteComponent } from '../modal-delete/modal-delete.component';

@Component({
  selector: 'piao-swot-element-list',
  imports: [
    SharedModule,
    TextAreaComponent,
    TooltipComponent,
    ModalDeleteComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './swot-element-list.component.html',
  styleUrl: './swot-element-list.component.scss',
})
export class SwotElementListComponent implements OnInit {
  @Input() form!: FormGroup;
  @Input() nameArrayForm!: string; // es: 'swotPuntiForza.properties'
  @Input() label!: string; // es: 'Punti di forza (S)'
  @Input() maxLength: number = 100;

  fb: FormBuilder = inject(FormBuilder);

  openModalDelete: boolean = false;
  elementToDelete: any = null;

  ngOnInit(): void {
    // Assicura che ci sia sempre almeno un elemento vuoto
    if (this.arrayForm.length === 0) {
      this.addElement();
    } else {
      // Aggiorna le key esistenti se non sono già impostate correttamente
      this.updateExistingKeys();
    }
  }

  get arrayForm(): FormArray<FormGroup> {
    const parts = this.nameArrayForm.split('.');
    let current: any = this.form;
    for (const part of parts) {
      current = current.get(part);
      if (!current) {
        return new FormArray<FormGroup>([]);
      }
    }
    return current as FormArray<FormGroup>;
  }

  /**
   * Genera la key automaticamente basandosi sul tipo di SWOT e sull'indice
   */
  private generateKey(index: number): string {
    const swotType = this.getSwotType();
    return `${swotType}${index + 1}`;
  }

  /**
   * Estrae il tipo di SWOT dal nameArrayForm
   */
  private getSwotType(): string {
    if (this.nameArrayForm.includes('swotPuntiForza')) {
      return 'puntiForza';
    } else if (this.nameArrayForm.includes('swotPuntiDebolezza')) {
      return 'puntiDebolezza';
    } else if (this.nameArrayForm.includes('swotOpportunita')) {
      return 'opportunita';
    } else if (this.nameArrayForm.includes('swotMinacce')) {
      return 'minacce';
    }
    return 'swot';
  }

  /**
   * Aggiorna le key degli elementi esistenti se non sono già impostate
   */
  private updateExistingKeys(): void {
    this.arrayForm.controls.forEach((control, index) => {
      const keyControl = control.get('key');
      const currentKey = keyControl?.value;
      const expectedKey = this.generateKey(index);

      // Se la key non è impostata o non corrisponde al pattern, la aggiorna
      if (
        !currentKey ||
        !currentKey.match(/^(puntiForza|puntiDebolezza|opportunita|minacce)\d+$/)
      ) {
        keyControl?.setValue(expectedKey);
      }
    });
  }

  addElement(): void {
    const newIndex = this.arrayForm.length;
    const newKey = this.generateKey(newIndex);

    const newGroup = this.fb.group({
      key: [newKey, [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)]],
      value: [null, [Validators.maxLength(this.maxLength), Validators.pattern(INPUT_REGEX)]],
    });
    this.arrayForm.push(newGroup);
  }

  removeElement(index: number): void {
    if (this.arrayForm.length > 1) {
      this.arrayForm.removeAt(index);
      // Rinumera le key dopo la rimozione
      this.renumberKeys();
    } else {
      // Se rimane solo un elemento, lo resetta invece di rimuoverlo
      const control = this.arrayForm.at(0);
      control.reset();
      control.get('key')?.setValue(this.generateKey(0));
    }

    this.handleCloseModalDelete();
  }

  /**
   * Rinumera le key dopo una rimozione
   */
  private renumberKeys(): void {
    this.arrayForm.controls.forEach((control, index) => {
      control.get('key')?.setValue(this.generateKey(index));
    });
  }

  getControl(index: number): FormGroup {
    return this.arrayForm.at(index) as FormGroup;
  }

  handleOpenModalDelete(element: any) {
    this.openModalDelete = true;
    this.elementToDelete = element;
  }
  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = null;
  }
}
