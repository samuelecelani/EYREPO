import { Component, inject, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { AccordionComponent } from '../../../../../../components/accordion/accordion.component';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';
import { CardInfoComponent } from '../../../../../card-info/card-info.component';
import { IndicatoriComponent } from '../../indicatori/indicatori.component';
import { PIAODTO } from '../../../../../../models/classes/piao-dto';
import { ToastService } from '../../../../../../services/toast.service';
import { INPUT_REGEX, KEY_PIAO } from '../../../../../../utils/constants';
import {
  canAddToFormArray,
  createFormArrayGenericIndicatoreFromPiaoSession,
  estraiAnniDaDenominazione,
  minArrayLength,
} from '../../../../../../utils/utils';
import { CodTipologiaDimensioneEnum } from '../../../../../../models/enums/cod-tipologia-dimensione.enum';
import { CodTipologiaIndicatoreEnum } from '../../../../../../models/enums/cod-tipologia-indicatore.enum';
import { SectionEnum } from '../../../../../../models/enums/section.enum';
import { SessionStorageService } from '../../../../../../services/session-storage.service';
import { ObiettivoPrevenzioneIndicatoreDTO } from '../../../../../../models/classes/obiettivo-prevenzione-indicatore-dto';
import { ObiettivoPrevenzioneService } from '../../../../../../services/obiettivo-prevenzione.service';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';
import { ObiettivoPrevenzioneDTO } from '../../../../../../models/classes/obiettivo-prevenzione-dto';

@Component({
  selector: 'piao-obiettivi-generali',
  standalone: true,
  imports: [
    SharedModule,
    AccordionComponent,
    TextBoxComponent,
    CardInfoComponent,
    IndicatoriComponent,
    ReactiveFormsModule,
    ModalDeleteComponent,
  ],
  templateUrl: './obiettivi-generali.component.html',
  styleUrl: './obiettivi-generali.component.scss',
})
export class ObiettiviGeneraliComponent implements OnInit, OnChanges {
  @Input() obiettiviControls!: FormArray;
  @Input() idSezione23!: number;
  @Input() idPiao!: number;

  // Labels for translations
  labelObiettivoPrefix: string = 'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.ACCORDION_TITLE';
  labelIntroText: string = 'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.DESCRIPTION';
  labelIdObiettivo: string = 'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.ID_OBIETTIVO_LABEL';
  labelDenominazione: string = 'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.DENOMINAZIONE_LABEL';
  labelDescrizione: string = 'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.DESCRIZIONE_LABEL';
  titleCardInfoObiettivo: string = 'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.CARD_INFO_TITLE';
  titleCardInfoNotFoundObiettivo: string =
    'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.CARD_INFO_NOT_FOUND';
  subTitleAddObiettivo: string = 'SEZIONE_23.OBIETTIVI_PREVENZIONE_CORRUZIONE.CARD_INFO_ADD';

  private fb = inject(FormBuilder);
  private sessionStorageService = inject(SessionStorageService);
  toastService = inject(ToastService);
  obiettivoPrevenzioneService = inject(ObiettivoPrevenzioneService);

  // Configurazione indicatori
  codTipologiaFK: string = CodTipologiaDimensioneEnum.OBB_2_3;
  codTipologiaIndicatoreFK: string = CodTipologiaIndicatoreEnum.OBIETTIVO_GENERALE;
  sectionEnum: string = SectionEnum.SEZIONE_2_3;

  openModalDelete: boolean = false;
  elementToDelete: any = null;
  openAccordionIndex!: any;

  ngOnInit(): void {
    console.log('[ObiettiviGenerali] ngOnInit, controls length:', this.obiettivi.length);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['obiettiviControls']) {
      console.log(
        '[ObiettiviGenerali] obiettiviControls changed, new length:',
        this.obiettivi.length
      );
    }
  }

  /**
   * Imposta il codice dell'obiettivo in base all'indice (ANTG1, ANTG2, ecc.)
   */
  setCodice(control: any, index: number): any {
    control.setValue('ANTG' + (index + 1));
    return control;
  }

  /**
   * Rimuove un obiettivo dal FormArray
   */
  handleRemoveObiettivo(index: number): void {
    const obiettivo = this.obiettivi.at(index);
    const obiettivoId = obiettivo?.get('id')?.value;

    if (obiettivoId) {
      this.obiettivoPrevenzioneService.delete(obiettivoId).subscribe({
        next: () => {
          this.toastService.success('Obiettivo eliminato con successo');
          this.obiettivi.removeAt(index);
        },
        error: (err) => {
          console.error("Errore nell'eliminazione dell'obiettivo:", err);
        },
      });
    } else {
      // Se non ha ID, è solo locale, rimuovilo dal FormArray
      this.obiettivi.removeAt(index);
      this.toastService.success('Obiettivo eliminato con successo');
    }
    this.handleCloseModalDelete();
  }

  /**
   * Aggiunge un nuovo obiettivo al FormArray
   */
  handleAddObiettivo(): void {
    const newObiettivo = this.fb.group({
      id: [null],
      idSezione23: [this.idSezione23],
      codice: [
        null,
        [Validators.required, Validators.maxLength(50), Validators.pattern(INPUT_REGEX)],
      ],
      denominazione: [
        null,
        [Validators.required, Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
      ],
      descrizione: [null, [Validators.maxLength(2000), Validators.pattern(INPUT_REGEX)]],
      indicatori: (() => {
        const formArray =
          createFormArrayGenericIndicatoreFromPiaoSession<ObiettivoPrevenzioneIndicatoreDTO>(
            this.fb,
            [],
            ['id', 'indicatore'],
            INPUT_REGEX
          );
        formArray?.setValidators([Validators.required, minArrayLength(1)]);
        return formArray;
      })(),
    });

    this.setCodice(newObiettivo.controls['codice'], this.obiettivi.length);
    this.obiettivoPrevenzioneService.save(newObiettivo.value).subscribe({
      next: (data: any) => {
        this.toastService.success('Obiettivo aggiunto con successo');
        newObiettivo.get('id')?.setValue(data.id || null); // Simula un ID univoco per il nuovo obiettivo
        this.obiettivi.push(newObiettivo);
        console.log('Obiettivo aggiunto:', newObiettivo.value);
        this.openAccordionIndex = this.obiettivi.length;
      },
      error: (err) => {
        console.error("Errore nell'aggiunta dell'obiettivo:", err);
      },
    });
  }

  /**
   * Getter per il FormArray degli obiettivi
   */
  get obiettivi(): FormArray {
    const formArray = this.obiettiviControls as FormArray;
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

  /**
   * Track function per ngFor
   */
  trackByObiettivoId(index: number, item: any): any {
    return item.get('id')?.value || index;
  }

  handleOpenModalDelete(element: any) {
    this.openModalDelete = true;
    this.elementToDelete = element;
  }
  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = null;
  }
}
