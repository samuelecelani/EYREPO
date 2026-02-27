import {
  Component,
  OnInit,
  OnChanges,
  SimpleChanges,
  inject,
  Input,
  Output,
  EventEmitter,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  FormControl,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { SharedModule } from '../../../../../../../module/shared/shared.module';
import { ModalBodyComponent } from '../../../../../../../components/modal/modal-body/modal-body.component';
import { TextBoxComponent } from '../../../../../../../components/text-box/text-box.component';
import { TextAreaComponent } from '../../../../../../../components/text-area/text-area.component';
import { DropdownComponent } from '../../../../../../../components/dropdown/dropdown.component';
import { IconComponent } from '../../../../../../../components/icon/icon.component';
import { INPUT_REGEX, KEY_PIAO, ONLY_NUMBERS_REGEX } from '../../../../../../../utils/constants';
import { LabelValue } from '../../../../../../../models/interfaces/label-value';
import { AreaOrganizzativaDTO } from '../../../../../../../models/classes/area-organizzativa-dto';
import { PrioritaPoliticaDTO } from '../../../../../../../models/classes/priorita-politica-dto';
import { StakeHolderDTO } from '../../../../../../../models/classes/stakeholder-dto';
import { SvgComponent } from '../../../../../../../components/svg/svg.component';
import { OVPDTO } from '../../../../../../../models/classes/ovp-dto';
import {
  ModalAddEntityComponent,
  EntityType,
} from '../../../../../../../components/modal-add-entity/modal-add-entity.component';
import { CardInfoComponent } from '../../../../../../card-info/card-info.component';
import { CodTipologiaIndicatoreEnum } from '../../../../../../../models/enums/cod-tipologia-indicatore.enum';
import { PIAODTO } from '../../../../../../../models/classes/piao-dto';
import { SessionStorageService } from '../../../../../../../services/session-storage.service';
import { AreaOrganizzativaService } from '../../../../../../../services/area-organizzativa.service';
import { PrioritaPoliticaService } from '../../../../../../../services/priorita-politica.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'piao-modal-ovp',
  imports: [
    SharedModule,
    TextBoxComponent,
    TextAreaComponent,
    DropdownComponent,
    CardInfoComponent,
    ReactiveFormsModule,
    SvgComponent,
    ModalAddEntityComponent,
  ],
  templateUrl: './modal-ovp.component.html',
  styleUrl: './modal-ovp.component.scss',
})
export class ModalOvpComponent extends ModalBodyComponent implements OnInit, OnChanges {
  private fb = inject(FormBuilder);
  private areaOrganizzativaService = inject(AreaOrganizzativaService);
  private prioritaPoliticaService = inject(PrioritaPoliticaService);

  @Input() existingOvpData: OVPDTO[] = [];
  @Input() editingOvpIndex: number | null = null;
  @Input() idSezione1?: number;
  @Input() idSezione21?: number;
  @Input() idPiao?: number;
  @Input() stakeholdersList: StakeHolderDTO[] = [];
  @Input() isModalOpen: boolean = false;
  areeOrganizzativeList: AreaOrganizzativaDTO[] = [];
  prioritaPoliticheList: PrioritaPoliticaDTO[] = [];

  isEditMode: boolean = false;
  ovpId: string = '';
  editingOvpId?: number; // ID del record in modifica (per la PUT/update)

  // Dropdown options
  areeOrganizzativeOptions: LabelValue[] = [];
  prioritaPoliticheOptions: LabelValue[] = [];
  stakeholdersOptions: LabelValue[] = [];

  // Modale per aggiungere nuove entità
  openModalAddEntity: boolean = false;
  currentEntityType: EntityType = 'stakeholder';

  //cardInfoTitle
  cardInfoTitle: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.DETAILS.CARD_INFO_TITLE';

  //cardInfoSubTitle
  cardInfoSubTitle: string =
    'SCRIVANIA_PA.SERVIZI_PIAO.INDICE_PIAO.SEZIONE.SEZIONE_21.OBIETTIVI_VP.OBIETTIVO_VP.DETAILS.CARD_INFO_SUB_TITLE';

  // Output per notificare il componente padre quando viene aggiunta una nuova entità
  @Output() entityAdded = new EventEmitter<{
    type: EntityType;
    entity: AreaOrganizzativaDTO | PrioritaPoliticaDTO | StakeHolderDTO;
  }>();

  main: string = 'main';

  piaoDTO!: PIAODTO;

  sessionStorageSession: SessionStorageService = inject(SessionStorageService);

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageSession.getItem(KEY_PIAO);
    // Inizializza dropdown e form al primo caricamento
    this.isEditMode = this.editingOvpIndex !== null;

    // Carica aree organizzative e priorità politiche dal BE
    if (this.idSezione1) {
      forkJoin({
        aree: this.idSezione1
          ? this.areaOrganizzativaService.getBySezione(this.idSezione1)
          : this.areaOrganizzativaService.getByIdPiao(this.idPiao!),
        priorita: this.idSezione1
          ? this.prioritaPoliticaService.getBySezione(this.idSezione1)
          : this.prioritaPoliticaService.getByIdPiao(this.idPiao!),
      }).subscribe({
        next: ({ aree, priorita }) => {
          this.areeOrganizzativeList = aree || [];
          this.prioritaPoliticheList = priorita || [];
          this.initializeDropdowns();
          this.initializeForm();
        },
        error: () => {
          this.initializeDropdowns();
          this.initializeForm();
        },
      });
    } else {
      // Fallback: inizializza con le liste vuote/passate via @Input
      this.initializeDropdowns();
      this.initializeForm();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Reinizializza quando la modale viene aperta o quando cambiano le liste
    if (changes['isModalOpen'] && changes['isModalOpen'].currentValue === true) {
      this.isEditMode = this.editingOvpIndex !== null;
      // Prima popola le opzioni dei dropdown, poi inizializza il form con i valori
      this.initializeDropdowns();
      this.initializeForm();
    }

    // Aggiorna le opzioni dei dropdown se cambiano le liste (senza reinizializzare il form)
    if (
      changes['areeOrganizzativeList'] ||
      changes['prioritaPoliticheList'] ||
      changes['stakeholdersList']
    ) {
      this.initializeDropdowns();
    }
  }

  /**
   * Resetta il form ai valori iniziali
   */
  resetForm(): void {
    this.isEditMode = false;
    this.editingOvpId = undefined;
    const nextId = this.getNextOvpId();
    this.ovpId = nextId;

    this.formGroup.reset({
      codice: nextId,
      denominazione: null,
      descrizione: null,
      contesto: null,
      ambito: null,
      responsabilePolitico: null,
      responsabileAmministrativo: null,
      areaOrganizzativaSelect: [],
      prioritaPoliticaSelect: [],
      stakeholderSelect: [],
    });
  }

  private initializeForm(): void {
    // Ottieni i dati esistenti se in modalità edit, altrimenti usa valori di default
    const existingOvp =
      this.isEditMode && this.editingOvpIndex !== null
        ? this.existingOvpData[this.editingOvpIndex]
        : null;

    // Imposta codice e ID
    this.ovpId = existingOvp?.codice || this.getNextOvpId();
    this.editingOvpId = existingOvp?.id;

    // Estrai i valori delle select (prendi l'ID dell'entità interna, non l'ID della relazione)
    // ao.id = ID relazione OVP-AreaOrganizzativa, ao.areaOrganizzativa.id = ID dell'AreaOrganizzativa
    const areaOrganizzativaIds =
      existingOvp?.areeOrganizzative
        ?.map((ao) => ao.areaOrganizzativa?.id?.toString())
        .filter(Boolean) || [];
    const prioritaPoliticaIds =
      existingOvp?.prioritaPolitiche
        ?.map((pp) => pp.prioritaPolitica?.id?.toString())
        .filter(Boolean) || [];
    const stakeholderIds =
      existingOvp?.stakeholders?.map((sh) => sh.stakeholder?.id?.toString()).filter(Boolean) || [];

    this.formGroup = this.fb.group({
      id: [this.editingOvpId || null],
      codice: [this.ovpId, [Validators.required]],
      sezione21Id: [this.piaoDTO?.idSezione21 || null],
      denominazione: [
        existingOvp?.denominazione || null,
        [Validators.required, Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      descrizione: [
        existingOvp?.descrizione || null,
        [Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
      ],
      contesto: [
        existingOvp?.contesto || null,
        [Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
      ],
      ambito: [
        existingOvp?.ambito || null,
        [Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      responsabilePolitico: [
        existingOvp?.responsabilePolitico || null,
        [Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      responsabileAmministrativo: [
        existingOvp?.responsabileAmministrativo || null,
        [Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      valoreIndice: [
        existingOvp?.valoreIndice || null,
        [Validators.maxLength(250), Validators.pattern(ONLY_NUMBERS_REGEX)],
      ],
      descrizioneIndice: [
        existingOvp?.descrizioneIndice || null,
        [Validators.maxLength(500), Validators.pattern(INPUT_REGEX)],
      ],
      areaOrganizzativaSelect: [areaOrganizzativaIds],
      prioritaPoliticaSelect: [prioritaPoliticaIds],
      stakeholderSelect: [stakeholderIds],
      ovpStrategias: this.fb.array([]), // Inizializzato vuoto, può essere popolato successivamente
      risorseFinanziarie: this.fb.array([]), // Inizializzato vuoto, può essere popolato successivamente
    });

    // Forza l'aggiornamento dei valori delle select dopo il binding del form (per multi-select)
    setTimeout(() => {
      this.formGroup.patchValue({
        areaOrganizzativaSelect: areaOrganizzativaIds,
        prioritaPoliticaSelect: prioritaPoliticaIds,
        stakeholderSelect: stakeholderIds,
      });
    }, 0);
  }

  private initializeDropdowns(): void {
    // Inizializza le opzioni dei dropdown dalle liste passate
    this.areeOrganizzativeOptions = this.areeOrganizzativeList.map((area) => ({
      label: area.nomeArea || '',
      value: area.id?.toString() || '',
    }));

    this.prioritaPoliticheOptions = this.prioritaPoliticheList.map((priorita) => ({
      label: priorita.nomePrioritaPolitica || '',
      value: priorita.id?.toString() || '',
    }));

    this.stakeholdersOptions = this.stakeholdersList.map((stakeholder) => ({
      label: `${stakeholder.nomeStakeHolder || ''} - ${stakeholder.relazionePA || ''}`,
      value: stakeholder.id?.toString() || '',
    }));
  }

  private getNextOvpId(): string {
    // Estrai tutti gli ID esistenti dall'array di dati
    const existingIds: number[] = [];

    this.existingOvpData.forEach((ovp) => {
      const codice = ovp.codice;
      if (codice && typeof codice === 'string' && codice.startsWith('VP')) {
        const match = codice.match(/VP(\d+)/);
        if (match) {
          const idNum = parseInt(match[1], 10);
          if (!isNaN(idNum)) {
            existingIds.push(idNum);
          }
        }
      }
    });

    // Se non ci sono ID esistenti, inizia da VP1
    if (existingIds.length === 0) {
      return 'VP1';
    }

    // Trova il massimo e incrementa
    const maxId = Math.max(...existingIds);
    return `VP${maxId + 1}`;
  }

  handleAddStakeholder(): void {
    this.currentEntityType = 'stakeholder';
    this.main = 'main-double-modal';
    this.openModalAddEntity = true;
  }

  handleCloseModalAddEntity(): void {
    this.main = 'main';
    this.openModalAddEntity = false;
  }

  handleEntitySaved(
    savedEntity: AreaOrganizzativaDTO | PrioritaPoliticaDTO | StakeHolderDTO
  ): void {
    // Gestisce solo stakeholder (l'unica entità che può essere aggiunta dalla modale OVP)
    if (this.currentEntityType === 'stakeholder') {
      const stakeholder = savedEntity as StakeHolderDTO;
      this.stakeholdersList = [...this.stakeholdersList, stakeholder];
      this.stakeholdersOptions = this.stakeholdersList.map((sh) => ({
        label: `${sh.nomeStakeHolder || ''} - ${sh.relazionePA || ''}`,
        value: sh.id?.toString() || '',
      }));
      // Aggiungi automaticamente il nuovo stakeholder alla selezione multipla
      if (stakeholder.id) {
        const currentSelection = this.formGroup.get('stakeholderSelect')?.value || [];
        const newSelection = [...currentSelection, stakeholder.id.toString()];
        this.formGroup.patchValue({ stakeholderSelect: newSelection });
      }

      // Emetti l'evento per notificare il componente padre
      this.entityAdded.emit({ type: this.currentEntityType, entity: savedEntity });
    }
    this.handleCloseModalAddEntity();
  }

  /**
   * Costruisce il DTO da inviare al backend basandosi sui valori del form
   */
  buildOvpDTO(): OVPDTO {
    const formValue = this.formGroup.value;
    const existingOvp =
      this.isEditMode && this.editingOvpIndex !== null
        ? this.existingOvpData[this.editingOvpIndex]
        : null;

    // Costruisci gli array con tutti gli elementi selezionati (multi-select)
    // Formato: { id?: number (relazione), areaOrganizzativa: AreaOrganizzativaDTO }
    const areeOrganizzative: { id?: number; areaOrganizzativa: AreaOrganizzativaDTO }[] = [];
    const areaOrganizzativaIds: string[] = formValue.areaOrganizzativaSelect || [];
    areaOrganizzativaIds.forEach((areaId: string) => {
      const selectedArea = this.areeOrganizzativeList.find(
        (area) => area.id?.toString() === areaId
      );
      if (selectedArea) {
        // Cerca se esiste già un record di relazione con questa areaOrganizzativa
        const existingRelation = existingOvp?.areeOrganizzative?.find(
          (ao) => ao.areaOrganizzativa?.id?.toString() === areaId
        );
        areeOrganizzative.push({
          id: existingRelation?.id,
          areaOrganizzativa: selectedArea,
        });
      }
    });

    // Formato: { id?: number (relazione), prioritaPolitica: PrioritaPoliticaDTO }
    const prioritaPolitiche: { id?: number; prioritaPolitica: PrioritaPoliticaDTO }[] = [];
    const prioritaPoliticaIds: string[] = formValue.prioritaPoliticaSelect || [];
    prioritaPoliticaIds.forEach((prioritaId: string) => {
      const selectedPriorita = this.prioritaPoliticheList.find(
        (priorita) => priorita.id?.toString() === prioritaId
      );
      if (selectedPriorita) {
        // Cerca se esiste già un record di relazione con questa prioritaPolitica
        const existingRelation = existingOvp?.prioritaPolitiche?.find(
          (pp) => pp.prioritaPolitica?.id?.toString() === prioritaId
        );
        prioritaPolitiche.push({
          id: existingRelation?.id,
          prioritaPolitica: selectedPriorita,
        });
      }
    });

    // Formato: { id?: number (relazione), stakeholder: StakeHolderDTO }
    const stakeholders: { id?: number; stakeholder: StakeHolderDTO }[] = [];
    const stakeholderIds: string[] = formValue.stakeholderSelect || [];
    stakeholderIds.forEach((shId: string) => {
      const selectedStakeholder = this.stakeholdersList.find(
        (stakeholder) => stakeholder.id?.toString() === shId
      );
      if (selectedStakeholder) {
        // Cerca se esiste già un record di relazione con questo stakeholder
        const existingRelation = existingOvp?.stakeholders?.find(
          (sh) => sh.stakeholder?.id?.toString() === shId
        );
        stakeholders.push({
          id: existingRelation?.id,
          stakeholder: selectedStakeholder,
        });
      }
    });

    // Costruisci il DTO finale
    const ovpDTO: OVPDTO = {
      id: this.editingOvpId,
      codice: formValue.codice,
      denominazione: formValue.denominazione,
      descrizione: formValue.descrizione,
      contesto: formValue.contesto,
      ambito: formValue.ambito,
      responsabilePolitico: formValue.responsabilePolitico,
      responsabileAmministrativo: formValue.responsabileAmministrativo,
      sezione21Id: this.idSezione21,
      areeOrganizzative: areeOrganizzative || [],
      prioritaPolitiche: prioritaPolitiche || [],
      stakeholders: stakeholders || [],
      descrizioneIndice: formValue.descrizioneIndice,
      valoreIndice: formValue.valoreIndice,
      ovpStrategias: this.isEditMode
        ? this.existingOvpData[this.editingOvpIndex!]?.ovpStrategias
        : [],
    };

    return ovpDTO;
  }
}
