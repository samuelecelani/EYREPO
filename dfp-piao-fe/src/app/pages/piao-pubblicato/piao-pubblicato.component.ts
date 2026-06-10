import { CommonModule } from '@angular/common';
import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { SharedModule } from '../../shared/module/shared/shared.module';
import { AttachmentService } from '../../shared/services/attachment.service';

/** Info del PIAO pubblicato, veicolate via base64 nel query param `info`. */
export interface PiaoPubblicatoInfo {
  id: number;
  denominazione: string;
  versione: string;
  tipologia: string;
  tipologiaOnline: string;
  denominazionePA: string;
}

@Component({
  selector: 'piao-piao-pubblicato',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './piao-pubblicato.component.html',
  styleUrl: './piao-pubblicato.component.scss',
})
export class PiaoPubblicatoComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly attachmentService = inject(AttachmentService);
  private readonly unsubscribe$ = new Subject<void>();

  readonly info = signal<PiaoPubblicatoInfo | null>(null);
  readonly pdfUrl = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly downloading = signal<boolean>(false);
  readonly downloadCompleted = signal<boolean>(false);

  ngOnInit(): void {
    this.route.queryParamMap.pipe(takeUntil(this.unsubscribe$)).subscribe((params) => {
      const encoded = params.get('data') || params.get('info');
      if (!encoded) {
        // Fallback: supporta anche query param diretto ?idPiao=123
        const directId = params.get('idPiao');
        if (directId && directId !== 'undefined' && directId !== 'null') {
          this.loadPdfUrl(Number(directId));
          return;
        }
        this.info.set(null);
        this.errorMessage.set('Parametro "data" mancante.');
        return;
      }
      try {
        const json = this.decodeBase64Utf8(encoded);
        const parsed = JSON.parse(json) as PiaoPubblicatoInfo & { idPiao?: number };
        // Accetta sia "id" che "idPiao" dal JSON
        if (parsed.id == null && parsed.idPiao != null) {
          parsed.id = parsed.idPiao;
        }
        this.info.set(parsed);
        this.errorMessage.set(null);
        if (parsed.id != null) {
          this.loadPdfUrl(parsed.id);
        } else {
          this.errorMessage.set('ID PIAO mancante nei dati ricevuti.');
        }
      } catch (err) {
        console.error('Impossibile decodificare il parametro "data"', err);
        this.info.set(null);
        this.errorMessage.set('Parametro "data" non valido.');
      }
    });
  }

  ngOnDestroy(): void {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
  }

  /** Carica l'URL del PDF del PIAO pubblicato. */
  private loadPdfUrl(idPiao: number): void {
    this.attachmentService
      .getPiaoPdfUrl(idPiao)
      .pipe(takeUntil(this.unsubscribe$))
      .subscribe({
        next: (response) => {
          if (response?.data) {
            this.pdfUrl.set(response.data);
            this.triggerDownload(response.data, idPiao);
          } else {
            this.pdfUrl.set(null);
            this.errorMessage.set('PDF non disponibile.');
          }
        },
        error: (err) => {
          console.error('Errore recupero PDF URL per idPiao=' + idPiao, err);
          this.pdfUrl.set(null);
          this.errorMessage.set('Errore durante il recupero del PDF.');
        },
      });
  }

  /** Avvia il download automatico del PDF. */
  private triggerDownload(url: string, idPiao: number): void {
    this.downloading.set(true);
    const link = document.createElement('a');
    link.href = url;
    link.download = `PIAO_${idPiao}.pdf`;
    link.target = '_blank';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    this.downloading.set(false);
    this.downloadCompleted.set(true);
  }

  /** Decodifica base64 (anche urlsafe) in stringa UTF-8. */
  private decodeBase64Utf8(value: string): string {
    const normalized = value.replace(/-/g, '+').replace(/_/g, '/');
    const padded = normalized + '='.repeat((4 - (normalized.length % 4)) % 4);
    const binary = atob(padded);
    const bytes = Uint8Array.from(binary, (c) => c.charCodeAt(0));
    return new TextDecoder('utf-8').decode(bytes);
  }
}
