import { Component, DestroyRef, Input, OnInit, inject } from '@angular/core';
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
import { createFormMongoFromPiaoSession, getChangedFields } from '../../../../../../../utils/utils';
import { SottofaseMonitoraggioService } from '../../../../../../../services/sottofase-monitoraggio.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

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
  private destroyRef = inject(DestroyRef);
  @Input() formGroup!: FormGroup;
  @Input() testoSezione!: string;
  @Input() isDettaglio: boolean = false;

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
  sottofaseMonitoraggioService = inject(SottofaseMonitoraggioService);

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

    // Usa getRawValue() per ottenere tutti i valori, inclusi quelli nested come attore
    const sottofase = (sottofaseControl as FormGroup).getRawValue() as SottofaseMonitoraggioDTO;

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
    return sottofase.get('attore.properties') as FormArray;
  }

  trackBySottofaseId(index: number, item: any): any {
    return item.get('id')?.value || index;
  }

  handleOpenModalSottofase(): void {
    if (this.isDettaglio) return;
    this.sottofaseEdit = undefined;
    this.openModalSottofase = true;
  }

  handleCloseModalSottofase(): void {
    this.openModalSottofase = false;
    this.sottofaseEdit = undefined;
  }

  handleConfirmSottofase(sottofase: SottofaseMonitoraggioDTO): void {
    // Ottieni l'idSezione4 dal piaoDTO
    const idSezione4 = this.piaoDTO?.idSezione4;

    if (!idSezione4) {
      this.toastService.error('ID Sezione 4 non trovato');
      return;
    }

    // Prepara il payload per la chiamata API
    // Se è in edit mode, usa l'id da sottofaseEdit, altrimenti usa quello dalla sottofase emessa (se presente)
    const id = this.sottofaseEdit?.id || sottofase.id;

    const payload: SottofaseMonitoraggioDTO = {
      ...sottofase,
      id: id, // Include l'id se presente (per update)
      idSezione4: idSezione4,
    };

    const payloadForLog = {
      ...payload,
      idPiao: this.piaoDTO.id || -1,
      testoSezione: this.testoSezione,
      campiModificati: getChangedFields(
        sottofase,
        this.sottofaseEdit,
        ['id', 'idSezione4', 'externalId', 'key', 'fase', 'milestone'],
        'sottofaseMonitoraggio'
      ),
    };

    // Chiama l'API per salvare la sottofase
    this.sottofaseMonitoraggioService.save(payloadForLog).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (response) => {
        // Se la POST va a buon fine, chiudi la modale e mostra successo
        // Il reload della sezione4 viene gestito in background dal servizio
        this.toastService.success(
          this.sottofaseEdit
            ? 'Sottofase aggiornata con successo'
            : 'Sottofase inserita con successo'
        );
        // Chiudi la modale e resetta i campi
        this.openModalSottofase = false;
        this.sottofaseEdit = undefined;
      },
      error: (err) => {
        console.error('Errore nel salvataggio della sottofase:', err);
        this.toastService.error('Errore nel salvataggio della sottofase');
      },
    });
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
    const sottofase = element.sottofase;
    const id = sottofase?.id;

    if (!id) {
      this.toastService.error('ID sottofase non trovato');
      this.handleCloseModalDelete();
      return;
    }

    // Chiama l'API per eliminare la sottofase
    this.sottofaseMonitoraggioService
      .delete(id, this.piaoDTO?.id || -1, this.testoSezione)
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: (response) => {
          // Se la DELETE va a buon fine, mostra successo e chiudi la modale
          // Il reload della sezione4 viene gestito in background dal servizio
          this.toastService.success('Sottofase eliminata con successo');
          this.handleCloseModalDelete();
        },
        error: (err) => {
          console.error("Errore nell'eliminazione della sottofase:", err);
          this.toastService.error("Errore nell'eliminazione della sottofase");
          this.handleCloseModalDelete();
        },
      });
  }

  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = undefined;
  }
}
