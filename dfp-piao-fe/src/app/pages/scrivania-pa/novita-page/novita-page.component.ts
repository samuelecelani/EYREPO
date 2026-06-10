import { Component, OnInit, Signal, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SharedModule } from '../../../shared/module/shared/shared.module';
import { BaseComponent } from '../../../shared/components/base/base.component';
import { NovitaComponent } from '../novita/novita.component';
import { CardHeaderNovitaPAComponent } from '../../../shared/ui/novita-pa/card-header-novita-pa/card-header-novita-pa.component';
import { SvgComponent } from '../../../shared/components/svg/svg.component';
import {
  INewsTipologia,
  INewsSearchResult,
  NewsService,
} from '../../../shared/services/news.service';
import { INews } from '../../../shared/ui/news/news.component';
import { QueryClientService } from '../../../shared/services/query-client.service';
import { AssetService } from '../../../shared/services/asset.service';

@Component({
  selector: 'piao-novita-page',
  imports: [SharedModule, CardHeaderNovitaPAComponent, NovitaComponent, SvgComponent, RouterLink],
  templateUrl: './novita-page.component.html',
  styleUrl: './novita-page.component.scss',
})
export class NovitaPageComponent extends BaseComponent implements OnInit {
  private readonly newsService = inject(NewsService);
  private readonly query = inject(QueryClientService);
  protected readonly asset = inject(AssetService);

  searchTerm: string = '';
  appliedSearchTerm: string = '';
  selectedTypology: string = '';
  selectedDate: string = '';
  selectedOrder: string = '';

  appliedTypology: string = '';
  appliedDate: string = '';
  appliedOrder: string = '';

  inEvidenzaQuery!: {
    data: Signal<INewsSearchResult | null>;
    status: Signal<'idle' | 'loading' | 'success' | 'error'>;
    error: Signal<any>;
    refetch: () => void;
    invalidate: () => void;
  };

  tipologieQuery!: {
    data: Signal<INewsTipologia[] | null>;
    status: Signal<'idle' | 'loading' | 'success' | 'error'>;
    error: Signal<any>;
    refetch: () => void;
    invalidate: () => void;
  };

  highlightedNews = computed<INews[]>(() => {
    const items = this.inEvidenzaQuery?.data?.()?.items || [];
    const parseDate = (value: string) => {
      const parsed = Date.parse(value || '');
      return Number.isNaN(parsed) ? 0 : parsed;
    };

    return [...items].sort((a, b) => parseDate(b.dateIso) - parseDate(a.dateIso)).slice(0, 3);
  });

  typologyOptions = computed(() => {
    const fromApi = this.tipologieQuery?.data?.() || [];

    if (fromApi.length > 0) {
      return fromApi.map((item) => ({
        value: item.label,
        label: item.label,
      }));
    }

    return [
      { value: 'Circolare', label: 'Circolare' },
      { value: 'Novita', label: 'Novita' },
      { value: 'Altro', label: 'Altro' },
    ];
  });

  ngOnInit(): void {
    this.inEvidenzaQuery = this.query.useQuery<INewsSearchResult>(
      'news:highlighted:in-evidenza',
      () =>
        this.newsService.search({
          inEvidenza: true,
          sort: 'date_desc',
          page: 1,
          limit: 3,
        }),
      {
        staleTimeMs: 120000,
        refetchOnMount: true,
      }
    );

    this.tipologieQuery = this.query.useQuery<INewsTipologia[]>(
      'news:tipologie:piao',
      () => this.newsService.listTipologie(),
      {
        staleTimeMs: 300000,
        refetchOnMount: true,
      }
    );
  }

  onSearchInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchTerm = input.value ?? '';
  }

  applySearch(): void {
    this.appliedSearchTerm = this.searchTerm;
  }

  onTypologyChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.selectedTypology = select.value;
  }

  onDateChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedDate = input.value;
  }

  onOrderChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.selectedOrder = select.value;
  }

  resetFilters(): void {
    this.searchTerm = '';
    this.appliedSearchTerm = '';
    this.selectedTypology = '';
    this.selectedDate = '';
    this.selectedOrder = '';

    this.appliedTypology = '';
    this.appliedDate = '';
    this.appliedOrder = '';
  }

  applyFilters(): void {
    this.appliedSearchTerm = this.searchTerm;
    this.appliedTypology = this.selectedTypology;
    this.appliedDate = this.selectedDate;
    this.appliedOrder = this.selectedOrder;
  }

  hasAppliedSearch(): boolean {
    return this.appliedSearchTerm.trim().length > 0;
  }
}
