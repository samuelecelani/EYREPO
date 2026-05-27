import { Component, inject, OnDestroy, OnInit, Input } from '@angular/core';
import { SharedModule } from '../../../module/shared/shared.module';
import { LabelValue } from '../../../models/interfaces/label-value';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { ModalBodyComponent } from '../../../components/modal/modal-body/modal-body.component';
import { DropdownComponent } from '../../../components/dropdown/dropdown.component';
import { PIAOService } from '../../../services/piao.service';
import { Subject, takeUntil } from 'rxjs';
import { SvgComponent } from '../../../components/svg/svg.component';
import { ONLINE, PDF, ORDINARIO, SEMPLIFICATO } from '../../../utils/constants';
import { PIAODTO } from '../../../models/classes/piao-dto';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'piao-modal-redigi-piao',
    imports: [SharedModule, DropdownComponent, ReactiveFormsModule, SvgComponent],
    templateUrl: './modal-redigi-piao.component.html',
    styleUrl: './modal-redigi-piao.component.scss'
})
export class ModalRedigiPiaoComponent extends ModalBodyComponent implements OnInit, OnDestroy {
  @Input() codPAFK: string = '';

  private piaoService: PIAOService = inject(PIAOService);
  private unsubscribe$: Subject<void> = new Subject<void>();

  private readonly translate: TranslateService = inject(TranslateService);

  showChoiceNumberOfEmployees: boolean = false;
  showWarningAlert: boolean = false;
  warningMessage: string = '';
  titleDropdown: string = 'SCRIVANIA_PA.SERVIZI_PIAO.MODAL.REDIGI.DROPDOWN.TITLE';
  triennioLabel: string = 'PIAO_PDF.TRIENNIO_LABEL';
  triennioOptions: LabelValue[] = [];
  isTriennioReadOnly: boolean = false;

  private piaoCorrente: PIAODTO | null = null;

  ngOnInit(): void {
    this.formGroup = new FormGroup({
      choice: new FormControl<string | null>(null, Validators.required),
      numberOfEmployess: new FormControl<string | null>(null),
      triennio: new FormControl<string | null>(null, Validators.required),
    });

    this.loadTipologiaCorrente();

    this.loadTrienni();
  }

  private loadTrienni(): void {
    this.piaoService
      .getTrienniRiferimento()
      .pipe(takeUntil(this.unsubscribe$))
      .subscribe({
        next: (trienni: string[]) => {
          this.triennioOptions = (trienni ?? []).map((triennio) => ({
            label: triennio,
            value: triennio,
          }));

          if (this.triennioOptions.length === 1) {
            this.formGroup.controls['triennio'].setValue(this.triennioOptions[0].value);
            this.isTriennioReadOnly = true;
          } else {
            this.isTriennioReadOnly = false;
          }
        },
        error: () => {
          this.triennioOptions = [];
          this.isTriennioReadOnly = false;
        },
      });
  }

  ngOnDestroy(): void {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
  }

  inputContent: LabelValue[] = [
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.MODAL.REDIGI.RADIO_BUTTONS.CHOICE_1',
      value: ONLINE,
      formControlName: 'choice',
    },
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.MODAL.REDIGI.RADIO_BUTTONS.CHOICE_2',
      value: PDF,
      formControlName: 'choice',
    },
  ];

  dropDown: LabelValue[] = [
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.MODAL.REDIGI.DROPDOWN.UP_50',
      value: 'plus50',
    },
    {
      label: 'SCRIVANIA_PA.SERVIZI_PIAO.MODAL.REDIGI.DROPDOWN.DOWN_50',
      value: 'min50',
    },
  ];

  handleChoice(value: string | boolean | undefined): void {
    const control = this.formGroup.controls['numberOfEmployess'];

    if (value === ONLINE) {
      control.addValidators([Validators.required]);
      this.showChoiceNumberOfEmployees = true;
    } else {
      control.clearValidators();
      control.reset();
      this.showChoiceNumberOfEmployees = false;
    }

    //aggiornamento del control e del form dopo aggiunta/rimozione validator
    control.updateValueAndValidity();
    this.formGroup.updateValueAndValidity();

    this.checkTipologiaChange();
  }

  handleDropdownChange(): void {
    this.checkTipologiaChange();
  }

  private loadTipologiaCorrente(): void {
    if (!this.codPAFK) return;

    this.piaoService.getTipologiaCorrente(this.codPAFK).subscribe({
      next: (res) => {
        this.piaoCorrente = res;
      },
      error: () => {
        this.piaoCorrente = null;
      }
    });
  }

  private readonly warningBase = 'SCRIVANIA_PA.SERVIZI_PIAO.MODAL.REDIGI.WARNING';

  private label(key: string): string {
    return this.translate.instant(`${this.warningBase}.TIPOLOGIA.${key}`);
  }

  private buildWarning(messageKey: string, correnteKey: string, selezionataKey: string): void {
    this.warningMessage = this.translate.instant(`${this.warningBase}.${messageKey}`, {
      tipologiaCorrente: this.label(correnteKey),
      tipologiaSelezionata: this.label(selezionataKey),
    });
    this.showWarningAlert = true;
  }

  private checkTipologiaChange(): void {
    this.showWarningAlert = false;
    this.warningMessage = '';

    const tipologiaCorrente = this.piaoCorrente?.tipologia;
    const selectedChoice = this.formGroup.controls['choice'].value;
    if (!tipologiaCorrente || !selectedChoice) return;

    // Caso 1: tipologia diversa (ONLINE vs PDF)
    if (selectedChoice !== tipologiaCorrente) {
      this.buildWarning('TIPOLOGIA_DIVERSA', tipologiaCorrente, selectedChoice);
      return;
    }

    // Caso 2: stessa tipologia ONLINE ma sotto-tipologia diversa (ORDINARIO vs SEMPLIFICATO)
    if (selectedChoice !== ONLINE) return;

    const numberOfEmployees = this.formGroup.controls['numberOfEmployess'].value;
    const tipologiaOnlineCorrente = this.piaoCorrente?.tipologiaOnline;
    if (!numberOfEmployees || !tipologiaOnlineCorrente) return;

    const tipologiaOnlineSelezionata = numberOfEmployees === 'plus50' ? ORDINARIO : SEMPLIFICATO;
    if (tipologiaOnlineSelezionata !== tipologiaOnlineCorrente) {
      this.buildWarning('SOTTOTIPOLOGIA_DIVERSA', tipologiaOnlineCorrente, tipologiaOnlineSelezionata);
    }
  }
}
