import { Component, inject, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { FormGroup, FormArray, FormBuilder } from '@angular/forms';
import { BaseComponent } from '../../../../../../components/base/base.component';
import { ModalComponent } from '../../../../../../components/modal/modal.component';
import { ModalFotografiaObiettiviComponent } from '../modal-fotografia-obiettivi/modal-fotografia-obiettivi.component';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';
import { SvgComponent } from '../../../../../../components/svg/svg.component';
import { AzioniComponent } from '../../../../../../components/azioni/azioni.component';
import { IVerticalEllipsisActions } from '../../../../../../models/interfaces/vertical-ellipsis-actions';
import { ObiettiviRisultatiFotografiaDTO } from '../../../../../../models/classes/obiettivi-risultati-fotografia-dto';
import { ObiettiviRisultatiFotografiaService } from '../../../../../../services/obiettivi-risultati-fotografia.service';
import { SessionStorageService } from '../../../../../../services/session-storage.service';
import { PIAODTO } from '../../../../../../models/classes/piao-dto';
import { KEY_PIAO, SHAPE_ICON } from '../../../../../../utils/constants';
import { DatePipe } from '@angular/common';
import { Sezione332Service } from '../../../../../../services/sezione332.service';
import { LabelValue } from '../../../../../../models/interfaces/label-value';
import { forkJoin, takeUntil } from 'rxjs';
import { TruncatePipe } from '../../../../../../pipe/truncate.pipe';
import { CodTipologiaFotoObiettivoEnum } from '../../../../../../models/enums/cod-tipologia-foto-obi.enum';
import { getChangedFields } from '../../../../../../utils/utils';

@Component({
  selector: 'piao-elenco-fotografia-obiettivi',
  imports: [
    SharedModule,
    ModalComponent,
    ModalFotografiaObiettiviComponent,
    ModalDeleteComponent,
    SvgComponent,
    AzioniComponent,
    DatePipe,
    TruncatePipe,
  ],
  templateUrl: './elenco-fotografia-obiettivi.component.html',
  styleUrl: './elenco-fotografia-obiettivi.component.scss',
})
export class ElencoFotografiaObiettiviComponent extends BaseComponent implements OnInit {
  @Input() form!: FormGroup;
  @Input() testoSezione!: string;
  @Input() codTipologiaFK!: string;
  @Input() isDettaglio: boolean = false;
  @Input() formArrayName: string = 'obiettiviRisultatiFotografia';

  fotografiaService = inject(ObiettiviRisultatiFotografiaService);
  fb = inject(FormBuilder);
  private sezione332Service = inject(Sezione332Service);

  icon: string = SHAPE_ICON;
  iconStyle: string = 'icon-modal';

  piaoDTO!: PIAODTO;
  fotografiaToEdit?: ObiettiviRisultatiFotografiaDTO;
  fotografiaIndex: number = 0;
  openModalFotografia = false;

  titleElenco: string = 'SEZIONE-3.3.2.ELENCO_FOTOGRAFIA_OBIETTIVI.TITLE_ELENCO';
  labelAddElenco: string = 'SEZIONE-3.3.2.ELENCO_FOTOGRAFIA_OBIETTIVI.LABEL_ADD_ELENCO';
  fotografiaNotFound: string = 'SEZIONE-3.3.2.ELENCO_FOTOGRAFIA_OBIETTIVI.NOT_FOUND';

  openModalDelete: boolean = false;
  elementToDelete: any = null;

  expandedTitoli = new Set<number>();
  expandedRiferimenti = new Set<number>();
  expandedEnti = new Set<number>();

  dropdownTipologiaAttivita: LabelValue[] = [];
  dropdownAmbitoCompetenza: LabelValue[] = [];
  dropdownAreaTematica: LabelValue[] = [];
  dropdownTipologiaDestinatari: LabelValue[] = [];

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
    this.fotografiaIndex = this.fotografiaArray.length;
    this.loadDropdowns();
  }

  private loadDropdowns(): void {
    forkJoin({
      tipologiaAttivita: this.sezione332Service.getTipologiaAttivita(),
      ambitoCompetenza: this.sezione332Service.getAmbitoCompetenza(),
      areaTematica: this.sezione332Service.getAreaTematica(),
      tipologiaDestinatari: this.sezione332Service.getTipologiaDestinatari(),
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: ({ tipologiaAttivita, ambitoCompetenza, areaTematica, tipologiaDestinatari }) => {
        this.dropdownTipologiaAttivita = tipologiaAttivita.map((t) => ({
          label: t.testo,
          value: t.id,
          additionalField: t.codice,
        }));
        this.dropdownAmbitoCompetenza = ambitoCompetenza.map((a) => ({
          label: a.testo,
          value: a.id,
        }));
        this.dropdownAreaTematica = areaTematica.map((a) => ({
          label: a.testo,
          value: a.id,
        }));
        this.dropdownTipologiaDestinatari = tipologiaDestinatari.map((d) => ({
          label: d.testo,
          value: d.id,
        }));
      },
      error: (err) => {
        console.error('Errore nel caricamento dei dropdown:', err);
      },
    });
  }

  handleAddFotografia(): void {
    const formValue = {
      ...this.child.formGroup.value,
      idPiao: this.piaoDTO.id,
      testoSezione: this.testoSezione,
      campiModificati: getChangedFields(
        this.child.formGroup.value,
        this.fotografiaToEdit,
        [
          'id',
          'idSezione332',
          'codTipologiaFK',
          'validity',
          'createdBy',
          'createdTs',
          'updatedBy',
          'updatedTs',
          'createdByRole',
          'updatedByRole',
          'createdByNameSurname',
          'updatedByNameSurname',
        ], // campi da escludere dal confronto
        this.codTipologiaFK
      ),
    };
    this.fotografiaService.save(formValue).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.toastService.success('Fotografia obiettivi salvata con successo');
        this.child?.formGroup.reset();
      },
      error: (err) => {
        console.error('Errore nel salvare la fotografia obiettivi:', err);
        this.child?.formGroup.reset();
      },
    });
    this.openModalFotografia = false;
    this.fotografiaToEdit = undefined;
    this.fotografiaIndex = this.fotografiaArray.length;
  }

  trackByFotografiaId(index: number, item: any): any {
    return item.get('id')?.value || index;
  }

  getActionsFor(index: number): IVerticalEllipsisActions[] {
    const control = this.fotografiaArray.at(index);

    if (!control) {
      return [];
    }

    const fotografia = control.value;

    return [
      {
        label: 'Modifica',
        callback: () => {
          this.handleEditFotografia(fotografia, index);
        },
      },
      {
        label: 'Elimina',
        callback: () => this.handleOpenModalDelete(fotografia),
      },
    ];
  }

  handleOpenFotografiaModal(): void {
    this.fotografiaToEdit = undefined;
    this.fotografiaIndex = this.fotografiaArray.length;
    this.openModalFotografia = true;
  }

  handleEditFotografia(fotografia: ObiettiviRisultatiFotografiaDTO, index: number): void {
    this.fotografiaToEdit = fotografia;
    this.openModalFotografia = true;
    this.fotografiaIndex = index;
  }

  handleRemoveForm(fotografia: ObiettiviRisultatiFotografiaDTO) {
    const index = this.fotografiaArray.controls.findIndex(
      (control) => control.get('id')?.value === fotografia.id
    );

    if (index === -1) {
      console.error('Fotografia obiettivi non trovata nel FormArray');
      this.handleCloseModalDelete();
      return;
    }

    if (fotografia.id) {
      this.fotografiaService
        .delete(fotografia.id, this.piaoDTO.id || -1, this.testoSezione, this.codTipologiaFK)
        .pipe(takeUntil(this.destroy$)).subscribe({
          next: () => {
            this.toastService.success('Fotografia obiettivi eliminata con successo');
          },
          error: (err) => {
            console.error("Errore nell'eliminazione della fotografia obiettivi:", err);
          },
        });
    } else {
      this.fotografiaArray.removeAt(index);
      this.toastService.success('Fotografia obiettivi eliminata con successo');
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

  get fotografiaArray(): FormArray {
    const formArray = this.form.get(this.formArrayName) as FormArray;

    if (!formArray) {
      return this.fb.array([]);
    }

    if (formArray.length === 0) {
      return formArray;
    }

    const sorted = [...formArray.controls].sort((a, b) => {
      const idA = a.get('id')?.value;
      const idB = b.get('id')?.value;

      if (idA == null && idB == null) return 0;
      if (idA == null) return 1;
      if (idB == null) return -1;

      return idA - idB;
    });

    return this.fb.array(sorted);
  }

  getTipologiaAttivitaLabel(value: number): string {
    const found = this.dropdownTipologiaAttivita.find((item) => item.value === value);
    return found ? found.label : '';
  }
  getAmbitoCompetenzaLabel(value: number): string {
    const found = this.dropdownAmbitoCompetenza.find((item) => item.value === value);
    return found ? found.label : '';
  }
  getAreaTematicaLabel(value: number): string {
    const found = this.dropdownAreaTematica.find((item) => item.value === value);
    return found ? found.label : '';
  }
  getTipologiaDestinatariLabel(value: number): string {
    const found = this.dropdownTipologiaDestinatari.find((item) => item.value === value);
    return found ? found.label : '';
  }
}
