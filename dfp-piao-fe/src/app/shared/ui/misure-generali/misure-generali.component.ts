import {
  Component,
  ElementRef,
  HostListener,
  inject,
  Input,
  OnInit,
  QueryList,
  ViewChildren,
} from '@angular/core';
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
  minArrayLength,
} from '../../utils/utils';
import { TipologiaObbiettivo } from '../../models/enums/tipologia-obbiettivo.enum';
import { CodTipologiaIndicatoreEnum } from '../../models/enums/cod-tipologia-indicatore.enum';
import { DropdownComponent } from '../../components/dropdown/dropdown.component';
import { LabelValue } from '../../models/interfaces/label-value';
import { SessionStorageService } from '../../services/session-storage.service';
import { PIAODTO } from '../../models/classes/piao-dto';
import { OVPDTO } from '../../models/classes/ovp-dto';
import { OVPStrategiaDTO } from '../../models/classes/ovp-strategia-dto';
import { ContributoreInternoDTO } from '../../models/classes/contributore-interno-dto';
import { DynamicTBConfig } from '../../models/classes/config/dynamic-tb';
import { DynamicTBComponent } from '../../components/dynamic-tb/dynamic-tb.component';
import { CodTipologiaDimensioneEnum } from '../../models/enums/cod-tipologia-dimensione.enum';
import { SectionEnum } from '../../models/enums/section.enum';
import { Sezione23Service } from '../../services/sezione23.service';
import { MisuraGeneraleDTO } from '../../models/classes/misura-generale-dto';
import { MisuraGeneraleIndicatoreDTO } from '../../models/classes/misura-generale-indicatore-dto';
import { MisurePrevenzioneService } from '../../services/misure-prevenzione.service';
import { ModalDeleteComponent } from '../../components/modal-delete/modal-delete.component';

@Component({
  selector: 'piao-misure-generali',
  standalone: true,
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
  templateUrl: './misure-generali.component.html',
  styleUrl: './misure-generali.component.scss',
})
export class MisureGeneraliComponent implements OnInit {
  @Input() formGroup!: FormGroup;
  @Input() idSezione23!: number;
  @Input() idPiao!: number;
  @Input() codTipologiaIndicatoreFK: string = CodTipologiaIndicatoreEnum.MISURA_GENERALE;
  @Input() testoSezione!: string;
  @Input() isDettaglio: boolean = false;

  @ViewChildren('misuraBody') misuraBodies!: QueryList<ElementRef>;

  /** Indice della misura in cui l'utente sta interagendo */
  editingMisuraIndex: number | null = null;
  /** Flag che indica se l'utente ha interagito con campi di una misura */
  isEditingMisura: boolean = false;
  /** Snapshot JSON del valore della misura al momento del focus (senza codice) */
  private misuraSnapshot: string | null = null;

  labelMisure: string = 'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.TITLE';
  subTitleMisura: string = 'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.MISURA.SUB_TITLE';
  titleCardInfoMisura: string = 'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.MISURA.CARD_TITLE';
  titleCardInfoNotFoundMisura: string =
    'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.CARD_INFO_NOT_FOUND';
  subTitleAddMisura: string = 'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.MISURA.BTN_ADD';

  labelIdMisura: string = 'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.MISURA.DETAILS.ID_MISURA';
  labelObiettivoPrevenzione: string =
    'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.MISURA.DETAILS.OBIETTIVO_PREVENZIONE';
  labelMisuraNotFound: string =
    'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.MISURA.DETAILS.MISURA_NOT_FOUND';
  labelMisuraGenerale: string =
    'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.MISURA.DETAILS.MISURA_GENERALE';
  labelTipologiaDescrizione: string =
    'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.MISURA.DETAILS.TIPOLOGIA_DESCRIZIONE';
  labelResponsabileMisura: string =
    'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.MISURA.DETAILS.RESPONSABILE_MISURA';
  labelStakeholder: string = 'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.MISURA.DETAILS.STAKEHOLDER';
  messageNotFoundStakeholder =
    'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.MISURA.DETAILS.NOT_FOUND_STAKEHOLDER';
  labelSelezionaStakeholder: string =
    'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.MISURA.DETAILS.SELEZIONA_STAKEHOLDER';
  labelMisuraPrefix: string = 'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.MISURA.MISURA_PREFIX';
  labelMisuraPrefixDettaglio: string =
    'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.MISURA.MISURA_PREFIX_DETTAGLIO';
  labelIntroText: string = 'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.MISURA.INTRO_TEXT';
  labelIntroTextDettaglio: string =
    'SEZIONE_23.MISURE_GENERALI_PREVENZIONE.MISURA.INTRO_TEXT_DETTAGLIO';

  contributoreConfig!: DynamicTBConfig;

  private fb = inject(FormBuilder);
  private sessionStorageService = inject(SessionStorageService);
  private sezione23Service = inject(Sezione23Service);
  misurePrevenzioneService = inject(MisurePrevenzioneService);
  toastService = inject(ToastService);

  codTipologiaFK: string = CodTipologiaDimensioneEnum.OBB_2_3;
  sectionEnum: string = SectionEnum.SEZIONE_2_3;

  // Dropdown options
  stakeholderOptions: LabelValue[] = [];

  // Gestione stato accordion
  openAccordionIndex: number | null = null;

  piaoDTO!: PIAODTO;

  openModalDelete: boolean = false;
  elementToDelete: any = null;
  savedMisure: any[] = [];

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);

    this.savedMisure = structuredClone(this.misureGenerali?.value ?? []);

    this.initContributoreConfig();
    this.loadStakeholderOptions();
  }

  /** Chiamato dal (focusin) nel template per marcare che l'utente sta editando una specifica misura */
  onMisuraFocusIn(misuraIndex: number): void {
    if (this.editingMisuraIndex !== misuraIndex) {
      // Se stavamo editando un'altra misura, auto-save prima di switchare
      if (this.isEditingMisura && this.editingMisuraIndex !== null) {
        const currentSnapshot = this.getMisuraSnapshotJson(this.editingMisuraIndex);
        if (this.misuraSnapshot !== null && currentSnapshot !== this.misuraSnapshot) {
          this.autoSaveSingolaMisura(this.editingMisuraIndex);
        }
      }
      this.editingMisuraIndex = misuraIndex;
      this.misuraSnapshot = this.getMisuraSnapshotJson(misuraIndex);
    }
    this.isEditingMisura = true;
  }

  /** Crea un JSON stringify del valore della misura escludendo la property 'codice' */
  private getMisuraSnapshotJson(misuraIndex: number): string | null {
    if (misuraIndex >= this.misureGenerali.length) return null;
    const fg = this.misureGenerali.at(misuraIndex) as FormGroup;
    const value = { ...fg.value };
    delete value.codice;
    return JSON.stringify(value);
  }

  /** Rileva click fuori dalla misura attiva e triggera l'auto-save */
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.isEditingMisura || this.editingMisuraIndex === null) return;

    // Ignora se ci sono modal aperti
    if (this.openModalDelete) return;

    const target = event.target as HTMLElement;

    // Ignora click su elementi rimossi dal DOM (es. menu azioni CDK overlay già chiuso)
    if (!target.isConnected) return;

    // Ignora click dentro CDK overlay (es. menu azioni, popup ancora aperto)
    if (target.closest('.cdk-overlay-container')) return;

    const bodies = this.misuraBodies?.toArray() || [];

    const activeMisuraBody = bodies.find((el) => el.nativeElement.contains(target));

    if (activeMisuraBody) {
      const mIdx = activeMisuraBody.nativeElement.getAttribute('data-misura-index');
      if (Number(mIdx) === this.editingMisuraIndex) {
        return; // Siamo dentro la stessa misura, non fare nulla
      }
    }

    // Click fuori → auto-save solo se il form è cambiato rispetto allo snapshot
    const currentSnapshot = this.getMisuraSnapshotJson(this.editingMisuraIndex);

    if (this.misuraSnapshot !== null && currentSnapshot !== this.misuraSnapshot) {
      this.autoSaveSingolaMisura(this.editingMisuraIndex);
    }
    this.isEditingMisura = false;
    this.editingMisuraIndex = null;
    this.misuraSnapshot = null;
  }

  /** Salva una singola misura generale se dirty e con id */
  private autoSaveSingolaMisura(misuraIndex: number): void {
    if (misuraIndex >= this.misureGenerali.length) return;

    const fg = this.misureGenerali.at(misuraIndex) as FormGroup;
    const obj = { ...fg.value };
    // Confronta PRIMA della trasformazione stakeholders, così entrambi i lati sono array di ID piatti
    let campiModificati = getChangedFields(
      obj,
      this.savedMisure?.[misuraIndex],
      ['id', 'indicatori', 'key', 'externalId', 'idSezione23'], // campi da escludere dal confronto
      `misuraPrevenzione`
    );

    // Trasforma stakeholders da array di ID a array di DTO: { stakeholder: { id } }
    const stakeholderIds: number[] = obj.stakeholder || [];
    obj.stakeholder = stakeholderIds.map((id: number) => ({ stakeholder: { id: id } }));

    // Controlla se sono stati eliminati indicatori rispetto al saved
    const savedIndicatori = this.savedMisure?.[misuraIndex]?.indicatori || [];
    const currentIndicatori = fg.value?.indicatori || [];
    if (
      savedIndicatori.length > currentIndicatori.length ||
      savedIndicatori.length < currentIndicatori.length
    ) {
      campiModificati = campiModificati
        ? campiModificati + `,misuraPrevenzione.indicatori`
        : `misuraPrevenzione.indicatori`;
    }

    const obiettiviRequest = {
      ...obj,
      idPiao: this.piaoDTO.id,
      testoSezione: this.testoSezione,
      campiModificati: campiModificati,
    };

    this.misurePrevenzioneService.save(obiettiviRequest).subscribe({
      next: () => {
        fg.markAsPristine();
        this.savedMisure = structuredClone(this.misureGenerali?.value ?? []);
        this.toastService.success('Misura generale salvata con successo');
      },
      error: (err) => {
        console.error('Errore nel salvataggio automatico della misura generale:', err);
      },
    });
  }

  handleObiettivoChange(idObiettivo: number | null, index: number): void {
    const misura = this.misureGenerali.at(index) as FormGroup;
    misura.get('idObiettivoPrevenzione')?.setValue(idObiettivo);
    // Ricalcola il codice quando cambia l'obiettivo
    this.setCodice(misura, index);
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

  getSelectedStakeholders(misura: FormGroup): LabelValue[] {
    const selectedValues: any[] = misura.controls['stakeholder'].value || [];
    return this.stakeholderOptions.filter((opt) => selectedValues.includes(opt.value));
  }

  setCodice(misura: FormGroup, index: number): FormControl {
    const idObiettivoPrevenzione = misura.get('idObiettivoPrevenzione')?.value;

    let codice = '';
    const prefix = 'MG';

    // Recupera il codice dell'obiettivo di prevenzione se selezionato
    if (idObiettivoPrevenzione) {
      // Cerca l'obiettivo di prevenzione nel form parent
      const parentForm = this.formGroup;
      if (parentForm) {
        const obiettiviFormArray = parentForm.get('obiettivoPrevenzione') as FormArray;
        if (obiettiviFormArray) {
          const obiettivo = obiettiviFormArray.controls.find(
            (control) => control.get('id')?.value === idObiettivoPrevenzione
          );
          if (obiettivo?.get('codice')?.value) {
            codice = obiettivo.get('codice')?.value;
          }
        }
      }
    }

    // Genera il codice finale
    codice = codice ? `${codice}_${prefix}${index + 1}` : `${prefix}${index + 1}`;

    misura.get('codice')?.setValue(codice);
    return misura.get('codice') as FormControl;
  }

  handleRemoveMisura(index: number): void {
    const misura = this.misureGenerali.at(index);
    const misuraId = misura?.get('id')?.value;

    // Se la misura ha un ID, significa che è stata salvata sul backend
    if (misuraId) {
      this.misurePrevenzioneService.delete(misuraId).subscribe({
        next: () => {
          this.toastService.success('Misura eliminata con successo');
          this.misureGenerali.removeAt(index);
          this.savedMisure = structuredClone(this.misureGenerali?.value ?? []);
        },
        error: () => {
          console.log("Errore nell'eliminazione della misura");
        },
      });
    } else {
      // Se non ha ID, è solo locale, rimuovila dal FormArray
      this.misureGenerali.removeAt(index);
      this.savedMisure = structuredClone(this.misureGenerali?.value ?? []);
      this.toastService.success('Misura eliminata con successo');
    }

    this.handleCloseModalDelete();
  }

  handleAddMisura(): void {
    const newMisura = this.fb.group({
      id: [null, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
      idSezione23: [this.idSezione23, [Validators.maxLength(20), Validators.pattern(INPUT_REGEX)]],
      idObiettivoPrevenzione: [
        null,
        [Validators.required, Validators.maxLength(20), Validators.pattern(INPUT_REGEX)],
      ],
      codice: [
        null,
        [Validators.maxLength(50), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
      denominazione: [
        null,
        [Validators.maxLength(250), Validators.pattern(INPUT_REGEX), Validators.required],
      ],
      descrizione: [null, [Validators.maxLength(500), Validators.pattern(INPUT_REGEX)]],
      responsabileMisura: [null, [Validators.maxLength(250), Validators.pattern(INPUT_REGEX)]],
      stakeholder: [[], []],
      indicatori: (() => {
        const formArray =
          createFormArrayGenericIndicatoreFromPiaoSession<MisuraGeneraleIndicatoreDTO>(
            this.fb,
            [],
            ['id', 'indicatore'],
            INPUT_REGEX
          );
        formArray?.setValidators([Validators.required, minArrayLength(1)]);
        return formArray;
      })(),
    });

    const newIndex = this.misureGenerali.length;
    this.misureGenerali.push(newMisura);

    // Imposta il codice per la nuova misura
    this.setCodice(newMisura, newIndex);

    // Apri l'accordion della nuova misura
    this.openAccordionIndex = this.misureGenerali.length;
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

  get misureGenerali(): FormArray {
    const formArray = this.formGroup?.get('misuraPrevenzione') as FormArray;
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

  trackByMisuraId(index: number, item: any): any {
    // Usa l'ID della misura se disponibile, altrimenti usa l'indice
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

  get obiettiviPrevenzioneOptions(): LabelValue[] {
    const parentForm = this.formGroup;
    if (!parentForm) return [];

    const obiettiviFormArray = parentForm.get('obiettivoPrevenzione') as FormArray;
    if (!obiettiviFormArray) return [];

    return obiettiviFormArray.controls
      .filter((control) => control.get('id')?.value)
      .map((control, index) => ({
        label: control.get('denominazione')?.value || control.get('codice')?.value || '',
        value: control.get('id')?.value || index,
      }));
  }
}
