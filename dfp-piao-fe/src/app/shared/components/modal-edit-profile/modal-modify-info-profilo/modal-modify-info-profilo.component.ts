import { TELEFONO_REGEX } from './../../../utils/constants';
import { Component, Input, Output, EventEmitter, inject, OnInit } from '@angular/core';
import { ModalBodyComponent } from '../../../components/modal/modal-body/modal-body.component';
import { SharedModule } from '../../../module/shared/shared.module';
import { TextBoxComponent } from '../../../components/text-box/text-box.component';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProfiloUtenteDTO } from '../../../models/classes/profilo-utente-dto';
import { PIAODTO } from '../../../models/classes/piao-dto';
import { SessionStorageService } from '../../../services/session-storage.service';
import { EMAIL_REGEX, URL_REGEX } from '../../../utils/constants';

@Component({
  selector: 'piao-modal-modify-info-profilo-component',
  imports: [SharedModule, TextBoxComponent, ReactiveFormsModule],
  templateUrl: './modal-modify-info-profilo.component.html',
  styleUrl: './modal-modify-info-profilo.component.scss',
})
export class ModalModifyInfoProfiloComponent extends ModalBodyComponent implements OnInit {
  @Input() open: boolean = false;
  @Input() profiloData: ProfiloUtenteDTO = new ProfiloUtenteDTO();

  @Output() closed = new EventEmitter<void>();
  @Output() saved = new EventEmitter<ProfiloUtenteDTO>();
  @Input() required: boolean = false;
  @Input() piaoDTO!: PIAODTO;
  fb: FormBuilder = inject(FormBuilder);
  sessionStorageService = inject(SessionStorageService);
  main: string = 'main';

  title: string = 'PROFILE.MODAL_PROFILE.TITLE';
  labelQualifica: string = 'PROFILE.MODAL_PROFILE.QUALIFICA';
  labelPhoneNumber: string = 'PROFILE.MODAL_PROFILE.PHONE_NUMBER';
  labelEmail: string = 'PROFILE.MODAL_PROFILE.EMAIL';
  labelSave: string = 'PROFILE.MODAL_PROFILE.SAVE';
  labelAnnulla: string = 'PROFILE.MODAL_PROFILE.ANNULLA';

  ngOnInit(): void {
    this.createForm();
  }

  createForm(): void {
    this.formGroup = this.fb.group({
      qualifica: [this.profiloData?.qualifica || null, [Validators.maxLength(250)]],

      telefono: [
        this.profiloData?.telefono || null,
        [Validators.pattern(TELEFONO_REGEX), Validators.maxLength(30)],
      ],

      email: [
        this.profiloData?.email || null,
        [Validators.pattern(EMAIL_REGEX), Validators.maxLength(250)],
      ],
    });

    if (this.profiloData) {
      this.updateFormValues();
    }
  }

  private updateFormValues(): void {
    this.formGroup.patchValue({
      qualifica: this.profiloData.qualifica || '',
      telefono: this.profiloData.telefono || '',
      email: this.profiloData.email || '',
    });
  }

  get isFormInvalid(): boolean {
    return !this.formGroup || this.formGroup.invalid;
  }

  save(): void {
    if (this.formGroup.invalid) {
      return;
    }

    const datiAggiornati: ProfiloUtenteDTO = {
      qualifica: this.formGroup.get('qualifica')?.value || '',
      telefono: this.formGroup.get('telefono')?.value || '',
      email: this.formGroup.get('email')?.value || '',
    };

    this.saved.emit(datiAggiornati);
    this.closed.emit();
  }

  cancel(): void {
    this.closed.emit();
  }
}
