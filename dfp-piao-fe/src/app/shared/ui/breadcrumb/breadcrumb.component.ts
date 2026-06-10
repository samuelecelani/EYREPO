import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, Input, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd, ActivatedRoute } from '@angular/router';
import { filter, take } from 'rxjs/operators';
import { AccountService } from '../../services/account.service';
import { NewsService } from '../../services/news.service';
import { RoleRoutingService } from '../../services/role-routing.service';
import { SessionStorageService } from '../../services/session-storage.service';
import { KEY_PIAO } from '../../utils/constants';
import { getCurrentTriennio } from '../../utils/utils';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

export interface BreadcrumbItem {
  label: string;
  url: string;
  active?: boolean;
}

@Component({
  selector: 'piao-breadcrumb',
  imports: [CommonModule],
  templateUrl: './breadcrumb.component.html',
  styleUrl: './breadcrumb.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BreadcrumbComponent implements OnInit {
  private destroyRef = inject(DestroyRef);
  sessionStorageService: SessionStorageService = inject(SessionStorageService);
  private readonly accountService = inject(AccountService);
  private readonly roleRoutingService = inject(RoleRoutingService);
  private readonly newsService = inject(NewsService);
  private readonly cdr = inject(ChangeDetectorRef);

  @Input() items: BreadcrumbItem[] = [];
  @Input() autoGenerate: boolean = true;

  breadcrumbs: BreadcrumbItem[] = [];
  private newsTitleCache = new Map<string, string>();

  // Mappa personalizzata per i label delle rotte
  private routeLabels: { [key: string]: string } = {
    'area-privata-PA': 'Scrivania',
    'area-privata-DFP': 'Scrivania',
    'cruscotti-di-analisi': 'Cruscotti di analisi',
    documenti: 'Documenti',
    avvisi: 'Avvisi e Comunicazioni',
    'pubblica-avviso': 'Pubblica un avviso o una comunicazione',
    'dettaglio-avviso': 'Dettaglio avviso o comunicazione',
    'modifica-avviso': 'Modifica avviso o comunicazione',
    'servizi-piao': 'Servizi PIAO',
    'indice-piao': 'Indice del PIAO',
    'carica-piao': 'Carica il PIAO',
    'piao-pdf': 'Carica il PIAO',
    profilo: 'Profilo',
    novita: 'Novità',
    gestionale: 'Gestionale',
    'help-desk': 'Help Desk',
    'carica-file': 'Carica File',
    note: 'Note',
    sezioni: 'Sezioni',
    dettaglio: 'Dettaglio',
    aggiorPiao: "Aggiorna il PIAO dell'anno corrente",
    redigiPiao: 'Redigi o completa un nuovo PIAO',
    complPiao: 'Completa la compilazione del PIAO',
    'mancata-compilazione': 'Dichiarazione di mancata o ritardata Compilazione',
    'consulta-piao': 'Consulta PIAO pubblicati',
    'gestione-piao': 'Gestione PIAO',
    revisione: 'Ricerca PIAO da visionare',
    'scarico-massivo': 'Scarico massivo dei documenti PIAO',
    'storico-dichiarazioni': 'Storico dichiarazioni',
    'dettaglio-mancata-compilazione': 'Dettaglio dichiarazione di mancata o ritardata compilazione',
    solleciti: `Invia sollecito alle PA per il PIAO ${getCurrentTriennio()}`,
  };

  private readonly profileDetailLabel = 'Gestione profilo utente';
  private homeRoute = '/area-privata-PA';
  private homeLabel = 'Scrivania';
  private isDfp = false;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.accountService
      .getAccount()
      .pipe(take(1))
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe((user) => {
        this.isDfp = this.roleRoutingService.isDfpAuthority(user?.typeAuthority);
        this.homeRoute = this.roleRoutingService.getHomeRouteByAuthority(user?.typeAuthority);
        this.homeLabel = 'Scrivania';
        this.generateBreadcrumbs();
        this.cdr.markForCheck();
      });

    // Se i breadcrumb sono passati come input, usali
    if (this.items.length > 0) {
      this.breadcrumbs = this.items;
      this.autoGenerate = false;
    }

    // Genera breadcrumb automaticamente se richiesto
    if (this.autoGenerate) {
      this.generateBreadcrumbs();

      // Aggiorna breadcrumb quando cambia la rotta
      this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
        this.generateBreadcrumbs();
        this.cdr.markForCheck();
      });
    }
  }

  private generateBreadcrumbs(): void {
    const url = this.router.url;

    //console.log(url);

    // Non mostrare breadcrumb per la home
    if (url === '/' || url === '') {
      this.breadcrumbs = [];
      return;
    }

    // Divide l'URL in segmenti
    const urlWithoutQuery = url.split('?')[0];
    let urlSegments = urlWithoutQuery.split('/').filter((segment) => segment);
    const rawUrlSegments = [...urlSegments];
    this.breadcrumbs = [];

    const isGestioneProfiloDettaglio =
      urlSegments.includes('gestionale') && urlSegments.includes('gestione-profilo-utente');
    const isNovitaDettaglio = urlSegments.includes('novita') && urlSegments.includes('dettaglio');
    const isAvvisiDettaglioOrModifica =
      urlSegments.includes('avvisi') &&
      (urlSegments.includes('dettaglio-avviso') || urlSegments.includes('modifica-avviso'));
    const isStoricoDettaglioMancata =
      urlSegments.includes('storico-dichiarazioni') &&
      urlSegments.includes('dettaglio-mancata-compilazione');
    const isSollecitiDettaglioMancata =
      urlSegments.includes('solleciti') && urlSegments.includes('dettaglio-mancata-compilazione');
    const newsDetailId = this.extractNovitaDetailId(rawUrlSegments);

    // Non mostrare breadcrumb per route fuori dal layout protetto (es. login, index, page).
    // Il BreadcrumbComponent vive dentro BaseLayoutComponent quindi normalmente
    // è renderizzato solo nelle route protette; teniamo questo check come safety net.
    const publicRootSegments = new Set(['auth', 'index', 'page']);
    if (urlSegments.length === 0 || publicRootSegments.has(urlSegments[0])) {
      this.breadcrumbs = [];
      return;
    }

    // Aggiungi sempre "Scrivania" come primo elemento
    this.breadcrumbs.push({
      label: this.homeLabel,
      url: this.homeRoute,
      active: false,
    });

    // Costruisci i breadcrumb per ogni segmento
    let currentUrl = '';

    // Hide only non-routable intermediate segments.
    urlSegments = urlSegments.filter(
      (segment) =>
        segment !== 'new' &&
        segment !== 'gestione-profilo-utente' &&
        !(
          isGestioneProfiloDettaglio &&
          (/^\d+$/.test(segment) || /^[a-f0-9]{24}$/i.test(segment))
        ) &&
        !(
          isNovitaDettaglio &&
          (/^\d+$/.test(segment) ||
            /^[a-f0-9]{24}$/i.test(segment) ||
            /^[0-9a-f-]{32,}$/i.test(segment))
        ) &&
        !(isAvvisiDettaglioOrModifica && /^[A-Za-z]?\d+$/.test(segment)) &&
        !(isStoricoDettaglioMancata && /^[A-Za-z]?\d+$/.test(segment)) &&
        !(isSollecitiDettaglioMancata && /^[A-Za-z]?\d+$/.test(segment))
    );

    for (let i = 0; i < urlSegments.length; i++) {
      let segment = urlSegments[i];

      currentUrl += `/${segment}`;
      const isLast = i === urlSegments.length - 1;

      // Se il segmento è una home, aggiorna solo l'active del primo elemento
      if (segment === 'area-privata-PA' || segment === 'area-privata-DFP') {
        if (isLast) {
          this.breadcrumbs[0].active = true;
        }
        continue;
      }

      if (segment === 'sezione' || segment === 'piao-pdf') {
        if (this.isDfp) {
          segment = 'complPiao';
        } else {
          let piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);
          segment = piaoDTO.aggiornamento ? 'aggiorPiao' : 'redigiPiao';
        }
      }

      this.breadcrumbs.push({
        label: this.getLabel(segment),
        url: currentUrl,
        active: isLast,
      });
    }

    if (isGestioneProfiloDettaglio && this.breadcrumbs.length > 0) {
      this.breadcrumbs = this.breadcrumbs.map((item) => ({
        ...item,
        active: false,
      }));

      this.breadcrumbs.push({
        label: this.profileDetailLabel,
        url,
        active: true,
      });
    }

    if (isNovitaDettaglio && newsDetailId) {
      this.applyNovitaDetailTitle(newsDetailId, url);
    }

    // Se c'è un solo elemento, non mostrare il breadcrumb
    if (this.breadcrumbs.length === 1 && this.breadcrumbs[0].active) {
      this.breadcrumbs = [];
    }
  }

  private getLabel(segment: string): string {
    // Usa la mappa personalizzata se disponibile
    if (this.routeLabels[segment]) {
      return this.routeLabels[segment];
    }

    // Altrimenti formatta il segmento: rimuovi trattini e capitalizza
    return segment
      .split('-')
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  private extractNovitaDetailId(segments: string[]): string | null {
    const dettaglioIndex = segments.indexOf('dettaglio');
    if (dettaglioIndex < 0) {
      return null;
    }

    const idSegment = segments[dettaglioIndex + 1];
    return idSegment ? decodeURIComponent(idSegment) : null;
  }

  private applyNovitaDetailTitle(newsId: string, routeUrl: string): void {
    if (this.newsTitleCache.has(newsId)) {
      this.setActiveBreadcrumbLabel(this.newsTitleCache.get(newsId)!);
      return;
    }

    this.newsService.getById(newsId, 'PIAO').pipe(takeUntilDestroyed(this.destroyRef)).subscribe((news) => {
      const title = (news?.title || '').trim();
      if (!title) {
        return;
      }

      this.newsTitleCache.set(newsId, title);

      // Avoid stale async updates when user has already navigated away.
      if (this.router.url !== routeUrl) {
        return;
      }

      this.setActiveBreadcrumbLabel(title);
    });
  }

  private setActiveBreadcrumbLabel(label: string): void {
    const activeIndex = this.breadcrumbs.findIndex((item) => item.active);
    if (activeIndex < 0) {
      return;
    }

    const updated = [...this.breadcrumbs];
    updated[activeIndex] = { ...updated[activeIndex], label };
    this.breadcrumbs = updated;
    this.cdr.markForCheck();
  }

  navigateTo(item: BreadcrumbItem): void {
    if (!item.active) {
      this.router.navigate([item.url]);
    }
  }

  trackByBreadcrumbUrl(_index: number, item: BreadcrumbItem): string {
    return item.url;
  }
}
