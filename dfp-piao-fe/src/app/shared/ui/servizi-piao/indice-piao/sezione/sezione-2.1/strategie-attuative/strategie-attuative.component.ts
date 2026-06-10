import { Component, DestroyRef, ElementRef, HostListener, Input, OnInit, QueryList, ViewChildren, inject } from '@angular/core';
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
import { canAddToFormArray, getChangedFields } from '../../../../../../utils/utils';
import { OvpStrategiaAttuativaService } from '../../../../../../services/ovp-strategia-attuativa.service';
import { CodTipologiaIndicatoreEnum } from '../../../../../../models/enums/cod-tipologia-indicatore.enum';
import { CodTipologiaDimensioneEnum } from '../../../../../../models/enums/cod-tipologia-dimensione.enum';
import { SectionEnum } from '../../../../../../models/enums/section.enum';
import { ModalDeleteComponent } from '../../../../../../components/modal-delete/modal-delete.component';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

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
export class StrategieAttuativeComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  @Input() strategieAttuativeControls!: FormArray;
  @Input() indexOVP!: string;
  @Input() idOVP!: number;
  @Input() idPiao!: number;
  @Input() testoSezione!: string;
  @Input() isDettaglio: boolean = false;

  @ViewChildren('strategiaBody') strategiaBodies!: QueryList<ElementRef>;

  /** Indice della strategia in cui l'utente sta interagendo */
  editingStrategiaIndex: number | null = null;
  /** Flag che indica se l'utente ha interagito con campi di una strategia */
  isEditingStrategia: boolean = false;
  /** Snapshot JSON del valore della strategia al momento del focus (senza codStrategia) */
  private strategiaSnapshot: string | null = null;

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
  savedStrategieAttuative: any[] = [];

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

  ngOnInit(): void {
    this.savedStrategieAttuative = structuredClone(this.strategieAttuativeControls?.value ?? []);
  }

  /** Chiamato dal (focusin) nel template per marcare che l'utente sta editando una specifica strategia */
  onStrategiaFocusIn(strategiaIndex: number): void {
    if (this.editingStrategiaIndex !== strategiaIndex) {
      // Se stavamo editando un'altra strategia, auto-save prima di switchare
      if (this.isEditingStrategia && this.editingStrategiaIndex !== null) {
        const currentSnapshot = this.getStrategiaSnapshotJson(this.editingStrategiaIndex);
        if (this.strategiaSnapshot !== null && currentSnapshot !== this.strategiaSnapshot) {
          this.autoSaveSingolaStrategia(this.editingStrategiaIndex);
        }
      }
      this.editingStrategiaIndex = strategiaIndex;
      this.strategiaSnapshot = this.getStrategiaSnapshotJson(strategiaIndex);
    }
    this.isEditingStrategia = true;
  }

  /** Crea un JSON stringify del valore della strategia escludendo la property 'codStrategia' */
  private getStrategiaSnapshotJson(strategiaIndex: number): string | null {
    if (strategiaIndex >= this.strategieAttuative.length) return null;
    const fg = this.strategieAttuative.at(strategiaIndex) as FormGroup;
    const value = { ...fg.value };
    delete value.codStrategia;
    return JSON.stringify(value);
  }

  /** Rileva click fuori dalla strategia attiva e triggera l'auto-save */
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.isEditingStrategia || this.editingStrategiaIndex === null) return;

    // Ignora se ci sono modal aperti
    if (this.openModalDelete) return;

    const target = event.target as HTMLElement;

    // Ignora click su elementi rimossi dal DOM (es. menu azioni CDK overlay già chiuso)
    if (!target.isConnected) return;

    // Ignora click dentro CDK overlay (es. menu azioni, popup ancora aperto)
    if (target.closest('.cdk-overlay-container')) return;

    const bodies = this.strategiaBodies?.toArray() || [];

    const activeStrategiaBody = bodies.find((el) => el.nativeElement.contains(target));

    if (activeStrategiaBody) {
      const sIdx = activeStrategiaBody.nativeElement.getAttribute('data-strategia-index');
      if (Number(sIdx) === this.editingStrategiaIndex) {
        return; // Siamo dentro la stessa strategia, non fare nulla
      }
    }

    // Click fuori → auto-save solo se il form è cambiato rispetto allo snapshot
    const currentSnapshot = this.getStrategiaSnapshotJson(this.editingStrategiaIndex);

    if (this.strategiaSnapshot !== null && currentSnapshot !== this.strategiaSnapshot) {
      this.autoSaveSingolaStrategia(this.editingStrategiaIndex);
    }
    this.isEditingStrategia = false;
    this.editingStrategiaIndex = null;
    this.strategiaSnapshot = null;
  }

  /** Salva una singola strategia attuativa se dirty e con id */
  private autoSaveSingolaStrategia(strategiaIndex: number): void {
    if (strategiaIndex >= this.strategieAttuative.length) return;

    const fg = this.strategieAttuative.at(strategiaIndex) as FormGroup;
    let campiModificati = getChangedFields(
      fg.value,
      this.savedStrategieAttuative?.[strategiaIndex] || undefined,
      ['id', 'indicatori'], // campi da escludere dal confronto
      'ovp.ovpStrategias'
    );

    // Controlla se sono stati eliminati indicatori rispetto al saved
    const savedIndicatori = this.savedStrategieAttuative?.[strategiaIndex]?.indicatori || [];
    const currentIndicatori = fg.value?.indicatori || [];
    if (
      savedIndicatori.length > currentIndicatori.length ||
      savedIndicatori.length < currentIndicatori.length
    ) {
      campiModificati = campiModificati
        ? campiModificati + ',ovp.ovpStrategias.indicatori'
        : 'ovp.ovpStrategias.indicatori';
    }

    const strategiaRequest = {
      ...fg.value,
      idPiao: this.idPiao,
      testoSezione: this.testoSezione,
      campiModificati,
    };
    this.ovpStrategiaAttuativaService.save(this.idOVP, strategiaRequest).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        fg.markAsPristine();
        this.savedStrategieAttuative = structuredClone(
          this.strategieAttuativeControls?.value ?? []
        );
        this.toastService.success('Strategia attuativa salvata con successo');
      },
      error: (err) => {
        console.error('Errore nel salvataggio automatico della strategia attuativa:', err);
      },
    });
  }

  setCodStrategia(control: any, index: number): any {
    control.setValue(this.indexOVP + '_' + 'ST' + index);
    return control;
  }

  handleRemoveStrategia(strategia: any, forceDelete: boolean = false): void {
    if (this.isDettaglio) return;

    if (strategia instanceof FormGroup) {
      strategia = strategia.value;
    }
    const strategiaId = strategia?.id;
    const index = this.strategieAttuativeControls.controls.findIndex(
      (control) => (control as FormGroup).controls['id'].value === strategiaId
    );
    if (strategiaId) {
      this.ovpStrategiaAttuativaService
        .delete(strategiaId, this.idPiao, this.testoSezione, forceDelete)
        .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
          next: () => {
            this.strategieAttuative.removeAt(index);
            this.savedStrategieAttuative = structuredClone(
              this.strategieAttuativeControls?.value ?? []
            );
            this.toastService.success('Strategia attuativa eliminata con successo');
          },
        });
    } else {
      this.strategieAttuative.removeAt(index);
      this.savedStrategieAttuative = structuredClone(this.strategieAttuativeControls?.value ?? []);
      this.toastService.success('Strategia attuativa eliminata con successo');
    }
    this.handleCloseModalDelete();
  }

  handleAddStrategiaAttuativa(): void {
    if (this.isDettaglio) return;

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
    if (this.isDettaglio) return;

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
