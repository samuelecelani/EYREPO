import {
  Component,
  DestroyRef,
  OnInit,
  Signal,
  computed,
  effect,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { SharedModule } from '../../../shared/module/shared/shared.module';
import { BaseComponent } from '../../../shared/components/base/base.component';
import { QueryClientService } from '../../../shared/services/query-client.service';
import { NewsService } from '../../../shared/services/news.service';
import { INews, INewsDocument } from '../../../shared/ui/news/news.component';
import { SvgComponent } from '../../../shared/components/svg/svg.component';
import { AssetService } from '../../../shared/services/asset.service';
import { distinctUntilChanged, map } from 'rxjs/operators';

@Component({
  selector: 'piao-novita-detail',
  imports: [SharedModule, SvgComponent, RouterLink],
  templateUrl: './novita-detail.component.html',
  styleUrl: './novita-detail.component.scss',
})
export class NovitaDetailComponent extends BaseComponent implements OnInit {
  private readonly RELATED_PAGE_SIZE = 3;
  private readonly RELATED_MAX_PAGES = 3;

  private readonly route = inject(ActivatedRoute);
  private readonly query = inject(QueryClientService);
  private readonly newsService = inject(NewsService);
  private readonly translate = inject(TranslateService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly asset = inject(AssetService);
  private shareFeedbackTimeoutId: number | null = null;

  newsId = signal('');
  shareFeedback = signal('');
  relatedPage = signal(1);

  newsQuery!: {
    data: Signal<INews | null>;
    status: Signal<'idle' | 'loading' | 'success' | 'error'>;
    error: Signal<any>;
    refetch: () => void;
    invalidate: () => void;
  };

  relatedQuery!: {
    data: Signal<INews[] | null>;
    status: Signal<'idle' | 'loading' | 'success' | 'error'>;
    error: Signal<any>;
    refetch: () => void;
    invalidate: () => void;
  };

  currentNews = computed(() => {
    // Re-evaluate when route id changes, then bind to the current query signal.
    this.newsId();
    return this.newsQuery?.data?.() ?? null;
  });

  isCircolare = computed(() => this.currentNews()?.type === 'CIRCOLARE');

  contentHtml = computed(() => this.currentNews()?.content || '');

  hasHtmlContent = computed(() => /<\/?[a-z][\s\S]*>/i.test(this.contentHtml()));

  contentParagraphs = computed(() => {
    const content = this.contentHtml();

    if (this.hasHtmlContent()) {
      return [];
    }

    return content
      .split('\n')
      .map((paragraph) => paragraph.trim())
      .filter((paragraph) => paragraph.length > 0);
  });

  documents = computed<INewsDocument[]>(() => {
    // Mock documents removed: show the empty-state message until backend attachments are available.
    return this.currentNews()?.documents || [];
  });

  detailImageStyle = computed<Record<string, string>>(() => {
    const dataUrl = this.toImageDataUrl(this.currentNews()?.imageBase64);
    const bgValue = dataUrl ? `url('${dataUrl}')` : this.asset.bg('img/backHeaderNovita.jpg');
    return { 'background-image': bgValue };
  });

  relatedNewsPool = computed(() => {
    const list = this.relatedQuery?.data?.() || [];
    const activeNewsId = this.newsId();
    const activeType = this.currentNews()?.type;

    const parseDate = (value: string) => {
      const parsed = Date.parse(value || '');
      return Number.isNaN(parsed) ? 0 : parsed;
    };

    return list
      .filter((item) => String(item.id) !== activeNewsId)
      .filter((item) => !activeType || item.type === activeType)
      .sort((a, b) => parseDate(b.dateIso) - parseDate(a.dateIso))
      .slice(0, this.RELATED_PAGE_SIZE * this.RELATED_MAX_PAGES);
  });

  relatedAvailablePages = computed(() => {
    const pages = Math.ceil(this.relatedNewsPool().length / this.RELATED_PAGE_SIZE);
    return Math.min(this.RELATED_MAX_PAGES, Math.max(1, pages));
  });

  relatedDots = computed(() => {
    return Array.from({ length: this.RELATED_MAX_PAGES }, (_, index) => index + 1);
  });

  relatedNews = computed(() => {
    const currentPage = this.relatedPage();
    const startIndex = (currentPage - 1) * this.RELATED_PAGE_SIZE;
    const endIndex = startIndex + this.RELATED_PAGE_SIZE;

    return this.relatedNewsPool().slice(startIndex, endIndex);
  });

  canGoPreviousRelated = computed(() => this.relatedPage() > 1);

  canGoNextRelated = computed(() => this.relatedPage() < this.relatedAvailablePages());

  constructor() {
    super();

    effect(() => {
      const maxPage = this.relatedAvailablePages();
      const current = this.relatedPage();

      if (current > maxPage) {
        this.relatedPage.set(maxPage);
      }
    });

    this.destroyRef.onDestroy(() => {
      if (this.shareFeedbackTimeoutId !== null) {
        window.clearTimeout(this.shareFeedbackTimeoutId);
      }
    });
  }

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        map((params) => params.get('id') || ''),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((id) => {
        this.newsId.set(id);
        this.relatedPage.set(1);

        this.newsQuery = this.query.useQuery<INews | null>(
          `news:detail:${id}`,
          () => this.newsService.getById(id, 'PIAO'),
          {
            staleTimeMs: 60000,
            refetchOnMount: true,
          }
        );
      });

    this.relatedQuery = this.query.useQuery<INews[]>(
      'news:list:PIAO:related',
      () =>
        this.newsService
          .search({
            sort: 'date_desc',
            page: 1,
            limit: 50,
          })
          .pipe(map((response) => response.items)),
      {
        staleTimeMs: 60000,
        refetchOnMount: true,
      }
    );
  }

  async shareCurrentNews(): Promise<void> {
    const news = this.currentNews();
    const shareUrl = this.getShareUrl(news);
    const shareData = {
      title: news?.title || this.translate.instant('NOVITA_PA.TITLE'),
      text: news?.description || '',
      url: shareUrl,
    };

    const nav = navigator as Navigator & {
      share?: (data: { title?: string; text?: string; url?: string }) => Promise<void>;
      canShare?: (data?: { title?: string; text?: string; url?: string }) => boolean;
    };

    try {
      if (typeof nav.share === 'function') {
        if (!nav.canShare || nav.canShare(shareData)) {
          await nav.share(shareData);
          return;
        }
      }

      await this.copyToClipboard(shareUrl);
      this.setShareFeedback(this.translate.instant('NOVITA_PA.DETAIL.SHARE_LINK_COPIED'));
    } catch (error: unknown) {
      if (this.isShareAbortError(error)) {
        return;
      }

      this.setShareFeedback(this.translate.instant('NOVITA_PA.DETAIL.SHARE_NOT_AVAILABLE'));
    }
  }

  previousRelatedPage(): void {
    if (!this.canGoPreviousRelated()) {
      return;
    }

    this.relatedPage.update((page) => Math.max(1, page - 1));
  }

  nextRelatedPage(): void {
    if (!this.canGoNextRelated()) {
      return;
    }

    this.relatedPage.update((page) => Math.min(this.relatedAvailablePages(), page + 1));
  }

  goToRelatedPage(page: number): void {
    if (page < 1 || page > this.relatedAvailablePages()) {
      return;
    }

    this.relatedPage.set(page);
  }

  private getShareUrl(news: INews | null): string {
    if (news?.id) {
      return `${window.location.origin}/area-riservata/novita/dettaglio/${news.id}`;
    }

    return window.location.href;
  }

  private async copyToClipboard(value: string): Promise<void> {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(value);
      return;
    }

    const textarea = document.createElement('textarea');
    textarea.value = value;
    textarea.setAttribute('readonly', 'true');
    textarea.style.position = 'fixed';
    textarea.style.left = '-9999px';
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand('copy');
    document.body.removeChild(textarea);
  }

  private setShareFeedback(message: string): void {
    this.shareFeedback.set(message);

    if (this.shareFeedbackTimeoutId !== null) {
      window.clearTimeout(this.shareFeedbackTimeoutId);
    }

    this.shareFeedbackTimeoutId = window.setTimeout(() => {
      this.shareFeedback.set('');
      this.shareFeedbackTimeoutId = null;
    }, 2500);
  }

  private isShareAbortError(error: unknown): boolean {
    return (
      !!error &&
      typeof error === 'object' &&
      'name' in error &&
      (error as { name: string }).name === 'AbortError'
    );
  }

  private toImageDataUrl(rawValue?: string | null): string | null {
    if (!rawValue) {
      return null;
    }

    const trimmed = rawValue.trim();
    if (!trimmed) {
      return null;
    }

    if (/^data:image\/[a-zA-Z+.-]+;base64,/.test(trimmed)) {
      return trimmed;
    }

    if (!/^[A-Za-z0-9+/=\s]+$/.test(trimmed)) {
      return null;
    }

    const compact = trimmed.replace(/\s+/g, '');
    return compact ? `data:image/jpeg;base64,${compact}` : null;
  }
}
