import { Component, inject, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CardInfoComponent } from '../../../../../card-info/card-info.component';
import { IndicatoriComponent } from '../../indicatori/indicatori.component';
import { AccordionComponent } from '../../../../../../components/accordion/accordion.component';
import { DropdownComponent } from '../../../../../../components/dropdown/dropdown.component';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';
import { IIndicatoreWrapper } from '../../../../../../models/interfaces/indicatore-wrapper';
import { INPUT_REGEX, KEY_PIAO } from '../../../../../../utils/constants';
import { createFormArrayGenericIndicatoreFromPiaoSession } from '../../../../../../utils/utils';
import { SessionStorageService } from '../../../../../../services/session-storage.service';
import { PIAODTO } from '../../../../../../models/classes/piao-dto';
import { LabelValue } from '../../../../../../models/interfaces/label-value';
import { OVPDTO } from '../../../../../../models/classes/ovp-dto';
import { OVPStrategiaDTO } from '../../../../../../models/classes/ovp-strategia-dto';
import { ObbiettivoPerformanceDTO } from '../../../../../../models/classes/obiettivo-performance-dto';
import { TipologiaObbiettivo } from '../../../../../../models/enums/tipologia-obbiettivo.enum';
import { ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoriDTO } from '../../../../../../models/classes/obiettivo-prevenzione-corruzione-trasparenza-indicatori-dto';
import { ObiettivoPrevenzioneCorruzioneTrasparenzaService } from '../../../../../../services/obiettivo-prevenzione-corruzione-trasparenza.service';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';
import { ToastService } from '../../../../../../services/toast.service';

@Component({
  selector: 'piao-valore-pubblico-anticorruzione',
  imports: [
    SharedModule,
    CardInfoComponent,
    IndicatoriComponent,
    AccordionComponent,
    DropdownComponent,
    TextBoxComponent,
    ModalDeleteComponent,
  ],
  templateUrl: './valore-pubblico-anticorruzione.component.html',
  styleUrl: './valore-pubblico-anticorruzione.component.scss',
})
export class ValorePubblicoAnticorruzioneComponent implements OnInit {
  @Input() formGroup!: FormGroup;

  private fb: FormBuilder = inject(FormBuilder);
  private sessionStorageService = inject(SessionStorageService);
  private obiettivoPrevenzioneCorruzioneTrasparenzaService = inject(
    ObiettivoPrevenzioneCorruzioneTrasparenzaService
  );
  private toastService = inject(ToastService);

  labelCardInfo: string = 'SEZIONE_23.ANTICORRUZIONE.CARD_INFO_TITLE';
  labelNotFoundCardInfo: string = 'SEZIONE_23.ANTICORRUZIONE.CARD_INFO_NOT_FOUND';
  labelAddObiettivo: string = 'SEZIONE_23.ANTICORRUZIONE.CARD_INFO_ADD';

  labelObiettivoPrefix: string = 'SEZIONE_23.ANTICORRUZIONE.OBIETTIVO_PREVENZIONE.TITLE';
  labelIntroText: string = 'SEZIONE_23.ANTICORRUZIONE.OBIETTIVO_PREVENZIONE.INTRO_TEXT';

  labelOvpLabel: string = 'SEZIONE_23.ANTICORRUZIONE.OBIETTIVO_PREVENZIONE.OVP_LABEL';
  labelStrategiaOvpLabel: string =
    'SEZIONE_23.ANTICORRUZIONE.OBIETTIVO_PREVENZIONE.STRATEGIA_ATTUATIVA_OVP_LABEL';
  labelObiettivoPerformanceLabel: string =
    'SEZIONE_23.ANTICORRUZIONE.OBIETTIVO_PREVENZIONE.OBIETTIVO_PERFORMANCE_LABEL';

  labelCodice: string = 'SEZIONE_23.ANTICORRUZIONE.OBIETTIVO_PREVENZIONE.CODICE_LABEL';
  labelDenominazione: string =
    'SEZIONE_23.ANTICORRUZIONE.OBIETTIVO_PREVENZIONE.DENOMINAZIONE_LABEL';
  labelDescrizione: string = 'SEZIONE_23.ANTICORRUZIONE.OBIETTIVO_PREVENZIONE.DESCRIZIONE_LABEL';

  // Dropdown options
  ovpOptions: LabelValue[] = [];
  strategieOptionsPerObiettivo: LabelValue[][] = []; // Array di array per ogni obiettivo
  obiettiviPerformanceOptionsPerObiettivo: LabelValue[][] = []; // Array di array per ogni obiettivo
  stakeholderOptions: LabelValue[] = [];

  // Gestione stato accordion
  openAccordionIndex: number | null = null;

  // Mappa degli OVP per lookup veloce
  private ovpMap: Map<number, OVPDTO> = new Map();
  private obiettiviPerformanceList: ObbiettivoPerformanceDTO[] = [];
  piaoDTO!: PIAODTO;

  openModalDelete: boolean = false;
  elementToDelete: any = null;

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
    this.loadOvpOptions();
    this.loadObiettiviPerformance();
    this.initializeStrategieForExistingObiettivi();
  }

  private loadOvpOptions(): void {
    if (this.piaoDTO?.sezione21?.ovp) {
      this.ovpOptions = this.getOvpDropdownOptions(this.piaoDTO.sezione21.ovp);
      // Crea la mappa degli OVP per lookup veloce
      this.piaoDTO.sezione21.ovp.forEach((ovp) => {
        if (ovp.id) {
          this.ovpMap.set(ovp.id, ovp);
        }
      });
    }
  }

  private loadObiettiviPerformance(): void {
    if (this.piaoDTO?.sezione22?.obbiettiviPerformance) {
      // Filtra solo gli obiettivi di tipo PERFORMANCE_ORGANIZZATIVA
      console.log(
        'Tutti gli obiettivi di performance:',
        this.piaoDTO.sezione22.obbiettiviPerformance
      );
      this.obiettiviPerformanceList = this.piaoDTO.sezione22.obbiettiviPerformance.filter(
        (obb) => obb.tipologia === TipologiaObbiettivo.PERFORMANCE_ORGANIZZATIVA
      );
    }
  }

  private getOvpDropdownOptions(ovpList: OVPDTO[]): LabelValue[] {
    return ovpList.map((ovp) => ({
      label: ovp.denominazione || '',
      value: ovp.id || 0,
    }));
  }

  private initializeStrategieForExistingObiettivi(): void {
    // Inizializza le strategie per gli obiettivi già esistenti
    this.obiettivoPrev.controls.forEach((obiettivo, index) => {
      const idOvp = obiettivo.get('idOVP')?.value;
      const idStrategiaOvp = obiettivo.get('idStrategiaOVP')?.value;

      this.strategieOptionsPerObiettivo[index] = idOvp ? this.getStrategieForOvp(idOvp) : [];
      this.obiettiviPerformanceOptionsPerObiettivo[index] =
        idOvp && idStrategiaOvp
          ? this.getObiettiviPerformanceForStrategia(idOvp, idStrategiaOvp)
          : [];

      // Imposta il codice per gli obiettivi esistenti
      this.setCodice(obiettivo as FormGroup, index);
    });
  }

  private getStrategieForOvp(idOvp: number): LabelValue[] {
    const ovp = this.ovpMap.get(idOvp);
    if (ovp?.ovpStrategias && ovp.ovpStrategias.length > 0) {
      return this.getStrategieDropdownOptions(ovp.ovpStrategias);
    }
    return [];
  }

  handleOvpChange(idOvp: number | null, index: number): void {
    console.log('Nuovo valore OVP selezionato:', idOvp);

    // Imposta esplicitamente il valore nel form control
    const obiettivo = this.obiettivoPrev.at(index) as FormGroup;
    obiettivo?.get('idOVP')?.setValue(idOvp);

    if (idOvp) {
      // Carica le strategie per l'OVP selezionato
      this.strategieOptionsPerObiettivo[index] = this.getStrategieForOvp(idOvp);
    } else {
      // Se non c'è OVP selezionato, svuota le strategie
      this.strategieOptionsPerObiettivo[index] = [];
    }

    // Reset della strategia e dell'obiettivo di performance quando cambia l'OVP
    obiettivo?.get('idStrategiaOVP')?.setValue(null);
    obiettivo?.get('idObbiettivoPerformance')?.setValue(null);

    // Svuota gli obiettivi di performance
    this.obiettiviPerformanceOptionsPerObiettivo[index] = [];

    // Ricalcola il codice
    this.setCodice(obiettivo, index);
  }

  handleStrategiaChange(idStrategia: number | null, index: number): void {
    const obiettivo = this.obiettivoPrev.at(index) as FormGroup;

    // Imposta esplicitamente il valore nel form control
    obiettivo?.get('idStrategiaOVP')?.setValue(idStrategia);

    const idOvp = obiettivo.get('idOVP')?.value;

    console.log('Obiettivo dopo cambio strategia:', obiettivo.value);

    if (idStrategia && idOvp) {
      // Carica gli obiettivi di performance per la strategia selezionata
      this.obiettiviPerformanceOptionsPerObiettivo[index] =
        this.getObiettiviPerformanceForStrategia(idOvp, idStrategia);
    } else {
      // Se non c'è strategia selezionata, svuota gli obiettivi
      this.obiettiviPerformanceOptionsPerObiettivo[index] = [];
    }

    // Reset dell'obiettivo di performance selezionato quando cambia la strategia
    obiettivo?.get('idObbiettivoPerformance')?.setValue(null);

    // Ricalcola il codice quando cambia la strategia
    this.setCodice(obiettivo, index);
  }

  private getStrategieDropdownOptions(strategieList: OVPStrategiaDTO[]): LabelValue[] {
    return strategieList.map((strategia) => ({
      label: strategia.denominazioneStrategia || strategia.codStrategia || '',
      value: strategia.id || 0,
    }));
  }

  private getObiettiviPerformanceForStrategia(idOvp: number, idStrategia: number): LabelValue[] {
    const obiettiviFiltered = this.obiettiviPerformanceList.filter(
      (obb) => obb.idOvp === idOvp && obb.idStrategiaOvp === idStrategia
    );
    return this.getObiettiviPerformanceDropdownOptions(obiettiviFiltered);
  }

  private getObiettiviPerformanceDropdownOptions(
    obiettiviList: ObbiettivoPerformanceDTO[]
  ): LabelValue[] {
    return obiettiviList.map((obiettivo) => ({
      label: obiettivo.denominazione || obiettivo.codice || '',
      value: obiettivo.id || 0,
    }));
  }

  setCodice(obiettivo: FormGroup, index: number): void {
    const idOvp = obiettivo.get('idOVP')?.value;
    const idStrategiaOvp = obiettivo.get('idStrategiaOVP')?.value;
    const idObiettivoPerformace = obiettivo.get('idObbiettivoPerformance')?.value;

    console.log('Set codice per obiettivo index:', index);
    console.log('idOvp:', idOvp);
    console.log('idStrategiaOvp:', idStrategiaOvp);
    console.log('idObiettivoPerformace:', idObiettivoPerformace);

    console.log('Obiettivi di performance disponibili:', this.obiettiviPerformanceList);
    console.log('OVP disponibili:', this.ovpMap);

    let codice = '';

    // Recupera il codice OVP se selezionato
    if (idOvp) {
      const ovp = this.ovpMap.get(idOvp);
      if (ovp?.codice) {
        codice = ovp.codice;
      }
    }

    // Recupera il codice strategia se selezionato
    if (idStrategiaOvp && idOvp) {
      const ovp = this.ovpMap.get(idOvp);
      const strategia = ovp?.ovpStrategias?.find((s) => s.id === idStrategiaOvp);
      if (strategia?.codStrategia) {
        codice = strategia.codStrategia;
      }
    }

    if (idStrategiaOvp && idOvp && idObiettivoPerformace) {
      const obiettivoPerformance = this.obiettiviPerformanceList.find(
        (obb) => obb.id === idObiettivoPerformace
      );
      if (obiettivoPerformance?.codice) {
        codice = obiettivoPerformance.codice;
      }
    }

    // Aggiungi l'indice in base alla tipologia
    const suffix = 'ANT';
    codice = codice ? `${codice}_${suffix}${index + 1}` : `${suffix}${index + 1}`;

    obiettivo.get('codice')?.setValue(codice);
  }

  trackByObiettivoId(index: number, item: any): number {
    return item.get('id')?.value ?? index;
  }

  handleObiettivoPerformanceChange(event: any, index: number): void {
    const obiettivo = this.obiettivoPrev.at(index) as FormGroup;
    console.log('Obiettivo performance changed, new value:', obiettivo.value);
    obiettivo?.get('idObbiettivoPerformance')?.setValue(event);
    this.setCodice(obiettivo, index);
  }

  handleAddObiettivo(): void {
    const obiettivoForm = this.fb.group({
      id: [null],
      idSezione23: [this.piaoDTO?.sezione23?.id || null],
      idOVP: [null, Validators.required],
      idStrategiaOVP: [null, Validators.required],
      idObbiettivoPerformance: [null],
      codice: [null, [Validators.required, Validators.pattern(INPUT_REGEX)]],
      denominazione: [
        null,
        [Validators.required, Validators.pattern(INPUT_REGEX), Validators.maxLength(250)],
      ],
      descrizione: [null, [Validators.pattern(INPUT_REGEX), Validators.maxLength(100)]],
      indicatori: (() => {
        const formArray =
          createFormArrayGenericIndicatoreFromPiaoSession<ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoriDTO>(
            this.fb,
            [],
            ['id', 'indicatore'],
            INPUT_REGEX
          );
        formArray?.setValidators(Validators.required);
        return formArray;
      })(),
    });

    this.setCodice(obiettivoForm, this.obiettivoPrev.length);

    this.obiettivoPrev.push(obiettivoForm);
    this.openAccordionIndex = this.obiettivoPrev.length; // Apri l'accordion appena aggiunto
  }

  handleRemoveObiettivo(index: number): void {
    const obiettivo = this.obiettivoPrev.at(index);
    const id = obiettivo.get('id')?.value;
    if (id) {
      this.obiettivoPrevenzioneCorruzioneTrasparenzaService.delete(id).subscribe({
        next: () => {
          this.toastService.success('Obiettivo eliminato con successo');
        },
        error: () => {
          this.toastService.error("Errore durante l'eliminazione dell'obiettivo");
        },
      });
    } else {
      this.obiettivoPrev.removeAt(index);
      this.toastService.success('Obiettivo eliminato con successo');
    }
    this.handleCloseModalDelete();
  }

  get obiettivoPrev(): FormArray {
    const formArray = this.formGroup.controls[
      'obiettivoPrevenzioneCorruzioneTrasparenza'
    ] as FormArray;
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

  handleOpenModalDelete(element: any) {
    this.openModalDelete = true;
    this.elementToDelete = element;
  }
  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = null;
  }
}
