import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  inject,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SharedModule } from '../../module/shared/shared.module';
import { ModalComponent } from '../modal/modal.component';
import { TextBoxComponent } from '../text-box/text-box.component';
import { IconComponent } from '../icon/icon.component';
import { INPUT_REGEX, SHAPE_ICON, WARNING_ICON } from '../../utils/constants';
import { StakeholderService } from '../../services/stakeholder.service';
import { PrioritaPoliticaService } from '../../services/priorita-politica.service';
import { AreaOrganizzativaService } from '../../services/area-organizzativa.service';
import { ToastService } from '../../services/toast.service';
import { StakeHolderDTO } from '../../models/classes/stakeholder-dto';
import { PrioritaPoliticaDTO } from '../../models/classes/priorita-politica-dto';
import { AreaOrganizzativaDTO } from '../../models/classes/area-organizzativa-dto';
import { CardAlertComponent } from '../../ui/card-alert/card-alert.component';

export type EntityType = 'stakeholder' | 'prioritaPolitica' | 'areaOrganizzativa';

@Component({
  selector: 'piao-modal-add-entity',
  imports: [
    SharedModule,
    ModalComponent,
    TextBoxComponent,
    CardAlertComponent,
    ReactiveFormsModule,
  ],
  templateUrl: './modal-add-entity.component.html',
  styleUrl: './modal-add-entity.component.scss',
})
export class ModalAddEntityComponent implements OnChanges {
  private fb = inject(FormBuilder);
  private stakeholderService = inject(StakeholderService);
  private prioritaPoliticaService = inject(PrioritaPoliticaService);
  private areaOrganizzativaService = inject(AreaOrganizzativaService);
  private toastService = inject(ToastService);

  @Input() open: boolean = false;
  @Input() entityType: EntityType = 'stakeholder';
  @Input() idPiao?: number;
  @Input() idSezione1?: number;

  @Output() closed = new EventEmitter<void>();
  @Output() saved = new EventEmitter<StakeHolderDTO | PrioritaPoliticaDTO | AreaOrganizzativaDTO>();

  formGroup!: FormGroup;
  isSaving: boolean = false;
  icon: string = SHAPE_ICON;

  iconWarning: string = WARNING_ICON;

  // Labels dinamiche in base al tipo di entità
  get modalTitle(): string {
    const titles: Record<EntityType, string> = {
      stakeholder: 'MODAL_ADD_ENTITY.STAKEHOLDER.TITLE',
      prioritaPolitica: 'MODAL_ADD_ENTITY.PRIORITA_POLITICA.TITLE',
      areaOrganizzativa: 'MODAL_ADD_ENTITY.AREA_ORGANIZZATIVA.TITLE',
    };
    return titles[this.entityType];
  }

  get modalSubTitle(): string {
    const subTitles: Record<EntityType, string> = {
      stakeholder: 'MODAL_ADD_ENTITY.STAKEHOLDER.SUB_TITLE',
      prioritaPolitica: 'MODAL_ADD_ENTITY.PRIORITA_POLITICA.SUB_TITLE',
      areaOrganizzativa: 'MODAL_ADD_ENTITY.AREA_ORGANIZZATIVA.SUB_TITLE',
    };
    return subTitles[this.entityType];
  }

  get labelField1(): string {
    const labels: Record<EntityType, string> = {
      stakeholder: 'MODAL_ADD_ENTITY.STAKEHOLDER.FIELD_1',
      prioritaPolitica: 'MODAL_ADD_ENTITY.PRIORITA_POLITICA.FIELD_1',
      areaOrganizzativa: 'MODAL_ADD_ENTITY.AREA_ORGANIZZATIVA.FIELD_1',
    };
    return labels[this.entityType];
  }

  get labelField2(): string {
    const labels: Record<EntityType, string> = {
      stakeholder: 'MODAL_ADD_ENTITY.STAKEHOLDER.FIELD_2',
      prioritaPolitica: 'MODAL_ADD_ENTITY.PRIORITA_POLITICA.FIELD_2',
      areaOrganizzativa: 'MODAL_ADD_ENTITY.AREA_ORGANIZZATIVA.FIELD_2',
    };
    return labels[this.entityType];
  }

  get alertMessage(): string {
    const messages: Record<EntityType, string> = {
      stakeholder: 'MODAL_ADD_ENTITY.STAKEHOLDER.ALERT_MESSAGE',
      prioritaPolitica: 'MODAL_ADD_ENTITY.PRIORITA_POLITICA.ALERT_MESSAGE',
      areaOrganizzativa: 'MODAL_ADD_ENTITY.AREA_ORGANIZZATIVA.ALERT_MESSAGE',
    };
    return messages[this.entityType];
  }

  get isFormInvalid(): boolean {
    return !this.formGroup || this.formGroup.invalid || this.isSaving;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['open'] && changes['open'].currentValue === true) {
      this.initializeForm();
    }
  }

  private initializeForm(): void {
    this.formGroup = this.fb.group({
      field1: [
        null,
        [Validators.required, Validators.maxLength(250), Validators.pattern(INPUT_REGEX)],
      ],
      field2: [null, [Validators.maxLength(500), Validators.pattern(INPUT_REGEX)]],
    });
  }

  handleClose(): void {
    this.formGroup?.reset();
    this.closed.emit();
  }

  handleSave(): void {
    if (this.formGroup.invalid) {
      return;
    }

    this.isSaving = true;
    const formValue = this.formGroup.value;

    switch (this.entityType) {
      case 'stakeholder':
        this.saveStakeholder(formValue);
        break;
      case 'prioritaPolitica':
        this.savePrioritaPolitica(formValue);
        break;
      case 'areaOrganizzativa':
        this.saveAreaOrganizzativa(formValue);
        break;
    }
  }

  private saveStakeholder(formValue: { field1: string; field2: string }): void {
    const stakeholder: StakeHolderDTO = {
      idPiao: this.idPiao,
      nomeStakeHolder: formValue.field1,
      relazionePA: formValue.field2,
    };

    this.stakeholderService.save(stakeholder).subscribe({
      next: (savedEntity) => {
        this.isSaving = false;
        this.toastService.success('Stakeholder aggiunto con successo');
        this.saved.emit(savedEntity);
        this.handleClose();
      },
      error: (err) => {
        this.isSaving = false;
        console.error('Errore nel salvare lo stakeholder:', err);
        this.toastService.error('Errore nel salvare lo stakeholder');
      },
    });
  }

  private savePrioritaPolitica(formValue: { field1: string; field2: string }): void {
    const prioritaPolitica: PrioritaPoliticaDTO = {
      idSezione1: this.idSezione1,
      nomePrioritaPolitica: formValue.field1,
      descrizionePrioritaPolitica: formValue.field2,
    };

    this.prioritaPoliticaService.save(prioritaPolitica).subscribe({
      next: (savedEntity) => {
        this.isSaving = false;
        this.toastService.success('Priorità politica aggiunta con successo');
        this.saved.emit(savedEntity);
        this.handleClose();
      },
      error: (err) => {
        this.isSaving = false;
        console.error('Errore nel salvare la priorità politica:', err);
        this.toastService.error('Errore nel salvare la priorità politica');
      },
    });
  }

  private saveAreaOrganizzativa(formValue: { field1: string; field2: string }): void {
    const areaOrganizzativa: AreaOrganizzativaDTO = {
      idSezione1: this.idSezione1,
      nomeArea: formValue.field1,
      descrizioneArea: formValue.field2,
    };

    this.areaOrganizzativaService.save(areaOrganizzativa).subscribe({
      next: (savedEntity) => {
        this.isSaving = false;
        this.toastService.success('Area organizzativa aggiunta con successo');
        this.saved.emit(savedEntity);
        this.handleClose();
      },
      error: (err) => {
        this.isSaving = false;
        console.error("Errore nel salvare l'area organizzativa:", err);
        this.toastService.error("Errore nel salvare l'area organizzativa");
      },
    });
  }
}
