import { Component, inject, Input, OnInit } from '@angular/core';
import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { SharedModule } from '../../module/shared/shared.module';
import { AccordionComponent } from '../../components/accordion/accordion.component';
import { TextBoxComponent } from '../../components/text-box/text-box.component';
import { IndicatoriComponent } from '../servizi-piao/indice-piao/sezione/indicatori/indicatori.component';
import { INPUT_REGEX, KEY_PIAO } from '../../utils/constants';
import { ToastService } from '../../services/toast.service';
import {
  canAddToFormArray,
  createFormArrayGenericIndicatoreFromPiaoSession,
  createFormMongoFromPiaoSession,
} from '../../utils/utils';
import { TipologiaObbiettivo } from '../../models/enums/tipologia-obbiettivo.enum';
import { DropdownComponent } from '../../components/dropdown/dropdown.component';
import { ContributoreInternoDTO } from '../../models/classes/contributore-interno-dto';
import { DynamicTBConfig } from '../../models/classes/config/dynamic-tb';
import { CodTipologiaDimensioneEnum } from '../../models/enums/cod-tipologia-dimensione.enum';
import { CodTipologiaIndicatoreEnum } from '../../models/enums/cod-tipologia-indicatore.enum';
import { SectionEnum } from '../../models/enums/section.enum';
import { Sezione22Service } from '../../services/sezioni-22.service';
import { ObbiettivoPerformanceService } from '../../services/obbiettivo-performance.service';
import { ObbiettivoPerformanceDTO } from '../../models/classes/obiettivo-performance-dto';
import { ObiettivoIndicatoriDTO } from '../../models/classes/obiettivo-indicatori-dto';
import { DynamicTBFieldsComponent } from '../../components/dynamic-tb-fields/dynamic-tb-fields.component';
import { LabelValue } from '../../models/interfaces/label-value';
import { SessionStorageService } from '../../services/session-storage.service';
import { PIAODTO } from '../../models/classes/piao-dto';
import { OVPDTO } from '../../models/classes/ovp-dto';
import { OVPStrategiaDTO } from '../../models/classes/ovp-strategia-dto';
import { CardInfoComponent } from '../card-info/card-info.component';
import { ModalDeleteComponent } from '../../components/modal-delete/modal-delete.component';
import { OvpService } from '../../services/ovp.service';

@Component({
  selector: 'piao-obbiettivi-trasversali',
  standalone: true,
  imports: [
    SharedModule,
    AccordionComponent,
    TextBoxComponent,
    IndicatoriComponent,
    ReactiveFormsModule,
    DropdownComponent,
    DynamicTBFieldsComponent,
    CardInfoComponent,
    ModalDeleteComponent,
  ],
  templateUrl: './obbiettivi-trasversali.component.html',
  styleUrl: './obbiettivi-trasversali.component.scss',
})
export class ObbiettiviTrasversaliComponent implements OnInit {
  @Input() formGroup!: FormGroup;
  @Input() idSezione22!: number;
  @Input() idPiao!: number;
  @Input() tipologia!: TipologiaObbiettivo;
  @Input() labelTitle!: string;
  @Input() accordionIndex!: number;
  @Input() isOpen: boolean = false;
  @Input() codTipologiaIndicatoreFK!: string;
  @Input() isFirstAccordion: boolean = false;
  @Input() ovpList: OVPDTO[] = [];
  @Input() stakeholderOptions: LabelValue[] = [];

  labelObbiettivi: string = 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.TITLE';
  titleCardHeader: string = 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.TITLE_CARD_HEADER';
  subTitleObiettivo: string = 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.SUB_TITLE';
  titleCardInfoObiettivo: string = 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.CARD_TITLE';
  subTitleAddObiettivo: string = 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.BTN_ADD';

  labelIdObiettivo: string = 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.ID_OBIETTIVO';
  labelObiettivoVP: string = 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.OBIETTIVO_VP';
  labelStrategiaOVP: string =
    'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.STRATEGIA_ATTUATIVA_OVP';
  labelObiettivoPerformance: string =
    'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.OBIETTIVO_PERFORMANCE';
  labelObiettivoPerformanceRispettoPA: string =
    'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.OBIETTIVO_PERFORMANCE_RISPETTO_PA';
  labelResponsabileAmministrativo: string =
    'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.RESPONSABILE_AMMINISTRATIVO';
  labelContributoriInterni: string =
    'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.CONTRIBUTORI_INTERNI';
  labelStakeholder: string = 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.STAKEHOLDER';
  labelSelezionaStakeholder: string =
    'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.SELEZIONA_STAKEHOLDER';
  labelObiettivoPrefix: string = 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.OBIETTIVO_PREFIX';
  labelIntroText: string = 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.INTRO_TEXT';
  labelAggiungiContributore: string =
    'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.AGGIUNGI_CONTRIBUTORE';

  titleCardInfoAddNewAccessiDigitale: string =
    'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.CARD_TITLE_ACCESSI_DIGITALE';
  titleCardInfoAddNewAccessiFisici: string =
    'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.CARD_TITLE_ACCESSI_FISICI';
  titleCardInfoAddNewSemplificazione: string =
    'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.CARD_TITLE_SEMPLIFICAZIONE';
  titleCardInfoAddNewPariOpportunita: string =
    'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.CARD_TITLE_PARI_OPPORTUNITA';

  contributoreConfig!: DynamicTBConfig;

  private fb = inject(FormBuilder);
  private sessionStorageService = inject(SessionStorageService);
  private sezione22Service = inject(Sezione22Service);
  private obiettivoPerformanceService = inject(ObbiettivoPerformanceService);
  toastService = inject(ToastService);
  private ovpService = inject(OvpService);

  codTipologiaFK: string = CodTipologiaDimensioneEnum.OBB;
  sectionEnum: string = SectionEnum.SEZIONE_2_2;

  // Dropdown options
  ovpOptions: LabelValue[] = [];
  strategieOptionsPerObiettivo: LabelValue[][] = []; // Array di array per ogni obiettivo

  // Gestione stato accordion
  openAccordionIndex: number | null = null;

  // Mappa degli OVP per lookup veloce
  private ovpMap: Map<number, OVPDTO> = new Map();
  piaoDTO!: PIAODTO;

  openModalDelete: boolean = false;
  elementToDelete: any = null;

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);

    this.initContributoreConfig();
    this.loadOvpOptions();
    this.initializeStrategieForExistingObiettivi();
    console.log('[ObbiettiviTrasversali] ngOnInit, controls length:', this.obbiettivi.length);
  }

  private loadOvpOptions(): void {
    if (this.ovpList && this.ovpList.length > 0) {
      this.ovpOptions = this.getOvpDropdownOptions(this.ovpList);
      this.ovpList.forEach((ovp) => {
        if (ovp.id) {
          this.ovpMap.set(ovp.id, ovp);
        }
      });
      return;
    }
  }

  private loadOvpOptionsBE(): void {
    this.ovpService.getAllOvpByIdPiao(this.piaoDTO.id || -1).subscribe({
      next: (ovpList: OVPDTO[]) => {
        this.ovpOptions = this.getOvpDropdownOptions(ovpList);
        // Crea la mappa degli OVP per lookup veloce
        ovpList.forEach((ovp) => {
          if (ovp.id) {
            this.ovpMap.set(ovp.id, ovp);
          }
        });
      },
      error: (err) => {
        console.error('Errore nel caricamento degli OVP:', err);
      },
    });
  }

  private getOvpDropdownOptions(ovpList: OVPDTO[]): LabelValue[] {
    return ovpList.map((ovp) => ({
      label: ovp.denominazione || '',
      value: ovp.id || 0,
    }));
  }

  private initializeStrategieForExistingObiettivi(): void {
    // Inizializza le strategie per gli obiettivi già esistenti
    this.obbiettivi.controls.forEach((obiettivo, index) => {
      const idOvp = obiettivo.get('idOvp')?.value;
      this.strategieOptionsPerObiettivo[index] = idOvp ? this.getStrategieForOvp(idOvp) : [];

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
    if (idOvp) {
      // Carica le strategie per l'OVP selezionato
      this.strategieOptionsPerObiettivo[index] = this.getStrategieForOvp(idOvp);
    } else {
      // Se non c'è OVP selezionato, svuota le strategie
      this.strategieOptionsPerObiettivo[index] = [];
    }
    // Reset della strategia selezionata quando cambia l'OVP
    const obiettivo = this.obbiettivi.at(index) as FormGroup;
    obiettivo?.get('idStrategiaOvp')?.setValue(null);

    // Ricalcola il codice
    this.setCodice(obiettivo, index);
  }

  handleStrategiaChange(idStrategia: number | null, index: number): void {
    // Ricalcola il codice quando cambia la strategia
    const obiettivo = this.obbiettivi.at(index) as FormGroup;
    this.setCodice(obiettivo, index);
  }

  private getStrategieDropdownOptions(strategieList: OVPStrategiaDTO[]): LabelValue[] {
    return strategieList.map((strategia) => ({
      label: strategia.denominazioneStrategia || strategia.codStrategia || '',
      value: strategia.id || 0,
    }));
  }

  handleAddContributoreInterno(control: any): void {
    const contributore = control as FormGroup;
    const properties = contributore.get('properties') as FormArray;
    if (canAddToFormArray(properties, ['value'])) {
      properties.push(
        this.fb.group({
          value: [null, [Validators.maxLength(100), Validators.pattern(INPUT_REGEX)]],
        })
      );
    }
  }

  handleRemoveContributore(control: any, index: number): void {
    const contributore = control as FormGroup;
    const properties = contributore.get('properties') as FormArray;
    properties.removeAt(index);
  }

  setCodice(obiettivo: FormGroup, index: number): FormControl {
    const idOvp = obiettivo.get('idOvp')?.value;
    const idStrategiaOvp = obiettivo.get('idStrategiaOvp')?.value;

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

    // Aggiungi l'indice in base alla tipologia
    const suffix = 'TR';
    codice = codice ? `${codice}_${suffix}${index + 1}` : `${suffix}${index + 1}`;

    obiettivo.get('codice')?.setValue(codice);
    return obiettivo.get('codice') as FormControl;
  }

  handleRemoveObiettivo(index: number): void {
    const obiettivo = this.obbiettivi.at(index);
    const obiettivoId = obiettivo?.get('id')?.value;

    // Se l'obiettivo ha un ID, significa che è stato salvato sul backend
    if (obiettivoId) {
      // Chiama il backend per eliminarlo
      this.obiettivoPerformanceService.delete(obiettivoId).subscribe({
        next: () => {
          this.obbiettivi.removeAt(index);
          // Rimuovi anche le strategie associate
          this.strategieOptionsPerObiettivo.splice(index, 1);
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
      this.obbiettivi.removeAt(index);
      // Rimuovi anche le strategie associate
      this.strategieOptionsPerObiettivo.splice(index, 1);
    }

    this.handleCloseModalDelete();
  }

  handleAddObiettivo(): void {
    const newObiettivo = this.fb.group({
      id: [null, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
      idSezione22: [this.idSezione22, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
      idOvp: [
        null,
        [Validators.maxLength(20), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
      idStrategiaOvp: [
        null,
        [Validators.maxLength(20), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
      codice: [
        null,
        [Validators.maxLength(50), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
      tipologia: [
        this.tipologia,
        [Validators.maxLength(50), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
      denominazione: [
        null,
        [Validators.maxLength(250), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
      responsabileAmministrativo: [
        null,
        [Validators.maxLength(100), Validators.pattern(INPUT_REGEX)],
      ],
      contributoreInterno: createFormMongoFromPiaoSession<ContributoreInternoDTO>(
        this.fb,
        new ContributoreInternoDTO(),
        ['id', 'externalId', 'properties'],
        INPUT_REGEX,
        50,
        false,
        this.contributoreConfig.labelTB
      ),
      stakeholders: [[], []],
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

    const newIndex = this.obbiettivi.length;
    this.obbiettivi.push(newObiettivo);

    // Imposta il codice per il nuovo obiettivo
    this.setCodice(newObiettivo, newIndex);

    this.loadOvpOptionsBE();
    this.initializeStrategieForExistingObiettivi();

    // Inizializza le strategie vuote per il nuovo obiettivo
    this.strategieOptionsPerObiettivo[newIndex] = [];

    // Apri l'accordion del nuovo obiettivo
    this.openAccordionIndex = this.obbiettivi.length;
  }

  private initContributoreConfig(): void {
    this.contributoreConfig = {
      labelTB: 'Contributore interno',
      typeTB: 'text',
      idTB: 'value',
      controlTB: 'value',
      controlTBMongo: 'key',
      classTB: 'input',
      maxValidatorLengthTB: 50,
      descTooltipTB: 'Contributore interno',
    };
  }

  get obbiettivi(): FormArray {
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

  getTitleCardInfo(): string {
    if (this.obbiettivi.length === 0) {
      return this.titleCardInfoObiettivo;
    } else {
      switch (this.tipologia) {
        case TipologiaObbiettivo.ACCESSI_DIGITALE:
          return this.titleCardInfoAddNewAccessiDigitale;
        case TipologiaObbiettivo.ACCESSI_FISICI:
          return this.titleCardInfoAddNewAccessiFisici;
        case TipologiaObbiettivo.SEMPLIFICAZIONE:
          return this.titleCardInfoAddNewSemplificazione;
        case TipologiaObbiettivo.PARI_OPPORTUNITA:
          return this.titleCardInfoAddNewPariOpportunita;
        default:
          return '';
      }
    }
  }

  getLabelByTipologia(): string {
    switch (this.tipologia) {
      case TipologiaObbiettivo.ACCESSI_DIGITALE:
        return 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.LABELS.ACCESSI_DIGITALE';
      case TipologiaObbiettivo.ACCESSI_FISICI:
        return 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.LABELS.ACCESSI_FISICI';
      case TipologiaObbiettivo.SEMPLIFICAZIONE:
        return 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.LABELS.SEMPLIFICAZIONE';
      case TipologiaObbiettivo.PARI_OPPORTUNITA:
        return 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.LABELS.PARI_OPPORTUNITA';
      default:
        return '';
    }
  }

  getLabelIdObiettivo(): string {
    switch (this.tipologia) {
      case TipologiaObbiettivo.ACCESSI_DIGITALE:
        return 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.ID_ACCESSI_DIGITALE';
      case TipologiaObbiettivo.ACCESSI_FISICI:
        return 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.ID_ACCESSI_FISICI';
      case TipologiaObbiettivo.SEMPLIFICAZIONE:
        return 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.ID_SEMPLIFICAZIONE';
      case TipologiaObbiettivo.PARI_OPPORTUNITA:
        return 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.ID_PARI_OPPORTUNITA';
      default:
        return '';
    }
  }

  getLabelObiettivoTipologia(): string {
    switch (this.tipologia) {
      case TipologiaObbiettivo.ACCESSI_DIGITALE:
        return 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.OBIETTIVO_ACCESSI_DIGITALE';
      case TipologiaObbiettivo.ACCESSI_FISICI:
        return 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.OBIETTIVO_ACCESSI_FISICI';
      case TipologiaObbiettivo.SEMPLIFICAZIONE:
        return 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.OBIETTIVO_SEMPLIFICAZIONE';
      case TipologiaObbiettivo.PARI_OPPORTUNITA:
        return 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.DETAILS.OBIETTIVO_PARI_OPPORTUNITA';
      default:
        return '';
    }
  }

  getCardInfoTitle(): string {
    switch (this.tipologia) {
      case TipologiaObbiettivo.ACCESSI_DIGITALE:
        return 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.CARD_TITLE_ACCESSI_DIGITALE';
      case TipologiaObbiettivo.ACCESSI_FISICI:
        return 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.CARD_TITLE_ACCESSI_FISICI';
      case TipologiaObbiettivo.SEMPLIFICAZIONE:
        return 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.CARD_TITLE_SEMPLIFICAZIONE';
      case TipologiaObbiettivo.PARI_OPPORTUNITA:
        return 'SEZIONE_22.OBBIETTIVI_TRASVERSALI.OBIETTIVO.CARD_TITLE_PARI_OPPORTUNITA';
      default:
        return '';
    }
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
