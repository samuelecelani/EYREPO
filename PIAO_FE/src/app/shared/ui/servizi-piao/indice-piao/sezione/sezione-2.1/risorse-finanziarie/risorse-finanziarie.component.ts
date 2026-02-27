import { Component, Input, OnDestroy } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, debounceTime, takeUntil } from 'rxjs';
import { TextAreaComponent } from '../../../../../../components/text-area/text-area.component';
import { CardInfoComponent } from '../../../../../card-info/card-info.component';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';
import { INPUT_REGEX, ONLY_NUMBERS_REGEX, WARNING_ICON } from '../../../../../../utils/constants';
import { inject } from '@angular/core';
import { PIAODTO } from '../../../../../../models/classes/piao-dto';
import { DropdownComponent } from '../../../../../../components/dropdown/dropdown.component';
import { LabelValue } from '../../../../../../models/interfaces/label-value';
import { CardAlertComponent } from '../../../../../card-alert/card-alert.component';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';

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
  @Input() form!: FormGroup;
  private fb = inject(FormBuilder);
  private destroy$ = new Subject<void>();

  // Icons
  iconAlert: string = WARNING_ICON;
  openModalDelete: boolean = false;
  elementToDelete: any = null;

  // Risorse finanziarie
  subTitleRisorseFinanziarie: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.RISORSE_FINANZIARIE.TITLE';
  descriptionRisorseFinanziarie: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.RISORSE_FINANZIARIE.DESCRIPTION';
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

  handleAddRisorsaFinanziaria(): void {
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
  }

  handleRemoveRisorsaFinanziaria(risorsaIndex: number): void {
    this.risorseFinanziarieArray.removeAt(risorsaIndex);
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
  }

  handleOpenModalDelete(element: any) {
    this.openModalDelete = true;
    this.elementToDelete = element;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
