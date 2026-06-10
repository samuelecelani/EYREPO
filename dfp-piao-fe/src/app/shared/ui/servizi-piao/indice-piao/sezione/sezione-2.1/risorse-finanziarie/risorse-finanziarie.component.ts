import {
  Component,
  DestroyRef,
  EventEmitter,
  Input,
  OnDestroy,
  Output,
  inject,
} from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, debounceTime, takeUntil } from 'rxjs';
import { TextAreaComponent } from '../../../../../../components/text-area/text-area.component';
import { CardInfoComponent } from '../../../../../card-info/card-info.component';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';
import { INPUT_REGEX, ONLY_NUMBERS_REGEX, WARNING_ICON } from '../../../../../../utils/constants';
import { PIAODTO } from '../../../../../../models/classes/piao-dto';
import { DropdownComponent } from '../../../../../../components/dropdown/dropdown.component';
import { LabelValue } from '../../../../../../models/interfaces/label-value';
import { CardAlertComponent } from '../../../../../card-alert/card-alert.component';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';
import { RisorseFinanziarieService } from '../../../../../../services/risorse-finanziarie.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'piao-risorse-finanziarie',
  imports: [
    SharedModule,
    TextAreaComponent,
    CardInfoComponent,
    CardAlertComponent,
    TextBoxComponent,
    DropdownComponent,
    ModalDeleteComponent,
    TextAreaComponent,
  ],
  templateUrl: './risorse-finanziarie.component.html',
  styleUrl: './risorse-finanziarie.component.scss',
})
export class RisorseFinanziarieComponent implements OnDestroy {
  private destroyRef = inject(DestroyRef);
  @Input() form!: FormGroup;
  @Input() isDettaglio: boolean = false;
  @Input() idPiao: number = -1;
  @Input() testoSezione: string = '';
  @Output() risorseChanged = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private risorseFinanziarieService = inject(RisorseFinanziarieService);
  private destroy$ = new Subject<void>();

  // Icons
  iconAlert: string = WARNING_ICON;
  openModalDelete: boolean = false;
  elementToDelete: any = null;
  elementToDeleteIndex: number = -1;

  // Risorse finanziarie
  subTitleRisorseFinanziarie: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.RISORSE_FINANZIARIE.TITLE';
  descriptionRisorseFinanziarie: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.RISORSE_FINANZIARIE.DESCRIPTION';
  descriptionRisorseFinanziarieDettaglio: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.RISORSE_FINANZIARIE.DESCRIPTION_DETTAGLIO';
  labelTAIntroduzioneRisorseFinanziarie: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.RISORSE_FINANZIARIE.LABEL_INTRO';
  titleCardRisorsaFinanziaria: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.RISORSE_FINANZIARIE.CARD_TITLE';
  labelBtnAddRisorsaFinanziaria: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.RISORSE_FINANZIARIE.BTN_ADD';
  labelIniziativa: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.RISORSE_FINANZIARIE.LABEL_INIZIATIVA';
  labelDescrizione: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.RISORSE_FINANZIARIE.LABEL_DESCRIZIONE';
  labelDotazioneFinanziaria: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.RISORSE_FINANZIARIE.LABEL_DOTAZIONE';
  labelFonteFinanziamento: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.RISORSE_FINANZIARIE.LABEL_FONTE';
  labelOvpAssociato: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.RISORSE_FINANZIARIE.LABEL_OVP';
  labelBtnRimuovi: string = 'BUTTONS.REMOVE';

  titleAlertObiettiviVP: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.ALERT_TITLE';
  alertNoOVP: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.RISORSE_FINANZIARIE.ALERT_NO_OVP';

  get dropdownOvp(): LabelValue[] {
    return this.ovpArray.controls.map((ovp) => ({
      label: ovp.get('denominazione')?.value || ovp.get('codice')?.value || '',
      value: ovp.get('id')?.value || ovp.get('codice')?.value,
    }));
  }

  getOvpLabelById(idOvp: any): string {
    const found = this.dropdownOvp.find((x) => x.value === idOvp);
    return found?.label || '';
  }

  handleAddRisorsaFinanziaria(): void {
    if (this.isDettaglio) return;

    // Aggiungi la risorsa all'array flat
    // L'utente potrà selezionare l'OVP di riferimento tramite un dropdown
    this.risorseFinanziarieArray.push(
      this.fb.group({
        id: [null, [Validators.maxLength(20), Validators.pattern(ONLY_NUMBERS_REGEX)]],
        idOvp: [null, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
        iniziativa: [null, [Validators.maxLength(250), Validators.pattern(INPUT_REGEX)]],
        descrizione: [null, [Validators.maxLength(500), Validators.pattern(INPUT_REGEX)]],
        dotazioneFinanziaria: [
          null,
          [Validators.maxLength(20), Validators.pattern(ONLY_NUMBERS_REGEX)],
        ],
        fonteFinanziamento: [null, [Validators.maxLength(50), Validators.pattern(INPUT_REGEX)]],
      })
    );
    this.risorseChanged.emit();
  }

  handleRemoveRisorsaFinanziaria(risorsaIndex: number): void {
    if (this.isDettaglio) return;
    if (risorsaIndex < 0 || risorsaIndex >= this.risorseFinanziarieArray.length) return;

    const risorsaToDelete = this.risorseFinanziarieArray.at(risorsaIndex).value;
    const risorseFinanziarieId = risorsaToDelete?.id;

    // Se la risorsa ha un ID, la elimina via API
    if (risorseFinanziarieId) {
      this.risorseFinanziarieService
        .delete(risorseFinanziarieId, this.idPiao, this.testoSezione)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => {
            this.risorseFinanziarieArray.removeAt(risorsaIndex);
            this.risorseChanged.emit();
          },
          error: () => {
            console.error('Errore nella cancellazione della risorsa finanziaria');
          },
        });
    } else {
      // Se è nuova (no ID), rimuove solo localmente
      this.risorseFinanziarieArray.removeAt(risorsaIndex);
      this.risorseChanged.emit();
    }
  }

  get ovpArray(): FormArray {
    const formArray = this.form.get('ovp') as FormArray;

    // Ordina i controlli per id (dal più basso al più alto)
    // Gli elementi senza id vanno alla fine
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

  get risorseFinanziarieArray(): FormArray {
    const formArray = this.form.get('risorseFinanziarie') as FormArray;

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

  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = null;
    this.elementToDeleteIndex = -1;
  }

  handleOpenModalDelete(element: any, index: number) {
    if (this.isDettaglio) return;

    this.openModalDelete = true;
    this.elementToDelete = element;
    this.elementToDeleteIndex = index;
  }

  confirmDeleteRisorsa(): void {
    this.handleRemoveRisorsaFinanziaria(this.elementToDeleteIndex);
    this.openModalDelete = false;
    this.elementToDelete = null;
    this.elementToDeleteIndex = -1;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
