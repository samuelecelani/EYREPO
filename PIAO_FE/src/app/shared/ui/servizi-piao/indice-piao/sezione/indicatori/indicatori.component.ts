import { Component, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../../../../module/shared/shared.module';
import { AzioniComponent } from '../../../../../components/azioni/azioni.component';
import { SvgComponent } from '../../../../../components/svg/svg.component';
import { IndicatoreDTO } from '../../../../../models/classes/indicatore-dto';
import { IVerticalEllipsisActions } from '../../../../../models/interfaces/vertical-ellipsis-actions';
import { ModalComponent } from '../../../../../components/modal/modal.component';
import { BaseComponent } from '../../../../../components/base/base.component';
import { ModalBodyIndicatoreComponent } from './modal-body-indicatore/modal-body-indicatore.component';
import { FormArray, FormBuilder, FormGroup } from '@angular/forms';
import { inject } from '@angular/core';
import { IIndicatoreWrapper } from '../../../../../models/interfaces/indicatore-wrapper';
import { IndicatoreService } from '../../../../../services/indicatore.service';
import { CodTipologiaIndicatoreEnum } from '../../../../../models/enums/cod-tipologia-indicatore.enum';
import { OvpStrategiaIndicatoreService } from '../../../../../services/ovp-strategia-indicatore.service';
import { forkJoin, of, switchMap, map, tap } from 'rxjs';
import { SessionStorageService } from '../../../../../services/session-storage.service';
import { PIAODTO } from '../../../../../models/classes/piao-dto';
import { KEY_PIAO } from '../../../../../utils/constants';
import { UlterioriInfoDTO } from '../../../../../models/classes/ulteriori-info-dto';
import {
  cleanSingleMongoDTO,
  estraiAnniDaDenominazione,
  mapToLabelValue,
} from '../../../../../utils/utils';
import { TargetIndicatoreService } from '../../../../../services/target-indicatore.service';
import { DimensioneIndicatoreService } from '../../../../../services/dimensione-indicatore.service';
import { LabelValue } from '../../../../../models/interfaces/label-value';
import { ModalDeleteComponent } from '../../../../../components/modal-delete/modal-delete.component';
import { OvpStrategiaAttuativaService } from '../../../../../services/ovp-strategia-attuativa.service';

import { OVPStrategiaIndicatoreDTO } from '../../../../../models/classes/ovp-strategia-indicatore-dto';
import { CodTipologiaDimensioneEnum } from '../../../../../models/enums/cod-tipologia-dimensione.enum';
import { SectionEnum } from '../../../../../models/enums/section.enum';
import { ObiettivoIndicatoriDTO } from '../../../../../models/classes/obiettivo-indicatori-dto';
import { Sezione22Service } from '../../../../../services/sezioni-22.service';

@Component({
  selector: 'piao-indicatori',
  imports: [
    SharedModule,
    AzioniComponent,
    SvgComponent,
    ModalComponent,
    ModalBodyIndicatoreComponent,
    ModalDeleteComponent,
  ],
  templateUrl: './indicatori.component.html',
  styleUrl: './indicatori.component.scss',
})
export class IndicatoriComponent extends BaseComponent implements OnInit {
  @Input() indicatoriControls?: FormArray;
  @Input() codTipologiaFK!: string;
  @Input() codTipologiaIndicatoreFK!: string;
  @Input() sectionEnum!: SectionEnum;
  @Input() idEntitaFK!: number;
  @Input() formParent?: FormGroup; // Form della strategia attuativa (opzionale)

  indicatoreService: IndicatoreService = inject(IndicatoreService);
  sessionStorageService: SessionStorageService = inject(SessionStorageService);
  ovpStrategiaIndicatoreService: OvpStrategiaIndicatoreService = inject(
    OvpStrategiaIndicatoreService
  );
  ovpStrategiaAttuativaService: OvpStrategiaAttuativaService = inject(OvpStrategiaAttuativaService);
  sezione22Service: Sezione22Service = inject(Sezione22Service);

  dimensioneIndicatoreService = inject(DimensioneIndicatoreService);
  targetIndicatoreService = inject(TargetIndicatoreService);

  private fb = inject(FormBuilder);
  piaoDTO!: PIAODTO;

  dimensioniDropdown: LabelValue[] = [];
  targetDropdown: LabelValue[] = [];

  labelElencoIndicatori: string = 'INDICATORI.LIST_INDICATORI';

  labelIndicatori: string = 'INDICATORI.TITLE';

  labelAddIndicatori: string = 'INDICATORI.ADD_INDICATORE';

  sortAscending: boolean = true;
  isSortedManually: boolean = false;

  openModalIndicatore: boolean = false;
  isAddNewIndicatore: boolean = true;
  indicatoreToEdit?: IndicatoreDTO;

  icon: string = 'Pencil';
  iconStyle: string = 'icon-modal';

  years: number[] = [];

  openModalDelete: boolean = false;

  elementToDelete!: IndicatoreDTO;

  subDimensioniDropdown: LabelValue[] = [];

  verticalEllipsis: IVerticalEllipsisActions[] = [
    {
      label: 'INDICATORI.TH_INDICATORI.ACTIONS.EDIT',
      callback: (element) => this.handleEdit(element),
    },
    {
      label: 'INDICATORI.TH_INDICATORI.ACTIONS.RELEVANT',
      callback: (element) => this.handleRelevant(element),
    },
    {
      label: 'INDICATORI.TH_INDICATORI.ACTIONS.REMOVE',
      callback: (element) => this.handleOpenModalDelete(element),
    },
  ];

  ngOnInit(): void {
    console.log('formParent:', this.formParent, this.formParent?.value);
    console.log('indicatori:', this.indicatoriControls, this.indicatoriControls?.value);

    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
    this.years = estraiAnniDaDenominazione(this.piaoDTO?.denominazione || '');
    // Carica dimensioni e target in parallelo
    forkJoin({
      dimensioni: this.dimensioneIndicatoreService.getDimensioniIndicatore(this.codTipologiaFK),
      targets: this.targetIndicatoreService.getTargetIndicatore(),
    }).subscribe({
      next: ({ dimensioni, targets }) => {
        // Gestione dimensioni in base al tipo di indicatore
        if (this.codTipologiaFK === CodTipologiaDimensioneEnum.OBB) {
          this.dimensioniDropdown = mapToLabelValue(
            dimensioni.filter((dim) => dim.codTipologiaFK === CodTipologiaDimensioneEnum.OBB)
          );
          this.subDimensioniDropdown = mapToLabelValue(
            dimensioni.filter((dim) => dim.codTipologiaFK === CodTipologiaDimensioneEnum.OBB_SUB)
          );
        } else {
          this.dimensioniDropdown = mapToLabelValue(dimensioni);
        }

        this.targetDropdown = mapToLabelValue(targets);
      },
      error: (err) => console.error('Errore nel caricamento dati:', err),
    });
  }

  handleOpenModalIndicatore() {
    this.isAddNewIndicatore = true;
    this.indicatoreToEdit = undefined;
    this.openModalIndicatore = true;
  }

  handleOpenModalDelete(element: any) {
    this.openModalDelete = true;
    this.elementToDelete = element;
  }

  handleCloseModalOnAddInfo() {
    this.openModalIndicatore = !this.openModalIndicatore;
  }

  handleAddIndicatore() {
    if (!this.child?.formGroup) {
      console.error('Form non disponibile');
      return;
    }

    const form = this.child.formGroup;
    const formValue = form.value;

    console.log('Valori del form:', formValue);

    // Crea gli oggetti tipAndVal in base all'anno
    const indicatoreData: any = {
      ...formValue,
      tipAndValAnno1: {
        id: formValue.idTip1,
        idTargetFK: formValue.andamento1,
        valore: formValue.valore1,
      },
      ...(this.codTipologiaFK !== 'OBB_PER_IND' && {
        tipAndValAnno2: {
          id: formValue.idTip2,
          idTargetFK: formValue.andamento2,
          valore: formValue.valore2,
        },
        tipAndValAnnoCorrente: {
          id: formValue.idTip3,
          idTargetFK: formValue.andamento3,
          valore: formValue.valore3,
        },
      }),
      idDimensioneFK: formValue.dimensione,
      idSubDimensioneFK: formValue.subDimensione,
      addInfo: cleanSingleMongoDTO<UlterioriInfoDTO>(form.controls['addInfo'].value),
      idPiao: this.piaoDTO.id,
      idEntitaFK: this.idEntitaFK === 0 ? undefined : this.idEntitaFK,
      codTipologiaFK: this.codTipologiaIndicatoreFK,
    };

    // Rimuovi i campi temporanei usati nel form
    delete indicatoreData.andamento1;
    delete indicatoreData.valore1;
    delete indicatoreData.andamento2;
    delete indicatoreData.valore2;
    delete indicatoreData.andamento3;
    delete indicatoreData.valore3;
    delete indicatoreData.choice;

    console.log('Indicatore data da inviare:', indicatoreData);

    if (formValue.choice === 'recupera') {
      if (this.isAddNewIndicatore) {
        // Aggiunge l'indicatore direttamente al FormArray locale (come per le strategie)
        if (this.formParent && indicatoreData) {
          const indicatoriFormArray = this.formParent.get('indicatori') as FormArray;
          if (indicatoriFormArray) {
            // Crea il FormGroup per il nuovo indicatore (usa data direttamente)
            const newIndicatoreGroup = this.fb.group({
              id: [undefined],
              indicatore: [indicatoreData],
            });

            // Aggiunge al FormArray
            indicatoriFormArray.push(newIndicatoreGroup);
          }
        }
      }
      // Chiudi il modal e resetta il form solo dopo che tutto è completato
      this.openModalIndicatore = false;
      this.child.formGroup.reset();
    } else {
      this.indicatoreService
        .saveIndicatore(indicatoreData, this.isAddNewIndicatore, this.sectionEnum)
        .subscribe({
          next: (data: any) => {
            if (this.isAddNewIndicatore) {
              // Aggiorna l'id dell'indicatore nel form
              if (data) this.child.formGroup.controls['id'].setValue(data.id);

              // Aggiunge l'indicatore direttamente al FormArray locale (come per le strategie)
              if (this.formParent && data) {
                const indicatoriFormArray = this.formParent.get('indicatori') as FormArray;
                if (indicatoriFormArray) {
                  // Crea il FormGroup per il nuovo indicatore (usa data direttamente)
                  const newIndicatoreGroup = this.fb.group({
                    id: [undefined],
                    indicatore: [data],
                  });

                  // Aggiunge al FormArray
                  indicatoriFormArray.push(newIndicatoreGroup);
                }
              }
            } else {
              // Se stiamo modificando, aggiorna direttamente il valore nel FormArray senza ricaricare tutto
              const formArray = this.indicatoriControls as FormArray;
              const index = formArray.controls.findIndex((control) => {
                const value = control.value as IIndicatoreWrapper;
                return value.indicatore?.id === data.id;
              });

              if (index !== -1) {
                const control = formArray.at(index);
                const updatedWrapper: IIndicatoreWrapper = {
                  ...control.value,
                  indicatore: data,
                };
                control.patchValue(updatedWrapper);
              }
            }
            // Chiudi il modal e resetta il form solo dopo che tutto è completato
            this.openModalIndicatore = false;
            this.child.formGroup.reset();
          },
          error: (err) => {
            console.error("Errore nel salvataggio dell'indicatore:", err);
          },
        });
    }
  }

  handleEdit(element: any) {
    this.isAddNewIndicatore = false;
    this.indicatoreToEdit = element.indicatore;
    this.openModalIndicatore = true;
    // Non serve ricaricare, stiamo solo modificando un indicatore esistente
  }

  handleRelevant(element: IIndicatoreWrapper) {
    console.log('Toggling rilevante for indicatore:', element);
    element.indicatore.rilevante = !element.indicatore.rilevante;

    const indicatoreData: any = {
      ...element.indicatore,
      idPiao: this.piaoDTO.id,
      idEntitaFK: this.idEntitaFK,
      codTipologiaFK: this.codTipologiaFK,
    };

    switch (this.codTipologiaFK) {
      case CodTipologiaIndicatoreEnum.OVP:
        this.indicatoreService.saveIndicatore(indicatoreData, false, this.sectionEnum).subscribe({
          next: () => {
            console.log('Indicatore OVP aggiornato con successo');
          },
          error: (err) => {
            console.error("Errore nell'aggiornamento dell'indicatore OVP:", err);
          },
        });

        break;

      default:
        break;
    }
  }

  handleRemove(element: IIndicatoreWrapper) {
    const index = this.indicatori.controls.findIndex((control) => {
      const value = control.value as IIndicatoreWrapper;
      return value.indicatore?.id === element.indicatore.id;
    });
    console.log('Index', index, this.indicatori.controls);
    this.indicatori.removeAt(index);
  }

  handleSortList() {
    this.isSortedManually = true;
    const formArray = this.indicatoriControls as FormArray;
    const controls = [...formArray.controls];
    const sorted = controls.sort((a, b) => {
      const valueA = a.value as IIndicatoreWrapper;
      const valueB = b.value as IIndicatoreWrapper;
      const denominazioneA = valueA.indicatore?.denominazione?.toLowerCase() || '';
      const denominazioneB = valueB.indicatore?.denominazione?.toLowerCase() || '';

      if (this.sortAscending) {
        return denominazioneA.localeCompare(denominazioneB);
      } else {
        return denominazioneB.localeCompare(denominazioneA);
      }
    });

    formArray.clear();
    sorted.forEach((control) => formArray.push(control, { emitEvent: false }));

    this.sortAscending = !this.sortAscending;
  }

  getActionsFor(row: IIndicatoreWrapper): IVerticalEllipsisActions[] {
    const element = row;
    const actions = this.verticalEllipsis.map((a) => ({
      ...a,
      callback: a.callback ? () => a.callback!(element) : undefined,
    }));

    return actions;
  }

  getDimensioneLabel(idDimensioneFK: number | undefined): string {
    if (!idDimensioneFK) return '';
    const dimensione = this.dimensioniDropdown.find((dim) => dim.value === idDimensioneFK);
    return dimensione?.label || '';
  }

  getSubDimensioneLabel(idSubDimensioneFK: number | undefined): string {
    if (!idSubDimensioneFK) return '';
    const subDimensione = this.subDimensioniDropdown.find(
      (subDim) => subDim.value === idSubDimensioneFK
    );
    return subDimensione?.label || '';
  }

  getTargetLabel(idTargetFK: number | undefined): string {
    if (!idTargetFK) return '';
    const target = this.targetDropdown.find((t) => t.value === idTargetFK);
    return target?.label || '';
  }

  get indicatori(): FormArray {
    const formArray = this.indicatoriControls as FormArray;

    // Se l'utente ha fatto un sort manuale, non riordinare
    if (this.isSortedManually) {
      return formArray;
    }

    // Altrimenti ordina i controlli per id (dal più basso al più alto)
    // Gli elementi senza id vanno alla fine

    if (!formArray || formArray.length === 0) {
      return this.fb.array([]);
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
