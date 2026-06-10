import {
  Component,
  DestroyRef,
  ElementRef,
  HostListener,
  inject,
  Input,
  OnInit,
  QueryList,
  ViewChildren,
} from '@angular/core';
import { TooltipComponent } from '../../components/tooltip/tooltip.component';
import {
  Form,
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { SharedModule } from '../../module/shared/shared.module';
import { AccordionComponent } from '../../components/accordion/accordion.component';
import { CardInfoComponent } from '../card-info/card-info.component';
import { TextBoxComponent } from '../../components/text-box/text-box.component';
import { IndicatoriComponent } from '../servizi-piao/indice-piao/sezione/indicatori/indicatori.component';
import { INPUT_REGEX, KEY_PIAO } from '../../utils/constants';
import { ToastService } from '../../services/toast.service';
import {
  canAddToFormArray,
  createFormArrayGenericIndicatoreFromPiaoSession,
  createFormMongoFromPiaoSession,
  getChangedFields,
} from '../../utils/utils';
import { TipologiaObbiettivo } from '../../models/enums/tipologia-obbiettivo.enum';
import { CodTipologiaIndicatoreEnum } from '../../models/enums/cod-tipologia-indicatore.enum';
import { DropdownComponent } from '../../components/dropdown/dropdown.component';
import { LabelValue } from '../../models/interfaces/label-value';
import { SessionStorageService } from '../../services/session-storage.service';
import { PIAODTO } from '../../models/classes/piao-dto';
import { OVPDTO } from '../../models/classes/ovp-dto';
import { OVPStrategiaDTO } from '../../models/classes/ovp-strategia-dto';
import { SocialDTO } from '../../models/classes/social-dto';
import { ContributoreInternoDTO } from '../../models/classes/contributore-interno-dto';
import { DynamicTBConfig } from '../../models/classes/config/dynamic-tb';
import { DynamicTBComponent } from '../../components/dynamic-tb/dynamic-tb.component';
import { CodTipologiaDimensioneEnum } from '../../models/enums/cod-tipologia-dimensione.enum';
import { SectionEnum } from '../../models/enums/section.enum';
import { Sezione22Service } from '../../services/sezioni-22.service';
import { ObbiettivoPerformanceService } from '../../services/obbiettivo-performance.service';
import { ObbiettivoPerformanceDTO } from '../../models/classes/obiettivo-performance-dto';
import { ObiettivoIndicatoriDTO } from '../../models/classes/obiettivo-indicatori-dto';
import { DynamicTBFieldsComponent } from '../../components/dynamic-tb-fields/dynamic-tb-fields.component';
import { ModalDeleteComponent } from '../../components/modal-delete/modal-delete.component';
import { OvpService } from '../../services/ovp.service';
import { StakeholderService } from '../../services/stakeholder.service';
import { ModalService } from '../../services/modal.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CodDeleteImpactEnum } from '../../models/enums/cod-delete-impact.enum';

@Component({
  selector: 'piao-obbiettivi-performance',
  standalone: true,
  imports: [
    SharedModule,
    AccordionComponent,
    CardInfoComponent,
    TextBoxComponent,
    IndicatoriComponent,
    ReactiveFormsModule,
    DropdownComponent,
    DynamicTBFieldsComponent,
    ModalDeleteComponent,
  ],
  templateUrl: './obbiettivi-performance.component.html',
  styleUrl: './obbiettivi-performance.component.scss',
})
export class ObbiettiviPerformanceComponent implements OnInit {
  @Input() formGroup!: FormGroup;
  @Input() idSezione22!: number;
  @Input() idPiao!: number;
  @Input() tipologia: TipologiaObbiettivo = TipologiaObbiettivo.PERFORMANCE;
  @Input() codTipologiaIndicatoreFK: string = CodTipologiaIndicatoreEnum.PERFORMANCE;
  @Input() ovpList: OVPDTO[] = [];
  @Input() stakeholderOptions!: LabelValue[];
  @Input() testoSezione!: string;
  @Input() isDettaglio: boolean = false;
  @ViewChildren('obiettivoBody') obiettivoBodies!: QueryList<ElementRef>;

  /** Indice dell'obiettivo in cui l'utente sta interagendo */
  editingObiettivoIndex: number | null = null;
  /** Flag che indica se l'utente ha interagito con campi di un obiettivo */
  isEditingObiettivo: boolean = false;
  /** Snapshot JSON del valore dell'obiettivo al momento del focus (senza codice) */
  private obiettivoSnapshot: string | null = null;

  labelObbiettivi: string = 'SEZIONE_22.OBIETTIVI_PERFORMANCE.TITLE';
  labelObiettiviPerformanceNotFound: string = 'SEZIONE_22.OBIETTIVI_PERFORMANCE.NOT_FOUND';
  subTitleObiettivo: string = 'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.SUB_TITLE';
  titleCardInfoObiettivo: string = 'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.CARD_TITLE';
  titleCardInfoObiettivoOrganizzativa: string =
    'SEZIONE_22.OBIETTIVO_PERFORMANCE_ORGANIZZATIVA.CARD_INFO_TITLE';
  subTitleAddObiettivo: string = 'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.BTN_ADD';

  labelIdObiettivo: string = 'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.DETAILS.ID_OBIETTIVO';
  labelObiettivoVP: string = 'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.DETAILS.OBIETTIVO_VP';
  labelStrategiaOVP: string =
    'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.DETAILS.STRATEGIA_ATTUATIVA_OVP';
  labelObiettivoPerformance: string =
    'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.DETAILS.OBIETTIVO_PERFORMANCE';
  labelObiettivoPerformanceRispettoPA: string =
    'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.DETAILS.OBIETTIVO_PERFORMANCE_RISPETTO_PA';
  labelResponsabileAmministrativo: string =
    'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.DETAILS.RESPONSABILE_AMMINISTRATIVO';
  labelContributoriInterni: string =
    'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.DETAILS.CONTRIBUTORI_INTERNI';
  labelContributoreInterno: string =
    'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.DETAILS.CONTRIBUTORE_INTERNO';
  labelStakeholder: string = 'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.DETAILS.STAKEHOLDER';
  labelSelezionaStakeholder: string =
    'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.DETAILS.SELEZIONA_STAKEHOLDER';
  labelObiettivoPrefix: string = 'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.OBIETTIVO_PREFIX';
  labelIntroText: string = 'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.INTRO_TEXT';
  labelAggiungiContributore: string =
    'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.DETAILS.AGGIUNGI_CONTRIBUTORE';

  //Pianificazione risorse
  titlePianificazioneRisorse: string =
    'SEZIONE_22.OBIETTIVO_PERFORMANCE_ORGANIZZATIVA.RISORSE_TITLE';
  labelRisorseUmane: string = 'SEZIONE_22.OBIETTIVO_PERFORMANCE_ORGANIZZATIVA.RISORSE_UMANE_LABEL';
  labelRisorseStrumentali: string =
    'SEZIONE_22.OBIETTIVO_PERFORMANCE_ORGANIZZATIVA.RISORSE_STRUMENTALI_LABEL';
  labelRisorseFinanziarie: string =
    'SEZIONE_22.OBIETTIVO_PERFORMANCE_ORGANIZZATIVA.RISORSE_FINANZIARIE_LABEL';

  //NOT FOUND STAKEHOLDER
  messageNotFoundStakeholder =
    'SEZIONE_22.OBIETTIVI_PERFORMANCE.OBIETTIVO.DETAILS.NOT_FOUND_STAKEHOLDER';

  // Espone l'enum al template
  TipologiaObbiettivo = TipologiaObbiettivo;

  contributoreConfig!: DynamicTBConfig;

  private fb = inject(FormBuilder);
  private sessionStorageService = inject(SessionStorageService);
  private obiettivoPerformanceService = inject(ObbiettivoPerformanceService);
  private ovpService = inject(OvpService);
  toastService = inject(ToastService);
  private modalService = inject(ModalService);
  private destroyRef = inject(DestroyRef);

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
  savedObbiettivi: any[] = [];

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);

    if (this.tipologia === TipologiaObbiettivo.PERFORMANCE_ORGANIZZATIVA) {
      this.titleCardInfoObiettivo = this.titleCardInfoObiettivoOrganizzativa;

      //catturo l'evento e apro il canale di intercettazione eventi solamente se il tipo di obiettivo è PERFORMANCE_ORGANIZZATIVA
      this.modalService.onConfirmAction$
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(({ metadato }) => {
          switch (metadato.value as string) {
            case CodDeleteImpactEnum.OBIETTIVO_PERFORMANCE_ORGANIZZATIVA:
              this.handleRemoveObiettivo({ id: metadato.idFK! }, true);
              break;
            default:
              break;
          }
        });
    }

    this.initContributoreConfig();
    this.loadOvpOptions();
    this.initializeStrategieForExistingObiettivi();
    this.savedObbiettivi = structuredClone(this.obbiettiviPerformance?.value ?? []);
  }

  /** Chiamato dal (focusin) nel template per marcare che l'utente sta editando uno specifico obiettivo */
  onObiettivoFocusIn(obiettivoIndex: number): void {
    if (this.editingObiettivoIndex !== obiettivoIndex) {
      // Se stavamo editando un altro obiettivo, auto-save prima di switchare
      if (this.isEditingObiettivo && this.editingObiettivoIndex !== null) {
        const currentSnapshot = this.getObiettivoSnapshotJson(this.editingObiettivoIndex);
        if (this.obiettivoSnapshot !== null && currentSnapshot !== this.obiettivoSnapshot) {
          this.autoSaveSingoloObiettivo(this.editingObiettivoIndex);
        }
      }
      this.editingObiettivoIndex = obiettivoIndex;
      this.obiettivoSnapshot = this.getObiettivoSnapshotJson(obiettivoIndex);
    }
    this.isEditingObiettivo = true;
  }

  /** Crea un JSON stringify del valore dell'obiettivo escludendo la property 'codice' */
  private getObiettivoSnapshotJson(obiettivoIndex: number): string | null {
    if (obiettivoIndex >= this.obbiettiviPerformance.length) return null;
    const fg = this.obbiettiviPerformance.at(obiettivoIndex) as FormGroup;
    const value = { ...fg.value };
    delete value.codice;
    return JSON.stringify(value);
  }

  /** Rileva click fuori dall'obiettivo attivo e triggera l'auto-save (solo PERFORMANCE_ORGANIZZATIVA) */
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.isEditingObiettivo || this.editingObiettivoIndex === null) return;

    // Ignora se ci sono modal aperti
    if (this.openModalDelete) return;

    const target = event.target as HTMLElement;

    // Ignora click su elementi rimossi dal DOM (es. menu azioni CDK overlay già chiuso)
    if (!target.isConnected) return;

    // Ignora click dentro CDK overlay (es. menu azioni, popup ancora aperto)
    if (target.closest('.cdk-overlay-container')) return;

    const bodies = this.obiettivoBodies?.toArray() || [];

    const activeObiettivoBody = bodies.find((el) => el.nativeElement.contains(target));

    if (activeObiettivoBody) {
      const oIdx = activeObiettivoBody.nativeElement.getAttribute('data-obiettivo-index');
      if (Number(oIdx) === this.editingObiettivoIndex) {
        return; // Siamo dentro lo stesso obiettivo, non fare nulla
      }
    }

    // Click fuori → auto-save solo se il form è cambiato rispetto allo snapshot
    const currentSnapshot = this.getObiettivoSnapshotJson(this.editingObiettivoIndex);

    if (this.obiettivoSnapshot !== null && currentSnapshot !== this.obiettivoSnapshot) {
      this.autoSaveSingoloObiettivo(this.editingObiettivoIndex);
    }
    this.isEditingObiettivo = false;
    this.editingObiettivoIndex = null;
    this.obiettivoSnapshot = null;
  }

  /** Salva un singolo obiettivo performance organizzativa se dirty e con id */
  private autoSaveSingoloObiettivo(obiettivoIndex: number): void {
    if (obiettivoIndex >= this.obbiettiviPerformance.length) return;

    const fg = this.obbiettiviPerformance.at(obiettivoIndex) as FormGroup;
    const obj = { ...fg.value };

    // Confronta PRIMA della trasformazione stakeholders, così entrambi i lati sono array di ID piatti
    let campiModificati = getChangedFields(
      obj,
      this.savedObbiettivi?.[obiettivoIndex],
      ['id', 'indicatori', 'key', 'externalId', 'tipologia', 'idSezione22'], // campi da escludere dal confronto
      `obbiettivi_${this.tipologia}`
    );

    // Trasforma stakeholders da array di ID a array di DTO: { stakeholder: { id } }
    const stakeholderIds: number[] = obj.stakeholders || [];
    obj.stakeholders = stakeholderIds.map((id: number) => ({ stakeholder: { id: id } }));

    // Controlla se sono stati eliminati indicatori rispetto al saved
    const savedIndicatori = this.savedObbiettivi?.[obiettivoIndex]?.indicatori || [];
    const currentIndicatori = fg.value?.indicatori || [];
    if (
      savedIndicatori.length > currentIndicatori.length ||
      savedIndicatori.length < currentIndicatori.length
    ) {
      campiModificati = campiModificati
        ? campiModificati + `,obbiettivi_${this.tipologia}.indicatori`
        : `obbiettivi_${this.tipologia}.indicatori`;
    }

    const obiettiviRequest = {
      ...obj,
      idPiao: this.piaoDTO.id,
      testoSezione: this.testoSezione,
      campiModificati: campiModificati,
    };

    this.obiettivoPerformanceService.save(obiettiviRequest).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        fg.markAsPristine();
        this.savedObbiettivi = structuredClone(this.obbiettiviPerformance?.value ?? []);
        this.toastService.success('Obiettivo performance organizzativa salvato con successo');
      },
      error: (err) => {
        console.error("Errore nel salvataggio automatico dell'obiettivo performance:", err);
      },
    });
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
    this.ovpService.getAllOvpByIdPiao(this.piaoDTO.id || -1).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
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
    this.obbiettiviPerformance.controls.forEach((obiettivo, index) => {
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
    const obiettivo = this.obbiettiviPerformance.at(index) as FormGroup;
    obiettivo?.get('idStrategiaOvp')?.setValue(null);

    // Ricalcola il codice
    this.setCodice(obiettivo, index);
  }

  handleStrategiaChange(idStrategia: number | null, index: number): void {
    // Ricalcola il codice quando cambia la strategia
    const obiettivo = this.obbiettiviPerformance.at(index) as FormGroup;
    this.setCodice(obiettivo, index);
  }

  private getStrategieDropdownOptions(strategieList: OVPStrategiaDTO[]): LabelValue[] {
    return strategieList.map((strategia) => ({
      label: strategia.denominazioneStrategia || strategia.codStrategia || '',
      value: strategia.id || 0,
    }));
  }

  setCodice(obiettivo: FormGroup, index: number): FormControl {
    const idOvp = obiettivo.get('idOvp')?.value;
    const idStrategiaOvp = obiettivo.get('idStrategiaOvp')?.value;

    let codice = '';
    let prefix = '';

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

    if (this.tipologia === TipologiaObbiettivo.PERFORMANCE_ORGANIZZATIVA) {
      prefix = 'PERF';
    } else {
      prefix = 'PP';
    }

    // Aggiungi l'indice
    codice = codice ? `${codice}_${prefix}${index + 1}` : `${prefix}${index + 1}`;

    obiettivo.get('codice')?.setValue(codice);
    return obiettivo.get('codice') as FormControl;
  }

  handleRemoveObiettivo(obiettivo: any, forceDelete: boolean = false): void {
    if (obiettivo instanceof FormGroup) {
      obiettivo = obiettivo.value;
    }

    const obiettivoId = obiettivo?.id;

    const index = this.obbiettiviPerformance.controls.findIndex(
      (ctrl) => ctrl.get('id')?.value === obiettivoId
    );

    // Se l'obiettivo ha un ID, significa che è stato salvato sul backend
    if (obiettivoId) {
      // Chiama il backend per eliminarlo
      this.obiettivoPerformanceService
        .delete(obiettivoId, this.idPiao, this.testoSezione, this.tipologia, forceDelete)
        .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
          next: () => {
            // Se non ha ID, è solo locale, rimuovilo dal FormArray
            this.obbiettiviPerformance.removeAt(index);
            // Rimuovi anche le strategie associate
            this.strategieOptionsPerObiettivo.splice(index, 1);
            this.savedObbiettivi = structuredClone(this.obbiettiviPerformance?.value ?? []);
            this.toastService.success('Obiettivo eliminato con successo');
          },
          error: (err) => {
            console.error("Errore nell'eliminazione dell'obiettivo:", err);
          },
        });
    } else {
      // Se non ha ID, è solo locale, rimuovilo dal FormArray
      this.obbiettiviPerformance.removeAt(index);
      // Rimuovi anche le strategie associate
      this.strategieOptionsPerObiettivo.splice(index, 1);
      this.savedObbiettivi = structuredClone(this.obbiettiviPerformance?.value ?? []);
    }
    this.handleCloseModalDelete();
  }

  handleAddObiettivo(): void {
    const newObiettivo = this.fb.group({
      id: [null, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
      idSezione22: [this.idSezione22, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
      idOvp: [
        null,
        [Validators.required, Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
      ],
      idStrategiaOvp: [
        null,
        [Validators.required, Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
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
      risorseUmane: [null, [Validators.maxLength(250), Validators.pattern(INPUT_REGEX)]],
      risorseEconomicaFinanziaria: [
        null,
        [Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      risorseStrumentali: [null, [Validators.maxLength(250), Validators.pattern(INPUT_REGEX)]],
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

    const newIndex = this.obbiettiviPerformance.length;
    this.obbiettiviPerformance.push(newObiettivo);

    // Imposta il codice per il nuovo obiettivo
    this.setCodice(newObiettivo, newIndex);

    this.loadOvpOptionsBE();
    this.initializeStrategieForExistingObiettivi();

    // Inizializza le strategie vuote per il nuovo obiettivo
    this.strategieOptionsPerObiettivo[newIndex] = [];

    // Apri l'accordion del nuovo obiettivo
    this.openAccordionIndex = this.obbiettiviPerformance.length;
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

  handleOpenModalDelete(element: any) {
    this.openModalDelete = true;
    this.elementToDelete = element;
  }
  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = null;
  }

  getSelectedStakeholders(obiettivo: FormGroup): LabelValue[] {
    const selectedIds: number[] = obiettivo.get('stakeholders')?.value ?? [];
    return this.stakeholderOptions.filter((opt) => selectedIds.includes(opt.value));
  }
}
