import {
  Component,
  inject,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { FormArray, FormBuilder, FormGroup } from '@angular/forms';
import { SharedModule } from '../../../../../../../../module/shared/shared.module';
import { ModalMilestoneComponent } from '../modal-milestone/modal-milestone.component';
import { ModalDeleteComponent } from '../../../../../../../../components/modal-delete/modal-delete.component';
import { ModalComponent } from '../../../../../../../../components/modal/modal.component';
import { AzioniComponent } from '../../../../../../../../components/azioni/azioni.component';
import { SvgComponent } from '../../../../../../../../components/svg/svg.component';
import { MilestoneDTO } from '../../../../../../../../models/classes/milestone-dto';
import { IVerticalEllipsisActions } from '../../../../../../../../models/interfaces/vertical-ellipsis-actions';
import { MilestoneService } from '../../../../../../../../services/milestone-service';
import { KEY_PIAO, SHAPE_ICON } from '../../../../../../../../utils/constants';
import { LabelValue } from '../../../../../../../../models/interfaces/label-value';
import { PaginationComponent } from '../../../../../../../../components/pagination/pagination.component';
import { forkJoin, of, Subscription } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PIAODTO } from '../../../../../../../../models/classes/piao-dto';
import { SessionStorageService } from '../../../../../../../../services/session-storage.service';
import { Sezione4Service } from '../../../../../../../../services/sezione4.service';
import { Sezione4DTO } from '../../../../../../../../models/classes/sezione-4-dto';
import { BaseComponent } from '../../../../../../../../components/base/base.component';
import { DatePipe } from '@angular/common';
import { PromemoriaService } from '../../../../../../../../services/promemoria-service';
import { getChangedFields } from '../../../../../../../../utils/utils';

@Component({
  selector: 'piao-elenco-milestone',
  imports: [
    SharedModule,
    ModalMilestoneComponent,
    ModalComponent,
    ModalDeleteComponent,
    AzioniComponent,
    SvgComponent,
    PaginationComponent,
    DatePipe,
  ],
  templateUrl: './elenco-milestone.component.html',
  styleUrl: './elenco-milestone.component.scss',
})
export class ElencoMilestoneComponent
  extends BaseComponent
  implements OnInit, OnDestroy, OnChanges
{
  @Input() formGroup!: FormGroup;
  @Input() dropdownSottofaseMonitoraggio: LabelValue[] = [];
  @Input() testoSezione!: string;
  @Input() isDettaglio: boolean = false;

  openModalMilestone = false;
  openModalDelete = false;
  elementToDelete: any = null;

  icon: string = SHAPE_ICON;
  iconStyle: string = 'icon-modal';

  titleElencoMilestone: string = 'SEZIONE_4.ELENCO_MILESTONE.LABEL';
  notFoundElencoMilestone: string = 'SEZIONE_4.ELENCO_MILESTONE.DESC';
  labelAddMilestone: string = 'SEZIONE_4.ELENCO_MILESTONE.ADD';

  milestoneEdit?: MilestoneDTO;
  milestoneList: MilestoneDTO[] = [];
  paginatedMilestoneList: MilestoneDTO[] = [];

  // Paginazione
  currentPage: number = 1;
  itemsPerPage: number = 3; // Numero di milestone visualizzate per pagina
  totalItems: number = 0;

  milestoneService = inject(MilestoneService);
  fb: FormBuilder = inject(FormBuilder);
  sezione4Service = inject(Sezione4Service);
  promemoriaService = inject(PromemoriaService);

  promemoriaDropdown: LabelValue[] = [];
  piaoDTO!: PIAODTO;
  private subscription = new Subscription();

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO) as PIAODTO;
    this.loadPromemoriaDropdown();
    this.loadMilestones();

    // Sottoscrizione agli aggiornamenti della sezione4 per ricaricare le milestone
    this.subscription.add(
      this.sezione4Service.onSezione4Updated$.subscribe((sezione4: Sezione4DTO) => {
        if (sezione4) {
          this.loadMilestones();
        }
      })
    );
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['dropdownSottofaseMonitoraggio'] && this.milestoneList.length > 0) {
      this.enrichMilestonesWithSottofaseName();
    }
  }

  override ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  /**
   * Carica tutte le milestone da tutte le sottofasi
   */
  private loadMilestones(): void {
    const sottofasiArray = this.formGroup?.get('sottofaseMonitoraggio') as FormArray;
    if (!sottofasiArray || sottofasiArray.length === 0) {
      this.milestoneList = [];
      this.updatePagination();
      return;
    }

    // Raccogli tutti gli id delle sottofasi
    const sottofaseIds = sottofasiArray.controls
      .map((control) => control.get('id')?.value)
      .filter((id) => id != null) as number[];

    if (sottofaseIds.length === 0) {
      this.milestoneList = [];
      this.updatePagination();
      return;
    }

    // Carica le milestone per ogni sottofase usando forkJoin
    const requests = sottofaseIds.map((id) =>
      this.milestoneService.getBySottofaseMonitoraggio(id).pipe(
        catchError(() => of([])) // Se una chiamata fallisce, restituisce array vuoto
      )
    );

    forkJoin(requests).subscribe({
      next: (results) => {
        this.milestoneList = results.flat().filter((m) => m != null) as MilestoneDTO[];
        // Aggiungi il nome della sottofase a ogni milestone per la visualizzazione
        this.enrichMilestonesWithSottofaseName();
        this.updatePagination();
      },
      error: (err: any) => {
        console.error('Errore nel caricamento delle milestone:', err);
        this.milestoneList = [];
        this.updatePagination();
      },
    });
  }

  /**
   * Arricchisce le milestone con il nome della sottofase
   */
  private enrichMilestonesWithSottofaseName(): void {
    this.milestoneList = this.milestoneList.map((milestone) => {
      const sottofase = this.dropdownSottofaseMonitoraggio.find(
        (s) => s.value === milestone.idSottofaseMonitoraggio
      );
      return {
        ...milestone,
        sottofaseDenominazione: sottofase?.label || '',
      } as MilestoneDTO & { sottofaseDenominazione: string };
    });
  }

  /**
   * ricreo la dropdown delle sottofasi escludendo quelle già associate alle milestone,
   * in modo da non poter associare una stessa sottofase a più milestone (vincolo attuale del sistema)
   */
  get dropSottoFaseMon(): LabelValue[] {
    return this.dropdownSottofaseMonitoraggio.map((s) => ({
      ...s,
      hidden: this.milestoneList.some((m) => m.idSottofaseMonitoraggio === s.value),
    }));
  }

  /**
   * Aggiorna la lista paginata
   */
  private updatePagination(): void {
    this.totalItems = this.milestoneList.length;

    // Calcola il numero totale di pagine basato sulle milestone rimanenti
    const totalPages = this.totalItems > 0 ? Math.ceil(this.totalItems / this.itemsPerPage) : 1;

    // Se non ci sono milestone, torna alla pagina 1
    if (this.totalItems === 0) {
      this.currentPage = 1;
      this.paginatedMilestoneList = [];
      return;
    }

    // Se la pagina corrente è maggiore del numero totale di pagine disponibili,
    // torna all'ultima pagina disponibile
    if (this.currentPage > totalPages) {
      this.currentPage = totalPages;
    }

    // Calcola gli indici per la pagina corrente
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    const pageItems = this.milestoneList.slice(startIndex, endIndex);

    // Se la pagina corrente è vuota (non ci sono milestone in questa pagina),
    // calcola la pagina corretta basata sul numero di milestone rimanenti
    if (pageItems.length === 0 && this.totalItems > 0) {
      // Torna all'ultima pagina disponibile
      this.currentPage = totalPages;
      // Ricalcola gli indici con la nuova pagina
      const newStartIndex = (this.currentPage - 1) * this.itemsPerPage;
      const newEndIndex = newStartIndex + this.itemsPerPage;
      this.paginatedMilestoneList = this.milestoneList.slice(newStartIndex, newEndIndex);
    } else {
      this.paginatedMilestoneList = pageItems;
    }
  }

  /**
   * Gestisce il cambio pagina
   */
  onPageChange(page: number): void {
    this.currentPage = page;
    this.updatePagination();
  }

  getActionsFor(milestone: MilestoneDTO): IVerticalEllipsisActions[] {
    return [
      {
        label: 'Modifica',
        callback: () => {
          this.handleEditMilestone(milestone);
        },
      },
      {
        label: 'Elimina',
        callback: () => this.handleOpenModalDelete(milestone),
      },
    ];
  }

  trackByMilestoneId(index: number, milestone: MilestoneDTO): any {
    return milestone.id || index;
  }

  handleOpenModalMilestone(): void {
    if (this.isDettaglio) return;
    this.milestoneEdit = undefined;
    this.openModalMilestone = true;
  }

  handleCloseModalMilestone(): void {
    this.child.formGroup?.reset();
    this.openModalMilestone = false;
    this.milestoneEdit = undefined;
  }

  handleConfirmModalMilestone(): void {
    const modalBody = this.child;
    if (modalBody && modalBody.formGroup && modalBody.formGroup.valid) {
      const formValue = modalBody.formGroup.getRawValue();

      // Costruisce il payload base
      const milestone: MilestoneDTO = {
        id: this.milestoneEdit?.id || formValue.id || undefined, // Include l'id solo se presente (per update)
        idSottofaseMonitoraggio: formValue.idSottofaseMonitoraggio,
        descrizione: formValue.descrizione,
        data: formValue.data ? formValue.data + 'T00:00:00' : undefined, // Aggiunge l'ora alla data per il formato ISO
        isPromemoria: formValue.isPromemoria || false,
      };

      // Include i campi promemoria solo se isPromemoria è true
      if (milestone.isPromemoria) {
        milestone.idPromemoria = formValue.idPromemoria;
        // Include dataPromemoria solo se idPromemoria è 6 (promemoria personalizzato)
        if (formValue.idPromemoria === 6) {
          milestone.dataPromemoria = formValue.dataPromemoria
            ? formValue.dataPromemoria + 'T00:00:00'
            : undefined;
        }
      }

      const milestoneRequest = {
        ...milestone,
        idPiao: this.piaoDTO.id || -1,
        testoSezione: this.testoSezione,
        campiModificati: getChangedFields(
          formValue,
          this.milestoneEdit,
          [
            'id',
            'idSezione4',
            'externalId',
            'key',
            'validity',
            'createdBy',
            'createdTs',
            'updatedBy',
            'updatedTs',
            'createdByRole',
            'updatedByRole',
            'createdByNameSurname',
            'updatedByNameSurname',
            'sottofaseDenominazione',
            'idPiao',
            'testoSezione',
            'campiModificati',
          ], // campi da escludere dal confronto
          'milestone'
        ),
      };

      // Chiama l'API per salvare la milestone
      this.milestoneService.saveOrUpdate(milestoneRequest).subscribe({
        next: () => {
          // Se la POST va a buon fine, chiudi la modale e mostra successo
          // Il reload della sezione4 viene gestito in background dal servizio
          this.toastService.success(
            this.milestoneEdit
              ? 'Milestone aggiornata con successo'
              : 'Milestone inserita con successo'
          );
          // Chiudi la modale e resetta i campi
          this.handleCloseModalMilestone();
          // Ricarica le milestone
          this.loadMilestones();
        },
        error: (err: any) => {
          console.error('Errore nel salvataggio della milestone:', err);
          this.toastService.error('Errore nel salvataggio della milestone');
        },
      });
    } else {
      if (modalBody?.formGroup) {
        modalBody.formGroup.markAllAsTouched();
      }
    }
  }

  handleEditMilestone(milestone: MilestoneDTO): void {
    if (this.isDettaglio) return;
    this.milestoneEdit = milestone;
    this.openModalMilestone = true;
  }

  handleOpenModalDelete(milestone: MilestoneDTO): void {
    if (this.isDettaglio) return;
    this.openModalDelete = true;
    this.elementToDelete = milestone;
  }

  handleRemoveForm(milestone: MilestoneDTO): void {
    const id = milestone?.id;

    if (!id) {
      this.toastService.error('ID milestone non trovato');
      this.handleCloseModalDelete();
      return;
    }

    // Chiama l'API per eliminare la milestone
    this.milestoneService.delete(id, this.piaoDTO.id || -1, this.testoSezione).subscribe({
      next: () => {
        // Se la DELETE va a buon fine, mostra successo e chiudi la modale
        // Il reload della sezione4 viene gestito in background dal servizio
        this.toastService.success('Milestone eliminata con successo');
        this.handleCloseModalDelete();
        // Ricarica le milestone
        this.loadMilestones();
      },
      error: (err: any) => {
        console.error("Errore nell'eliminazione della milestone:", err);
        this.toastService.error("Errore nell'eliminazione della milestone");
        this.handleCloseModalDelete();
      },
    });
  }

  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = undefined;
  }

  /**
   * Ottiene il nome della sottofase per una milestone
   */
  getSottofaseName(milestone: MilestoneDTO): string {
    const sottofase = this.dropdownSottofaseMonitoraggio.find(
      (s) => s.value === milestone.idSottofaseMonitoraggio
    );
    return sottofase?.label || '';
  }

  getPromemoriaById(idPromemoria: number | undefined): string {
    const promemoria = this.promemoriaDropdown.find((p) => p.value === idPromemoria);
    return promemoria?.label || '';
  }

  private loadPromemoriaDropdown(): void {
    this.promemoriaService.getAll().subscribe({
      next: (response) => {
        this.promemoriaDropdown = response.data.map((p) => ({
          label: p.descrizione,
          value: p.id,
        }));
      },
      error: (err: any) => {
        console.error('Errore nel caricamento dei promemoria:', err);
      },
    });
  }
}
