import { Component, inject, Input, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SharedModule } from '../../module/shared/shared.module';
import { AccordionComponent } from '../../components/accordion/accordion.component';
import { INPUT_REGEX, KEY_PIAO, PENCIL_ICON } from '../../utils/constants';
import { ToastService } from '../../services/toast.service';
import { TipologiaAdempimento } from '../../models/enums/tipologia-adempimento.enum';
import { AzioniComponent } from '../../components/azioni/azioni.component';
import { IVerticalEllipsisActions } from '../../models/interfaces/vertical-ellipsis-actions';
import { AdempimentiService } from '../../services/adempimenti.service';
import { BaseComponent } from '../../components/base/base.component';
import { ModalBodyAdempimentoComponent } from './modal-body-adempimento/modal-body-adempimento.component';
import { ModalComponent } from '../../components/modal/modal.component';
import { ModalDeleteComponent } from '../../components/modal-delete/modal-delete.component';
import { AdempimentoDTO } from '../../models/classes/adempimento-dto';
import { SessionStorageService } from '../../services/session-storage.service';
import { PIAODTO } from '../../models/classes/piao-dto';

@Component({
  selector: 'piao-adempimenti',
  standalone: true,
  imports: [
    SharedModule,
    AccordionComponent,
    ReactiveFormsModule,
    AzioniComponent,
    ModalComponent,
    ModalBodyAdempimentoComponent,
    ModalDeleteComponent,
  ],
  templateUrl: './adempimenti.component.html',
  styleUrl: './adempimenti.component.scss',
})
export class AdempimentiComponent extends BaseComponent implements OnInit {
  @Input() formGroup!: FormGroup;
  @Input() idSezione22!: number;
  @Input() idPiao!: number;
  @Input() tipologia!: TipologiaAdempimento;
  @Input() labelTitle!: string;
  @Input() accordionIndex!: number;
  @Input() isOpen: boolean = false;
  @Input() isFirstAccordion: boolean = false;

  openModalDelete: boolean = false;
  elementToDelete: any = null;
  openAdempimentiModal: boolean = false;
  titleModal: string = '';
  subTitleModal: string = '';
  labelDescModal: string = '';

  adempimentoToEdit?: AdempimentoDTO;

  icon: string = PENCIL_ICON;
  iconStyle: string = 'icon-modal';

  piaoDTO!: PIAODTO;

  toastService = inject(ToastService);
  private adempimentiService = inject(AdempimentiService);
  sessionStorageService: SessionStorageService = inject(SessionStorageService);
  fb: FormBuilder = inject(FormBuilder);

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
    console.log('[Adempimenti] ngOnInit, controls length:', this.adempimenti?.length || 0);
  }

  getActionsFor(adempimento: AdempimentoDTO): IVerticalEllipsisActions[] {
    return [
      {
        label: 'Modifica',
        callback: () => {
          const adempimentoId = adempimento.id;
          console.log('Apertura modale per modifica adempimento:', {
            id: adempimentoId,
            adempimento,
          });
          this.handleEditAdempimento(adempimento);
        },
      },
      {
        label: 'Elimina',
        callback: () => this.handleOpenModalDelete(adempimento),
      },
    ];
  }

  handleRemoveAdempimento(adempimento: AdempimentoDTO): void {
    const adempimentoId = adempimento.id;

    // Se l'adempimento ha un ID, significa che è stato salvato sul backend
    if (adempimentoId) {
      // Chiama il backend per eliminarlo
      this.adempimentiService.deleteAdempimento(adempimentoId).subscribe({
        next: () => {
          this.toastService.success('Adempimento eliminato con successo');
          // Il reload viene gestito automaticamente dal service via reloadSezione22AndUpdateSession()
          // che triggera la subscription nel parent component per ricreare il form
        },
        error: (err) => {
          console.error("Errore nell'eliminazione dell'adempimento:", err);
          this.toastService.error("Errore durante l'eliminazione dell'adempimento");
        },
      });
    } else {
      // Se non ha ID, è solo locale, rimuovilo dal FormArray
      const index = this.adempimenti.controls.findIndex(
        (control) => control.get('id')?.value === adempimento.id
      );
      if (index !== -1) {
        this.adempimenti.removeAt(index);
      }
      this.toastService.success('Adempimento rimosso');
    }

    this.handleCloseModalDelete();
  }

  handleOpenModalAdempimento(): void {
    this.getLabelByTipologia();
    this.openAdempimentiModal = true;
  }
  handleAddAdempimento(): void {
    if (!this.child?.formGroup?.valid) {
      this.toastService.error('Compila tutti i campi obbligatori');
      return;
    }

    const formValue = this.child.formGroup.value;

    const adempimentoDTO: AdempimentoDTO = {
      idSezione22: this.idSezione22,
      tipologia: this.tipologia,
      denominazione: formValue.denominazione,
      azione: formValue.azione,
      ulterioriInfo: formValue.ulterioriInfo,
    };

    // Se stiamo modificando un adempimento esistente, mantieni l'ID
    if (this.adempimentoToEdit?.id) {
      adempimentoDTO.id = this.adempimentoToEdit.id;
    }

    this.adempimentiService.saveOrUpdateAdempimento(adempimentoDTO).subscribe({
      next: () => {
        const message = this.adempimentoToEdit?.id
          ? 'Adempimento modificato con successo'
          : 'Adempimento aggiunto con successo';
        this.toastService.success(message);
        // Il reload viene gestito automaticamente dal service via reloadSezione22AndUpdateSession()
        this.adempimentoToEdit = undefined;
      },
      error: (err) => {
        console.error("Errore nel salvataggio dell'adempimento:", err);
        this.toastService.error("Errore durante il salvataggio dell'adempimento");
      },
    });
  }

  get adempimenti(): FormArray {
    const formArray = this.formGroup?.get(`adempimenti_${this.tipologia}`) as FormArray;
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

  trackByAdempimentoId(index: number, item: any): any {
    return item.get('id')?.value || index;
  }

  getAzioniArray(adempimento: any): FormArray {
    const properties = adempimento.get('azione.properties') as FormArray;
    return properties;
  }

  getLabelByTipologia(): string {
    switch (this.tipologia) {
      case TipologiaAdempimento.INNOVAZIONI_AMM:
        this.titleModal = 'SEZIONE_22.ADEMPIMENTI.MODAL.TITLE_INN_AMM';
        this.subTitleModal = 'SEZIONE_22.ADEMPIMENTI.MODAL.SUB_TITLE_INN_AMM';
        this.labelDescModal = 'SEZIONE_22.ADEMPIMENTI.MODAL.LABELS.INN_AMM';
        return 'Innovazioni amministrative';
      case TipologiaAdempimento.COMPORTAMENTI_UNI:
        this.titleModal = 'SEZIONE_22.ADEMPIMENTI.MODAL.TITLE_COMP_UNI';
        this.subTitleModal = 'SEZIONE_22.ADEMPIMENTI.MODAL.SUB_TITLE_COMP_UNI';
        this.labelDescModal = 'SEZIONE_22.ADEMPIMENTI.MODAL.LABELS.COMP_UNI';
        return 'Comportamenti uniformi';
      case TipologiaAdempimento.OBIETTIVI:
        this.titleModal = 'SEZIONE_22.ADEMPIMENTI.MODAL.TITLE_OBIET_POS';
        this.subTitleModal = 'SEZIONE_22.ADEMPIMENTI.MODAL.SUB_TITLE_OBIET_POS';
        this.labelDescModal = 'SEZIONE_22.ADEMPIMENTI.MODAL.LABELS.OBIET_POS';
        return 'Obiettivi di tipo positivo';
      case TipologiaAdempimento.OBIETTIVI_INFRA:
        this.titleModal = 'SEZIONE_22.ADEMPIMENTI.MODAL.TITLE_OBIET_INFRA';
        this.subTitleModal = 'SEZIONE_22.ADEMPIMENTI.MODAL.SUB_TITLE_OBIET_INFRA';
        this.labelDescModal = 'SEZIONE_22.ADEMPIMENTI.MODAL.LABELS.OBIET_INFRA';
        return 'Obiettivi infrastrutturali e digitali';
      case TipologiaAdempimento.OBIETTIVI_PATRIMONIALI:
        this.titleModal = 'SEZIONE_22.ADEMPIMENTI.MODAL.TITLE_OBIET_PATRIM';
        this.subTitleModal = 'SEZIONE_22.ADEMPIMENTI.MODAL.SUB_TITLE_OBIET_PATRIM';
        this.labelDescModal = 'SEZIONE_22.ADEMPIMENTI.MODAL.LABELS.OBIET_PATRIM';
        return 'Obiettivi patrimoniali';
      default:
        return 'Adempimenti';
    }
  }

  getEmptyMessage(): string {
    switch (this.tipologia) {
      case TipologiaAdempimento.INNOVAZIONI_AMM:
        return 'SEZIONE_22.ADEMPIMENTI.EMPTY_MESSAGE.INNOVAZIONI_AMM';
      case TipologiaAdempimento.COMPORTAMENTI_UNI:
        return 'SEZIONE_22.ADEMPIMENTI.EMPTY_MESSAGE.COMPORTAMENTI_UNI';
      case TipologiaAdempimento.OBIETTIVI:
        return 'SEZIONE_22.ADEMPIMENTI.EMPTY_MESSAGE.OBIETTIVI';
      case TipologiaAdempimento.OBIETTIVI_INFRA:
        return 'SEZIONE_22.ADEMPIMENTI.EMPTY_MESSAGE.OBIETTIVI_INFRA';
      case TipologiaAdempimento.OBIETTIVI_PATRIMONIALI:
        return 'SEZIONE_22.ADEMPIMENTI.EMPTY_MESSAGE.OBIETTIVI_PATRIMONIALI';
      default:
        return 'SEZIONE_22.ADEMPIMENTI.EMPTY_MESSAGE.DEFAULT';
    }
  }

  getAddButtonLabel(): string {
    switch (this.tipologia) {
      case TipologiaAdempimento.INNOVAZIONI_AMM:
        return 'SEZIONE_22.ADEMPIMENTI.ADD_BUTTON.INNOVAZIONI_AMM';
      case TipologiaAdempimento.COMPORTAMENTI_UNI:
        return 'SEZIONE_22.ADEMPIMENTI.ADD_BUTTON.COMPORTAMENTI_UNI';
      case TipologiaAdempimento.OBIETTIVI:
        return 'SEZIONE_22.ADEMPIMENTI.ADD_BUTTON.OBIETTIVI';
      case TipologiaAdempimento.OBIETTIVI_INFRA:
        return 'SEZIONE_22.ADEMPIMENTI.ADD_BUTTON.OBIETTIVI_INFRA';
      case TipologiaAdempimento.OBIETTIVI_PATRIMONIALI:
        return 'SEZIONE_22.ADEMPIMENTI.ADD_BUTTON.OBIETTIVI_PATRIMONIALI';
      default:
        return 'SEZIONE_22.ADEMPIMENTI.ADD_BUTTON.DEFAULT';
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

  handleEditAdempimento(adempimento: AdempimentoDTO) {
    this.adempimentoToEdit = adempimento;
    this.openAdempimentiModal = true;
  }
}
