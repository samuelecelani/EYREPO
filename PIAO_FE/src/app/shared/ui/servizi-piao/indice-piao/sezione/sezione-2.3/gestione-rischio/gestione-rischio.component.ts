import { Component, inject, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AccordionComponent } from '../../../../../../components/accordion/accordion.component';
import { CardInfoComponent } from '../../../../../card-info/card-info.component';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';
import { DropdownComponent } from '../../../../../../components/dropdown/dropdown.component';
import { LabelValue } from '../../../../../../models/interfaces/label-value';
import { createFormArrayGenericIndicatoreFromPiaoSession } from '../../../../../../utils/utils';
import { INPUT_REGEX, KEY_PIAO } from '../../../../../../utils/constants';
import { ObiettivoIndicatoriDTO } from '../../../../../../models/classes/obiettivo-indicatori-dto';
import { IIndicatoreWrapper } from '../../../../../../models/interfaces/indicatore-wrapper';
import { PIAODTO } from '../../../../../../models/classes/piao-dto';
import { SessionStorageService } from '../../../../../../services/session-storage.service';
import { IndicatoriComponent } from '../../indicatori/indicatori.component';
import { SectionEnum } from '../../../../../../models/enums/section.enum';
import { CodTipologiaDimensioneEnum } from '../../../../../../models/enums/cod-tipologia-dimensione.enum';
import { CodTipologiaIndicatoreEnum } from '../../../../../../models/enums/cod-tipologia-indicatore.enum';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';

@Component({
  selector: 'piao-gestione-rischio',
  imports: [
    SharedModule,
    AccordionComponent,
    CardInfoComponent,
    DropdownComponent,
    ModalDeleteComponent,
    IndicatoriComponent,
    TextBoxComponent,
  ],
  templateUrl: './gestione-rischio.component.html',
  styleUrl: './gestione-rischio.component.scss',
})
export class GestioneRischioComponent implements OnInit {
  @Input() formGroup!: FormGroup;

  private fb: FormBuilder = inject(FormBuilder);

  private sessionStorageService = inject(SessionStorageService);

  labelGestioneRischio: string = 'SEZIONE_23.GESTIONE_RISCHIO.EVENTO_RISCHIOSO.TITLE';
  subTitleGestioneRischio: string = 'SEZIONE_23.GESTIONE_RISCHIO.EVENTO_RISCHIOSO.SUB_TITLE';
  labelSelectEventoRischioso: string = 'SEZIONE_23.GESTIONE_RISCHIO.EVENTO_RISCHIOSO.SELECT_LABEL';

  titleCardInfo: string = 'SEZIONE_23.GESTIONE_RISCHIO.CARD_INFO_TITLE';
  titleCardInfoNotFound: string = 'SEZIONE_23.GESTIONE_RISCHIO.CARD_INFO_NOT_FOUND';
  labelAddEventoRischioso: string = 'SEZIONE_23.GESTIONE_RISCHIO.ADD_EVENTO_RISCHIOSO';

  //Misure Prevenzione
  labelMisuraPrevenzione: string =
    'SEZIONE_23.GESTIONE_RISCHIO.EVENTO_RISCHIOSO.MISURE_PREVENZIONE_LABEL';
  labelNotFoundMisurePrevenzione: string =
    'SEZIONE_23.GESTIONE_RISCHIO.EVENTO_RISCHIOSO.NOT_FOUND_MISURE_PREVENZIONE';
  labelAddMisurePrevenzione: string =
    'SEZIONE_23.GESTIONE_RISCHIO.EVENTO_RISCHIOSO.ADD_MISURE_PREVENZIONE';

  labelAddMisuraPrevenzione_2: string =
    'SEZIONE_23.GESTIONE_RISCHIO.EVENTO_RISCHIOSO.ADD_MISURE_PREVENZIONE_2';

  labelObiettivoPrev: string = 'SEZIONE_23.GESTIONE_RISCHIO.EVENTO_RISCHIOSO.OBB_PREV_LABEL';

  labelCodiceMisura: string = 'SEZIONE_23.GESTIONE_RISCHIO.EVENTO_RISCHIOSO.CODICE_MISURA_LABEL';
  labelDenominazioneMisura: string =
    'SEZIONE_23.GESTIONE_RISCHIO.EVENTO_RISCHIOSO.DENOMINAZIONE_MISURA_LABEL';
  labelDescrizioneMisura: string =
    'SEZIONE_23.GESTIONE_RISCHIO.EVENTO_RISCHIOSO.DESCRIZIONE_MISURA_LABEL';

  labelResponsabileMisura: string =
    'SEZIONE_23.GESTIONE_RISCHIO.EVENTO_RISCHIOSO.RESPONSABILE_MISURA_LABEL';

  labelStakeholder: string = 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.STAKEHOLDER';
  labelSelezionaStakeholder: string =
    'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.SELEZIONA_STAKEHOLDER';

  openAccordionIndex: any;

  openModalDelete: boolean = false;
  elementToDelete: any = null;

  gestioneRischioOptions: LabelValue[] = [
    { label: 'Evento rischioso 1', value: 1 },
    { label: 'Evento rischioso 2', value: 2 },
    { label: 'Evento rischioso 3', value: 3 },
  ];

  obbPrev: LabelValue[] = [
    { label: 'Obiettivo prevenzione 1', value: 1 },
    { label: 'Obiettivo prevenzione 2', value: 2 },
    { label: 'Obiettivo prevenzione 3', value: 3 },
  ];

  stakeholderOptions: LabelValue[] = [];

  sectionEnum: SectionEnum = SectionEnum.SEZIONE_2_3;
  codTipologiaFK: CodTipologiaDimensioneEnum = CodTipologiaDimensioneEnum.OBB_2_3;
  codTipologiaIndicatoreFK: CodTipologiaIndicatoreEnum =
    CodTipologiaIndicatoreEnum.MISURA_PREVENZIONE;

  piaoDTO!: PIAODTO;

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO) as PIAODTO;
    this.loadStakeholderOptions();
  }

  private loadStakeholderOptions(): void {
    if (this.piaoDTO?.stakeHolders) {
      this.stakeholderOptions = this.getStakeholderDropdownOptions(this.piaoDTO.stakeHolders);
    }
  }

  private getStakeholderDropdownOptions(stakeholderList: any[]): LabelValue[] {
    return stakeholderList.map((stakeholder) => ({
      label: stakeholder.nomeStakeHolder || '',
      value: stakeholder.id || 0,
    }));
  }

  handleAddEventoRischioso(): void {
    const gestioneRischioFormGroup = this.fb.group({
      id: [null],
      idEventoRischioso: [null, [Validators.required]],
      misuraPrevenzione: this.fb.array([], [Validators.required]),
    });

    this.gestioneRischio.push(gestioneRischioFormGroup);
    this.openAccordionIndex = this.gestioneRischio.length;
  }

  handleAddMisurePrevenzione(index: number): void {
    const misuraPrevenzioneFormGroup = this.fb.group({
      id: [null],
      idObiettivoPrevenzione: [null, [Validators.required]],
      codice: [null, [Validators.required]],
      denominazione: [null, [Validators.required, Validators.maxLength(250)]],
      descrizione: [null, [Validators.maxLength(100)]],
      responsabile: [null, [Validators.maxLength(100)]],
      stakeHolders: [[], []],
      indicatori: (() => {
        const formArray = createFormArrayGenericIndicatoreFromPiaoSession<IIndicatoreWrapper>(
          this.fb,
          [],
          ['id', 'indicatore'],
          INPUT_REGEX
        );
        formArray?.setValidators(Validators.required);
        return formArray;
      })(),
    });

    console.log('Misura prevenzione form group', misuraPrevenzioneFormGroup);
    console.log('Misura prevenzione', this.getMisuraPrevenzione(index));

    this.getMisuraPrevenzione(index).push(misuraPrevenzioneFormGroup);
  }

  handleRemoveGestione(index: number): void {
    this.gestioneRischio.removeAt(index);
  }

  handleRemoveMisuraPrevenzione(indexGestione: number, indexMisura: number): void {
    this.getMisuraPrevenzione(indexGestione).removeAt(indexMisura);
  }

  handleRemoveForm(): void {
    console.log('handle remove form');
  }

  handleObbPrev(indexR: number, indexM: number): void {
    this.setCodice(this.getMisuraPrevenzione(indexR).at(indexM) as FormGroup, indexM);
  }

  setCodice(mPrev: FormGroup, index: number): void {
    const idObiettivoPerformance = mPrev.get('idObiettivoPrevenzione')?.value;

    let codice = '';

    // Aggiungi l'indice in base alla tipologia
    const suffix = 'MS';
    codice = codice ? `${codice}_${suffix}${index + 1}` : `${suffix}${index + 1}`;

    mPrev.get('codice')?.setValue(codice);
  }

  handleOpenModalDelete(index: number) {
    this.openModalDelete = true;
    this.elementToDelete = index;
  }

  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = undefined;
  }

  trackByGestioneId(index: number, item: any): number {
    return item.get('id')?.value ?? index;
  }

  trackByMisuraPrevenzioneId(index: number, item: any): number {
    return item.get('id')?.value ?? index;
  }

  get gestioneRischio(): FormArray {
    return this.formGroup.get('gestioneRischio') as FormArray;
  }

  getMisuraPrevenzione(index: number): FormArray {
    const formArray = this.gestioneRischio.at(index).get('misuraPrevenzione') as FormArray;

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
