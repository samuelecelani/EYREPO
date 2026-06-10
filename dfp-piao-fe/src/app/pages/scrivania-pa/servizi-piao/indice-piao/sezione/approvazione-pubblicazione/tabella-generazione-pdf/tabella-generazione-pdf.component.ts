import { Component, Input, OnInit, signal, computed, inject, DestroyRef } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { map, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { BaseComponent } from 'src/app/shared/components/base/base.component';
import { SharedModule } from 'src/app/shared/module/shared/shared.module';
import { AllegatoDTO } from '../../../../../../../shared/models/classes/allegato-dto';
import { CodTipologiaAllegatoEnum } from '../../../../../../../shared/models/enums/cod-tipologia-allegato.enum';
import { SectionEnum } from '../../../../../../../shared/models/enums/section.enum';
import { PiaoPDFStatusEnum } from '../../../../../../../shared/models/enums/piao-pdf-status.enum';
import { StatusComponent } from 'src/app/shared/components/status/status.component';
import { EXCHANGE_ICON } from 'src/app/shared/utils/constants';
import { SvgComponent } from 'src/app/shared/components/svg/svg.component';
import { AttachmentService } from '../../../../../../../shared/services/attachment.service';
import { NotificaService } from '../../../../../../../shared/services/notifica.service';
import { SessionStorageService } from '../../../../../../../shared/services/session-storage.service';
import { KEY_PA_ATTIVA } from '../../../../../../../shared/utils/constants';
import { ToastService } from '../../../../../../../shared/services/toast.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'piao-tabella-generazione-pdf',
  imports: [SharedModule, StatusComponent, SvgComponent],
  templateUrl: './tabella-generazione-pdf.component.html',
  styleUrl: './tabella-generazione-pdf.component.scss',
})
export class TabellaGenerazionePdfComponent implements OnInit {
  @Input() form!: FormGroup;
  @Input() isDettaglio: boolean = false;
  @Input() idPiao!: number;

  icon = EXCHANGE_ICON;

  // Signal per tracciare i cambiamenti dello stato del PDF
  private piaoPDFState = signal<AllegatoDTO | null>(null);

  // Subject per gestire la cancellazione dei subscription
  private destroy$ = new Subject<void>();

  private toastService = inject(ToastService);

  descTh: string =
    'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.TABELLA_GENERAZIONE_PDF.TABLE.DESC_TH';
  dateTh: string =
    'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.TABELLA_GENERAZIONE_PDF.TABLE.DATE_TH';
  stateTh: string =
    'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.TABELLA_GENERAZIONE_PDF.TABLE.STATE_TH';
  actionTh: string =
    'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.TABELLA_GENERAZIONE_PDF.TABLE.ACTION_TH';
  title: string =
    'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.TABELLA_GENERAZIONE_PDF.TITLE';
  generateButton: string =
    'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.TABELLA_GENERAZIONE_PDF.TABLE.BUTTON';
  buttonDownload: string =
    'APPROVAZIONE_E_PUBBLICAZIONE.INVIA_RICHIESTA_APPROVAZIONE.TABELLA_GENERAZIONE_PDF.TABLE.BUTTON_DOWNLOAD';

  private destroyRef = inject(DestroyRef);
  private attachmentService: AttachmentService = inject(AttachmentService);
  private notificaService: NotificaService = inject(NotificaService);
  private sessionStorageService: SessionStorageService = inject(SessionStorageService);

  ngOnInit(): void {
    // Inizializzare con il valore iniziale del form
    const initialValue = this.form?.get('piaoPDF')?.value;
    this.piaoPDFState.set(initialValue || null);

    // Ascoltare i cambiamenti dello stato del PDF nel form
    // takeUntil(this.destroy$) cancella automaticamente il subscription quando destroy$ emette
    this.form
      .get('piaoPDF')
      ?.valueChanges.pipe(takeUntil(this.destroy$))
      .subscribe((value) => {
        this.piaoPDFState.set(value);
      });

    // Registrare il cleanup: quando il component muore, emetti il segnale di distruzione
    this.destroyRef.onDestroy(() => {
      this.destroy$.next(); // Emetti: "Component sta morendo!"
      this.destroy$.complete(); // Chiudi il subject
    });

    this.getAllAttachment();
  }

  private getAllAttachment(): void {
    if (!this.idPiao) {
      return;
    }

    this.attachmentService
      .getAllAttachmentsByTipologia(
        [SectionEnum.PIAO],
        [CodTipologiaAllegatoEnum.PIAO_PDF_GENERATO],
        this.idPiao,
        true
      )
      .pipe(map((res) => (res ? res.data : [])))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (attachments: AllegatoDTO[]) => {
          if (attachments && attachments.length > 0) {
            const allegato = { ...attachments[0] };
            // Il BE restituisce statusAllegato, lo mappiamo su status
            if (allegato.statusAllegato && !allegato.status) {
              allegato.status = allegato.statusAllegato as PiaoPDFStatusEnum;
            }
            this.form.get('piaoPDF')?.setValue(allegato);
          }
        },
      });
  }

  clickGeneratePDF(): void {
    const pa = this.sessionStorageService.getItem(KEY_PA_ATTIVA);
    const codicePa = pa?.codePA;
    if (!this.idPiao || !codicePa) {
      console.warn(
        '[clickGeneratePDF] Dati mancanti - idPiao:',
        this.idPiao,
        'codicePa:',
        codicePa
      );
      return;
    }

    // Imposta lo stato a IN_GENERAZIONE immediatamente
    this.form.get('piaoPDF')?.setValue({
      id: undefined,
      idEntitaFK: undefined,
      codDocumento: 'Piao annuale',
      codDocumentoFE: undefined,
      codTipologiaFK: SectionEnum.SEZIONE_INVIA_APPROVAZIONE,
      codTipologiaAllegato: CodTipologiaAllegatoEnum.PIAO,
      descrizione: undefined,
      downloadUrl: undefined,
      sizeAllegato: undefined,
      type: undefined,
      isDoc: true,
      base64: undefined,
      createdTs: undefined,
      status: PiaoPDFStatusEnum.IN_GENERAZIONE,
    } as AllegatoDTO);

    // Chiama il servizio di generazione PDF
    this.notificaService
      .generatePdf(this.idPiao, SectionEnum.PIAO, codicePa)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          // La generazione è stata avviata con successo, ora aspettiamo che il BE aggiorni lo stato tramite WebSocket
          this.toastService.success(
            'Generazione del documento avviata. Una volta completata riceverai una notifica per poter scaricare il file.'
          );
        },
        error: () => {
          // In caso di errore, ripristina lo stato a DA_GENERARE
          this.form.get('piaoPDF')?.setValue({
            id: undefined,
            idEntitaFK: undefined,
            codDocumento: 'Piao annuale',
            codDocumentoFE: undefined,
            codTipologiaFK: SectionEnum.SEZIONE_INVIA_APPROVAZIONE,
            codTipologiaAllegato: CodTipologiaAllegatoEnum.PIAO,
            descrizione: undefined,
            downloadUrl: undefined,
            sizeAllegato: undefined,
            type: undefined,
            isDoc: true,
            base64: undefined,
            createdTs: undefined,
            status: PiaoPDFStatusEnum.DA_GENERARE,
          } as AllegatoDTO);
        },
      });
  }

  // Computed signal reattivo che dipende dallo stato del PDF
  arrayForm = computed(() => {
    const value = this.piaoPDFState();
    return value
      ? [value]
      : [
          {
            id: undefined,
            idEntitaFK: undefined,
            codDocumento: 'Piao annuale',
            codDocumentoFE: undefined,
            codTipologiaFK: SectionEnum.SEZIONE_INVIA_APPROVAZIONE,
            codTipologiaAllegato: CodTipologiaAllegatoEnum.PIAO,
            descrizione: undefined,
            downloadUrl: undefined,
            sizeAllegato: undefined,
            type: undefined,
            isDoc: true,
            base64: undefined,
            createdTs: undefined,
            status: PiaoPDFStatusEnum.DA_GENERARE,
          } as AllegatoDTO,
        ];
  });

  // Computed signal reattivo che rappresenta lo stato dell'allegato
  allegatoStatus = computed(() => {
    const allegatos = this.arrayForm();
    for (const allegato of allegatos) {
      if (allegato.status === PiaoPDFStatusEnum.DA_GENERARE) {
        return PiaoPDFStatusEnum.DA_GENERARE;
      } else if (allegato.status === PiaoPDFStatusEnum.IN_GENERAZIONE) {
        return PiaoPDFStatusEnum.IN_GENERAZIONE;
      } else if (allegato.status === PiaoPDFStatusEnum.GENERATO) {
        return PiaoPDFStatusEnum.GENERATO;
      }
    }
    return PiaoPDFStatusEnum.ERRORE_GENERAZIONE;
  });

  handleDownloadPiaoPDF(): void {
    const allegato = this.arrayForm()[0];
    const downloadUrl = allegato?.downloadUrl;
    if (!downloadUrl) {
      return;
    }

    fetch(downloadUrl, { method: 'GET' })
      .then(async (response) => {
        if (!response.ok) {
          throw new Error(`Download failed with status ${response.status}`);
        }

        const blob = await response.blob();
        const contentDisposition = response.headers.get('content-disposition');
        const fileName =
          this.extractFileNameFromContentDisposition(contentDisposition) ||
          allegato.codDocumento ||
          'piao.pdf';

        const objectUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = objectUrl;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(objectUrl);
      })
      .catch((error) => {
        console.error('Errore durante il download PDF:', error);
      });
  }

  private extractFileNameFromContentDisposition(contentDisposition: string | null): string | null {
    if (!contentDisposition) {
      return null;
    }
    const match = contentDisposition.match(/filename\*=UTF-8''([^;]+)|filename="?([^";]+)"?/i);
    const rawName = match?.[1] || match?.[2];
    return rawName ? decodeURIComponent(rawName) : null;
  }
}
