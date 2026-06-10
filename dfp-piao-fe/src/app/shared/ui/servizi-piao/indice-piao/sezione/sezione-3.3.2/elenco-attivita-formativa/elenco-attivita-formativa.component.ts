import { Component, inject, Input, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { ModalComponent } from '../../../../../../components/modal/modal.component';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { BaseComponent } from '../../../../../../components/base/base.component';
import { PIAODTO } from '../../../../../../models/classes/piao-dto';
import { ModalAttivitaFormativaComponent } from '../modal-attivita-formativa/modal-attivita-formativa.component';
import { IVerticalEllipsisActions } from '../../../../../../models/interfaces/vertical-ellipsis-actions';
import { AttivitaFormativeDTO } from '../../../../../../models/classes/attivita-formativa-dto';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';
import { AzioniComponent } from '../../../../../../components/azioni/azioni.component';
import { AttivitaFormativeService } from '../../../../../../services/attivita-formative.service';
import { Sezione332Service } from '../../../../../../services/sezione332.service';
import { LabelValue } from '../../../../../../models/interfaces/label-value';
import { Subscription, forkJoin, takeUntil } from 'rxjs';
import { TipologiaAttivitaDTO } from '../../../../../../models/classes/tipologia-attivita-dto';
import { AmbitoCompetenzaDTO } from '../../../../../../models/classes/ambito-competenza-dto';
import { AreaTematicaDTO } from '../../../../../../models/classes/area-tematica-dto';
import { Sezione332DTO } from '../../../../../../models/classes/sezione-332-dto';
import { SvgComponent } from '../../../../../../components/svg/svg.component';
import { FormArray } from '@angular/forms';
import { getChangedFields } from '../../../../../../utils/utils';

@Component({
  selector: 'piao-elenco-attivita-formativa',
  imports: [
    SharedModule,
    ModalComponent,
    ModalAttivitaFormativaComponent,
    ModalDeleteComponent,
    SvgComponent,
    AzioniComponent,
  ],
  templateUrl: './elenco-attivita-formativa.component.html',
  styleUrl: './elenco-attivita-formativa.component.scss',
})
export class ElencoAttivitaFormativaComponent extends BaseComponent implements OnInit, OnDestroy {
  @Input() piaoDTO!: PIAODTO;
  @Input() sezione332Data?: Sezione332DTO;
  @Input() testoSezione: string = '';
  @Input() isDettaglio: boolean = false;

  attivitaFormativeService = inject(AttivitaFormativeService);
  sezione332Service = inject(Sezione332Service);

  icon: string = 'Note';
  iconStyle: string = 'icon-modal';

  attivitaFormativeCollection: AttivitaFormativeDTO[] = [];
  attivitaToEdit?: AttivitaFormativeDTO;
  rowIndex: number = -1;
  openModalAttivitaFormativa = false;
  openModalDelete: boolean = false;
  elementToDelete: AttivitaFormativeDTO | null = null;

  // Dropdown data for modal
  dropdownTipologiaAttivita: LabelValue[] = [];
  dropdownAmbitoCompetenza: LabelValue[] = [];
  dropdownAreaTematica: LabelValue[] = [];

  // Translation keys
  titleElenco: string = 'SEZIONE_332.ELENCO_ATTIVITA_FORMATIVE.TITLE';
  notFoundMessage: string = 'SEZIONE_332.ELENCO_ATTIVITA_FORMATIVE.NOT_FOUND';
  labelAddRiga: string = 'SEZIONE_332.ELENCO_ATTIVITA_FORMATIVE.ADD_RIGA';

  private subscription = new Subscription();

  ngOnInit(): void {
    this.loadDropdownData();

    // Subscribe to sezione332 updates for hot reloading
    this.subscription.add(
      this.sezione332Service.onSezione332Updated$.pipe(takeUntil(this.destroy$)).subscribe((sezione332) => {
        if (sezione332) {
          this.sezione332Data = sezione332;
          // Update activities from sezione332Data
          this.attivitaFormativeCollection = sezione332.attivitaFormative || [];
        }
      })
    );
  }

  override ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  private loadDropdownData(): void {
    forkJoin({
      tipologiaAttivita: this.sezione332Service.getTipologiaAttivita(),
      ambitoCompetenza: this.sezione332Service.getAmbitoCompetenza(),
      areaTematica: this.sezione332Service.getAreaTematica(),
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: ({ tipologiaAttivita, ambitoCompetenza, areaTematica }) => {
        // Map TipologiaAttivitaDTO to LabelValue
        this.dropdownTipologiaAttivita = (tipologiaAttivita || []).map(
          (item: TipologiaAttivitaDTO) => ({
            label: item.testo || item.codice || '',
            value: item.id || 0,
          })
        );

        // Map AmbitoCompetenzaDTO to LabelValue
        this.dropdownAmbitoCompetenza = (ambitoCompetenza || []).map(
          (item: AmbitoCompetenzaDTO) => ({
            label: item.testo || item.codice || '',
            value: item.id || 0,
          })
        );

        // Map AreaTematicaDTO to LabelValue
        this.dropdownAreaTematica = (areaTematica || []).map((item: AreaTematicaDTO) => ({
          label: item.testo || item.codice || '',
          value: item.id || 0,
        }));
      },
      error: (err) => {
        console.error('Errore nel caricamento dei dropdown:', err);
        this.dropdownTipologiaAttivita = [];
        this.dropdownAmbitoCompetenza = [];
        this.dropdownAreaTematica = [];
      },
    });
  }

  handleOpenModal(): void {
    this.attivitaToEdit = undefined;
    this.rowIndex = -1;
    this.openModalAttivitaFormativa = true;
  }

  handleCloseModal(): void {
    this.openModalAttivitaFormativa = false;
    this.attivitaToEdit = undefined;
  }

  handleConfirmModal(): void {
    if (this.child?.formGroup) {
      const formValue = { ...this.child.formGroup.value };
      // Convert verificaApprendimento from string to boolean
      if (
        formValue.verificaApprendimento !== null &&
        formValue.verificaApprendimento !== undefined
      ) {
        formValue.verificaApprendimento = formValue.verificaApprendimento === 'true';
      }

      // Convert form values to DTO
      const attivitaFormativaDTO: AttivitaFormativeDTO = {
        id: formValue.id || undefined,
        idSezione332: formValue.idSezione332 || this.piaoDTO.idSezione332 || undefined,
        idTipologiaAttivita: formValue.idTipologiaAttivita || undefined,
        idAmbitoCompetenza: formValue.idAmbitoCompetenza || undefined,
        idAreaTematica: formValue.idAreaTematica || undefined,
        numeroDirigenti: formValue.numeroDirigenti ? Number(formValue.numeroDirigenti) : undefined,
        numeroNonDirigenti: formValue.numeroNonDirigenti
          ? Number(formValue.numeroNonDirigenti)
          : undefined,
        oreFormazione: formValue.oreFormazione ? Number(formValue.oreFormazione) : undefined,
        verificaApprendimento: formValue.verificaApprendimento,
      };

      const attivitaFormativaRequest = {
        ...attivitaFormativaDTO,
        testoSezione: this.testoSezione,
        idPiao: this.piaoDTO.id || -1,
        campiModificati: getChangedFields(
          formValue,
          this.attivitaToEdit,
          ['id', 'idSezione332'], // campi da escludere dal confronto
          'attivitaFormative'
        ),
      };

      // Save via service
      this.attivitaFormativeService.save(attivitaFormativaRequest).pipe(takeUntil(this.destroy$)).subscribe({
        next: (saved) => {
          this.toastService.success('Attività formativa salvata con successo');
          this.openModalAttivitaFormativa = false;
          this.attivitaToEdit = undefined;
          // Reload section 332 to refetch all data including activities
          // The onSezione332Updated$ subscription will handle the update
          this.sezione332Service.reloadSezione332AndUpdateSession().pipe(takeUntil(this.destroy$)).subscribe();
        },
        error: (err) => {
          console.error("Errore nel salvataggio dell'attività formativa:", err);
          this.toastService.error("Errore nel salvataggio dell'attività formativa");
        },
      });
    }
  }

  getActionsFor(attivita: AttivitaFormativeDTO): IVerticalEllipsisActions[] {
    return [
      {
        label: 'Modifica',
        callback: () => {
          this.handleEditAttivita(attivita);
        },
      },
      {
        label: 'Elimina',
        callback: () => this.handleOpenModalDelete(attivita),
      },
    ];
  }

  handleEditAttivita(attivita: AttivitaFormativeDTO): void {
    this.attivitaToEdit = attivita;
    this.rowIndex = this.attivitaFormative.findIndex((a) => a.id === attivita.id);
    this.openModalAttivitaFormativa = true;
  }

  handleOpenModalDelete(attivita: AttivitaFormativeDTO): void {
    this.openModalDelete = true;
    this.elementToDelete = attivita;
  }

  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = null;
  }

  handleDeleteAttivita(attivita: AttivitaFormativeDTO): void {
    if (attivita.id) {
      this.attivitaFormativeService
        .delete(attivita.id, this.piaoDTO.id || -1, this.testoSezione)
        .pipe(takeUntil(this.destroy$)).subscribe({
          next: () => {
            this.toastService.success('Attività formativa eliminata con successo');
            this.handleCloseModalDelete();
            // Reload section 332 to refetch all data including activities
            // The onSezione332Updated$ subscription will handle the update
            this.sezione332Service.reloadSezione332AndUpdateSession().pipe(takeUntil(this.destroy$)).subscribe();
          },
          error: (err) => {
            console.error("Errore nell'eliminazione dell'attività formativa:", err);
            this.toastService.error("Errore nell'eliminazione dell'attività formativa");
          },
        });
    } else {
      this.handleCloseModalDelete();
    }
  }

  getTipologiaAttivitaLabel(id: number): string {
    const item = this.dropdownTipologiaAttivita.find((d) => d.value === id);
    return item?.label || '';
  }

  getAmbitoCompetenzaLabel(id: number): string {
    const item = this.dropdownAmbitoCompetenza.find((d) => d.value === id);
    return item?.label || '';
  }

  getAreaTematicaLabel(id: number): string {
    const item = this.dropdownAreaTematica.find((d) => d.value === id);
    return item?.label || '';
  }

  /**
   * Format large numbers for display, handling exponential notation
   */
  formatNumberForDisplay(value: number | null | undefined): string {
    if (value === null || value === undefined) {
      return '';
    }

    // Check if number is in exponential notation or very large
    const numValue = Number(value);
    if (!isFinite(numValue)) {
      return '';
    }

    // If the number is less than 1 billion, display normally
    if (numValue < 1e10) {
      return numValue.toString();
    }

    // For very large numbers, use exponential notation with proper formatting
    return numValue.toExponential(2);
  }

  get attivitaFormative(): AttivitaFormativeDTO[] {
    if (
      !this.sezione332Data ||
      !this.sezione332Data.attivitaFormative ||
      this.sezione332Data.attivitaFormative.length === 0
    ) {
      return [];
    }

    this.attivitaFormativeCollection = this.sezione332Data.attivitaFormative;

    this.attivitaFormativeCollection.sort((a, b) => {
      const idA = a.id;
      const idB = b.id;

      if (idA == null && idB == null) return 0;
      if (idA == null) return 1;
      if (idB == null) return -1;

      return idA - idB;
    });

    return this.attivitaFormativeCollection;
  }
}
