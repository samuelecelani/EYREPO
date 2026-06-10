import { PromemoriaService } from './../../../../../../../../services/promemoria-service';
import { Component, DestroyRef, Input, OnChanges, OnInit, SimpleChanges, inject } from '@angular/core';
import { SharedModule } from '../../../../../../../../module/shared/shared.module';
import { TextBoxComponent } from '../../../../../../../../components/text-box/text-box.component';
import { DropdownComponent } from '../../../../../../../../components/dropdown/dropdown.component';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePickerComponent } from '../../../../../../../../components/date-picker/date-picker.component';
import { ModalBodyComponent } from '../../../../../../../../components/modal/modal-body/modal-body.component';
import { LabelValue } from '../../../../../../../../models/interfaces/label-value';
import { PIAODTO } from '../../../../../../../../models/classes/piao-dto';
import { SessionStorageService } from '../../../../../../../../services/session-storage.service';
import { SottofaseMonitoraggioDTO } from '../../../../../../../../models/classes/sotto-fase-monitoraggio-dto';
import { MilestoneDTO } from '../../../../../../../../models/classes/milestone-dto';
import { DATE_REGEX, INPUT_REGEX } from '../../../../../../../../utils/constants';
import { PromemoriaDTO } from '../../../../../../../../models/classes/promemoria-dto';
import { getTodayISO } from '../../../../../../../../utils/utils';
import { min } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'piao-modal-milestone',
  imports: [
    SharedModule,
    TextBoxComponent,
    DropdownComponent,
    DatePickerComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './modal-milestone.component.html',
  styleUrl: './modal-milestone.component.scss',
})
export class ModalMilestoneComponent extends ModalBodyComponent implements OnInit, OnChanges {
  private destroyRef = inject(DestroyRef);
  @Input() piaoDTO!: PIAODTO;
  @Input() dropdownSottofaseMonitoraggio: LabelValue[] = [];
  @Input() promemoriaDropdown: LabelValue[] = [];
  @Input() sottofaseMonitoraggioEdit?: SottofaseMonitoraggioDTO;
  @Input() milestoneEdit?: MilestoneDTO;

  minDate: string = getTodayISO();
  maxDate: string = getTodayISO();

  labelTitle: string = 'SEZIONE_4.MILESTONE.TITLE';
  labelSubTitle: string = 'SEZIONE_4.MILESTONE.SUB_TITLE';
  labelSottoFase: string = 'SEZIONE_4.MILESTONE.SOTTOFASE';
  labelDataMilestone: string = 'SEZIONE_4.MILESTONE.DATA';
  labelDescrizioneMilestone: string = 'SEZIONE_4.MILESTONE.DESCRIZIONE';
  labelCheckboxPromemoria: string = 'SEZIONE_4.MILESTONE.CHECKBOX_PROMEMORIA';
  labelDropdownPromemoria: string = 'SEZIONE_4.MILESTONE.DROPDOWN_PROMEMORIA';
  labelDataPromemoria: string = 'SEZIONE_4.MILESTONE.DATA_PROMEMORIA';

  fb: FormBuilder = inject(FormBuilder);
  sessionStorageService = inject(SessionStorageService);
  main: string = 'main';
  isFormReady: boolean = false;

  constructor() {
    super();
  }

  ngOnInit(): void {
    this.createForm();
    this.setupPromemoriaValidation();
    // Il dropdownSottofaseMonitoraggio viene passato come @Input dalla sezione4
    this.isFormReady = true;
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Reinizializza il form quando cambia milestoneEdit
    if (changes['milestoneEdit'] && this.isFormReady) {
      this.createForm();
    }
  }

  private setupPromemoriaValidation(): void {
    const isPromemoriaControl = this.formGroup.get('isPromemoria');
    const idPromemoriaControl = this.formGroup.get('idPromemoria');
    const dataPromemoriaControl = this.formGroup.get('dataPromemoria');

    if (isPromemoriaControl && idPromemoriaControl && dataPromemoriaControl) {
      isPromemoriaControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((isPromemoria) => {
        if (isPromemoria) {
          idPromemoriaControl.setValidators([Validators.required]);
        } else {
          idPromemoriaControl.clearValidators();
          dataPromemoriaControl.clearValidators();
          dataPromemoriaControl.setValidators([Validators.pattern(DATE_REGEX)]);
        }
        idPromemoriaControl.updateValueAndValidity();
        dataPromemoriaControl.updateValueAndValidity();
      });

      idPromemoriaControl.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((idPromemoria) => {
        if (idPromemoria === 6) {
          dataPromemoriaControl.setValidators([
            Validators.required,
            Validators.pattern(DATE_REGEX),
          ]);
        } else {
          dataPromemoriaControl.clearValidators();
          dataPromemoriaControl.setValidators([Validators.pattern(DATE_REGEX)]);
        }
        dataPromemoriaControl.updateValueAndValidity();
      });
    }
  }

  createForm() {
    this.formGroup = this.fb.group({
      id: [this.milestoneEdit?.id || null],
      idSottofaseMonitoraggio: [
        this.milestoneEdit?.idSottofaseMonitoraggio || null,
        Validators.required,
      ],
      descrizione: [
        this.milestoneEdit?.descrizione || null,
        [Validators.maxLength(50), Validators.required, Validators.pattern(INPUT_REGEX)],
      ],
      data: [
        this.milestoneEdit?.data || null,
        [Validators.required, Validators.pattern(DATE_REGEX)],
      ],
      isPromemoria: [this.milestoneEdit?.isPromemoria || null],
      idPromemoria: [this.milestoneEdit?.idPromemoria || null],
      dataPromemoria: [
        this.milestoneEdit?.dataPromemoria || null,
        [Validators.pattern(DATE_REGEX)],
      ],
    });
  }

  //recupera le date di inizio e fine della sottofase selezionata
  // e le imposta come min e max della data della milestone
  changeMinMaxDate(event: any) {
    const selectedSottofase = this.dropdownSottofaseMonitoraggio.find(
      (option) => option.value === event
    );
    this.minDate = selectedSottofase?.additionalField?.dataInizio || getTodayISO();
    this.maxDate = selectedSottofase?.additionalField?.dataFine || getTodayISO();
  }

  /**
   * Getter che estrae tutte le misure di prevenzione dal FormArray 'gestioneRischio'
   * e le mappa come opzioni LabelValue per il dropdown.
   */
  get dropdownMisuraMonitoraggio(): LabelValue[] {
    const misuraMonitoraggio = this.formGroup?.get('gestioneRischio') as FormArray;
    if (!misuraMonitoraggio) return [];

    const options: LabelValue[] = [];

    return options;
  }

  // Getter sicuri per i controlli del form
  get idSottofaseMonitoraggioControl() {
    return this.formGroup?.get('idSottofaseMonitoraggio');
  }

  get dataControl() {
    return this.formGroup?.get('data');
  }

  get descrizioneControl() {
    return this.formGroup?.get('descrizione');
  }

  get isPromemoriaControl() {
    return this.formGroup?.get('isPromemoria');
  }

  get idPromemoriaControl() {
    return this.formGroup?.get('idPromemoria');
  }

  get dataPromemoriaControl() {
    return this.formGroup?.get('dataPromemoria');
  }

  /**
   * Resetta il form ai valori iniziali
   */
  resetForm(): void {
    if (this.formGroup) {
      this.formGroup.reset();
    }
  }
}
