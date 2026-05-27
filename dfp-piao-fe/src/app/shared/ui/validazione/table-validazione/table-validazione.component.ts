import { Component, inject, OnDestroy, OnInit } from '@angular/core';
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
import { KEY_PIAO, ORDINE_SEZIONI, SHAPE_ICON } from '../../../utils/constants';
import { ModalComponent } from '../../../components/modal/modal.component';
import { ModalValidazioneComponent } from '../modal-validazione/modal-validazione.component';
import { CodTipologiaValidazione } from '../../../models/enums/cod-tipologia-validazione.enum';
import { CodRuoloEnum } from '../../../models/enums/cod-ruolo-enum';
import { PIAOService } from '../../../services/piao.service';
import { switchMap, takeUntil } from 'rxjs';
import { CodStatoValidazioneEnum } from '../../../models/enums/cod-stato-validazione.enum';
import { SessionStorageService } from '../../../services/session-storage.service';
import {
  CodTipologiaSezValEnum,
  getSezioneLabel,
} from '../../../models/enums/cod-tipologia-sez-val.enum';
import { Sezione1Service } from '../../../services/sezioni-1.service';
import { Sezione21Service } from '../../../services/sezioni-21.service';
import { Sezione4Service } from '../../../services/sezione4.service';
import { Sezione23Service } from '../../../services/sezione23.service';
import { Sezione22Service } from '../../../services/sezioni-22.service';
import { CodPathEnum } from '../../../models/enums/cod-path.enum';
import { PiaoStatusEnum } from '../../../models/enums/piao-status.enum';

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
export class TableValidazioneComponent extends BaseComponent implements OnInit, OnDestroy {
  labelTriennioDropDown: string = 'VALIDAZIONE.DROPDOWN.TITLE';

  checkBoxLabel: string = 'VALIDAZIONE.CHECKBOX.LABEL';
  textHref: string = 'VALIDAZIONE.CHECKBOX.TEXT_HREF';

  href: string = '/servizi-piao/indice-piao';
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

  piaoService: PIAOService = inject(PIAOService);

  triennioOptions: LabelValue[] = [];

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

  piaoMap: Map<number, PIAODTO> = new Map();

  tableData: TabellaValidazioneDTO[] = [];

  testoSezioneMap: Record<string, string> = {};

  router: Router = inject(Router);

  fb: FormBuilder = inject(FormBuilder);

  ngOnInit(): void {
    this.initDropdownPiao();
    this.initializeValidationFormGroup();
  }

  private initDropdownPiao(): void {
    this.getPaRiferimento$()
      .pipe(switchMap((pa) => this.piaoService.getAllPiao(pa.codePA)))
      .subscribe({
        next: (response) => {
          if (response && response.length > 0) {
            const allPiaoSort = [...response].sort((a, b) =>
              (a.versione ?? '').localeCompare(b.versione ?? '')
            );
            allPiaoSort.forEach((piao) => {
              this.piaoMap.set(piao.id!, piao);
              let denomPiao =
                '<strong>' +
                piao.denominazione?.replace('PIAO', 'Triennio') +
                '</strong>' +
                ' - ' +
                piao.versione;
              if (
                !this.triennioOptions.find((option) => option.label === denomPiao) &&
                piao.statoPiao &&
                ![
                  PiaoStatusEnum.PUBBLICATO.toString(),
                  PiaoStatusEnum.APPROVATO.toString(),
                ].includes(piao.statoPiao)
              ) {
                this.triennioOptions.push({
                  label: denomPiao || '',
                  value: piao.id || 0,
                });
              }
            });
          }
        },
      });
  }

  private initializeValidationFormGroup(): void {
    this.validazione = this.fb.group({
      triennio: [null, [Validators.required]],
    });
  }

  changeTriennio(selectedValue: any): void {
    this.piaoService.getStructureValidazionePiao(selectedValue).subscribe({
      next: (response) => {
        this.tableData = [];

        this.buildTestoSezioneMap(response);
        console.log('testoSezioneMap costruito:', this.testoSezioneMap);

        //escludo la sezione di approvazione (numeroSezione = 5)
        // che non deve essere mostrata nella tabella di validazione
        response = response.filter((section) => section.numeroSezione !== '5');

        response.sort((a, b) => {
          const idxA = ORDINE_SEZIONI.indexOf(a.numeroSezione || '');
          const idxB = ORDINE_SEZIONI.indexOf(b.numeroSezione || '');
          return (idxA === -1 ? Infinity : idxA) - (idxB === -1 ? Infinity : idxB);
        });

        console.log('Struttura indice PIAO per validazione:', response);
        const allSectionsValidated = response
          .filter((x) => x.sezioneEnum !== CodTipologiaSezValEnum.PIAO)
          .every((x) => x.statoValidazione === CodStatoValidazioneEnum.VALIDATA);

        const ctx = this.getUserContext();
        const isReferente = ctx?.ruoloUtente === CodRuoloEnum.REFERENTE;

        response.forEach((x) => {
          if (
            x.sezioneEnum === CodTipologiaSezValEnum.PIAO &&
            (!allSectionsValidated || !isReferente)
          ) {
            return;
          }
          x.sezione = getSezioneLabel(x.sezioneEnum as CodTipologiaSezValEnum, x.triennio || '');
          this.tableData.push(x);
        });
        this.sessionStorageService.setItem(KEY_PIAO, this.piaoMap.get(selectedValue));
        this.piaoDTO = this.piaoMap.get(selectedValue)!;
      },
    });
  }

  redirectToSection(row: TabellaValidazioneDTO): void {
    if (row.sezioneEnum === CodTipologiaSezValEnum.PIAO) {
      this.router.navigate(['/servizi-piao/indice-piao']).then(() => {
        setTimeout(() => {
          window.scrollTo(0, 0);
          // Fallback per alcuni browser
          document.documentElement.scrollTop = 0;
          document.body.scrollTop = 0;
        }, 0);
      });
    } else {
      const idSezione = row.sezione?.match(/[\d.]+/)?.[0] ?? '';
      this.router
        .navigate(['/servizi-piao/indice-piao/sezione'], {
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
      case CodRuoloEnum.REDATTORE: {
        const isSezioneAssociata = ctx.ruoloAttivo.sezioneAssociata?.some(
          (s) => s === row.sezioneEnum
        );

        if (
          isSezioneAssociata &&
          row.statoValidazione === CodStatoValidazioneEnum.DA_VALIDARE &&
          row.updatedBy === ctx.cf
        ) {
          actions.push({
            ...this.verticalEllipsisAnnullaRichiesta,

            callback: this.verticalEllipsisAnnullaRichiesta.callback
              ? () => this.verticalEllipsisAnnullaRichiesta.callback!(element)
              : undefined,
          });
        }

        break;
      }

      case CodRuoloEnum.VALIDATORE:
      case CodRuoloEnum.REFERENTE:
        if (row.statoValidazione === CodStatoValidazioneEnum.DA_VALIDARE) {
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
          console.log(row.sezioneEnum, row.updatedBy, ctx.cf);
          if (row.updatedBy === ctx.cf) {
            actions.push({
              ...this.verticalEllipsisAnnullaRichiesta,
              callback: this.verticalEllipsisAnnullaRichiesta.callback
                ? () => this.verticalEllipsisAnnullaRichiesta.callback!(element)
                : undefined,
            });
          }
        } else if (row.statoValidazione === CodStatoValidazioneEnum.VALIDATA) {
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
      case CodRuoloEnum.REDATTORE: {
        const isSezioneAssociata = ctx.ruoloAttivo.sezioneAssociata?.some(
          (s) => s === row.sezioneEnum
        );

        if (!isSezioneAssociata) {
          response = true;
        } else {
          response = !(
            row.statoValidazione === CodStatoValidazioneEnum.DA_VALIDARE && row.updatedBy === ctx.cf
          );
        }

        break;
      }
      case CodRuoloEnum.VALIDATORE:
        response = !(
          (row.statoValidazione === CodStatoValidazioneEnum.DA_VALIDARE ||
            row.statoValidazione === CodStatoValidazioneEnum.VALIDATA) &&
          ctx.ruoloAttivo.sezioneAssociata.includes(row.sezioneEnum || '')
        );
        break;
      case CodRuoloEnum.REFERENTE: {
        // Se esiste una riga PIAO, disabilita tutte le righe di sezione
        const hasPiao = this.tableData.some((r) => r.sezioneEnum === CodTipologiaSezValEnum.PIAO);
        if (hasPiao && row.sezioneEnum !== CodTipologiaSezValEnum.PIAO) {
          response = true;
        } else {
          response = !(
            row.statoValidazione === CodStatoValidazioneEnum.DA_VALIDARE ||
            row.statoValidazione === CodStatoValidazioneEnum.VALIDATA
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
      case CodRuoloEnum.REDATTORE: {
        const isSezioneAssociata = ctx.ruoloAttivo.sezioneAssociata?.some(
          (s) => s === row.sezioneEnum
        );

        if (!isSezioneAssociata) {
          response = true;
        } else {
          response = !(
            row.statoValidazione === CodStatoValidazioneEnum.DA_VALIDARE && row.updatedBy === ctx.cf
          );
        }

        break;
      }

      case CodRuoloEnum.VALIDATORE:
        response = !(
          row.statoValidazione === CodStatoValidazioneEnum.DA_VALIDARE &&
          ctx.ruoloAttivo.sezioneAssociata.includes(row.sezioneEnum || '')
        );
        break;
      case CodRuoloEnum.REFERENTE: {
        const hasPiao = this.tableData.some((r) => r.sezioneEnum === CodTipologiaSezValEnum.PIAO);
        if (hasPiao && row.sezioneEnum !== CodTipologiaSezValEnum.PIAO) {
          response = true;
        } else {
          response = !(row.statoValidazione === CodStatoValidazioneEnum.DA_VALIDARE);
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
      this.selectedSections.some((section) => section.sezioneEnum === CodTipologiaSezValEnum.PIAO)
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
    let sezionePath = '';

    switch (this.elementSelected?.sezioneEnum) {
      case CodTipologiaSezValEnum.SEZ1:
        sezionePath = CodPathEnum.SEZIONE_1;
        break;
      case CodTipologiaSezValEnum.SEZ2_1:
        sezionePath = CodPathEnum.SEZIONE_2_1;
        break;
      case CodTipologiaSezValEnum.SEZ2_2:
        sezionePath = CodPathEnum.SEZIONE_2_2;
        break;
      case CodTipologiaSezValEnum.SEZ2_3:
        sezionePath = CodPathEnum.SEZIONE_2_3;
        break;
      case CodTipologiaSezValEnum.SEZ3_1:
        sezionePath = CodPathEnum.SEZIONE_3_1;
        break;
      case CodTipologiaSezValEnum.SEZ3_2:
        sezionePath = CodPathEnum.SEZIONE_3_2;
        break;
      case CodTipologiaSezValEnum.SEZ3_3_1:
        sezionePath = CodPathEnum.SEZIONE_3_3_1;
        break;
      case CodTipologiaSezValEnum.SEZ3_3_2:
        sezionePath = CodPathEnum.SEZIONE_3_3_2;
        break;
      case CodTipologiaSezValEnum.SEZ4:
        sezionePath = CodPathEnum.SEZIONE_4;
        break;
      case CodTipologiaSezValEnum.PIAO:
        sezionePath = CodPathEnum.PIAO;
        break;
      default:
        break;
    }

    let testoSezione = this.testoSezioneMap[this.elementSelected?.numeroSezione || ''] || '';

    switch (this.codTipologiaValidazione) {
      case CodTipologiaValidazione.ACCETTA:
        console.log('Sezione accettata:', this.elementSelected);
        //accetta logica
        this.piaoService
          .validaSezione(this.elementSelected?.id!, sezionePath, testoSezione, 'sezioneValidata')
          .subscribe({
            next: () => {
              this.toastService.success('Sezione validata con successo');
              this.changeTriennio(this.piaoDTO.id); // Ricarica la struttura di validazione per aggiornare lo stato delle sezioni
            },
            error: (err) => {
              console.error('Errore nella validazione della sezione:', err);
            },
          });
        break;
      case CodTipologiaValidazione.ANNULLA_INVIO:
        console.log('Sezione annullata:', this.elementSelected);
        //annulla invio logica
        this.piaoService
          .annullaValidazioneSezione(
            this.elementSelected?.id!,
            sezionePath,
            testoSezione,
            'sezioneAnnullata'
          )
          .subscribe({
            next: () => {
              this.toastService.success('Sezione annullata con successo');
              this.changeTriennio(this.piaoDTO.id); // Ricarica la struttura di validazione per aggiornare lo stato delle sezioni
            },
            error: (err) => {
              console.error("Errore nell'annullamento della sezione:", err);
            },
          });
        break;
      case CodTipologiaValidazione.RIFIUTA:
        console.log(
          'Osservazioni inserite:',
          this.child?.formGroup.get('osservazioni')?.value,
          this.elementSelected
        );
        this.piaoService
          .rifiutaSezione(
            this.elementSelected?.id!,
            sezionePath,
            this.child?.formGroup.get('osservazioni')?.value,
            testoSezione,
            'sezioneRifiutata'
          )
          .subscribe({
            next: () => {
              this.toastService.success('Sezione rifiutata con successo');
              this.changeTriennio(this.piaoDTO.id); // Ricarica la struttura di validazione per aggiornare lo stato delle sezioni
            },
            error: (err) => {
              console.error('Errore nel rifiuto della sezione:', err);
            },
          });
        //rifiuta logica
        break;
      case CodTipologiaValidazione.REVOCA:
        console.log(
          'Osservazioni inserite:',
          this.child?.formGroup.get('osservazioni')?.value,
          this.elementSelected
        );
        //revoca logica
        this.piaoService
          .revocaSezione(
            this.elementSelected?.id!,
            sezionePath,
            this.child?.formGroup.get('osservazioni')?.value,
            testoSezione,
            'sezioneRevocata'
          )
          .subscribe({
            next: () => {
              this.toastService.success('Sezione revocata con successo');
              this.changeTriennio(this.piaoDTO.id); // Ricarica la struttura di validazione per aggiornare lo stato delle sezioni
            },
            error: (err) => {
              console.error('Errore nella revoca della sezione:', err);
            },
          });
        break;
      case CodTipologiaValidazione.ACCETTA_SEZIONI:
      case CodTipologiaValidazione.ACCETTA_PIAO:
        this.piaoService
          .accettaValidazioneSezioniSelezionate(
            this.piaoDTO.id!,
            new Map(this.selectedSections.map((s) => [s.sezioneEnum!, s.id!]))
          )
          .subscribe({
            next: () => {
              let isPiao = this.codTipologiaValidazione === CodTipologiaValidazione.ACCETTA_PIAO;
              if (isPiao) {
                this.toastService.success('PIAO validato con successo');
              } else {
                this.toastService.success('Sezioni validate con successo');
              }
              this.changeTriennio(this.piaoDTO.id); // Ricarica la struttura di validazione per aggiornare lo stato delle sezioni
            },
            error: (err) => {
              console.error("Errore nell'accettazione delle sezioni:", err);
            },
          });
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
    if (this.tableData.some((section) => section.sezioneEnum === CodTipologiaSezValEnum.PIAO)) {
      return 'BUTTONS.ACCEPT_VALIDATION_PIAO';
    } else {
      return 'BUTTONS.ACCEPT_VALIDATION';
    }
  }

  private buildTestoSezioneMap(response: TabellaValidazioneDTO[]): void {
    const findTesto = (parentNumero: string): string => {
      return response.find((s) => s.numeroSezione === parentNumero)?.testo || '';
    };

    this.testoSezioneMap = {
      '1': '1 ' + findTesto('1'),
      '21': '2.1 ' + findTesto('21'),
      '22': '2.2 ' + findTesto('22'),
      '23': '2.3 ' + findTesto('23'),
      '31': '3.1 ' + findTesto('31'),
      '32': '3.2 ' + findTesto('32'),
      '331': '3.3.1 ' + findTesto('331'),
      '332': '3.3.2 ' + findTesto('332'),
      '4': '4 ' + findTesto('4'),
    };
  }

  override ngOnDestroy(): void {
    //this.sessionStorageService.removeItem(KEY_PIAO);
    super.ngOnDestroy();
  }
}
