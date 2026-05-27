import { Component, OnInit, computed, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SharedModule } from '../../../../shared/module/shared/shared.module';
import { ButtonComponent } from '../../../../shared/components/button/button.component';
import { TextBoxComponent } from '../../../../shared/components/text-box/text-box.component';
import { TextAreaComponent } from '../../../../shared/components/text-area/text-area.component';
import { DropdownComponent } from '../../../../shared/components/dropdown/dropdown.component';
import { DatePickerComponent } from '../../../../shared/components/date-picker/date-picker.component';
import {
  AutocompleteOption,
  AutocompleteTextBoxComponent,
} from '../../../../shared/components/autocomplete-text-box/autocomplete-text-box.component';
import { LabelValue } from '../../../../shared/models/interfaces/label-value';
import { AlertsService } from '../../../../shared/services/alerts.service';
import { AlertsDTO } from '../../../../shared/models/classes/alerts-dto';
import {
  AvvisiUiStateService,
  IAvvisoAttachment,
} from '../../../../shared/services/avvisi-ui-state.service';
import { AmministrazioneInternalDTO } from '../../../../shared/models/classes/amministrazione-internal-dto';
import { AmministrazioneService } from '../../../../shared/services/amministrazione.service';
import { GestionePiaoService } from '../../../../shared/services/gestione-piao.service';
import { ToastService } from '../../../../shared/services/toast.service';
import { SectionEnum } from '../../../../shared/models/enums/section.enum';
import { CodTipologiaAllegatoEnum } from '../../../../shared/models/enums/cod-tipologia-allegato.enum';
import { AttachmentComponent } from '../../../../shared/ui/attachment/attachment.component';

@Component({
  selector: 'piao-avviso-form',
  imports: [
    SharedModule,
    ReactiveFormsModule,
    ButtonComponent,
    TextBoxComponent,
    TextAreaComponent,
    DropdownComponent,
    DatePickerComponent,
    AutocompleteTextBoxComponent,
    AttachmentComponent,
  ],
  templateUrl: './avviso-form.component.html',
  styleUrl: './avviso-form.component.scss',
})
export class AvvisoFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly alertsService = inject(AlertsService);
  private readonly amministrazioneService = inject(AmministrazioneService);
  private readonly gestionePiaoService = inject(GestionePiaoService);
  private readonly toastService = inject(ToastService);

  readonly isSubmitting = signal(false);
  readonly loading = signal(false);
  readonly attachments = signal<IAvvisoAttachment[]>([]);
  readonly isDetails = signal<boolean>(false);

  private currentAvviso: AlertsDTO | null = null;
  avvisoId: string | null = null;
  avvisoIdAttachment: number | null = null;

  codTipologia: SectionEnum = SectionEnum.AVVISI_DFP;
  codTipologiaAllegato: CodTipologiaAllegatoEnum = CodTipologiaAllegatoEnum.AVVISI_DFP;
  sezioneSection: SectionEnum = SectionEnum.AVVISI_DFP;

  readonly contentTypeOptions: LabelValue[] = [
    { value: 'AVVISO', label: 'Avviso' },
    { value: 'COMUNICAZIONE', label: 'Comunicazione' },
  ];

  administrationTypeOptions: LabelValue[] = [];

  // ----- Autocomplete state -----
  amministrazioneSuggestions = signal<AutocompleteOption<AmministrazioneInternalDTO>[]>([]);
  amministrazioneLoading = signal<boolean>(false);
  /** Etichetta dell'amministrazione effettivamente selezionata dal popup di suggerimenti. */
  private selectedAmministrazioneLabel: string | null = null;

  private amministrazioneSelectedValidator = (
    control: AbstractControl
  ): ValidationErrors | null => {
    const value = (control.value ?? '').toString().trim();
    if (!value) {
      return null;
    }
    return value === (this.selectedAmministrazioneLabel ?? '').trim()
      ? null
      : { notSelected: true };
  };

  ngOnInit(): void {
    this.avvisoId = this.route.snapshot.paramMap.get('id');
    this.isDetails.set(this.route.snapshot.data?.['isDetails'] === true);

    this.amministrazioneService.getTipologieAmministrazioni().subscribe({
      next: (tipologie) => {
        this.administrationTypeOptions = tipologie.map((t) => ({ label: t, value: t }));
      },
    });

    this.tipologiaAmministrazioneControl.valueChanges.subscribe(() => {
      this.selectedAmministrazioneLabel = null;
      this.amministrazioneControl.setValue('', { emitEvent: false });
      this.amministrazioneControl.updateValueAndValidity();
      this.amministrazioneSuggestions.set([]);
    });

    // Quando l'utente digita liberamente nel campo amministrazione, invalida la selezione precedente
    this.amministrazioneControl.valueChanges.subscribe((value: string) => {
      if (
        this.selectedAmministrazioneLabel !== null &&
        (value ?? '').toString().trim() !== this.selectedAmministrazioneLabel.trim()
      ) {
        this.selectedAmministrazioneLabel = null;
        this.tipologiaAmministrazioneControl.setValue('', { emitEvent: false });
      }
    });

    if (this.avvisoId) {
      this.loading.set(true);
      this.alertsService.getByIdData(this.avvisoId).subscribe({
        next: (avviso) => {
          this.currentAvviso = avviso;
          if (avviso) {
            // L'amministrazione caricata da BE è considerata già selezionata
            this.selectedAmministrazioneLabel = avviso.amministrazione || null;
            this.form.patchValue({
              id: avviso.id || -1,
              tipologiaContenuto: avviso.tipologiaContenuto || '',
              dataPubblicazione: this.toIsoDate(
                avviso.dataPubblicazione || avviso.createdTs?.toString()
              ),
              oggetto: avviso.oggetto || '',
              tipologiaAmministrazione: avviso.tipologiaAmministrazione || '',
              amministrazione: avviso.amministrazione || '',
              messaggio: avviso.messaggio || '',
            });
            this.amministrazioneControl.updateValueAndValidity();
            this.avvisoIdAttachment = avviso.id || null;
          }
          this.loading.set(false);
        },
        error: (error) => {
          console.error('Load avviso failed', error);
          this.loading.set(false);
        },
      });
    } else {
      // Nuovo avviso: inizializza con una create vuota per ottenere l'id
      this.alertsService.create({} as AlertsDTO).subscribe({
        next: (response) => {
          const newId = response.data?.id;
          this.avvisoIdAttachment = newId ? newId : null;
          if (newId) {
            this.form.patchValue({ id: newId });
          }
        },
        error: (error) => {
          console.error('Create avviso failed', error);
        },
      });
    }
  }

  private toAmministrazioneOption(
    a: AmministrazioneInternalDTO
  ): AutocompleteOption<AmministrazioneInternalDTO> {
    return {
      label: a.denominazioneEnte || '',
      sublabel: a.codiceIPA || '',
      value: a,
    };
  }

  onAmministrazioneSearch(query: string): void {
    const tipologia = this.tipologiaAmministrazioneControl.value || undefined;
    this.amministrazioneLoading.set(true);
    this.gestionePiaoService
      .getAmministrazioneByTipologiaAndDenomAndCode(tipologia, query)
      .subscribe({
        next: (list) => {
          const unique = this.dedupAmministrazioni(list);
          this.amministrazioneSuggestions.set(unique.map((a) => this.toAmministrazioneOption(a)));
          this.amministrazioneLoading.set(false);
        },
        error: () => this.amministrazioneLoading.set(false),
      });
  }

  private dedupAmministrazioni(list: AmministrazioneInternalDTO[]): AmministrazioneInternalDTO[] {
    const seen = new Set<string>();
    return list.filter((a) => {
      const key = `${a.codiceIPA ?? ''}|${a.denominazioneEnte ?? ''}`;
      if (seen.has(key)) return false;
      seen.add(key);
      return true;
    });
  }

  clearAmministrazioneSuggestions(): void {
    this.amministrazioneSuggestions.set([]);
  }

  onAmministrazioneSelected(opt: AutocompleteOption<AmministrazioneInternalDTO>): void {
    const a = opt.value;
    const label = a.denominazioneEnte || '';
    this.selectedAmministrazioneLabel = label;
    this.amministrazioneControl.setValue(label, { emitEvent: false });
    if (a.tipologiaIstat) {
      this.tipologiaAmministrazioneControl.setValue(a.tipologiaIstat, { emitEvent: false });
    }
    this.amministrazioneControl.updateValueAndValidity();
    this.amministrazioneSuggestions.set([]);
  }

  readonly pageTitle = computed(() =>
    this.avvisoId
      ? 'Modifica un avviso o una comunicazione'
      : 'Pubblica un avviso o una comunicazione'
  );

  readonly form = this.fb.nonNullable.group({
    id: [-1],
    tipologiaContenuto: ['', Validators.required],
    dataPubblicazione: [{ value: this.getTodayIso(), disabled: false }, Validators.required],
    oggetto: ['', [Validators.required, Validators.maxLength(255)]],
    tipologiaAmministrazione: ['', Validators.required],
    amministrazione: [
      '',
      [Validators.required, Validators.maxLength(255), this.amministrazioneSelectedValidator],
    ],
    messaggio: ['', [Validators.required, Validators.maxLength(2000)]],
  });

  private getTodayIso(): string {
    return new Date().toISOString().split('T')[0];
  }

  get tipologiaContenutoControl(): FormControl {
    return this.form.controls.tipologiaContenuto as unknown as FormControl;
  }
  get dataPubblicazioneControl(): FormControl {
    return this.form.controls.dataPubblicazione as unknown as FormControl;
  }
  get oggettoControl(): FormControl {
    return this.form.controls.oggetto as unknown as FormControl;
  }
  get tipologiaAmministrazioneControl(): FormControl {
    return this.form.controls.tipologiaAmministrazione as unknown as FormControl;
  }
  get amministrazioneControl(): FormControl {
    return this.form.controls.amministrazione as unknown as FormControl;
  }
  get messaggioControl(): FormControl {
    return this.form.controls.messaggio as unknown as FormControl;
  }
  get messageLength(): number {
    return this.form.controls.messaggio.value.length;
  }

  handleSubmit(state: string): void {
    if (this.form.invalid || this.isSubmitting()) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);

    const payload: AlertsDTO = {
      ...(this.currentAvviso || {}),
      validity: this.currentAvviso?.validity ?? true,
      active: this.currentAvviso?.active ?? true,
      id: Number(this.form.getRawValue().id),
      tipologiaContenuto: this.form.getRawValue().tipologiaContenuto,
      dataPubblicazione: this.form.getRawValue().dataPubblicazione,
      oggetto: this.form.getRawValue().oggetto,
      tipologiaAmministrazione: this.form.getRawValue().tipologiaAmministrazione,
      amministrazione: this.form.getRawValue().amministrazione,
      messaggio: this.form.getRawValue().messaggio,
      stato: state,
    };

    const save$ = this.alertsService.update(payload.id || -1, payload);

    save$.subscribe({
      next: (response) => {
        this.toastService.success(
          this.avvisoId ? 'Avviso aggiornato con successo' : 'Avviso creato con successo'
        );
        const savedId = response.data?.id;
        if (savedId) {
          this.router.navigate(['/avvisi/dettaglio-avviso', savedId]);
        }
      },
      error: (error) => {
        console.error('Save avviso failed', error);
        this.isSubmitting.set(false);
      },
      complete: () => {
        this.isSubmitting.set(false);
      },
    });
  }

  private toIsoDate(value?: string): string {
    if (!value) {
      return '';
    }

    const parsedDate = new Date(value);
    if (Number.isNaN(parsedDate.getTime())) {
      return value;
    }

    return parsedDate.toISOString().split('T')[0];
  }
}
