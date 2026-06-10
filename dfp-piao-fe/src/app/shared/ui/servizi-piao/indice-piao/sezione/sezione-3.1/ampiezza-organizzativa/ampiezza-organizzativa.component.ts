import { Component, inject, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { BaseComponent } from '../../../../../../components/base/base.component';
import { FormGroup, FormArray, FormBuilder } from '@angular/forms';
import { ModalComponent } from '../../../../../../components/modal/modal.component';
import { ModalAmpiezzaOrganizzativaComponent } from './modal-ampiezza-organizzativa/modal-ampiezza-organizzativa.component';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';
import { SvgComponent } from '../../../../../../components/svg/svg.component';
import { AzioniComponent } from '../../../../../../components/azioni/azioni.component';
import { IVerticalEllipsisActions } from '../../../../../../models/interfaces/vertical-ellipsis-actions';
import { AmpiezzaOrganizzativaDTO } from '../../../../../../models/classes/ampiezza-organizzativa-dto';
import { AmpiezzaOrganizzativaService } from '../../../../../../services/ampiezza-organizzativa.service';
import { SessionStorageService } from '../../../../../../services/session-storage.service';
import { PIAODTO } from '../../../../../../models/classes/piao-dto';
import { KEY_PIAO, PENCIL_ICON, SHAPE_ICON } from '../../../../../../utils/constants';
import { getChangedFields } from '../../../../../../utils/utils';
import { takeUntil } from 'rxjs';

@Component({
  selector: 'piao-ampiezza-organizzativa',
  imports: [
    SharedModule,
    ModalComponent,
    ModalAmpiezzaOrganizzativaComponent,
    ModalDeleteComponent,
    SvgComponent,
    AzioniComponent,
  ],
  templateUrl: './ampiezza-organizzativa.component.html',
  styleUrl: './ampiezza-organizzativa.component.scss',
})
export class AmpiezzaOrganizzativaComponent extends BaseComponent implements OnInit {
  @Input() form!: FormGroup;
  @Input() testoSezione!: string;
  @Input() isDettaglio: boolean = false;

  ampiezzaOrganizzativaService = inject(AmpiezzaOrganizzativaService);
  fb = inject(FormBuilder);

  icon: string = SHAPE_ICON;
  iconStyle: string = 'icon-modal';

  piaoDTO!: PIAODTO;
  ampiezzaToEdit?: AmpiezzaOrganizzativaDTO;
  openModalAmpiezza = false;

  titleElenco: string = 'SEZIONE_31.ACCORDION_1.AMPIEZZA_ORG.TITLE_ELENCO';
  labelAddElenco: string = 'SEZIONE_31.ACCORDION_1.AMPIEZZA_ORG.LABEL_ADD_ELENCO';
  ampiezzaNotFound: string = 'SEZIONE_31.ACCORDION_1.AMPIEZZA_ORG.NOT_FOUND';

  openModalDelete: boolean = false;
  elementToDelete: any = null;
  ampiezzaIndex: number = 0;

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
    this.ampiezzaIndex = this.ampiezzaOrganizzativaArray.length;
  }

  handleAddAmpiezzaOrganizzativa(): void {
    const formValue = {
      ...this.child.formGroup.value,
      idPiao: this.piaoDTO.id,
      testoSezione: this.testoSezione,
      campiModificati: getChangedFields(
        this.child.formGroup.value,
        this.ampiezzaToEdit,
        ['id', 'idSezione31'],
        'ampiezzaOrganizzative'
      ),
    };
    this.ampiezzaOrganizzativaService.save(formValue).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.toastService.success('Ampiezza organizzativa salvata con successo');
        this.child?.formGroup.reset();
      },
      error: (err) => {
        console.error("Errore nel salvare l'ampiezza organizzativa:", err);
        this.child?.formGroup.reset();
      },
    });
    this.openModalAmpiezza = false;
    this.ampiezzaIndex = this.ampiezzaOrganizzativaArray.length;
    this.ampiezzaToEdit = undefined;
  }

  trackByAmpiezzaId(index: number, item: any): any {
    return item.get('id')?.value || index;
  }

  getActionsFor(index: number): IVerticalEllipsisActions[] {
    const control = this.ampiezzaOrganizzativaArray.at(index);

    if (!control) {
      return [];
    }

    const ampiezza = control.value;

    return [
      {
        label: 'Modifica',
        callback: () => {
          this.handleEditAmpiezza(ampiezza, index);
        },
      },
      {
        label: 'Elimina',
        callback: () => this.handleOpenModalDelete(ampiezza),
      },
    ];
  }

  handleOpenAmpiezzaModal(): void {
    this.ampiezzaToEdit = undefined;
    this.ampiezzaIndex = this.ampiezzaOrganizzativaArray.length;
    this.openModalAmpiezza = true;
  }

  handleEditAmpiezza(ampiezza: AmpiezzaOrganizzativaDTO, index: number): void {
    this.ampiezzaToEdit = ampiezza;
    this.openModalAmpiezza = true;
    this.ampiezzaIndex = index;
  }

  handleRemoveForm(ampiezza: AmpiezzaOrganizzativaDTO) {
    const index = this.ampiezzaOrganizzativaArray.controls.findIndex(
      (control) => control.get('id')?.value === ampiezza.id
    );

    if (index === -1) {
      console.error('Ampiezza organizzativa non trovata nel FormArray');
      this.handleCloseModalDelete();
      return;
    }

    if (ampiezza.id) {
      this.ampiezzaOrganizzativaService
        .delete(ampiezza.id, this.piaoDTO.id || -1, this.testoSezione)
        .pipe(takeUntil(this.destroy$)).subscribe({
          next: () => {
            this.toastService.success('Ampiezza organizzativa eliminata con successo');
            //this.ampiezzaOrganizzativaArray.removeAt(index);
          },
          error: (err) => {
            console.error("Errore nell'eliminazione dell'ampiezza organizzativa:", err);
          },
        });
    } else {
      this.ampiezzaOrganizzativaArray.removeAt(index);
      this.toastService.success('Ampiezza organizzativa eliminata con successo');
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

  get totRisorse(): number {
    return this.ampiezzaOrganizzativaArray.controls.reduce((sum, control) => {
      return sum + (Number(control.get('numRisorseUmane')?.value) || 0);
    }, 0);
  }

  get ampiezzaMedia(): number {
    const len = this.ampiezzaOrganizzativaArray.length;
    return len > 0 ? this.totRisorse / len : 0;
  }

  get ampiezzaOrganizzativaArray(): FormArray {
    const formArray = this.form.get('ampiezzaOrganizzative') as FormArray;

    if (!formArray) {
      return this.fb.array([]);
    }

    if (formArray.length === 0) {
      return formArray;
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
    controls.forEach((control) => formArray.push(control));

    return formArray;
  }
}
