import { Component, inject, OnInit, AfterViewInit, ViewChild } from '@angular/core';
import { BaseComponent } from '../../../shared/components/base/base.component';
import { SharedModule } from '../../../shared/module/shared/shared.module';
import { TextBoxComponent } from '../../../shared/components/text-box/text-box.component';
import { TextAreaComponent } from '../../../shared/components/text-area/text-area.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { LabelValue } from '../../../shared/models/interfaces/label-value';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
  AbstractControl,
  ValidationErrors,
  ValidatorFn,
} from '@angular/forms';
import { AttachmentComponent } from 'src/app/shared/ui/attachment/attachment.component';
import { EMAIL_REGEX, INPUT_REGEX } from '../../../shared/utils/constants';
import { CodTipologiaAllegatoEnum } from '../../../shared/models/enums/cod-tipologia-allegato.enum';
import { SectionEnum } from '../../../shared/models/enums/section.enum';
import { ButtonListComponent } from 'src/app/shared/components/button-list/button-list.component';
import { TicketService } from '../../../shared/services/ticket.service';
import { TicketDTO } from '../../../shared/models/classes/ticket-dto';
import { AllegatoTicketDTO } from '../../../shared/models/classes/allegato-ticket-dto';
import { takeUntil } from 'rxjs';

@Component({
  selector: 'piao-help-desk',
  imports: [
    SharedModule,
    TextBoxComponent,
    TextAreaComponent,
    ButtonComponent,
    ReactiveFormsModule,
    AttachmentComponent,
    ButtonListComponent,
  ],
  templateUrl: './help-desk.component.html',
  styleUrl: './help-desk.component.scss',
})
export class HelpDeskComponent extends BaseComponent implements OnInit, AfterViewInit {
  title: string = 'HELP_DESK.TITLE';
  textboxNome: string = 'HELP_DESK.TEXTBOX_NOME';
  textboxCognome: string = 'HELP_DESK.TEXTBOX_COGNOME';
  textboxEmail: string = 'HELP_DESK.TEXTBOX_EMAIL';
  textboxVerificaEmail: string = 'HELP_DESK.TEXTBOX_VERIFICA_EMAIL';
  textareaOggetto: string = 'HELP_DESK.TEXTAREA_OGGETTO';
  textareaMessaggio: string = 'HELP_DESK.TEXTAREA_MESSAGGIO';
  problemiList: string = 'HELP_DESK.PROBLEMI';
  buttonSegnalazione: string = 'HELP_DESK.BUTTON_SEGNALAZIONE';

  isFormReady = false;

  codTipologia: SectionEnum = SectionEnum.HELP_DESK;
  codTipologiaAllegato: CodTipologiaAllegatoEnum = CodTipologiaAllegatoEnum.HELP_DESK;

  private fb = inject(FormBuilder);
  private ticketService = inject(TicketService);
  form!: FormGroup;

  @ViewChild('attachmentHelpDesk') attachmentComponent!: AttachmentComponent;

  buttonOptions: LabelValue[] = [];

  ngOnInit(): void {
    this.getCategorieByIdModulo();
    this.createForm();
  }

  ngAfterViewInit(): void {
    this.setupAttachmentForTicket();
  }

  setupAttachmentForTicket() {
    // Override getAllAttachment per usare il ticket service
    this.attachmentComponent.getAllAttachment = () => {
      const idTicket = this.form.controls['id'].value;
      if (!idTicket) {
        this.attachmentComponent.attachmentArray = [];
        return;
      }
      this.ticketService
        .getAllegatiByTicket(idTicket)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (allegati) => {
            this.attachmentComponent.attachmentArray = allegati.map((a) => ({
              id: a.id,
              codDocumento: a.codDocumento,
              codDocumentoFE: a.codDocumento?.split('.')[0],
              type: a.codDocumento?.split('.').pop(),
              sizeAllegato: a.sizeAllegato?.toString(),
            }));
          },
        });
    };

    // Override handleConfirmModal per usare il ticket service
    this.attachmentComponent.handleConfirmModal = () => {
      const modalForm = this.attachmentComponent.child.formGroup;
      const ctx = this.getUserContext();

      const allegato: AllegatoTicketDTO = {
        idTicketFk: this.form.controls['id'].value,
        codDocumento: modalForm.controls['fileName'].value,
        sizeAllegato: modalForm.controls['size'].value,
        idModulo: 'PIAO',
        codiceFiscale: ctx?.cf,
        codicePa: ctx?.paRiferimento.codePA,
      };

      this.ticketService
        .saveAllegato(allegato, modalForm.controls['file'].value)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (data) => {
            if (data && data.idTicketFk) {
              this.form.controls['id'].setValue(data.idTicketFk);
            }
            this.attachmentComponent.getAllAttachment();
            this.attachmentComponent.openModal = false;
            this.attachmentComponent.child.formGroup.reset();
          },
          error: (err) => {
            console.error('Errore salvataggio allegato:', err);
          },
        });
    };

    // Override remove per usare il ticket service
    this.attachmentComponent.remove = (idDoc: number) => {
      this.ticketService
        .deleteAllegato(idDoc)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.attachmentComponent.handleCloseModalDelete();
            this.attachmentComponent.getAllAttachment();
          },
          error: (err) => {
            console.error('Errore eliminazione allegato:', err);
          },
        });
    };
  }

  getCategorieByIdModulo() {
    this.ticketService
      .getCategorieByIdModulo('PIAO')
      .pipe(takeUntil(this.destroy$))
      .subscribe((categorie) => {
        this.buttonOptions = categorie.map((c) => ({ label: c.testo ?? '', value: c.id }));
      });
  }

  /**
   * Validatore custom per controllare che email e verificaEmail siano uguali
   */

  emailMismatchValidator(): ValidatorFn {
    return (formGroup: AbstractControl): ValidationErrors | null => {
      const email = formGroup.get('email');
      const verificaEmail = formGroup.get('verificaEmail');

      if (!email || !verificaEmail) {
        return null;
      }

      if (email.value && verificaEmail.value && email.value !== verificaEmail.value) {
        verificaEmail.setErrors({ ...verificaEmail.errors, emailMismatch: true });
        return null;
      } else if (email.value === verificaEmail.value && verificaEmail.errors) {
        // Rimuove solo l'errore emailMismatch
        const errors = { ...verificaEmail.errors };
        delete errors['emailMismatch'];
        verificaEmail.setErrors(Object.keys(errors).length > 0 ? errors : null);
      }

      return null;
    };
  }

  createForm() {
    this.form = this.fb.group(
      {
        id: this.fb.control<number | null>(null),
        nome: this.fb.control<String | null>(null, [
          Validators.required,
          Validators.pattern(INPUT_REGEX),
        ]),
        cognome: this.fb.control<String | null>(null, [
          Validators.required,
          Validators.pattern(INPUT_REGEX),
        ]),
        email: this.fb.control<String | null>(null, [
          Validators.required,
          Validators.pattern(EMAIL_REGEX),
        ]),
        verificaEmail: this.fb.control<String | null>(null, [
          Validators.required,
          Validators.pattern(EMAIL_REGEX),
        ]),
        oggetto: this.fb.control<String | null>(null, [
          Validators.required,
          Validators.pattern(INPUT_REGEX),
        ]),
        messaggio: this.fb.control<String | null>(null, [
          Validators.required,
          Validators.pattern(INPUT_REGEX),
        ]),
        problema: this.fb.control<number[] | null>(null, [Validators.required]),
      },
      { validators: [this.emailMismatchValidator()] }
    );
    this.isFormReady = true;
  }

  handleSelectedProblem(value: number) {
    this.form.controls['problema'].setValue(value);
  }

  handleSendSegnalazione() {
    if (this.form.invalid) return;

    const formValue = this.form.value;
    const ctx = this.getUserContext();

    const ticket: TicketDTO = {
      id: formValue.id ?? undefined,
      nome: formValue.nome,
      cognome: formValue.cognome,
      mail: formValue.email,
      oggetto: formValue.oggetto,
      messaggio: formValue.messaggio,
      idCategoriaTicket: formValue.problema,
      idModulo: 'PIAO',
      codiceFiscale: ctx?.cf,
      codicePa: ctx?.paRiferimento.codePA,
    };

    this.ticketService
      .createTicket(ticket)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.toastService.success('Segnalazione inviata con successo');
          this.form.reset();
          this.attachmentComponent.attachmentArray = [];
        },
        error: (err) => {
          console.error('Errore creazione ticket:', err);
        },
      });
  }
}
