import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TextAreaComponent } from '../../../../../../components/text-area/text-area.component';
import { CardInfoComponent } from '../../../../../card-info/card-info.component';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';
import { INPUT_REGEX, ONLY_NUMBERS_REGEX } from '../../../../../../utils/constants';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';
import { FondiEuropeiService } from '../../../../../../services/fondi-europei.service';

@Component({
  selector: 'piao-fondi-europei',
  imports: [
    SharedModule,
    TextAreaComponent,
    CardInfoComponent,
    TextBoxComponent,
    ModalDeleteComponent,
  ],
  templateUrl: './fondi-europei.component.html',
  styleUrl: './fondi-europei.component.scss',
})
export class FondiEuropeiComponent {
  @Input() form!: FormGroup;
  @Input() isDettaglio: boolean = false;
  @Input() idPiao: number = -1;
  @Input() testoSezione: string = '';
  @Output() fondiChanged = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private fondiEuropeiService = inject(FondiEuropeiService);

  openModalDelete: boolean = false;
  elementToDelete: any = null;
  elementToDeleteIndex: number = -1;

  // Fondi europei
  subTitleFondiEuropei: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.FONDI_EUROPEI.TITLE';
  descriptionFondiEuropei: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.FONDI_EUROPEI.DESCRIPTION';
  descriptionFondiEuropeiDettaglio: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.FONDI_EUROPEI.DESCRIPTION_DETTAGLIO';
  labelTAIntroduzioneFondiEuropei: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.FONDI_EUROPEI.LABEL_INTRO';
  titleCardFondiEuropei: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.FONDI_EUROPEI.CARD_TITLE';
  labelBtnAddFondiEuropei: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.FONDI_EUROPEI.BTN_ADD';
  labelTBProgettoFinanziato: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.FONDI_EUROPEI.LABEL_PROGETTO';
  labelTADescrizioneFondi: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.FONDI_EUROPEI.LABEL_DESCRIZIONE';
  labelTBFondiStanziati: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.FONDI_EUROPEI.LABEL_FONDI';
  labelBtnRimuovi: string = 'BUTTONS.REMOVE';

  handleAddFondiEuropei() {
    if (this.isDettaglio) return;

    const newFondo = this.fb.group({
      id: [null, [Validators.maxLength(20), Validators.pattern(ONLY_NUMBERS_REGEX)]],
      progettoFinanziato: [null, [Validators.maxLength(250), Validators.pattern(INPUT_REGEX)]],
      descrizione: [null, [Validators.maxLength(1000), Validators.pattern(INPUT_REGEX)]],
      fondiStanziati: [null, [Validators.maxLength(50), Validators.pattern(ONLY_NUMBERS_REGEX)]],
    });

    this.fondiEuropeiArray.push(newFondo);
    this.fondiChanged.emit();
  }

  handleRemoveFondiEuropei(index: number): void {
    if (this.isDettaglio) return;
    if (index < 0 || index >= this.fondiEuropeiArray.length) return;

    const fondoToDelete = this.fondiEuropeiArray.at(index).value;
    const fondiEuropeiId = fondoToDelete?.id;

    // Se il fondo ha un ID, lo elimina via API
    if (fondiEuropeiId) {
      this.fondiEuropeiService.delete(fondiEuropeiId, this.idPiao, this.testoSezione).subscribe({
        next: () => {
          this.fondiEuropeiArray.removeAt(index);
          this.fondiChanged.emit();
        },
        error: () => {
          console.error('Errore nella cancellazione del fondo europeo');
        },
      });
    } else {
      // Se è nuovo (no ID), rimuove solo localmente
      this.fondiEuropeiArray.removeAt(index);
      this.fondiChanged.emit();
    }
  }

  handleOpenModalDelete(element: any, index: number) {
    if (this.isDettaglio) return;

    this.openModalDelete = true;
    this.elementToDelete = element;
    this.elementToDeleteIndex = index;
  }

  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = null;
    this.elementToDeleteIndex = -1;
  }

  confirmDeleteFondo(): void {
    this.handleRemoveFondiEuropei(this.elementToDeleteIndex);
    this.openModalDelete = false;
    this.elementToDelete = null;
    this.elementToDeleteIndex = -1;
  }

  get fondiEuropeiArray(): FormArray {
    const formArray = this.form.get('fondiEuropei') as FormArray;

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
