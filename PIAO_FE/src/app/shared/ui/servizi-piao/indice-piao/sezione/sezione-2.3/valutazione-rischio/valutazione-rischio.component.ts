import { Component, EventEmitter, inject, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { BaseComponent } from '../../../../../../components/base/base.component';
import { ModalComponent } from '../../../../../../components/modal/modal.component';
import { ModalEventiRischiosiComponent } from '../eventi-rischiosi/modal-eventi-rischiosi/modal-eventi-rischiosi.component';
import { CardInfoComponent } from '../../../../../card-info/card-info.component';
import { AbstractControl, FormArray, FormBuilder, FormGroup } from '@angular/forms';
import { LabelValue } from '../../../../../../models/interfaces/label-value';
import { AccordionComponent } from '../../../../../../components/accordion/accordion.component';
import { DropdownComponent } from '../../../../../../components/dropdown/dropdown.component';
import { ToastService } from '../../../../../../services/toast.service';
import { EventoRischiosoDTO } from '../../../../../../models/classes/evento-rischioso-dto';
import { IVerticalEllipsisActions } from '../../../../../../models/interfaces/vertical-ellipsis-actions';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';
import { AzioniComponent } from '../../../../../../components/azioni/azioni.component';
import { SvgComponent } from '../../../../../../components/svg/svg.component';
import { EventoRischioService } from '../../../../../../services/evento-rischio.service';
import { FattoreDTO } from '../../../../../../models/classes/fattore-dto';
import {
  createFormArrayMisuraPrevenzioneEventoRischioFromPiaoSession,
  createFormMongoFromPiaoSession,
  minArrayLength,
} from '../../../../../../utils/utils';
import { INPUT_REGEX } from '../../../../../../utils/constants';
import { UlterioriInfoDTO } from '../../../../../../models/classes/ulteriori-info-dto';

@Component({
  selector: 'piao-valutazione-rischio',
  imports: [
    SharedModule,
    ModalComponent,
    ModalEventiRischiosiComponent,
    CardInfoComponent,
    AccordionComponent,
    DropdownComponent,
    ModalDeleteComponent,
    SvgComponent,
    AzioniComponent,
  ],
  templateUrl: './valutazione-rischio.component.html',
  styleUrls: ['./valutazione-rischio.component.scss'],
})
export class ValutazioneRischioComponent extends BaseComponent implements OnInit, OnDestroy {
  @Input() formGroup!: FormGroup;

  labelValutazioneRischio: string = 'SEZIONE_23.VALUTAZIONE_RISCHIO.ATTIVITA_SENSIBILE.TITLE';
  subTitleValutazioneRischio: string =
    'SEZIONE_23.VALUTAZIONE_RISCHIO.ATTIVITA_SENSIBILE.SUB_TITLE';
  labelSelectAttivita: string = 'SEZIONE_23.VALUTAZIONE_RISCHIO.ATTIVITA_SENSIBILE.SELECT_LABEL';
  labelEventiRischiosi: string =
    'SEZIONE_23.VALUTAZIONE_RISCHIO.ATTIVITA_SENSIBILE.EVENTI_RISCHIOSI_LABEL';
  notFoundEventiRischiosi: string =
    'SEZIONE_23.VALUTAZIONE_RISCHIO.ATTIVITA_SENSIBILE.NOT_FOUND_EVENTI_RISCHIOSI';
  labelAddEventoRischioso: string =
    'SEZIONE_23.VALUTAZIONE_RISCHIO.ATTIVITA_SENSIBILE.ADD_EVENTO_RISCHIOSO';

  labelAddValutazione: string = 'SEZIONE_23.VALUTAZIONE_RISCHIO.ADD_VALUTAZIONE';
  titleCardInfoNotFound: string = 'SEZIONE_23.VALUTAZIONE_RISCHIO.CARD_INFO_NOT_FOUND';
  titleCardInfo: string = 'SEZIONE_23.VALUTAZIONE_RISCHIO.CARD_INFO_TITLE';

  titleEventiRischiosi: string =
    'SEZIONE_23.VALUTAZIONE_RISCHIO.ATTIVITA_SENSIBILE.EVENTI_RISCHIOSI_TITLE';

  titleEventiRischiosiNotFound: string =
    'SEZIONE_23.VALUTAZIONE_RISCHIO.ATTIVITA_SENSIBILE.EVENTI_RISCHIOSI_NOT_FOUND';

  // Modal state
  openModalEventiRischiosi: boolean = false;

  eventoToEdit?: EventoRischiosoDTO;

  openModalDelete: boolean = false;
  openModalDeleteValutazione: boolean = false;
  elementToDelete: any = null;

  // Dropdown options for Livello del rischio
  //TODO aggancio servizio eventoRischioDropdown

  livelloRischioOptions: LabelValue[] = [
    { label: 'Basso', value: 1 },
    { label: 'Medio', value: 2 },
    { label: 'Alto', value: 3 },
  ];

  // Modal config
  iconPencil: string = 'Pencil';
  iconStyleModal: string = 'icon-modal';

  toastService = inject(ToastService);

  eventoRischioService = inject(EventoRischioService);

  private fb = inject(FormBuilder);
  openAccordionIndex: any;

  sortAscending: boolean = true;
  isSortedManuallyValutazione: boolean = false;
  isSortedManuallyEventi: { [key: number]: boolean } = {};

  currentValutazioneRischioIndex: number | null = null;
  currentEventoRischiosoIndex: number | null = null;

  ngOnInit(): void {
    console.log('FormGroup Valutazione Rischio:', this.valutazioneRischio);
  }

  get attivitaSensibileOptions(): LabelValue[] {
    const attivitaSensibileArray = this.formGroup?.get('attivitaSensibile') as FormArray;
    if (!attivitaSensibileArray) return [];

    // Raccoglie gli id delle attività già assegnate in valutazioneRischio
    const valutazioneRischioArray = this.formGroup?.get('valutazioneRischio') as FormArray;
    const usedIds = new Set<number>();
    if (valutazioneRischioArray) {
      valutazioneRischioArray.controls.forEach((ctrl) => {
        const id = ctrl.get('idAttivitaSensibile')?.value;
        if (id != null) {
          usedIds.add(id);
        }
      });
    }

    return attivitaSensibileArray.controls
      .filter(
        (control) => control.get('id')?.value != null && control.get('id')?.value != undefined
      )
      .map((control) => ({
        label: control.get('denominazione')?.value || '',
        value: control.get('id')?.value,
        // Nascondi nella lista delle scelte se è già assegnata ad una valutazione
        hidden: usedIds.has(control.get('id')?.value),
      }));
  }

  handleAttivitaSensibileChange(event: any, index: number): void {
    const valutazioneGroup = this.valutazioneRischio.at(index);
    valutazioneGroup?.get('idAttivitaSensibile')?.setValue(event);
    const idAttivitaSensibile = valutazioneGroup?.get('idAttivitaSensibile')?.value;
    this.eventoRischioService
      .getEventiRischiosiByIdAttivitaSensibile(idAttivitaSensibile)
      .subscribe({
        next: (eventiRischiosi) => {
          const eventiFormArray = valutazioneGroup?.get('eventiRischiosi') as FormArray;
          eventiFormArray.clear();
          eventiRischiosi.forEach((evento) => {
            eventiFormArray.push(
              this.fb.group({
                id: [evento.id],
                idAttivitaSensibile: [evento.idAttivitaSensibile],
                denominazione: [evento.denominazione],
                motivazione: [evento.motivazione],
                probabilita: [evento.probabilita],
                controlli: [evento.controlli],
                impatto: [evento.impatto],
                valutazione: [evento.valutazione],
                idLivelloRischio: [evento.idLivelloRischio],
                fattore: createFormMongoFromPiaoSession<FattoreDTO>(
                  this.fb,
                  evento.fattore || new FattoreDTO(),
                  ['id', 'externalId', 'properties'],
                  INPUT_REGEX,
                  50,
                  false
                ),
                ulterioriInfo: createFormMongoFromPiaoSession<UlterioriInfoDTO>(
                  this.fb,
                  evento.ulterioriInfo || new UlterioriInfoDTO(),
                  ['id', 'externalId', 'properties'],
                  INPUT_REGEX,
                  50,
                  false
                ),
                misure: createFormArrayMisuraPrevenzioneEventoRischioFromPiaoSession(
                  this.fb,
                  evento.misure || []
                ),
              })
            );
          });
        },
        error: () => {
          console.log(
            "Errore durante il caricamento degli eventi rischiosi per l'attività sensibile con id:",
            idAttivitaSensibile
          );
        },
      });
  }

  handleOpenModalEventiRischiosi(valutazioneIndex: number, idAttivita: number): void {
    if (idAttivita == null || idAttivita === undefined) {
      this.toastService.warning(
        "Per aggiungere un evento rischioso è necessario prima selezionare un'attività sensibile"
      );
      return;
    }
    this.currentValutazioneRischioIndex = valutazioneIndex;
    this.idAttivitaSensibile = idAttivita;
    this.openModalEventiRischiosi = true;
    this.eventoToEdit = undefined;
  }

  handleCloseModalEventiRischiosi(): void {
    this.openModalEventiRischiosi = false;
    this.currentValutazioneRischioIndex = null;
    if (this.child) {
      this.child.formGroup.reset();
    }
  }

  handleConfirmModalEventiRischiosi(): void {
    if (this.child && this.child.formGroup.valid) {
      const data = this.child.formGroup.value;
      const eventiFormArray = this.eventiRischiosi;
      const isEdit = this.currentEventoRischiosoIndex !== null;
      this.eventoRischioService.save(data).subscribe({
        next: (saved: EventoRischiosoDTO) => {
          const newGroup = this.fb.group({
            id: [saved.id],
            idAttivitaSensibile: [data.idAttivitaSensibile],
            denominazione: [data.denominazione],
            motivazione: [data.motivazione],
            probabilita: [data.probabilita],
            controlli: [data.controlli],
            impatto: [data.impatto],
            valutazione: [data.valutazione],
            idLivelloRischio: [data.idLivelloRischio],
            fattore: createFormMongoFromPiaoSession<FattoreDTO>(
              this.fb,
              saved.fattore || new FattoreDTO(),
              ['id', 'externalId', 'properties'],
              INPUT_REGEX,
              50,
              false
            ),
            ulterioriInfo: createFormMongoFromPiaoSession<UlterioriInfoDTO>(
              this.fb,
              saved.ulterioriInfo || new UlterioriInfoDTO(),
              ['id', 'externalId', 'properties'],
              INPUT_REGEX,
              50,
              false
            ),
            misure: createFormArrayMisuraPrevenzioneEventoRischioFromPiaoSession(
              this.fb,
              saved.misure || []
            ),
          });

          if (isEdit && this.currentEventoRischiosoIndex !== null) {
            eventiFormArray.setControl(this.currentEventoRischiosoIndex, newGroup);
            this.toastService.success('Evento rischioso modificato con successo');
          } else {
            eventiFormArray.push(newGroup);
            this.toastService.success('Evento rischioso salvato con successo');
          }
        },
        error: () => {
          console.log("Errore durante il salvataggio dell'evento rischioso:", data);
        },
      });
    }
    this.handleCloseModalEventiRischiosi();
  }

  handleAddValutazione(): void {
    if (this.attivitaSensibileOptions.filter((opt) => !opt.hidden).length === 0) {
      this.toastService.warning(
        "Per aggiungere una valutazione del rischio è necessario prima aggiungere e salvare un'attività sensibile"
      );
      return;
    }
    const newValutazioneRischio = this.fb.group({
      idAttivitaSensibile: [null],
      eventiRischiosi: this.fb.array<FormGroup>([], [minArrayLength(1)]),
    });
    this.valutazioneRischio.push(newValutazioneRischio);
    this.openAccordionIndex = this.valutazioneRischio.length;
  }

  handleRemoveValutazione(index: number): void {
    const idAttivitaSensibile = this.valutazioneRischio
      .at(index)
      ?.get('idAttivitaSensibile')?.value;
    if (idAttivitaSensibile) {
      this.eventoRischioService.deleteByAttivitaSensibile(idAttivitaSensibile).subscribe({
        next: () => {
          this.valutazioneRischio.removeAt(index);
          this.toastService.success('Valutazione del rischio eliminata con successo');
        },
        error: () => {},
      });
    } else {
      this.valutazioneRischio.removeAt(index);
      this.toastService.success('Valutazione del rischio eliminata con successo');
    }
    this.handleCloseModalDeleteValutazione();
  }

  trackByValutazioneId(index: number, item: any): any {
    // Usa l'ID della misura se disponibile, altrimenti usa l'indice
    return item.get('id')?.value || index;
  }

  getActionsFor(index: number, valutazioneIndex: number): IVerticalEllipsisActions[] {
    const eventiRischiosi = this.getEventiRischiosi(valutazioneIndex);
    const eventoRischiosoControl = eventiRischiosi.at(index);

    // Verifica che la fase esista ancora (potrebbe essere stata eliminata)
    if (!eventoRischiosoControl) {
      return [];
    }

    const eventoRischioso = eventoRischiosoControl.value;

    return [
      {
        label: 'Modifica',
        callback: () => {
          this.handleEditEventoRischioso(eventoRischioso, valutazioneIndex, index);
          console.log('Apertura modale per modifica evento rischioso:', {
            id: eventoRischioso.id,
            index,
          });
        },
      },
      {
        label: 'Elimina',
        callback: () => this.handleOpenModalDelete(eventoRischioso, index, valutazioneIndex),
      },
    ];
  }

  handleRemoveForm(data: { element: any; index: number }) {
    if (this.currentValutazioneRischioIndex !== null) {
      const eventiRischiosi = this.getEventiRischiosi(this.currentValutazioneRischioIndex);
      const eventoRischioso = eventiRischiosi.at(data.index)?.value;
      if (eventoRischioso?.id) {
        this.eventoRischioService.delete(eventoRischioso.id).subscribe({
          next: () => {
            eventiRischiosi.removeAt(data.index);
            this.toastService.success('Evento rischioso eliminato con successo');
          },
          error: () => {
            this.toastService.error("Errore durante l'eliminazione dell'evento rischioso");
          },
        });
      } else {
        eventiRischiosi.removeAt(data.index);
        this.toastService.success('Evento rischioso eliminato con successo');
      }
    }
    this.handleCloseModalDelete();
  }

  handleOpenModalDelete(element: any, index: number, valutazioneIndex: number) {
    this.currentValutazioneRischioIndex = valutazioneIndex;
    this.openModalDelete = true;
    this.elementToDelete = { element, index };
  }

  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = undefined;
    this.currentValutazioneRischioIndex = null;
    this.currentEventoRischiosoIndex = null;
  }

  handleEditEventoRischioso(
    evento: EventoRischiosoDTO,
    valutazioneIndex: number,
    index: number
  ): void {
    this.currentValutazioneRischioIndex = valutazioneIndex;
    this.currentEventoRischiosoIndex = index;
    this.eventoToEdit = evento;
    this.openModalEventiRischiosi = true;
  }

  handleSortList(valutazioneIndex: number) {
    this.isSortedManuallyEventi[valutazioneIndex] = true;
    const valutazioneGroup = this.formGroup.get('valutazioneRischio') as FormArray;
    const eventiRischiosi = valutazioneGroup
      .at(valutazioneIndex)
      ?.get('eventiRischiosi') as FormArray;
    const controls = [...eventiRischiosi.controls];
    const sorted = controls.sort((a, b) => {
      const valueA = a.value as EventoRischiosoDTO;
      const valueB = b.value as EventoRischiosoDTO;
      const denominazioneA = valueA?.denominazione?.toLowerCase() || '';
      const denominazioneB = valueB?.denominazione?.toLowerCase() || '';

      if (this.sortAscending) {
        return denominazioneA.localeCompare(denominazioneB);
      } else {
        return denominazioneB.localeCompare(denominazioneA);
      }
    });

    eventiRischiosi.clear();
    sorted.forEach((control) => eventiRischiosi.push(control, { emitEvent: false }));

    this.sortAscending = !this.sortAscending;
  }

  get valutazioneRischio(): any {
    const formArray = this.formGroup.get('valutazioneRischio') as FormArray;

    return formArray;
  }

  getEventiRischiosi(valutazioneIndex: number): FormArray {
    const valutazioneGroup = this.valutazioneRischio.at(valutazioneIndex);
    const formArray = valutazioneGroup?.get('eventiRischiosi') as FormArray;

    // Se l'utente ha fatto un sort manuale per questa valutazione, non riordinare
    if (this.isSortedManuallyEventi[valutazioneIndex]) {
      return formArray;
    }

    // Altrimenti ordina i controlli per id (dal più basso al più alto)
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

  get eventiRischiosi(): FormArray {
    if (this.currentValutazioneRischioIndex !== null) {
      const valutazioneGroup = this.valutazioneRischio.at(this.currentValutazioneRischioIndex);
      return valutazioneGroup?.get('eventiRischiosi') as FormArray;
    }
    return this.fb.array<FormGroup>([]);
  }

  get idAttivitaSensibile(): number | null {
    if (this.currentValutazioneRischioIndex !== null) {
      const valutazioneGroup = this.valutazioneRischio.at(this.currentValutazioneRischioIndex);
      return valutazioneGroup?.get('idAttivitaSensibile')?.value || null;
    }
    return null;
  }

  set idAttivitaSensibile(value: number | null) {
    if (this.currentValutazioneRischioIndex !== null) {
      const valutazioneGroup = this.valutazioneRischio.at(this.currentValutazioneRischioIndex);
      valutazioneGroup?.get('idAttivitaSensibile')?.setValue(value);
    }
  }

  getFattoriArray(evento: AbstractControl): FormArray {
    return evento.get('fattore.properties') as FormArray;
  }

  getLivelloRischioLabel(idLivelloFK: number | undefined): string {
    if (!idLivelloFK) return '';
    const lr = this.livelloRischioOptions.find((lr) => lr.value === idLivelloFK);
    return lr?.label || '';
  }

  handleOpenModalDeleteValutazione(element: any) {
    this.openModalDeleteValutazione = true;
    this.elementToDelete = element;
  }
  handleCloseModalDeleteValutazione(): void {
    this.openModalDeleteValutazione = false;
    this.elementToDelete = null;
  }
}
