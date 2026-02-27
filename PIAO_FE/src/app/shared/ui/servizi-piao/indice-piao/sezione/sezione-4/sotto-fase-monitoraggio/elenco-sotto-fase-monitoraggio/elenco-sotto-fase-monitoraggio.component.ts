import { Component, inject, Input, OnInit } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormGroup } from '@angular/forms';
import { SharedModule } from '../../../../../../../module/shared/shared.module';
import { ModalSottofaseComponent } from '../modal-sottofase/modal-sottofase.component';
import { ModalDeleteComponent } from '../../../../../../../components/modal-delete/modal-delete.component';
import { AzioniComponent } from '../../../../../../../components/azioni/azioni.component';
import { SvgComponent } from '../../../../../../../components/svg/svg.component';
import { SottofaseMonitoraggioDTO } from '../../../../../../../models/classes/sezione-4-dto';
import { IVerticalEllipsisActions } from '../../../../../../../models/interfaces/vertical-ellipsis-actions';
import { ToastService } from '../../../../../../../services/toast.service';
import { SessionStorageService } from '../../../../../../../services/session-storage.service';
import { PIAODTO } from '../../../../../../../models/classes/piao-dto';
import { INPUT_REGEX, KEY_PIAO, PENCIL_ICON } from '../../../../../../../utils/constants';
import { AttoreDTO } from '../../../../../../../models/classes/attore-dto';
import { createFormMongoFromPiaoSession } from '../../../../../../../utils/utils';

@Component({
  selector: 'piao-elenco-sotto-fase-monitoraggio',
  imports: [
    SharedModule,
    ModalSottofaseComponent,
    ModalDeleteComponent,
    AzioniComponent,
    SvgComponent,
  ],
  templateUrl: './elenco-sotto-fase-monitoraggio.component.html',
  styleUrl: './elenco-sotto-fase-monitoraggio.component.scss',
})
export class ElencoSottoFaseMonitoraggioComponent implements OnInit {
  @Input() formGroup!: FormGroup;

  openModalSottofase = false;
  openModalDelete = false;
  elementToDelete: any = null;

  icon: string = PENCIL_ICON;
  iconStyle: string = 'icon-modal';

  titleElencoSottofasi: string = 'SEZIONE_4.ELENCO_SOTTOFASI.LABEL';
  notFoundElencoSottofasi: string = 'SEZIONE_4.ELENCO_SOTTOFASI.DESC';
  labelAddSottofase: string = 'SEZIONE_4.ELENCO_SOTTOFASI.ADD';

  sortAscending: { [key: string]: boolean } = {};
  isSortedManually = false;

  sottofaseEdit?: SottofaseMonitoraggioDTO;

  toastService = inject(ToastService);
  sessionStorageService: SessionStorageService = inject(SessionStorageService);
  fb: FormBuilder = inject(FormBuilder);

  piaoDTO!: PIAODTO;

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
  }

  get sottofasiMonitoraggio(): FormArray {
    const formArray = this.formGroup?.get('sottofaseMonitoraggio') as FormArray;

    if (this.isSortedManually) {
      return formArray;
    }

    if (!formArray || formArray.length === 0) {
      return formArray ?? this.fb.array([]);
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

  handleSortList(type: string): void {
    const fieldMap: { [key: string]: string } = {
      fase: 'fase',
      denominazione: 'denominazione',
    };

    const fieldName = fieldMap[type] || type;

    if (this.sortAscending[type] === undefined) {
      this.sortAscending[type] = true;
    }

    this.isSortedManually = true;
    const formArray = this.formGroup?.get('sottofaseMonitoraggio') as FormArray;
    const controls = [...formArray.controls];
    const sorted = controls.sort((a, b) => {
      const valueA = (a.get(fieldName)?.value || '').toString().toLowerCase();
      const valueB = (b.get(fieldName)?.value || '').toString().toLowerCase();

      return this.sortAscending[type] ? valueA.localeCompare(valueB) : valueB.localeCompare(valueA);
    });

    formArray.clear();
    sorted.forEach((control) => formArray.push(control, { emitEvent: false }));

    this.sortAscending[type] = !this.sortAscending[type];
  }

  getActionsFor(index: number): IVerticalEllipsisActions[] {
    const formArray = this.formGroup?.get('sottofaseMonitoraggio') as FormArray;
    const sottofaseControl = formArray.at(index);

    if (!sottofaseControl) {
      return [];
    }

    const sottofase = sottofaseControl.value;

    return [
      {
        label: 'Modifica',
        callback: () => {
          this.handleEditSottofase(sottofase);
        },
      },
      {
        label: 'Elimina',
        callback: () => this.handleOpenModalDelete({ sottofase, index }),
      },
    ];
  }

  getAttoreArray(sottofase: AbstractControl): FormArray {
    console.log('Sottofase:', sottofase);
    return sottofase.get('attore.properties') as FormArray;
  }

  trackBySottofaseId(index: number, item: any): any {
    return item.get('id')?.value || index;
  }

  handleOpenModalSottofase(): void {
    this.sottofaseEdit = undefined;
    this.openModalSottofase = true;
  }

  handleConfirmSottofase(sottofase: SottofaseMonitoraggioDTO): void {
    const formArray = this.formGroup?.get('sottofaseMonitoraggio') as FormArray;
    const attoreGroup = createFormMongoFromPiaoSession<AttoreDTO>(
      this.fb,
      sottofase.attore || new AttoreDTO(),
      ['id', 'externalId', 'properties'],
      INPUT_REGEX,
      50,
      false
    );

    const newGroup: FormGroup = this.fb.group({
      id: [null],
      idSezione4: [this.piaoDTO?.id || null],
      denominazione: [sottofase.denominazione || null],
      descrizione: [sottofase.descrizione || null],
      dataInizio: [sottofase.dataInizio || null],
      dataFine: [sottofase.dataFine || null],
      strumenti: [sottofase.strumenti || null],
      fonteDato: [sottofase.fonteDato || null],
    });
    newGroup.addControl('attore', attoreGroup || null);
    formArray.push(newGroup);
    this.toastService.success('Sottofase inserita con successo');
    this.openModalSottofase = false;
  }

  handleEditSottofase(sottofase: SottofaseMonitoraggioDTO): void {
    this.sottofaseEdit = sottofase;
    this.openModalSottofase = true;
  }

  handleOpenModalDelete(element: any): void {
    this.openModalDelete = true;
    this.elementToDelete = element;
  }

  handleRemoveForm(element: any): void {
    const formArray = this.formGroup?.get('sottofaseMonitoraggio') as FormArray;
    formArray.removeAt(element.index);
    this.toastService.success('Sottofase eliminata con successo');
    this.handleCloseModalDelete();
  }

  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = undefined;
  }
}
