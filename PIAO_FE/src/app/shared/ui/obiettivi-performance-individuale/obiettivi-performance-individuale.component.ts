import { Component, inject, Input, OnDestroy, OnInit } from '@angular/core';
import { SharedModule } from '../../module/shared/shared.module';
import { IndicatoriComponent } from '../servizi-piao/indice-piao/sezione/indicatori/indicatori.component';
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { AccordionComponent } from '../../components/accordion/accordion.component';
import { DropdownComponent } from '../../components/dropdown/dropdown.component';
import { TextBoxComponent } from '../../components/text-box/text-box.component';
import { CardInfoComponent } from '../card-info/card-info.component';
import { TipologiaObbiettivo } from '../../models/enums/tipologia-obbiettivo.enum';
import { ObbiettivoPerformanceService } from '../../services/obbiettivo-performance.service';
import { ToastService } from '../../services/toast.service';
import { createFormArrayGenericIndicatoreFromPiaoSession } from '../../utils/utils';
import { INPUT_REGEX, KEY_PIAO } from '../../utils/constants';
import { ObiettivoIndicatoriDTO } from '../../models/classes/obiettivo-indicatori-dto';
import { ObbiettivoPerformanceDTO } from '../../models/classes/obiettivo-performance-dto';
import { PIAODTO } from '../../models/classes/piao-dto';
import { SessionStorageService } from '../../services/session-storage.service';
import { LabelValue } from '../../models/interfaces/label-value';
import { Subscription } from 'rxjs';
import { CodTipologiaDimensioneEnum } from '../../models/enums/cod-tipologia-dimensione.enum';
import { CodTipologiaIndicatoreEnum } from '../../models/enums/cod-tipologia-indicatore.enum';
import { ModalDeleteComponent } from '../../components/modal-delete/modal-delete.component';

@Component({
  selector: 'piao-obiettivi-performance-individuale',
  imports: [
    SharedModule,
    AccordionComponent,
    CardInfoComponent,
    TextBoxComponent,
    IndicatoriComponent,
    ReactiveFormsModule,
    DropdownComponent,
    ModalDeleteComponent,
  ],
  templateUrl: './obiettivi-performance-individuale.component.html',
  styleUrl: './obiettivi-performance-individuale.component.scss',
})
export class ObiettiviPerformanceIndividualeComponent implements OnInit, OnDestroy {
  @Input() formGroup!: FormGroup;
  @Input() idSezione22!: number;
  @Input() idPiao!: number;
  @Input() obbiettiviPerformanceOrganizzativa!: FormArray;
  @Input() tipologia: TipologiaObbiettivo = TipologiaObbiettivo.PERFORMANCE_INDIVIDUALE;

  private fb = inject(FormBuilder);
  private sessionStorageService = inject(SessionStorageService);
  private obiettivoPerformanceService = inject(ObbiettivoPerformanceService);
  toastService = inject(ToastService);

  codTipologiaFK: CodTipologiaDimensioneEnum = CodTipologiaDimensioneEnum.OBB_PER_IND;
  codTipologiaIndicatoreFK: CodTipologiaIndicatoreEnum =
    CodTipologiaIndicatoreEnum.PERFORMANCE_INDIVIDUALE;

  subTitleAddObiettivo: string = 'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.BTN_ADD';
  titleCardInfoObiettivo: string = 'SEZIONE_22.OBIETTIVO_PERFORMANCE_INDIVIDUALE.CARD_INFO_TITLE';
  labelIntroText: string = 'SEZIONE_22.OBIETTIVO_PERFORMANCE_INDIVIDUALE.INTRO_TEXT';
  labelObiettivoPrefix: string = 'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.OBIETTIVO_PREFIX';
  labelTipologiaRisorsa: string =
    'SEZIONE_22.OBIETTIVO_PERFORMANCE_INDIVIDUALE.TIPOLOGIA_RISORSA_LABEL';
  labelObiettivoPerformanceOrganizzativa: string =
    'SEZIONE_22.OBIETTIVO_PERFORMANCE_INDIVIDUALE.OBIETTIVO_PERFORMANCE_ORGANIZZATIVA_LABEL';
  labelIdObiettivoPerformanceIndividuale: string =
    'SEZIONE_22.OBIETTIVO_PERFORMANCE_INDIVIDUALE.ID_OBIETTIVO_PERFORMANCE_INDIVIDUALE_LABEL';
  labelObiettivoPerformanceIndividuale: string =
    'SEZIONE_22.OBIETTIVO_PERFORMANCE_INDIVIDUALE.OBIETTIVO_PERFORMANCE_INDIVIDUALE_LABEL';

  // Gestione stato accordion
  openAccordionIndex: number | null = null;

  obbPerorgOptions: LabelValue[] = [];

  private obbPerOrgMap: Map<number | string | undefined, ObbiettivoPerformanceDTO> = new Map();
  piaoDTO!: PIAODTO;
  private subscription = new Subscription();

  openModalDelete: boolean = false;
  elementToDelete: any = null;

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
    this.loadObbPerfOrg();

    // Ricarica le opzioni quando cambia l'array degli obiettivi organizzativi
    if (this.obbiettiviPerformanceOrganizzativa) {
      this.subscription.add(
        this.obbiettiviPerformanceOrganizzativa.valueChanges.subscribe(() => {
          this.loadObbPerfOrg();
        })
      );
    }
  }

  private loadObbPerfOrg(): void {
    if (this.obbiettiviPerformanceOrganizzativa) {
      const obbPerOrg = this.obbiettiviPerformanceOrganizzativa.controls
        .map((control) => control.value as ObbiettivoPerformanceDTO)
        .filter((obb) => obb.id != undefined && obb.id != null);

      obbPerOrg.forEach((obb) => {
        if (obb.id || obb.codice) {
          this.obbPerOrgMap.set(obb.id || obb.codice, obb);
        }
      });

      this.obbPerorgOptions = this.getObbPerOrg(obbPerOrg);
    }
  }

  private getObbPerOrg(obbPerOrgList: ObbiettivoPerformanceDTO[]): LabelValue[] {
    return obbPerOrgList.map((obb) => ({
      label: obb.denominazione || obb.codice || '',
      value: obb.id || null,
    }));
  }

  handleAddObiettivo(): void {
    if (this.obbiettiviPerformanceOrganizzativa.length === 0) {
      this.toastService.warning(
        'Per aggiungere un obiettivo di performance individuale è necessario prima aggiungere e salvare un obiettivo di performance organizzativa'
      );
      return;
    }
    const newObiettivo = this.fb.group({
      id: [null, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
      idSezione22: [this.idSezione22, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
      idObiettivoPeformance: [
        null,
        [Validators.required, Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
      ],
      codice: [
        null,
        [Validators.maxLength(50), Validators.pattern(INPUT_REGEX), Validators.required],
        ,
      ],
      tipologia: [
        'PERFORMANCE_INDIVIDUALE',
        [Validators.maxLength(50), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
      tipologiaRisorsa: [
        null,
        [Validators.maxLength(50), Validators.pattern(INPUT_REGEX), Validators.required],
        ,
      ],
      denominazione: [
        null,
        [Validators.maxLength(250), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
      indicatori: (() => {
        const formArray = createFormArrayGenericIndicatoreFromPiaoSession<ObiettivoIndicatoriDTO>(
          this.fb,
          [],
          ['id', 'indicatore'],
          INPUT_REGEX
        );
        formArray?.setValidators(Validators.required);
        return formArray;
      })(),
    });

    const newIndex = this.obbiettiviPerformance.length;
    this.obbiettiviPerformance.push(newObiettivo);

    // Imposta il codice per il nuovo obiettivo
    this.setCodice(newObiettivo, newIndex);

    // Apri l'accordion del nuovo obiettivo
    this.openAccordionIndex = this.obbiettiviPerformance.length;
  }

  handleRemoveObiettivo(index: number): void {
    const obiettivo = this.obbiettiviPerformance.at(index);
    const obiettivoId = obiettivo?.get('id')?.value;

    // Se l'obiettivo ha un ID, significa che è stato salvato sul backend
    if (obiettivoId) {
      // Chiama il backend per eliminarlo
      this.obiettivoPerformanceService.delete(obiettivoId).subscribe({
        next: () => {
          this.obbiettiviPerformance.removeAt(index);
          this.toastService.success('Obiettivo eliminato con successo');
          // Il reload viene gestito automaticamente dal service via reloadSezione22AndUpdateSession()
          // che triggera la subscription nel parent component per ricreare il form
        },
        error: (err) => {
          console.error("Errore nell'eliminazione dell'obiettivo:", err);
        },
      });
    } else {
      // Se non ha ID, è solo locale, rimuovilo dal FormArray
      this.obbiettiviPerformance.removeAt(index);
    }

    this.handleCloseModalDelete();
  }

  setCodice(obiettivo: FormGroup, index: number): FormControl {
    const idObbPerOrg = obiettivo.get('idObiettivoPeformance')?.value;

    let codice = '';

    // Recupera il codice OVP se selezionato
    if (idObbPerOrg) {
      const obbPerOrg = this.obbPerOrgMap.get(idObbPerOrg);
      if (obbPerOrg?.codice) {
        codice = obbPerOrg.codice;
      }
    }

    // Aggiungi l'indice
    codice = codice ? `${codice}_PI${index + 1}` : `PI${index + 1}`;

    obiettivo.get('codice')?.setValue(codice);
    return obiettivo.get('codice') as FormControl;
  }

  handleObiettivoChange(event: any, index: number): void {
    const obiettivo = this.obbiettiviPerformance.at(index) as FormGroup;
    this.setCodice(obiettivo, index);
  }

  get obbiettiviPerformance(): FormArray {
    const formArray = this.formGroup?.get(`obbiettivi_${this.tipologia}`) as FormArray;
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

  trackByObiettivoId(index: number, item: any): any {
    // Usa l'ID dell'obiettivo se disponibile, altrimenti usa l'indice
    return item.get('id')?.value || index;
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
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
