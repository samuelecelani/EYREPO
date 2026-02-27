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
import { SharedModule } from '../../../../../../module/shared/shared.module';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { AccordionComponent } from '../../../../../../components/accordion/accordion.component';
import { CardInfoComponent } from '../../../../../card-info/card-info.component';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';
import { DropdownComponent } from '../../../../../../components/dropdown/dropdown.component';
import { LabelValue } from '../../../../../../models/interfaces/label-value';
import {
  createFormArrayGenericIndicatoreFromPiaoSession,
  minArrayLength,
} from '../../../../../../utils/utils';
import { INPUT_REGEX, KEY_PIAO } from '../../../../../../utils/constants';
import { IIndicatoreWrapper } from '../../../../../../models/interfaces/indicatore-wrapper';
import { PIAODTO } from '../../../../../../models/classes/piao-dto';
import { SessionStorageService } from '../../../../../../services/session-storage.service';
import { IndicatoriComponent } from '../../indicatori/indicatori.component';
import { SectionEnum } from '../../../../../../models/enums/section.enum';
import { CodTipologiaDimensioneEnum } from '../../../../../../models/enums/cod-tipologia-dimensione.enum';
import { CodTipologiaIndicatoreEnum } from '../../../../../../models/enums/cod-tipologia-indicatore.enum';
import { TextBoxComponent } from '../../../../../../components/text-box/text-box.component';
import { ToastService } from '../../../../../../services/toast.service';
import { MisuraPrevenzioneService } from '../../../../../../services/misura-prevenzione.service';
import { MisuraPrevenzioneEventoRischioService } from '../../../../../../services/sezione23/misura-prevenzione-evento-rischio.service';

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

  @ViewChildren('misuraBody') misuraBodies!: QueryList<ElementRef>;

  /** Indici della misura in cui l'utente sta interagendo */
  editingGestioneIndex: number | null = null;
  editingMisuraIndex: number | null = null;
  /** Flag che indica se l'utente ha interagito con campi di una misura */
  isEditingMisura: boolean = false;
  /** Snapshot JSON del valore della misura al momento del focus (senza codice) */
  private misuraSnapshot: string | null = null;

  private fb: FormBuilder = inject(FormBuilder);

  private sessionStorageService = inject(SessionStorageService);

  private toastService = inject(ToastService);

  private misuraPrevenzioneService = inject(MisuraPrevenzioneService);

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
  openModalDeleteMisura: boolean = false;
  elementToDelete: any = null;

  stakeholderOptions: LabelValue[] = [];

  sectionEnum: SectionEnum = SectionEnum.SEZIONE_2_3;
  codTipologiaFK: CodTipologiaDimensioneEnum = CodTipologiaDimensioneEnum.OBB_2_3;
  codTipologiaIndicatoreFK: CodTipologiaIndicatoreEnum =
    CodTipologiaIndicatoreEnum.MISURA_PREVENZIONE;

  piaoDTO!: PIAODTO;

  misuraPrevezioneEventoRischioService: MisuraPrevenzioneEventoRischioService = inject(
    MisuraPrevenzioneEventoRischioService
  );

  ngOnInit(): void {
    this.piaoDTO = this.sessionStorageService.getItem(KEY_PIAO) as PIAODTO;
    this.loadStakeholderOptions();
  }

  /** Chiamato dal (focusin) nel template per marcare che l'utente sta editando una specifica misura */
  onMisuraFocusIn(gestioneIndex: number, misuraIndex: number): void {
    // Salva lo snapshot solo se cambia la misura attiva
    if (this.editingGestioneIndex !== gestioneIndex || this.editingMisuraIndex !== misuraIndex) {
      this.editingGestioneIndex = gestioneIndex;
      this.editingMisuraIndex = misuraIndex;
      this.misuraSnapshot = this.getMisuraSnapshotJson(gestioneIndex, misuraIndex);
    }
    this.isEditingMisura = true;
  }

  /** Crea un JSON stringify del valore della misura escludendo la property 'codice' */
  private getMisuraSnapshotJson(gestioneIndex: number, misuraIndex: number): string | null {
    const misure = this.getMisuraPrevenzione(gestioneIndex);
    if (!misure || misuraIndex >= misure.length) return null;
    const fg = misure.at(misuraIndex) as FormGroup;
    const value = { ...fg.value };
    delete value.codice;
    return JSON.stringify(value);
  }

  /** Rileva click fuori dalla misura attiva e triggera l'auto-save */
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (
      !this.isEditingMisura ||
      this.editingGestioneIndex === null ||
      this.editingMisuraIndex === null
    )
      return;

    // Ignora se ci sono modal aperti
    if (this.openModalDelete || this.openModalDeleteMisura) return;

    const target = event.target as HTMLElement;

    // Ignora click su elementi rimossi dal DOM (es. menu azioni CDK overlay già chiuso)
    if (!target.isConnected) return;

    // Ignora click dentro CDK overlay (es. menu azioni, popup ancora aperto)
    if (target.closest('.cdk-overlay-container')) return;

    const bodies = this.misuraBodies?.toArray() || [];

    // Trova il body della misura attiva: calcola l'indice flat nella QueryList
    const activeMisuraBody = bodies.find((el) => el.nativeElement.contains(target));

    // Se il click è dentro una qualsiasi misura body, verifica se è la stessa
    if (activeMisuraBody) {
      // Controlla se è la stessa misura tramite data attributes
      const gIdx = activeMisuraBody.nativeElement.getAttribute('data-gestione-index');
      const mIdx = activeMisuraBody.nativeElement.getAttribute('data-misura-index');
      if (Number(gIdx) === this.editingGestioneIndex && Number(mIdx) === this.editingMisuraIndex) {
        return; // Siamo dentro la stessa misura, non fare nulla
      }
    }

    // Click fuori → auto-save solo se il form è cambiato rispetto allo snapshot
    const currentSnapshot = this.getMisuraSnapshotJson(
      this.editingGestioneIndex,
      this.editingMisuraIndex
    );

    if (this.misuraSnapshot !== null && currentSnapshot !== this.misuraSnapshot) {
      this.autoSaveSingolaMisura(this.editingGestioneIndex, this.editingMisuraIndex);
    }
    this.isEditingMisura = false;
    this.editingGestioneIndex = null;
    this.editingMisuraIndex = null;
    this.misuraSnapshot = null;
  }

  /** Salva una singola misura prevenzione se dirty e con id */
  private autoSaveSingolaMisura(gestioneIndex: number, misuraIndex: number): void {
    const misure = this.getMisuraPrevenzione(gestioneIndex);
    if (!misure || misuraIndex >= misure.length) return;

    const fg = misure.at(misuraIndex) as FormGroup;
    if (fg.get('id')?.value) {
      let obj = { ...fg.value };
      obj.idEventoRischio = this.gestioneRischio.at(gestioneIndex).get('idEventoRischioso')?.value;

      // Trasforma stakeholder da array di ID a array di DTO: { stakeholder: { id } }
      const stakeholderIds: number[] = obj.stakeholder || [];
      obj.stakeholder = stakeholderIds.map((id: number) => ({ stakeholder: { id } }));

      this.misuraPrevezioneEventoRischioService.save(obj).subscribe({
        next: () => {
          fg.markAsPristine();
          this.toastService.success('Misura di prevenzione salvata con successo');
        },
        error: (err) => {
          console.error('Errore nel salvataggio automatico della misura di prevenzione:', err);
        },
      });
    }
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
    if (this.gestioneRischioOptions.filter((opt) => !opt.hidden).length === 0) {
      this.toastService.warning(
        'Per aggiungere una gestione del rischio è necessario prima aggiungere e salvare un evento rischioso'
      );
      return;
    }

    const gestioneRischioFormGroup = this.fb.group({
      idEventoRischioso: [null, [Validators.required]],
      misuraPrevenzione: this.fb.array([], [minArrayLength(1)]),
    });

    this.gestioneRischio.push(gestioneRischioFormGroup);
    this.openAccordionIndex = this.gestioneRischio.length;
  }

  handleAddMisurePrevenzione(index: number): void {
    if (this.obbPrev.length === 0) {
      this.toastService.warning(
        'Per aggiungere una misura prevenzione è necessario prima aggiungere e salvare un obiettivo di prevenzione della corruzione e della trasparenza'
      );
      return;
    }

    if (
      this.gestioneRischio.at(index).get('idEventoRischioso')?.value == null ||
      this.gestioneRischio.at(index).get('idEventoRischioso')?.value == undefined
    ) {
      this.toastService.warning(
        'Per aggiungere una misura prevenzione è necessario prima selezionare un evento rischioso'
      );
      return;
    }

    const misuraPrevenzioneFormGroup = this.fb.group({
      id: [null],
      idEventoRischio: [
        this.gestioneRischio.at(index).get('idEventoRischioso')?.value,
        [Validators.required],
      ],
      idObiettivoPrevenzioneCorruzioneTrasparenza: [null, [Validators.required]],
      codice: [null, [Validators.required]],
      denominazione: [null, [Validators.required, Validators.maxLength(250)]],
      descrizione: [null, [Validators.maxLength(100)]],
      responsabile: [null, [Validators.maxLength(100)]],
      stakeholder: [[], []],
      indicatori: (() => {
        const formArray = createFormArrayGenericIndicatoreFromPiaoSession<IIndicatoreWrapper>(
          this.fb,
          [],
          ['id', 'indicatore'],
          INPUT_REGEX
        );
        formArray?.setValidators(minArrayLength(1));
        return formArray;
      })(),
      monitoraggioPrevenzione: this.fb.array([]),
    });

    console.log('Misura prevenzione form group', misuraPrevenzioneFormGroup);
    console.log('Misura prevenzione', this.getMisuraPrevenzione(index));

    this.setCodice(misuraPrevenzioneFormGroup, this.getMisuraPrevenzione(index).length);
    this.misuraPrevezioneEventoRischioService.save(misuraPrevenzioneFormGroup.value).subscribe({
      next: (data: any) => {
        this.toastService.success('Misura di prevenzione aggiunta con successo');
        misuraPrevenzioneFormGroup.get('id')?.setValue(data.id || null); // Simula un ID univoco per la nuova misura di prevenzione
        this.getMisuraPrevenzione(index).push(misuraPrevenzioneFormGroup);
        console.log('Misura prevenzione aggiunta:', misuraPrevenzioneFormGroup.value);
      },
      error: (err) => {
        console.error("Errore nell'aggiunta della misura di prevenzione:", err);
      },
    });
  }

  handleRemoveGestione(index: number): void {
    const idEventoRischioso = this.gestioneRischio.at(index).get('idEventoRischioso')?.value;

    if (idEventoRischioso) {
      this.misuraPrevezioneEventoRischioService
        .deleteAllByIdEventoRischioso(idEventoRischioso)
        .subscribe({
          next: () => {
            this.toastService.success('Gestione del rischio eliminata con successo');
            this.gestioneRischio.removeAt(index);
          },
          error: () => {
            console.log("Errore durante l'eliminazione della gestione del rischio");
          },
        });
    } else {
      this.gestioneRischio.removeAt(index);
      this.toastService.success('Gestione del rischio eliminata con successo');
    }

    this.handleCloseModalDelete();
  }

  handleRemoveMisuraPrevenzione(indexGestione: number, indexMisura: number): void {
    if (this.getMisuraPrevenzione(indexGestione).at(indexMisura).get('id')?.value) {
      this.misuraPrevezioneEventoRischioService
        .delete(this.getMisuraPrevenzione(indexGestione).at(indexMisura).get('id')?.value)
        .subscribe({
          next: () => {
            //this.getMisuraPrevenzione(indexGestione).removeAt(indexMisura);
            this.toastService.success('Misura di prevenzione eliminata con successo');
          },
          error: () => {
            this.toastService.error("Errore durante l'eliminazione della misura di prevenzione");
          },
        });
    } else {
      this.getMisuraPrevenzione(indexGestione).removeAt(indexMisura);
      this.toastService.success('Misura di prevenzione eliminata con successo');
    }
    this.handleCloseModalDeleteMisura();
  }

  handleObbPrev($event: any, indexR: number, indexM: number): void {
    this.getMisuraPrevenzione(indexR)
      .at(indexM)
      .get('idObiettivoPrevenzioneCorruzioneTrasparenza')
      ?.setValue($event);
    this.setCodice(this.getMisuraPrevenzione(indexR).at(indexM) as FormGroup, indexM);
  }

  setCodice(mPrev: FormGroup, index: number): FormControl {
    const idObiettivo = mPrev.get('idObiettivoPrevenzioneCorruzioneTrasparenza')?.value;

    let codice = '';

    // Cerca il codice dell'obiettivo selezionato nel FormArray
    if (idObiettivo != null) {
      const obiettivi = this.formGroup.get(
        'obiettivoPrevenzioneCorruzioneTrasparenza'
      ) as FormArray;
      const obiettivo = obiettivi?.controls.find((ctrl) => ctrl.get('id')?.value === idObiettivo);
      const codiceObiettivo = obiettivo?.get('codice')?.value;
      if (codiceObiettivo) {
        codice = codiceObiettivo;
      }
    }

    const suffix = 'MS';
    codice = codice ? `${codice}_${suffix}${index + 1}` : `${suffix}${index + 1}`;

    mPrev.get('codice')?.setValue(codice);
    return mPrev.get('codice') as FormControl;
  }

  handleOpenModalDelete(index: number) {
    this.openModalDelete = true;
    this.elementToDelete = index;
  }

  handleCloseModalDelete(): void {
    this.openModalDelete = false;
    this.elementToDelete = undefined;
  }

  handleOpenModalDeleteMisura(indexEvento: number, indexMisura: number) {
    this.openModalDeleteMisura = true;
    this.elementToDelete = { indexEvento, indexMisura };
  }

  handleCloseModalDeleteMisura(): void {
    this.openModalDeleteMisura = false;
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
   * Getter che estrae gli eventi rischiosi dal FormArray 'valutazioneRischio'
   * e li mappa come opzioni LabelValue per il dropdown.
   */
  get gestioneRischioOptions(): LabelValue[] {
    const valutazioneRischio = this.formGroup.get('valutazioneRischio') as FormArray;
    if (!valutazioneRischio) return [];

    // Raccoglie gli id degli eventi già assegnati in gestioneRischio
    const usedIds = new Set<number>();
    this.gestioneRischio.controls.forEach((ctrl) => {
      const id = ctrl.get('idEventoRischioso')?.value;
      if (id != null) usedIds.add(id);
    });

    const options: LabelValue[] = [];
    valutazioneRischio.controls.forEach((valutazione) => {
      const eventiRischiosi = valutazione.get('eventiRischiosi') as FormArray;
      if (!eventiRischiosi) return;

      eventiRischiosi.controls.forEach((evento) => {
        const id = evento.get('id')?.value;
        const denominazione = evento.get('denominazione')?.value;
        if (id != null && id !== undefined) {
          options.push({ label: denominazione, value: id, hidden: usedIds.has(id) });
        }
      });
    });

    return options;
  }

  /**
   * Getter che estrae gli obiettivi di prevenzione corruzione e trasparenza
   * dal FormArray 'obiettivoPrevenzioneCorruzioneTrasparenza' e li mappa come opzioni LabelValue.
   */
  get obbPrev(): LabelValue[] {
    const obiettivi = this.formGroup.get('obiettivoPrevenzioneCorruzioneTrasparenza') as FormArray;
    if (!obiettivi) return [];

    return obiettivi.controls
      .filter((ctrl) => ctrl.get('id')?.value != null && ctrl.get('id')?.value != undefined)
      .map((ctrl) => ({
        label: ctrl.get('denominazione')?.value || ctrl.get('codice')?.value || '',
        value: ctrl.get('id')?.value,
      }));
  }
}
