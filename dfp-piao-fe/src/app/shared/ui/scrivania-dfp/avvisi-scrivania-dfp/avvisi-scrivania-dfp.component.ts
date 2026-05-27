import { Component, Signal, computed, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { SharedModule } from '../../../module/shared/shared.module';
import { ButtonComponent } from '../../../components/button/button.component';
import { SvgComponent } from '../../../components/svg/svg.component';
import { NewsComponent } from '../../news/news.component';
import { TBadge } from '../../../components/badge/badge.component';
import { AlertsService } from '../../../services/alerts.service';
import { QueryClientService } from '../../../services/query-client.service';
import { AlertsDTO } from '../../../models/classes/alerts-dto';

interface IAvvisoCard {
  id: number;
  icon: string;
  badgeText: string;
  badgeVariant: TBadge['variant'];
  title: string;
  description: string;
  date: string;
  detailLink: string;
}

@Component({
  selector: 'piao-avvisi-scrivania-dfp',
  imports: [SharedModule, ButtonComponent, SvgComponent, NewsComponent],
  templateUrl: './avvisi-scrivania-dfp.component.html',
  styleUrl: './avvisi-scrivania-dfp.component.scss',
})
export class AvvisiScrivaniaDFPComponent implements OnInit {
  private readonly alertsService = inject(AlertsService);
  private readonly query = inject(QueryClientService);
  private readonly router = inject(Router);

  readonly modulo = 'PIAO';
  readonly maxItems = 3;

  avvisiQuery!: {
    data: Signal<AlertsDTO[] | null>;
    status: Signal<'idle' | 'loading' | 'success' | 'error'>;
    error: Signal<unknown>;
    refetch: () => void;
    invalidate: () => void;
  };

  readonly cards = computed<IAvvisoCard[]>(() => {
    const items = this.avvisiQuery?.data() || [];
    return items
      .slice()
      .sort((left, right) => {
        const leftDate = new Date(left.dataPubblicazione || left.createdTs?.toString() || 0);
        const rightDate = new Date(right.dataPubblicazione || right.createdTs?.toString() || 0);
        return rightDate.getTime() - leftDate.getTime();
      })
      .slice(0, this.maxItems)
      .map((item) => this.toCard(item));
  });

  ngOnInit(): void {
    this.avvisiQuery = this.query.useQuery<AlertsDTO[]>(
      `avvisi:scrivania-dfp:${this.modulo}`,
      () => this.alertsService.getAllAvvisi(this.modulo),
      { staleTimeMs: 60000, refetchOnMount: true }
    );
  }

  handleGoToAvvisi(): void {
    this.router.navigate(['/avvisi']);
  }

  private toCard(item: AlertsDTO): IAvvisoCard {
    const tipologia = (item.tipologiaContenuto || item.tipoAvviso || '').toString();
    const id = item.id || 0;
    return {
      id,
      icon: this.getIconByTipologia(tipologia),
      badgeText: tipologia || 'Altro',
      badgeVariant: this.getBadgeVariantByTipologia(tipologia),
      title: item.oggetto || '-',
      description: item.messaggio || '-',
      date: this.formatDisplayDate(item.dataPubblicazione || item.createdTs?.toString()),
      detailLink: id ? `/avvisi/dettaglio-avviso/${id}` : '/avvisi',
    };
  }

  private getIconByTipologia(tipologia: string): string {
    const t = tipologia.toLowerCase();
    if (t.includes('comunicazione')) return 'Horn';
    if (t.includes('avvis')) return 'Docs';
    return 'SearchDocs';
  }

  private getBadgeVariantByTipologia(tipologia: string): TBadge['variant'] {
    const t = tipologia.toLowerCase();
    if (t.includes('comunicazione')) return 'success';
    if (t.includes('avvis')) return 'primary';
    return 'secondary';
  }

  private formatDisplayDate(value?: string): string {
    if (!value) return '-';
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) return value;
    return parsed.toLocaleDateString('it-IT', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
    });
  }
}
