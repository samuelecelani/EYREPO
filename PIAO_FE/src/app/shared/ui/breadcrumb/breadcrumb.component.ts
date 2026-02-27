import { Component, inject, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd, ActivatedRoute } from '@angular/router';
import { filter } from 'rxjs/operators';
import { SessionStorageService } from '../../services/session-storage.service';
import { KEY_PIAO } from '../../utils/constants';

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
})
export class BreadcrumbComponent implements OnInit {
  sessionStorageService: SessionStorageService = inject(SessionStorageService);

  @Input() items: BreadcrumbItem[] = [];
  @Input() autoGenerate: boolean = true;

  breadcrumbs: BreadcrumbItem[] = [];

  // Mappa personalizzata per i label delle rotte
  private routeLabels: { [key: string]: string } = {
    'area-privata-PA': 'Scrivania',
    'servizi-piao': 'Servizi PIAO',
    'indice-piao': 'Indice del PIAO',
    profilo: 'Profilo',
    novita: 'Novità',
    gestionale: 'Gestionale',
    'help-desk': 'Help Desk',
    'carica-file': 'Carica File',
    documenti: 'Documenti',
    note: 'Note',
    sezioni: 'Sezioni',
    dettaglio: 'Dettaglio',
    aggiorPiao: "Aggiorna il PIAO dell'anno corrente",
    redigiPiao: 'Redigi o completa un nuovo PIAO',
    'mancata-compilazione': 'Dichiarazione di mancata o ritardata Compilazione',
  };

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Se i breadcrumb sono passati come input, usali
    if (this.items.length > 0) {
      this.breadcrumbs = this.items;
      this.autoGenerate = false;
    }

    // Genera breadcrumb automaticamente se richiesto
    if (this.autoGenerate) {
      this.generateBreadcrumbs();

      // Aggiorna breadcrumb quando cambia la rotta
      this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).subscribe(() => {
        this.generateBreadcrumbs();
      });
    }
  }

  private generateBreadcrumbs(): void {
    const url = this.router.url;

    console.log(url);

    // Non mostrare breadcrumb per la home
    if (url === '/' || url === '') {
      this.breadcrumbs = [];
      return;
    }

    // Divide l'URL in segmenti
    let urlSegments = url.split('/').filter((segment) => segment);
    this.breadcrumbs = [];

    // Se non siamo sotto /pages, non mostrare breadcrumb
    if (urlSegments[0] !== 'pages') {
      this.breadcrumbs = [];
      return;
    }

    // Aggiungi sempre "Scrivania" come primo elemento
    this.breadcrumbs.push({
      label: 'Scrivania',
      url: '/pages/area-privata-PA',
      active: false,
    });

    // Costruisci i breadcrumb per ogni segmento dopo 'pages'
    let currentUrl = '';

    // Rimuovi tutti i segmenti che contengono solo numeri e il segmento 'new'
    urlSegments = urlSegments.filter((segment) => !/^\d+$/.test(segment) && segment !== 'new');

    for (let i = 0; i < urlSegments.length; i++) {
      let segment = urlSegments[i];

      // Salta il segmento 'pages'
      if (segment === 'pages') {
        currentUrl = '/pages';
        continue;
      }
      currentUrl += `/${segment}`;
      const isLast = i === urlSegments.length - 1;

      // Se il segmento è 'area-privata-PA', aggiorna solo l'active del primo elemento
      if (segment === 'area-privata-PA') {
        if (isLast) {
          this.breadcrumbs[0].active = true;
        }
        continue;
      }

      if (segment.includes('sezione?')) {
        let piaoDTO = this.sessionStorageService.getItem(KEY_PIAO);

        segment = piaoDTO.aggiornamento ? 'aggiorPiao' : 'redigiPiao';
      }

      this.breadcrumbs.push({
        label: this.getLabel(segment),
        url: currentUrl,
        active: isLast,
      });
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

  navigateTo(item: BreadcrumbItem): void {
    if (!item.active) {
      this.router.navigate([item.url]);
    }
  }
}
