import { Directive, inject, OnInit, TemplateRef, ViewContainerRef } from '@angular/core';
import { getConfig } from '../config/loader-config';

/**
 * Structural directive che mostra il contenuto solo se la data corrente
 * è compresa tra `dataCompilazionePiao` e `dataScadenzaPiao` configurate
 * nel config del backend.
 *
 * Utilizzo:
 *   <piao-card-alert *piaoPeriodoCompilazione ...></piao-card-alert>
 */
@Directive({
  selector: '[piaoPeriodoCompilazione]',
  standalone: true,
})
export class PiaoPeriodoCompilazioneDirective implements OnInit {
  private readonly templateRef = inject(TemplateRef<unknown>);
  private readonly viewContainer = inject(ViewContainerRef);

  ngOnInit(): void {
    if (this.isInsidePeriod()) {
      this.viewContainer.createEmbeddedView(this.templateRef);
    } else {
      this.viewContainer.clear();
    }
  }

  private isInsidePeriod(): boolean {
    const config = getConfig();
    if (!config) {
      return false;
    }

    const start = this.parseDate(config.dataCompilazionePiao);
    const end = this.parseDate(config.dataScadenzaPiao);
    if (!start || !end) {
      return false;
    }

    const now = new Date();
    return now.getTime() >= start.getTime() && now.getTime() <= end.getTime();
  }

  /**
   * Supporta sia il formato ISO (YYYY-MM-DD) sia il formato italiano (DD/MM/YYYY).
   */
  private parseDate(value: string | undefined | null): Date | null {
    if (!value) {
      return null;
    }

    // Formato italiano DD/MM/YYYY
    const itMatch = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(value);
    if (itMatch) {
      const [, day, month, year] = itMatch;
      const d = new Date(Number(year), Number(month) - 1, Number(day));
      return isNaN(d.getTime()) ? null : d;
    }

    const d = new Date(value);
    return isNaN(d.getTime()) ? null : d;
  }
}
