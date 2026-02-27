import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SharedModule } from '../../../module/shared/shared.module';
import { DropdownComponent } from '../../../components/dropdown/dropdown.component';
import { LabelValue } from '../../../models/interfaces/label-value';
import { PIAODTO } from '../../../models/classes/piao-dto';
import { AzioniComponent } from '../../../components/azioni/azioni.component';
import { TabellaValidazioneDTO } from '../../../models/classes/tabella-validazione-dto';
import { Router, RouterModule } from '@angular/router';
import { BaseComponent } from '../../../components/base/base.component';
import { IVerticalEllipsisActions } from '../../../models/interfaces/vertical-ellipsis-actions';
import { CodTipologiaSezioneEnum } from '../../../models/enums/cod-tipologia-sezione.enum';
import { ButtonComponent } from '../../../components/button/button.component';
import { SHAPE_ICON } from '../../../utils/constants';
import { ModalComponent } from '../../../components/modal/modal.component';
import { ModalValidazioneComponent } from '../modal-validazione/modal-validazione.component';
import { CodTipologiaValidazione } from '../../../models/enums/cod-tipologia-validazione.enum';
import { CodRuoloEnum } from '../../../models/enums/cod-ruolo-enum';

@Component({
  selector: 'piao-table-validazione',
  imports: [
    SharedModule,
    DropdownComponent,
    AzioniComponent,
    ButtonComponent,
    ModalValidazioneComponent,
    ModalComponent,
    RouterModule,
  ],
  templateUrl: './table-validazione.component.html',
  styleUrl: './table-validazione.component.scss',
})
export class TableValidazioneComponent extends BaseComponent implements OnInit {
  labelTriennioDropDown: string = 'VALIDAZIONE.DROPDOWN.TITLE';

  checkBoxLabel: string = 'VALIDAZIONE.CHECKBOX.LABEL';
  textHref: string = 'VALIDAZIONE.CHECKBOX.TEXT_HREF';

  href: string = '/pages/servizi-piao/indice-piao';
  openModalValidation: boolean = false;
  iconModalValidation: string = SHAPE_ICON;
  iconStyle: string = 'icon-modal';
  titleModalValidation: string = 'VALIDAZIONE.MODAL_VALIDATION.TITLE';
  subTitleModalValidation: string = 'VALIDAZIONE.MODAL_VALIDATION.SUB_TITLE';
  subTitleModalParams: Record<string, string> = {};
  infoOperazioneModalValidation: string = 'VALIDAZIONE.MODAL_VALIDATION.INFO_OPERAZIONE';

  elementSelected?: TabellaValidazioneDTO;

  isOsservazioni: boolean = false;

  codTipologiaValidazione?: CodTipologiaValidazione;

  triennioOptions: LabelValue[] = [
    { label: 'Triennio 25-27', value: 1 },
    { label: 'Triennio 28-30', value: 2 },
    { label: 'Triennio 31-33', value: 3 },
  ];

  verticalEllipsis: IVerticalEllipsisActions[] = [];

  verticalEllipsisAnnullaRichiesta: IVerticalEllipsisActions = {
    label: 'VALIDAZIONE.TABLE.ACTIONS.CANCEL_SENDED_VALIDATION_REQUEST',
    callback: (row: TabellaValidazioneDTO) => {
      console.log('Annulla richiesta per:', row);
      this.openModalCancelValidation(row);
    },
  };

  verticalEllipsisAccettaRichiesta: IVerticalEllipsisActions = {
    label: 'VALIDAZIONE.TABLE.ACTIONS.ACCEPT_VALIDATION_REQUEST',
    callback: (row: TabellaValidazioneDTO) => {
      console.log('Accetta richiesta per:', row);
      this.openModalAcceptValidation(row);
    },
  };

  verticalEllipsisRifiutaRichiesta: IVerticalEllipsisActions = {
    label: 'VALIDAZIONE.TABLE.ACTIONS.REJECT_VALIDATION_REQUEST',
    callback: (row: TabellaValidazioneDTO) => {
      console.log('Rifiuta richiesta per:', row);
      this.openModalRejectValidation(row);
    },
  };

  verticalEllipsisRevocaValidazione: IVerticalEllipsisActions = {
    label: 'VALIDAZIONE.TABLE.ACTIONS.REVOKE_VALIDATION',
    callback: (row: TabellaValidazioneDTO) => {
      this.openModalRevokeValidation(row);
    },
  };

  validazione!: FormGroup;

  piaoDTO!: PIAODTO;

  tableData: TabellaValidazioneDTO[] = [];

  router: Router = inject(Router);

  fb: FormBuilder = inject(FormBuilder);

  ngOnInit(): void {
    this.initializeValidationFormGroup();
    this.tableData.push(
      {
        selected: false,
        id: 2,
        triennio: '25-27',
        sezione: 'Sezione 1',
        statoValidazione: 'Validata',
        profUtenteInvioRichiesta: 'Mario Rossi\n Redattore',
        dataInvioRichiesta: new Date(),
        profUtenteValidazione: 'Luigi Bianchi\n Validatore',
        dataValidazione: new Date(),
        osservazioni:
          'Validata con successo Lorem ipsum dolor sit amet, consectetur adipiscing elit. ',
        sezioneEnum: CodTipologiaSezioneEnum.SEZ1,
      },
      {
        selected: false,
        id: 3,
        triennio: '25-27',
        sezione: 'Sezione 2.1',
        statoValidazione: 'Rifiutata',
        profUtenteInvioRichiesta: 'Gianni Verdi\n Redattore',
        dataInvioRichiesta: new Date(),
        profUtenteValidazione: 'Giulia Bianchi\n Validatore',
        dataValidazione: new Date(),
        osservazioni: 'Rifiutata Lorem ipsum dolor sit amet, consectetur adipiscing elit. ',
        sezioneEnum: CodTipologiaSezioneEnum.SEZ2_1,
      },
      {
        selected: false,
        id: 4,
        triennio: '25-27',
        sezione: 'Sezione 2.2',
        statoValidazione: 'Da validare',
        profUtenteInvioRichiesta: 'Mario Rossi\n Referente',
        dataInvioRichiesta: new Date(),
        updatedBy: 'XXXX',
        sezioneEnum: CodTipologiaSezioneEnum.SEZ2_2,
      }
    );
  }

  private initializeValidationFormGroup(): void {
    this.validazione = this.fb.group({
      triennio: [null, [Validators.required]],
    });
  }

  changeTriennio(selectedValue: any): void {
    console.log('Triennio selezionato:', selectedValue);
    //findyByIdPiao
  }

  redirectToSection(sezione: string) {
    const idSezione = sezione.match(/[\d.]+/)?.[0] ?? '';
    this.router
      .navigate(['/pages/servizi-piao/indice-piao/sezione'], {
        queryParams: { idSezione },
        queryParamsHandling: 'merge',
      })
      .then(() => {
        setTimeout(() => {
          window.scrollTo(0, 0);
          // Fallback per alcuni browser
          document.documentElement.scrollTop = 0;
          document.body.scrollTop = 0;
        }, 0);
      });
  }

  selectAll(): void {
    const allSelected = this.tableData.every((row) => row.selected);
    this.tableData.forEach((row) => (row.selected = !allSelected));
  }

  getActionsFor(row: TabellaValidazioneDTO): IVerticalEllipsisActions[] {
    const ctx = this.getUserContext();
    if (!ctx) {
      return []; // Se non c'è contesto utente, non mostrare azioni per sicurezza
    }
    const element = row;
    const actions: any = [];

    switch (ctx.ruoloUtente) {
      case CodRuoloEnum.REDATTORE:
        if (
          row.statoValidazione === 'Da validare' &&
          row.updatedBy === ctx.cf &&
          ctx.ruoloAttivo.sezioneAssociata.includes(row.sezioneEnum || '')
        ) {
          actions.push({
            ...this.verticalEllipsisAnnullaRichiesta,
            callback: this.verticalEllipsisAnnullaRichiesta.callback
              ? () => this.verticalEllipsisAnnullaRichiesta.callback!(element)
              : undefined,
          });
        }
        break;
      case CodRuoloEnum.VALIDATORE:
      case CodRuoloEnum.REFERENTE:
        if (row.statoValidazione === 'Da validare') {
          actions.push(
            {
              ...this.verticalEllipsisAccettaRichiesta,
              callback: this.verticalEllipsisAccettaRichiesta.callback
                ? () => this.verticalEllipsisAccettaRichiesta.callback!(element)
                : undefined,
            },
            {
              ...this.verticalEllipsisRifiutaRichiesta,
              callback: this.verticalEllipsisRifiutaRichiesta.callback
                ? () => this.verticalEllipsisRifiutaRichiesta.callback!(element)
                : undefined,
            }
          );
          if (row.createdBy === ctx.cf) {
            actions.push({
              ...this.verticalEllipsisAnnullaRichiesta,
              callback: this.verticalEllipsisAnnullaRichiesta.callback
                ? () => this.verticalEllipsisAnnullaRichiesta.callback!(element)
                : undefined,
            });
          }
        } else if (row.statoValidazione === 'Validata') {
          actions.push({
            ...this.verticalEllipsisRevocaValidazione,
            callback: this.verticalEllipsisRevocaValidazione.callback
              ? () => this.verticalEllipsisRevocaValidazione.callback!(element)
              : undefined,
          });
        }
        break;
      default:
        break;
    }

    // Qui puoi filtrare le azioni in base a ctx.ruoloUtente, ctx.cf, row, ecc.
    return actions;
  }

  getReadOnlyFor(row: TabellaValidazioneDTO): boolean {
    const ctx = this.getUserContext();
    if (!ctx) {
      return true; // Se non c'è contesto utente, rendi tutto read-only per sicurezza
    }

    let response = true;
    switch (ctx.ruoloUtente) {
      case CodRuoloEnum.REDATTORE:
        response = !(
          row.statoValidazione === 'Da validare' &&
          row.updatedBy === ctx.cf &&
          ctx.ruoloAttivo.sezioneAssociata.includes(row.sezioneEnum || '')
        );
        break;
      case CodRuoloEnum.VALIDATORE:
        response = !(
          (row.statoValidazione === 'Da validare' || row.statoValidazione === 'Validata') &&
          ctx.ruoloAttivo.sezioneAssociata.includes(row.sezioneEnum || '')
        );
        break;
      case CodRuoloEnum.REFERENTE: {
        // Se esiste una riga PIAO, disabilita tutte le righe di sezione
        const hasPiao = this.tableData.some((r) => r.sezioneEnum === CodTipologiaSezioneEnum.PIAO);
        if (hasPiao && row.sezioneEnum !== CodTipologiaSezioneEnum.PIAO) {
          response = true;
        } else {
          response = !(
            row.statoValidazione === 'Da validare' || row.statoValidazione === 'Validata'
          );
        }
        break;
      }
      default:
        break;
    }
    return response;
  }

  getReadOnlyForCheck(row: TabellaValidazioneDTO): boolean {
    const ctx = this.getUserContext();
    if (!ctx) {
      return true; // Se non c'è contesto utente, rendi tutto read-only per sicurezza
    }

    let response = true;
    switch (ctx.ruoloUtente) {
      case CodRuoloEnum.REDATTORE:
        response = !(row.statoValidazione === 'Da validare' && row.createdBy === ctx.cf);
        break;
      case CodRuoloEnum.VALIDATORE:
        response = !(
          row.statoValidazione === 'Da validare' &&
          ctx.ruoloAttivo.sezioneAssociata.includes(row.sezioneEnum || '')
        );
        break;
      case CodRuoloEnum.REFERENTE: {
        const hasPiao = this.tableData.some((r) => r.sezioneEnum === CodTipologiaSezioneEnum.PIAO);
        if (hasPiao && row.sezioneEnum !== CodTipologiaSezioneEnum.PIAO) {
          response = true;
        } else {
          response = !(row.statoValidazione === 'Da validare');
        }
        break;
      }
      default:
        break;
    }
    return response;
  }

  openModal(): void {
    this.openModalValidation = true;
    this.titleModalValidation = 'VALIDAZIONE.MODAL_VALIDATION.TITLE';
    if (
      this.selectedSections.some((section) => section.sezioneEnum === CodTipologiaSezioneEnum.PIAO)
    ) {
      this.subTitleModalValidation = 'VALIDAZIONE.MODAL_VALIDATION.PIAO.SUB_TITLE';
      this.codTipologiaValidazione = CodTipologiaValidazione.ACCETTA_PIAO;
    } else if (this.selectedSections.length > 1) {
      this.subTitleModalValidation = 'VALIDAZIONE.MODAL_VALIDATION.SEZIONE.MULTIPLE_SUB_TITLE';
      this.subTitleModalParams = {
        section: this.selectedSections.map((s) => s.sezione).join(', '),
      };
      this.codTipologiaValidazione = CodTipologiaValidazione.ACCETTA_SEZIONI;
    } else {
      this.subTitleModalValidation = 'VALIDAZIONE.MODAL_VALIDATION.SEZIONE.SUB_TITLE';
      this.subTitleModalParams = {
        section: this.selectedSections[0]?.sezione ?? '',
      };
      this.codTipologiaValidazione = CodTipologiaValidazione.ACCETTA;
      this.elementSelected = this.selectedSections[0];
    }
  }

  openModalCancelValidation(row: TabellaValidazioneDTO): void {
    this.openModalValidation = true;
    this.subTitleModalValidation = 'VALIDAZIONE.MODAL_VALIDATION.SEZIONE.CANCEL_SUB_TITLE';
    this.titleModalValidation = 'VALIDAZIONE.MODAL_VALIDATION.SEZIONE.CANCEL_TITLE';
    this.elementSelected = row;
    this.isOsservazioni = false;
    this.codTipologiaValidazione = CodTipologiaValidazione.ANNULLA_INVIO;
  }
  openModalAcceptValidation(row: TabellaValidazioneDTO): void {
    this.openModalValidation = true;
    this.subTitleModalValidation = 'VALIDAZIONE.MODAL_VALIDATION.SEZIONE.ACCEPT_SUB_TITLE';
    this.titleModalValidation = 'VALIDAZIONE.MODAL_VALIDATION.SEZIONE.ACCEPT_TITLE';
    this.elementSelected = row;
    this.isOsservazioni = false;
    this.codTipologiaValidazione = CodTipologiaValidazione.ACCETTA;
  }
  openModalRejectValidation(row: TabellaValidazioneDTO): void {
    this.openModalValidation = true;
    this.titleModalValidation = 'VALIDAZIONE.MODAL_VALIDATION.SEZIONE.TITLE_REJECT';
    this.elementSelected = row;
    this.isOsservazioni = true;
    this.codTipologiaValidazione = CodTipologiaValidazione.RIFIUTA;
  }
  openModalRevokeValidation(row: TabellaValidazioneDTO): void {
    this.openModalValidation = true;
    this.titleModalValidation = 'VALIDAZIONE.MODAL_VALIDATION.SEZIONE.TITLE_REVOKE';
    this.elementSelected = row;
    this.isOsservazioni = true;
    this.codTipologiaValidazione = CodTipologiaValidazione.REVOCA;
  }

  handleValidation(): void {
    switch (this.codTipologiaValidazione) {
      case CodTipologiaValidazione.ACCETTA:
        console.log('Sezione accettata:', this.elementSelected);
        //accetta logica
        break;
      case CodTipologiaValidazione.ANNULLA_INVIO:
        console.log('Sezione annullata:', this.elementSelected);
        //annulla invio logica
        break;
      case CodTipologiaValidazione.RIFIUTA:
        console.log(
          'Osservazioni inserite:',
          this.child?.formGroup.get('osservazioni')?.value,
          this.elementSelected
        );
        //rifiuta logica
        break;
      case CodTipologiaValidazione.REVOCA:
        console.log(
          'Osservazioni inserite:',
          this.child?.formGroup.get('osservazioni')?.value,
          this.elementSelected
        );
        //revoca logica
        break;
      case CodTipologiaValidazione.ACCETTA_SEZIONI:
        console.log('Sezioni accettate:', this.selectedSections);
        //accetta sezioni logica
        break;
      case CodTipologiaValidazione.ACCETTA_PIAO:
        console.log('PIAO accettato:', this.selectedSections);
        //accetta piao logica
        break;
      default:
        break;
    }
    this.closeModal();
  }

  closeModal(): void {
    this.openModalValidation = false;
    this.elementSelected = undefined;
    this.subTitleModalParams = {};
    this.isOsservazioni = false;
    this.codTipologiaValidazione = undefined;
    this.tableData.forEach((row) => (row.selected = false));
  }

  get selectedSections(): TabellaValidazioneDTO[] {
    return this.tableData.filter((row) => row.selected && !this.getReadOnlyForCheck(row));
  }

  get buttonSectionPiao(): string {
    if (this.tableData.some((section) => section.sezioneEnum === CodTipologiaSezioneEnum.PIAO)) {
      return 'BUTTONS.ACCEPT_VALIDATION_PIAO';
    } else {
      return 'BUTTONS.ACCEPT_VALIDATION';
    }
  }
}
