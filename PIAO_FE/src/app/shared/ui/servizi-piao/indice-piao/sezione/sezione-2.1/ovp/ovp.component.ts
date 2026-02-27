import { Component, Input, OnInit, inject } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { CardInfoComponent } from '../../../../../card-info/card-info.component';
import { CardAlertComponent } from '../../../../../card-alert/card-alert.component';
import { FormArray, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';
import { StrategieAttuativeComponent } from '../strategie-attuative/strategie-attuative.component';
import { ModalComponent } from '../../../../../../components/modal/modal.component';
import { ModalOvpComponent } from './modal-ovp/modal-ovp.component';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';
import { AreaOrganizzativaDTO } from '../../../../../../models/classes/area-organizzativa-dto';
import { PrioritaPoliticaDTO } from '../../../../../../models/classes/priorita-politica-dto';
import { StakeHolderDTO } from '../../../../../../models/classes/stakeholder-dto';
import { BaseComponent } from '../../../../../../components/base/base.component';
import { OvpService } from '../../../../../../services/ovp.service';
import { OVPDTO } from '../../../../../../models/classes/ovp-dto';
import { ToastService } from '../../../../../../services/toast.service';
import { Sezione21Service } from '../../../../../../services/sezioni-21.service';
import { TextAreaComponent } from '../../../../../../components/text-area/text-area.component';
import { TooltipComponent } from '../../../../../../components/tooltip/tooltip.component';
import { ONLY_NUMBERS_REGEX } from '../../../../../../utils/constants';

@Component({
  selector: 'piao-ovp',
  imports: [
    SharedModule,
    CardInfoComponent,
    CardAlertComponent,
    TextBoxComponent,
    StrategieAttuativeComponent,
    ModalComponent,
    ModalOvpComponent,
    ModalDeleteComponent,
    TextBoxComponent,
    TextAreaComponent,
    TooltipComponent,
  ],
  templateUrl: './ovp.component.html',
  styleUrl: './ovp.component.scss',
})
export class OvpComponent extends BaseComponent implements OnInit {
  private fb = inject(FormBuilder);
  private ovpService = inject(OvpService);
  private toastService = inject(ToastService);
  private sezione21Service = inject(Sezione21Service);

  @Input() formGroup!: FormGroup;
  @Input() subTitleObiettiviValorePubblico!: string;
  @Input() descriptionObiettiviValorePubblico!: string;
  @Input() iconAlert!: string;
  @Input() titleAlertObiettiviVP!: string;
  @Input() subTitleAlertObiettiviVP!: string;
  @Input() titleCardObiettivoVP!: string;
  @Input() labelBtnAddObiettivoVP!: string;
  @Input() idSezione1?: number;
  @Input() idSezione21?: number;
  @Input() idPiao?: number;
  @Input() areeOrganizzativeList: AreaOrganizzativaDTO[] = [];
  @Input() prioritaPoliticheList: PrioritaPoliticaDTO[] = [];
  @Input() stakeholdersList: StakeHolderDTO[] = [];

  openModalOvp: boolean = false;
  openModalDelete: boolean = false;
  editingOvpIndex: number | null = null;
  elementToDelete: OVPDTO | null = null;
  isSaving: boolean = false;
  modalTitle: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.MODAL.TITLE';
  titleObiettivoVP: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.TITLE';
  labelIdOVP: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.DETAILS.ID_OVP_DETAILS';
  labelDescription: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.DETAILS.DESCRIPTION';
  labelContext: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.DETAILS.CONTEXT_ANALYSIS';
  labelOrganizationalArea: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.DETAILS.ORGANIZATIONAL_AREA';
  labelPoliticalManager: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.DETAILS.POLITICAL_MANAGER';
  labelAdministrativeManager: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.DETAILS.ADMINISTRATIVE_MANAGER';
  labelStakeholder: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.DETAILS.STAKEHOLDERS';
  labelSector: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.DETAILS.SECTOR';
  labelPoliticalyPriority: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.DETAILS.POLITICAL_PRIORITY';

  labelTitleIndex: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.DETAILS.INDEX.TITLE';
  labelIndexValue: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.DETAILS.INDEX.VALUE';
  labelIndexDesc: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.DETAILS.INDEX.DESCRIPTION';

  ngOnInit(): void {
    console.log('ovpControls', this.formGroup.controls);
  }

  /**
   * Ottiene i dati OVP come array di OVPDTO per passarli alla modale
   */
  get ovpDataArray(): OVPDTO[] {
    return this.ovp.controls.map((control) => {
      const fg = control as FormGroup;
      return fg.value as OVPDTO;
    });
  }

  handleAddObiettivoVP(): void {
    this.editingOvpIndex = null;
    this.openModalOvp = true;
  }

  handleEditOVP(ovp: FormGroup): void {
    const index = this.ovp.controls.findIndex((control) => control === ovp);
    this.editingOvpIndex = index >= 0 ? index : null;
    console.log("Modifica OVP all'indice:", this.editingOvpIndex);
    this.openModalOvp = true;
  }

  handleRemoveOVP(ovp: FormGroup): void {
    // Apre la modale di conferma eliminazione
    this.elementToDelete = ovp.value as OVPDTO;
    this.openModalDelete = true;
  }

  handleConfirmDeleteOvp(ovpToDelete: OVPDTO): void {
    console.log('Eliminazione OVP:', ovpToDelete);
    console.log('Stato attuale OVP:', this.ovp.value);
    // Trova l'indice dell'elemento da eliminare
    const index = this.ovp.controls.findIndex(
      (control) => (control as FormGroup).controls['codice'].value === ovpToDelete.codice
    );

    console.log('Indice OVP da eliminare:', index);

    if (index < 0) {
      this.toastService.error("Impossibile trovare l'OVP da eliminare");
      this.handleCloseModalDelete();
      return;
    }

    const codiceOvp = ovpToDelete.codice || 'N/A';

    // Se ha un ID, elimina dal backend
    if (ovpToDelete.id) {
      this.ovpService.delete(ovpToDelete.id).subscribe({
        next: () => {
          this.toastService.success(`OVP ${codiceOvp} eliminato con successo`);
          this.handleCloseModalDelete();
        },
        error: (err) => {
          console.error("Errore durante l'eliminazione dell'OVP:", err);
          this.toastService.error("Errore durante l'eliminazione dell'OVP");
          this.handleCloseModalDelete();
        },
      });
    } else {
      // Altrimenti rimuovi solo dall'array
      this.ovp.removeAt(index);
      this.toastService.success(`OVP ${codiceOvp} rimosso con successo`);
      this.handleCloseModalDelete();
    }
  }

  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = null;
  }

  handleCloseModalOvp(): void {
    this.openModalOvp = false;
    this.editingOvpIndex = null;
  }

  handleConfirmModalOvp(): void {
    const modalBody = this.child as ModalOvpComponent;
    if (modalBody && modalBody.formGroup && modalBody.formGroup.valid) {
      // Usa il metodo buildOvpDTO() della modale per costruire il DTO
      const ovpDTO = modalBody.buildOvpDTO();

      // Imposta sezione21Id se non presente
      if (!ovpDTO.sezione21Id && this.idSezione21) {
        ovpDTO.sezione21Id = this.idSezione21;
      }

      this.isSaving = true;

      // Chiama il servizio POST /ovp/save
      this.ovpService.save(ovpDTO).subscribe({
        next: (savedOvp) => {
          this.isSaving = false;
          this.toastService.success('OVP salvato con successo');
          this.handleCloseModalOvp();
        },
        error: (err) => {
          this.isSaving = false;
          this.handleCloseModalOvp();
          console.error('Errore nel salvare OVP:', err);
        },
      });
    }
  }

  get ovp(): FormArray {
    const formArray = this.formGroup.get('ovp') as FormArray;

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
