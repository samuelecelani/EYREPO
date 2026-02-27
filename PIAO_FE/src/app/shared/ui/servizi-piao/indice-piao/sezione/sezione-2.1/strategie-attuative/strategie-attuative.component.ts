import { Component, inject, Input, OnInit } from '@angular/core';
import { TooltipComponent } from '../../../../../../components/tooltip/tooltip.component';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { AccordionComponent } from '../../../../../../components/accordion/accordion.component';
import { CardInfoComponent } from '../../../../../card-info/card-info.component';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';
import { TextAreaComponent } from '../../../../../../components/text-area/text-area.component';
import { AzioniComponent } from '../../../../../../components/azioni/azioni.component';
import { IndicatoriComponent } from '../../indicatori/indicatori.component';
import { IndicatoreDTO } from '../../../../../../models/classes/indicatore-dto';
import { INPUT_REGEX } from '../../../../../../utils/constants';
import { ToastService } from '../../../../../../services/toast.service';
import { canAddToFormArray } from '../../../../../../utils/utils';
import { OvpStrategiaAttuativaService } from '../../../../../../services/ovp-strategia-attuativa.service';
import { CodTipologiaIndicatoreEnum } from '../../../../../../models/enums/cod-tipologia-indicatore.enum';
import { CodTipologiaDimensioneEnum } from '../../../../../../models/enums/cod-tipologia-dimensione.enum';
import { SectionEnum } from '../../../../../../models/enums/section.enum';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';

@Component({
  selector: 'piao-strategie-attuative',
  imports: [
    SharedModule,
    TooltipComponent,
    AccordionComponent,
    CardInfoComponent,
    TextBoxComponent,
    TextAreaComponent,
    IndicatoriComponent,
    ModalDeleteComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './strategie-attuative.component.html',
  styleUrl: './strategie-attuative.component.scss',
})
export class StrategieAttuativeComponent {
  @Input() strategieAttuativeControls!: FormArray;
  @Input() indexOVP!: string;
  @Input() idOVP!: number;

  private fb = inject(FormBuilder);
  toastService = inject(ToastService);
  ovpStrategiaAttuativaService: OvpStrategiaAttuativaService = inject(OvpStrategiaAttuativaService);

  codTipologiaFK: string = CodTipologiaDimensioneEnum.OVP;
  codTipologiaIndicatoreFK: string = CodTipologiaIndicatoreEnum.OVP;
  sectionEnum: string = SectionEnum.SEZIONE_2_1;

  indicatori: IndicatoreDTO[] = [];

  openModalDelete: boolean = false;
  elementToDelete: any = null;

  openAccordionIndex: number | null = null;

  labelStrategieAttuative: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.STRATEGIE_ATTUATIVE.TITLE';

  subTitleStrategiaAttuativa: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.STRATEGIE_ATTUATIVE.STRATEGIA.SUB_TITLE';

  titleCardInfoStrategiaAttuativa: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.STRATEGIE_ATTUATIVE.STRATEGIA.CARD_TITLE';

  subTitleAddStrategiaAttuativa: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.STRATEGIE_ATTUATIVE.STRATEGIA.BTN_ADD';

  labelIdStrategia: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.STRATEGIE_ATTUATIVE.STRATEGIA.DETAILS.ID_STRATEGIA';

  labelStrategiaAttuativa: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.STRATEGIE_ATTUATIVE.STRATEGIA.DETAILS.STRATEGIA_ATTUATIVA';

  labelStrategiaDescrizione: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.STRATEGIE_ATTUATIVE.STRATEGIA.DETAILS.DESCRIPTION';

  labelStrategiaSoggettoResponsabile: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.STRATEGIE_ATTUATIVE.STRATEGIA.DETAILS.RESPONSIBLE_SUBJECT';

  setCodStrategia(control: any, index: number): any {
    control.setValue(this.indexOVP + '_' + 'ST' + index);
    return control;
  }

  handleRemoveStrategia(index: number): void {
    const strategia = this.strategieAttuative.at(index);
    const strategiaId = strategia?.get('id')?.value;
    if (strategiaId) {
      this.ovpStrategiaAttuativaService.delete(strategiaId).subscribe({
        next: () => {
          this.strategieAttuative.removeAt(index);
        },
      });
    } else {
      this.strategieAttuative.removeAt(index);
    }
    this.handleCloseModalDelete();
  }

  handleAddStrategiaAttuativa(): void {
    const newStrategia = this.fb.group({
      id: [null, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
      codStrategia: [null, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
      denominazioneStrategia: [
        null,
        [Validators.maxLength(250), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
      descrizioneStrategia: [null, [Validators.maxLength(500), Validators.pattern(INPUT_REGEX)]],
      soggettoResponsabile: [null, [Validators.maxLength(100), Validators.pattern(INPUT_REGEX)]],
      indicatori: this.fb.array([], Validators.required),
    });

    this.setCodStrategia(newStrategia.controls['codStrategia'], this.strategieAttuative.length + 1);

    // Aggiungi direttamente all'array
    this.strategieAttuative.push(newStrategia);

    this.openAccordionIndex = this.strategieAttuative.length;
  }

  handleOpenModalDelete(element: any) {
    this.openModalDelete = true;
    this.elementToDelete = element;
  }
  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = null;
  }

  get strategieAttuative(): FormArray {
    const formArray = this.strategieAttuativeControls as FormArray;

    // Se l'array non esiste, crea uno nuovo
    if (!formArray) {
      return this.fb.array([]);
    }

    // Se l'array è vuoto, restituisci l'array originale senza modifiche
    if (formArray.length === 0) {
      return formArray;
    }

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
}
